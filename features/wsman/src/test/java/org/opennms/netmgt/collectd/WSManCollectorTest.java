/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.wsman.Attrib;
import org.opennms.netmgt.config.wsman.Collection;
import org.opennms.netmgt.config.wsman.Group;
import org.opennms.netmgt.config.wsman.WsmanConfig;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.w3c.dom.Node;

import com.google.common.collect.Maps;
import com.mycila.xmltool.XMLDoc;
import com.mycila.xmltool.XMLTag;

public class WSManCollectorTest {

    @Test
    public void canProcessEnumerationResults() {
        Group group = new Group();
        group.setName("ComputerSystem");
        addAttribute(group, "PrimaryStatus", "GaugeWithValue", "Gauge");
        addAttribute(group, "!PrimaryStatus!", "GaugeWithoutValue", "Gauge");
        addAttribute(group, "ElementName", "StringWithValue", "String");
        addAttribute(group, "!ElementName!", "StringWithoutValue", "String");

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageDir()).thenReturn(new java.io.File(""));
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        Resource resource = mock(NodeLevelResource.class);

        XMLTag xmlTag = XMLDoc.newDocument(true).addRoot("body")
                .addTag("DCIM_ComputerSystem")
                    .addTag("ElementName")
                    .setText("Computer System")
                    .addTag("PrimaryStatus")
                    .setText("42.1")
                    .addTag("OtherIdentifyingInfo")
                    .setText("ANONYMIZED01")
                    .addTag("OtherIdentifyingInfo")
                    .setText("mainsystemchassis")
                    .addTag("OtherIdentifyingInfo")
                    .setText("ANONYMIZED02");

        List<Node> nodes = xmlTag.gotoRoot().getChildElement().stream()
            .map(el -> (Node)el)
            .collect(Collectors.toList());

        WsManCollector.processEnumerationResults(group, builder, resource, nodes);

        // Verify
        Map<String, CollectionAttribute> attributesByName = getAttributes(builder.build());
        assertFalse("The CollectionSet should not contain attributes for missing values.", attributesByName.containsKey("GaugeWithoutValue"));
        assertFalse("The CollectionSet should not contain attributes for missing values.", attributesByName.containsKey("StringWithoutValue"));
        assertEquals(42.1, attributesByName.get("GaugeWithValue").getNumericValue().doubleValue(), 2);
        assertEquals("Computer System", attributesByName.get("StringWithValue").getStringValue());
    }


    @Test
    public void canCollectFromMultivaluedKeyUsingIndexOf() {
        /* The iDrac provides the following keys in the DCIM_ComputerSystem entry:
         *  <n1:IdentifyingDescriptions>CIM:GUID</n1:IdentifyingDescriptions>
         *  <n1:IdentifyingDescriptions>CIM:Tag</n1:IdentifyingDescriptions>
         *  <n1:IdentifyingDescriptions>DCIM:ServiceTag</n1:IdentifyingDescriptions>
         *  <n1:OtherIdentifyingInfo>44454C4C-3700-104A-8052-C3C04BB25031</n1:OtherIdentifyingInfo>
         *  <n1:OtherIdentifyingInfo>mainsystemchassis</n1:OtherIdentifyingInfo>
         *  <n1:OtherIdentifyingInfo>C7BBBP1</n1:OtherIdentifyingInfo>
         *
         * We want to be able to collect the value of 'OtherIdentifyingInfo' at the same
         * index where 'IdentifyingDescriptions' has the value of 'DCIM:ServiceTag'.
         */
        Group group = new Group();
        group.setName("DCIM_ComputerSystem");

        Attrib attr = new Attrib();
        attr.setName("OtherIdentifyingInfo");
        attr.setAlias("ServiceTag");
        attr.setIndexOf("#IdentifyingDescriptions matches '.*ServiceTag'");
        attr.setType("String");
        group.addAttrib(attr);

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageDir()).thenReturn(new java.io.File(""));
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        Resource resource = mock(NodeLevelResource.class);

        XMLTag xmlTag = XMLDoc.newDocument(true).addRoot("body")
                .addTag("DCIM_ComputerSystem")
                    .addTag("IdentifyingDescriptions")
                    .setText("CIM:GUID")
                    .addTag("IdentifyingDescriptions")
                    // Place the ServiceTag in the middle, so we can be sure that it's not just
                    // picking up the first, or last
                    .setText("DCIM:ServiceTag")
                    .addTag("IdentifyingDescriptions")
                    .setText("CIM:Tag")
                    .addTag("OtherIdentifyingInfo")
                    .setText("44454C4C-3700-104A-8052-C3C04BB25031")
                    .addTag("OtherIdentifyingInfo")
                    .setText("C7BBBP1")
                    .addTag("OtherIdentifyingInfo")
                    .setText("mainsystemchassis");

        List<Node> nodes = xmlTag.gotoRoot().getChildElement().stream()
            .map(el -> (Node)el)
            .collect(Collectors.toList());

        WsManCollector.processEnumerationResults(group, builder, resource, nodes);

        // Verify
        Map<String, CollectionAttribute> attributesByName = getAttributes(builder.build());
        assertEquals("C7BBBP1", attributesByName.get("ServiceTag").getStringValue());
    }

    @Test
    public void canSuccesfullyCollectFromGroupWithNoAttributes() throws CollectionInitializationException, CollectionException {
        OnmsNode node = mock(OnmsNode.class);
        NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(0)).thenReturn(node);

        WsmanConfig config = new WsmanConfig();
        WSManConfigDao configDao = mock(WSManConfigDao.class);
        when(configDao.getConfig(anyObject())).thenReturn(config);

        Collection collection = new Collection();
        WSManDataCollectionConfigDao dataCollectionConfigDao = mock(WSManDataCollectionConfigDao.class);
        when(dataCollectionConfigDao.getCollectionByName("default")).thenReturn(collection);

        WsManCollector collector = new WsManCollector();
        collector.setWSManClientFactory(mock(WSManClientFactory.class));
        collector.setWSManConfigDao(configDao);
        collector.setWSManDataCollectionConfigDao(dataCollectionConfigDao);
        collector.setNodeDao(nodeDao);

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageDir()).thenReturn(new java.io.File(""));
        collector.initialize(agent, Maps.newHashMap());

        Map<String, Object> collectionParams = Maps.newHashMap();
        collectionParams.put("collection", "default");

        CollectionSet collectionSet = collector.collect(agent, null, collectionParams);

        assertEquals(ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());
        assertEquals(0, getAttributes(collectionSet).size());
    }

    private static void addAttribute(Group group, String name, String alias, String type) {
        Attrib attr = new Attrib();
        attr.setName(name);
        attr.setAlias(alias);
        attr.setType(type);
        group.addAttrib(attr);
    }

    private static Map<String, CollectionAttribute> getAttributes(CollectionSet collectionSet) {
        final Map<String, CollectionAttribute> attributesByName = Maps.newHashMap();
        collectionSet.visit(new AbstractCollectionSetVisitor() {
            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                attributesByName.put(attribute.getName(), attribute);
            }
        });
        return attributesByName;
    }
}
