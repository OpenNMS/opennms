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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.netmgt.xml.event.Event;

public class RebuildMinionRequisitionTest {

    private RebuildMinionRequisition command;
    private NodeDao nodeDao;
    private MinionDao minionDao;
    private ForeignSourceRepository repository;
    private EventForwarder eventForwarder;

    @Before
    public void setUp() throws Exception {
        command = new RebuildMinionRequisition();
        nodeDao = Mockito.mock(NodeDao.class);
        minionDao = Mockito.mock(MinionDao.class);
        repository = Mockito.mock(ForeignSourceRepository.class);
        eventForwarder = Mockito.mock(EventForwarder.class);

        command.nodeDao = nodeDao;
        command.minionDao = minionDao;
        command.deployedForeignSourceRepository = repository;
        command.eventForwarder = eventForwarder;
        command.eventSubscriptionService = Mockito.mock(EventSubscriptionService.class);

        Mockito.when(repository.getRequisitionURL("Minions")).thenReturn(new URL("file:/opt/opennms/etc/imports/Minions.xml"));

        final ForeignSource completeDefinition = new ForeignSource("Minions");
        completeDefinition.addPolicy(new PluginConfig("Minion-SNMP-Policy", "org.opennms.netmgt.provision.persist.policies.MatchingSnmpInterfacePolicy"));
        completeDefinition.addDetector(new PluginConfig("SNMP", "org.opennms.netmgt.provision.detector.snmp.SnmpDetector"));
        Mockito.when(repository.getForeignSource("Minions")).thenReturn(completeDefinition);
    }

    @Test
    public void testRebuildFromDatabaseWhenRequisitionIsGone() throws Exception {
        Mockito.when(minionDao.findAll()).thenReturn(Arrays.asList(new OnmsMinion("minion1", "loc1", "up", new Date())));
        Mockito.when(nodeDao.getForeignIdToNodeIdMap("Minions")).thenReturn(Collections.singletonMap("minion1", 1));
        Mockito.when(repository.getRequisition("Minions")).thenReturn(null);

        command.execute();

        final ArgumentCaptor<Requisition> captor = ArgumentCaptor.forClass(Requisition.class);
        Mockito.verify(repository).save(captor.capture());
        final Requisition saved = captor.getValue();
        assertEquals(1, saved.getNodeCount());
        final RequisitionNode node = saved.getNode("minion1");
        assertNotNull(node);
        assertEquals("minion1", node.getNodeLabel());
        assertEquals("loc1", node.getLocation());
        assertNotNull(node.getInterface("127.0.0.1"));
        assertNotNull(node.getInterface("127.0.0.1").getMonitoredService("Minion-RPC"));

        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        Mockito.verify(eventForwarder).sendNow(eventCaptor.capture());
        assertEquals(EventConstants.RELOAD_IMPORT_UEI, eventCaptor.getValue().getUei());
    }

    @Test
    public void testKeepsExistingEntriesAndDropsOrphans() throws Exception {
        Mockito.when(minionDao.findAll()).thenReturn(Arrays.asList(new OnmsMinion("minion1", "loc1", "up", new Date())));
        Mockito.when(nodeDao.getForeignIdToNodeIdMap("Minions")).thenReturn(Collections.emptyMap());

        final Requisition existing = new Requisition("Minions");
        final RequisitionNode keptNode = new RequisitionNode();
        keptNode.setForeignId("minion1");
        keptNode.setNodeLabel("custom-label");
        keptNode.setLocation("loc1");
        existing.putNode(keptNode);
        final RequisitionNode orphanNode = new RequisitionNode();
        orphanNode.setForeignId("ghost");
        orphanNode.setNodeLabel("ghost");
        orphanNode.setLocation("loc1");
        existing.putNode(orphanNode);
        Mockito.when(repository.getRequisition("Minions")).thenReturn(existing);

        command.execute();

        final ArgumentCaptor<Requisition> captor = ArgumentCaptor.forClass(Requisition.class);
        Mockito.verify(repository).save(captor.capture());
        final Requisition saved = captor.getValue();
        assertEquals(1, saved.getNodeCount());
        assertEquals("custom-label", saved.getNode("minion1").getNodeLabel());
        assertNull(saved.getNode("ghost"));
    }

    @Test
    public void testForeignSourceDefaultsCreatedWhenDefinitionMissing() throws Exception {
        Mockito.when(minionDao.findAll()).thenReturn(Arrays.asList(new OnmsMinion("minion1", "loc1", "up", new Date())));
        Mockito.when(nodeDao.getForeignIdToNodeIdMap("Minions")).thenReturn(Collections.singletonMap("minion1", 1));
        Mockito.when(repository.getRequisition("Minions")).thenReturn(null);

        final ForeignSource defaultDefinition = new ForeignSource("Minions");
        defaultDefinition.setDefault(true);
        Mockito.when(repository.getForeignSource("Minions")).thenReturn(defaultDefinition);

        command.execute();

        final ArgumentCaptor<ForeignSource> captor = ArgumentCaptor.forClass(ForeignSource.class);
        Mockito.verify(repository).save(captor.capture());
        assertNotNull(captor.getValue().getDetector("SNMP"));
        assertNotNull(captor.getValue().getPolicy("Minion-SNMP-Policy"));
    }

    @Test
    public void testCustomForeignSourceDefinitionPreserved() throws Exception {
        Mockito.when(minionDao.findAll()).thenReturn(Arrays.asList(new OnmsMinion("minion1", "loc1", "up", new Date())));
        Mockito.when(nodeDao.getForeignIdToNodeIdMap("Minions")).thenReturn(Collections.singletonMap("minion1", 1));
        Mockito.when(repository.getRequisition("Minions")).thenReturn(null);

        final ForeignSource customDefinition = new ForeignSource("Minions");
        customDefinition.addDetector(new PluginConfig("ICMP", "org.opennms.netmgt.provision.detector.icmp.IcmpDetector"));
        Mockito.when(repository.getForeignSource("Minions")).thenReturn(customDefinition);

        command.execute();

        final ArgumentCaptor<ForeignSource> captor = ArgumentCaptor.forClass(ForeignSource.class);
        Mockito.verify(repository).save(captor.capture());
        assertNotNull("the custom detector must be preserved", captor.getValue().getDetector("ICMP"));
        assertNotNull("the default detector must be added", captor.getValue().getDetector("SNMP"));
        assertNotNull("the default policy must be added", captor.getValue().getPolicy("Minion-SNMP-Policy"));
    }

    @Test
    public void testDryRunWritesNothing() throws Exception {
        Mockito.when(minionDao.findAll()).thenReturn(Arrays.asList(new OnmsMinion("minion1", "loc1", "up", new Date())));
        Mockito.when(nodeDao.getForeignIdToNodeIdMap("Minions")).thenReturn(Collections.singletonMap("minion1", 1));
        Mockito.when(repository.getRequisition("Minions")).thenReturn(null);

        command.dryRun = true;
        command.execute();

        Mockito.verify(repository, Mockito.never()).save(Mockito.any(Requisition.class));
        Mockito.verify(eventForwarder, Mockito.never()).sendNow(Mockito.any(Event.class));
    }
}
