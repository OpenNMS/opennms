/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.provision.requisition.command;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

@Command(scope = "opennms", name = "minion-rebuild-requisition",
        description = "Rebuilds the minion requisition from the database (provisioned minion nodes and live minion rows) and triggers an import. "
                + "Use when the requisition file was lost, corrupted, or has drifted from the database.")
@Service
public class RebuildMinionRequisition implements Action {

    static final String FOREIGN_SOURCE_PATTERN_PROPERTY = "opennms.minion.provisioning.foreignSourcePattern";

    private static final String MINION_INTERFACE = "127.0.0.1";
    private static final List<String> MINION_SERVICES = List.of("Minion-Heartbeat", "Minion-RPC", "JMX-Minion");
    private static final String DEFAULT_SNMP_POLICY = "Minion-SNMP-Policy";
    private static final String DEFAULT_SNMP_DETECTOR = "SNMP";

    @Reference
    NodeDao nodeDao;

    @Reference
    MinionDao minionDao;

    @Reference(filter = "(type=deployed)")
    ForeignSourceRepository deployedForeignSourceRepository;

    @Reference
    EventForwarder eventForwarder;

    @Reference
    EventSubscriptionService eventSubscriptionService;

    @Option(name = "-n", aliases = "--dry-run", description = "Show what would be written without saving or importing")
    boolean dryRun = false;

    @Option(name = "-w", aliases = "--wait", description = "Wait for the import completion event")
    boolean wait = false;

    @Argument(index = 0, name = "foreignSource", description = "Foreign source to rebuild; defaults to the configured foreign source pattern")
    String foreignSource;

    @Override
    public Object execute() throws Exception {
        final String pattern = System.getProperty(FOREIGN_SOURCE_PATTERN_PROPERTY, "Minions");
        if (foreignSource == null) {
            if (pattern.contains("%")) {
                System.out.printf("The foreign source pattern '%s' is location based, specify the foreign source explicitly.%n", pattern);
                return null;
            }
            foreignSource = pattern;
        }

        final Map<String, OnmsMinion> minionsByForeignId = new TreeMap<>();
        for (final OnmsMinion minion : minionDao.findAll()) {
            if (foreignSource.equals(String.format(pattern, minion.getLocation()))) {
                minionsByForeignId.put(minion.getLabel() != null ? minion.getLabel() : minion.getId(), minion);
            }
        }
        if (minionsByForeignId.isEmpty()) {
            System.out.printf("No minions found for foreign source %s, nothing to rebuild.%n", foreignSource);
            return null;
        }

        final Map<String, Integer> dbNodesByForeignId = nodeDao.getForeignIdToNodeIdMap(foreignSource);
        final Requisition existingRequisition = getExistingRequisition(foreignSource);

        final Requisition requisition = new Requisition(foreignSource);
        requisition.updateDateStamp();

        int kept = 0, restored = 0, skipped = 0;
        for (final Map.Entry<String, OnmsMinion> entry : minionsByForeignId.entrySet()) {
            final String foreignId = entry.getKey();
            final RequisitionNode existingNode = existingRequisition == null ? null : existingRequisition.getNode(foreignId);
            if (existingNode != null) {
                requisition.putNode(existingNode);
                kept++;
            } else if (dbNodesByForeignId.containsKey(foreignId)) {
                requisition.putNode(createMinionNode(foreignId, entry.getValue()));
                restored++;
            } else {
                // Never provisioned; the minion's next heartbeat will add it
                skipped++;
            }
        }

        int dropped = 0;
        if (existingRequisition != null) {
            for (final RequisitionNode node : existingRequisition.getNodes()) {
                if (requisition.getNode(node.getForeignId()) == null) {
                    System.out.printf("Dropping %s: no minion with this id exists.%n", node.getForeignId());
                    dropped++;
                }
            }
        }

        System.out.printf("Requisition %s: %d node(s) total (%d kept, %d restored from database, %d dropped, %d unprovisioned minion(s) left to their next heartbeat).%n",
                foreignSource, requisition.getNodeCount(), kept, restored, dropped, skipped);

        if (dryRun) {
            System.out.println("Dry run, nothing written.");
            return null;
        }

        deployedForeignSourceRepository.save(requisition);
        deployedForeignSourceRepository.flush();

        ensureForeignSourceDefaults(foreignSource);

        final String url = String.valueOf(deployedForeignSourceRepository.getRequisitionURL(foreignSource));
        final EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, ImportRequisition.EVENT_SOURCE);
        eventBuilder.addParam(EventConstants.PARM_URL, url);

        final ImportRequisition.ImportEventListener listener = new ImportRequisition.ImportEventListener(url);
        if (wait) {
            eventSubscriptionService.addEventListener(listener, ImportRequisition.ImportEventListener.UEIS);
        }

        eventForwarder.sendNow(eventBuilder.getEvent());
        System.out.printf("Requisition import triggered for %s%n", url);

        if (wait) {
            try {
                while (!listener.isDone()) {
                    Thread.sleep(1000);
                    System.out.print(".");
                    System.out.flush();
                }
                if (EventConstants.IMPORT_SUCCESSFUL_UEI.equals(listener.getReceivedUei())) {
                    System.out.printf("%nImport succeeded.%n");
                } else {
                    System.out.printf("%nImport failed.%n");
                }
            } finally {
                eventSubscriptionService.removeEventListener(listener);
            }
        }

        return null;
    }

    private void ensureForeignSourceDefaults(final String foreignSource) {
        final PluginConfig policy = new PluginConfig(DEFAULT_SNMP_POLICY, "org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy");
        policy.addParameter("ifDescr", "~^docker.*$");
        policy.addParameter("action", "DO_NOT_PERSIST");
        policy.addParameter("matchBehavior", "ALL_PARAMETERS");
        final PluginConfig detector = new PluginConfig(DEFAULT_SNMP_DETECTOR, "org.opennms.netmgt.provision.detector.snmp.SnmpDetector");

        final ForeignSource definition = deployedForeignSourceRepository.getForeignSource(foreignSource);
        if (definition.isDefault()) {
            definition.setDetectors(List.of(detector));
            definition.setPolicies(List.of(policy));
            deployedForeignSourceRepository.save(definition);
            System.out.printf("Created foreign source definition %s with the Minion defaults.%n", foreignSource);
            return;
        }

        boolean changed = false;
        if (definition.getPolicy(DEFAULT_SNMP_POLICY) == null) {
            definition.addPolicy(policy);
            changed = true;
        }
        if (definition.getDetector(DEFAULT_SNMP_DETECTOR) == null) {
            definition.addDetector(detector);
            changed = true;
        }
        if (changed) {
            deployedForeignSourceRepository.save(definition);
            System.out.printf("Added the default Minion policy and detector to foreign source definition %s.%n", foreignSource);
        }
    }

    private Requisition getExistingRequisition(final String foreignSource) {
        try {
            return deployedForeignSourceRepository.getRequisition(foreignSource);
        } catch (final ForeignSourceRepositoryException e) {
            System.out.printf("Existing requisition %s could not be read (%s), rebuilding from the database only.%n", foreignSource, e.getMessage());
            return null;
        }
    }

    private static RequisitionNode createMinionNode(final String foreignId, final OnmsMinion minion) {
        final RequisitionInterface requisitionInterface = new RequisitionInterface();
        requisitionInterface.setIpAddr(MINION_INTERFACE);
        requisitionInterface.setSnmpPrimary(PrimaryType.PRIMARY);
        for (final String serviceName : MINION_SERVICES) {
            final RequisitionMonitoredService service = new RequisitionMonitoredService();
            service.setServiceName(serviceName);
            requisitionInterface.putMonitoredService(service);
        }

        final RequisitionNode requisitionNode = new RequisitionNode();
        requisitionNode.setNodeLabel(minion.getId());
        requisitionNode.setForeignId(foreignId);
        requisitionNode.setLocation(minion.getLocation());
        requisitionNode.putInterface(requisitionInterface);
        return requisitionNode;
    }
}
