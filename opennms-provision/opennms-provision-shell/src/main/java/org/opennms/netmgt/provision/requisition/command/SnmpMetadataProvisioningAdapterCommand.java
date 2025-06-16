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

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.Terminal;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.RegExpRestriction;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.provision.SnmpMetadataException;
import org.opennms.netmgt.provision.SnmpMetadataProvisioningAdapter;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.primitives.Booleans;

@Command(scope = "opennms", name = "snmp-metadata-provisioning-adapter", description = "Trigger SnmpMetadataProvisioningAdapter for a set of nodes")
@Service
public class SnmpMetadataProvisioningAdapterCommand implements Action {
    @Reference
    public TransactionOperations transactionOperations;

    @Reference
    private NodeDao nodeDao;

    @Reference
    Terminal terminal;

    @Option(name = "-i", aliases = "--id", description = "Node Id to process", required = false, multiValued = false)
    Integer id;

    @Option(name = "-a", aliases = "--all", description = "Process all nodes", required = false, multiValued = false)
    Boolean all;

    @Option(name = "-r", aliases = "--regexp", description = "Process nodes by label (regular expression)", required = false, multiValued = false)
    String regexp;

    @Option(name = "-p", aliases = "--persist", description = "Persist data instead of displaying it", required = false, multiValued = false)
    Boolean persist = false;


    @Override
    public Object execute() {
        final int optionsSet = Booleans.countTrue(id != null, regexp != null, all != null);
        if (optionsSet == 0) {
            System.out.println("Please provide one of the options --id/-i, --all/-a or --regexp/-r");
        } else {
            if (optionsSet > 1) {
                System.out.println("Please provide only one option (--id, --all, --regexp)");
            } else {
                transactionOperations.execute(new TransactionCallback<Void>() {
                    @Override
                    public Void doInTransaction(TransactionStatus transactionStatus) {
                        final List<OnmsNode> onmsNodes = new ArrayList<>();

                        if (all != null) {
                            onmsNodes.addAll(nodeDao.findAll());
                        }

                        if (regexp != null) {
                            final Criteria criteria = new Criteria(OnmsNode.class);
                            criteria.addRestriction(new RegExpRestriction("label", regexp));
                            onmsNodes.addAll(nodeDao.findMatching(criteria));
                        }

                        if (id != null) {
                            onmsNodes.add(nodeDao.get(id));
                        }

                        processNodes(onmsNodes, persist);

                        return null;
                    }
                });
            }
        }

        return null;
    }


    private void processNodes(final List<OnmsNode> onmsNodes, final boolean persist) {
        final SnmpMetadataProvisioningAdapter snmpMetadataProvisioningAdapter = BeanUtils.getBean("provisiondContext", "snmpMetadataProvisioningAdapter", SnmpMetadataProvisioningAdapter.class);

        System.out.println("Processing " + onmsNodes.size() + " node(s)...");

        for (final OnmsNode onmsNode : onmsNodes) {
            System.out.printf("\nProcessing node '%s' (id=%d)...\n", onmsNode.getLabel(), onmsNode.getId());

            if (onmsNode == null) {
                System.out.println("Error: node is null.");
                continue;
            }

            final OnmsIpInterface onmsIpInterface = onmsNode.getPrimaryInterface();

            if (onmsIpInterface == null) {
                System.out.println("Error: primary interface is null.");
                continue;
            }

            final List<OnmsMetaData> results;

            try {
                if (persist) {
                    System.out.printf("Calling SnmpMetadataProvisioingAdapter.doUpdateNode(%d)...\n", onmsNode.getId());
                    snmpMetadataProvisioningAdapter.doUpdateNode(onmsNode.getId());
                    continue;
                } else {
                    results = snmpMetadataProvisioningAdapter.createMetadata(onmsNode, onmsNode.getPrimaryInterface());
                }
            } catch (SnmpMetadataException e) {
                System.out.println("Error: provisioning adapter threw exception");
                e.printStackTrace(System.out);
                continue;
            }

            if (results == null) {
                System.out.println("Error: node don't support SNMP (provisioning adapter returned null).");
                continue;
            }

            if (results.size() == 0) {
                System.out.println("Provisioning adapter returned no data.");
                continue;
            } else {
                System.out.printf("Provisioning adapter returned %d metadata entries.\n\n", results.size());
            }

            final ShellTable table = new ShellTable();
            table.size(terminal.getWidth() - 1);
            table.column("Context");
            table.column("Key");
            table.column("Value");


            for (final OnmsMetaData onmsMetaData : results) {
                table.addRow().addContent(onmsMetaData.getContext(), onmsMetaData.getKey(), onmsMetaData.getValue());
            }

            table.print(System.out);
        }
    }
}
