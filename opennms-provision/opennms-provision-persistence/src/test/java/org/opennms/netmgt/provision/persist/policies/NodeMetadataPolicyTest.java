/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

public class NodeMetadataPolicyTest implements InitializingBean {
    private List<OnmsNode> m_nodes;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
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
        m_nodes.add(node1);

        final OnmsNode node2 = new OnmsNode();
        node2.setNodeId("2");
        node2.setForeignSource("rdu");
        node2.setForeignId("crmsrv");
        node2.setLabel("CRM system");
        m_nodes.add(node2);
    }

    @Test
    @Transactional
    public void testMatchingLabel() {
        final NodeMetadataSettingPolicy p = new NodeMetadataSettingPolicy();
        p.setLabel("~.*mach.*");
        p.setMetadataKey("theKey");
        p.setMetadataValue("theValue");
        final List<OnmsNode> modifiedNodes = applyPolicy(p);

        final OnmsNode node1 = modifiedNodes.stream().filter(n->n.getId() == 1).findFirst().get();
        final OnmsNode node2 = modifiedNodes.stream().filter(n->n.getId() == 2).findFirst().get();

        assertEquals(1, node1.getRequisitionedMetaData().size());
        assertEquals("requisition", node1.getRequisitionedMetaData().get(0).getContext());
        assertEquals("theKey", node1.getRequisitionedMetaData().get(0).getKey());
        assertEquals("theValue", node1.getRequisitionedMetaData().get(0).getValue());
        assertEquals(0, node2.getRequisitionedMetaData().size());
    }

    @Test
    @Transactional
    public void testMatchingLabelWithCustomContext() {
        final NodeMetadataSettingPolicy p = new NodeMetadataSettingPolicy();
        p.setLabel("~.*mach.*");
        p.setMetadataKey("theKey");
        p.setMetadataValue("theValue");
        p.setMetadataContext("customContext");
        final List<OnmsNode> modifiedNodes = applyPolicy(p);

        final OnmsNode node1 = modifiedNodes.stream().filter(n->n.getId() == 1).findFirst().get();
        final OnmsNode node2 = modifiedNodes.stream().filter(n->n.getId() == 2).findFirst().get();

        assertEquals(1, node1.getRequisitionedMetaData().size());
        assertEquals("customContext", node1.getRequisitionedMetaData().get(0).getContext());
        assertEquals("theKey", node1.getRequisitionedMetaData().get(0).getKey());
        assertEquals("theValue", node1.getRequisitionedMetaData().get(0).getValue());
        assertEquals(0, node2.getRequisitionedMetaData().size());
    }

    @Test
    @Transactional
    public void testMatchingNothing() {
        final NodeMetadataSettingPolicy p = new NodeMetadataSettingPolicy();
        p.setLabel("~^foobar$");
        p.setMetadataKey("theKey");
        p.setMetadataValue("theValue");

        final List<OnmsNode> modifiedNodes = applyPolicy(p);

        final OnmsNode node1 = modifiedNodes.stream().filter(n->n.getId() == 1).findFirst().get();
        final OnmsNode node2 = modifiedNodes.stream().filter(n->n.getId() == 2).findFirst().get();

        assertEquals(0, node1.getRequisitionedMetaData().size());
        assertEquals(0, node1.getRequisitionedMetaData().size());
    }

    private List<OnmsNode> applyPolicy(final NodeMetadataSettingPolicy p) {
        return m_nodes.stream().map(n -> p.apply(n, new HashMap<>())).collect(Collectors.toList());
    }
}
