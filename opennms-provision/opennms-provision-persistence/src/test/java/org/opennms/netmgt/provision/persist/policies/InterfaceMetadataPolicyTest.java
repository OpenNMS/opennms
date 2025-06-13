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
package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

public class InterfaceMetadataPolicyTest implements InitializingBean {
    private List<OnmsNode> m_nodes;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_nodes = new ArrayList<>();
        final OnmsNode node1 = new OnmsNode();
        node1.setNodeId("1");
        node1.setForeignSource("fulda");
        node1.setForeignId("websrv");
        node1.setLabel("WWW machine");
        final OnmsIpInterface if1 = new OnmsIpInterface();
        if1.setIpAddress(InetAddressUtils.addr("192.168.1.1"));
        node1.addIpInterface(if1);
        m_nodes.add(node1);

        final OnmsNode node2 = new OnmsNode();
        node2.setNodeId("2");
        node2.setForeignSource("rdu");
        node2.setForeignId("crmsrv");
        node2.setLabel("CRM system");
        final OnmsIpInterface if2 = new OnmsIpInterface();
        if2.setIpAddress(InetAddressUtils.addr("172.16.2.2"));
        node2.addIpInterface(if2);
        m_nodes.add(node2);
    }

    @Test
    @Transactional
    public void testMatchingLabel() {
        final InterfaceMetadataSettingPolicy p = new InterfaceMetadataSettingPolicy();
        p.setIpAddress("~.*\\.168\\..*");
        p.setMetadataKey("theKey");
        p.setMetadataValue("theValue");
        final List<OnmsNode> modifiedNodes = applyPolicy(p);

        final OnmsNode node1 = modifiedNodes.stream().filter(n->n.getId() == 1).findFirst().get();
        final OnmsNode node2 = modifiedNodes.stream().filter(n->n.getId() == 2).findFirst().get();

        assertEquals(1, node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().size());
        assertEquals("requisition", node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().get(0).getContext());
        assertEquals("theKey", node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().get(0).getKey());
        assertEquals("theValue", node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().get(0).getValue());
        assertEquals(0, node2.getIpInterfaces().iterator().next().getRequisitionedMetaData().size());
    }

    @Test
    @Transactional
    public void testMatchingLabelWithCustomContext() {
        final InterfaceMetadataSettingPolicy p = new InterfaceMetadataSettingPolicy();
        p.setIpAddress("~.*\\.168\\..*");
        p.setMetadataKey("theKey");
        p.setMetadataValue("theValue");
        p.setMetadataContext("customContext");
        final List<OnmsNode> modifiedNodes = applyPolicy(p);

        final OnmsNode node1 = modifiedNodes.stream().filter(n->n.getId() == 1).findFirst().get();
        final OnmsNode node2 = modifiedNodes.stream().filter(n->n.getId() == 2).findFirst().get();

        assertEquals(1, node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().size());
        assertEquals("customContext", node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().get(0).getContext());
        assertEquals("theKey", node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().get(0).getKey());
        assertEquals("theValue", node1.getIpInterfaces().iterator().next().getRequisitionedMetaData().get(0).getValue());
        assertEquals(0, node2.getIpInterfaces().iterator().next().getRequisitionedMetaData().size());
    }

    @Test
    @Transactional
    public void testMatchingNothing() {
        final InterfaceMetadataSettingPolicy p = new InterfaceMetadataSettingPolicy();
        p.setIpAddress("~^foobar$");
        p.setMetadataKey("theKey");
        p.setMetadataValue("theValue");

        final List<OnmsNode> modifiedNodes = applyPolicy(p);

        final OnmsNode node1 = modifiedNodes.stream().filter(n->n.getId() == 1).findFirst().get();
        final OnmsNode node2 = modifiedNodes.stream().filter(n->n.getId() == 2).findFirst().get();

        assertEquals(0, node1.getRequisitionedMetaData().size());
        assertEquals(0, node1.getRequisitionedMetaData().size());
    }

    private List<OnmsNode> applyPolicy(final InterfaceMetadataSettingPolicy p) {
        final List<OnmsNode> newNodes = new ArrayList<>(m_nodes);
        newNodes.stream().forEach(n -> n.setIpInterfaces(n.getIpInterfaces().stream().map(i -> p.apply(i, new HashMap<>())).collect(Collectors.toSet())));
        return newNodes;
    }
}
