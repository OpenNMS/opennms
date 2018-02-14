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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.core.collection.test.CollectionSetUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.wsman.Attrib;
import org.opennms.netmgt.config.wsman.Collection;
import org.opennms.netmgt.config.wsman.Definition;
import org.opennms.netmgt.config.wsman.Group;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.WSManDataCollectionConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.SiblingColumnStorageStrategy;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourcePath;
import org.w3c.dom.Node;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mycila.xmltool.XMLDoc;
import com.mycila.xmltool.XMLTag;

public class WSManCollectorTest {

    @Test
    public void canProcessEnumerationResults() {
        Group group = new Group();
        group.setName("ComputerSystem");
        addAttribute(group, "PrimaryStatus", "GaugeWithValue", AttributeType.GAUGE);
        addAttribute(group, "!PrimaryStatus!", "GaugeWithoutValue", AttributeType.GAUGE);
        addAttribute(group, "ElementName", "StringWithValue", AttributeType.STRING);
        addAttribute(group, "!ElementName!", "StringWithoutValue", AttributeType.STRING);

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get());
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        Supplier<Resource> resourceSupplier = () -> mock(NodeLevelResource.class);

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

        WsManCollector.processEnumerationResults(group, builder, resourceSupplier, nodes);

        // Verify
        Map<String, CollectionAttribute> attributesByName = CollectionSetUtils.getAttributesByName(builder.build());
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
        attr.setType(AttributeType.STRING);
        group.addAttrib(attr);

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get());
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        Supplier<Resource> resourceSupplier = () -> mock(NodeLevelResource.class);

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

        WsManCollector.processEnumerationResults(group, builder, resourceSupplier, nodes);

        // Verify
        Map<String, CollectionAttribute> attributesByName = CollectionSetUtils.getAttributesByName(builder.build());
        assertEquals("C7BBBP1", attributesByName.get("ServiceTag").getStringValue());
    }

    @Test
    public void canCollectFromMultipleRecordsUsingFilter() {
        Group group = new Group();
        group.setName("DCIM_NumericSensor");

        Attrib attr = new Attrib();
        attr.setName("CurrentReading");
        attr.setAlias("sysBoardInletTemp");
        attr.setFilter("#ElementName == 'System Board Inlet Temp'");
        attr.setType(AttributeType.GAUGE);
        group.addAttrib(attr);

        attr = new Attrib();
        attr.setName("CurrentReading");
        attr.setAlias("sysBoardExhaustTemp");
        attr.setFilter("#ElementName == 'System Board Exhaust Temp'");
        attr.setType(AttributeType.GAUGE);
        group.addAttrib(attr);

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get());
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        Supplier<Resource> resourceSupplier = () -> mock(NodeLevelResource.class);

        XMLTag xmlTag = XMLDoc.newDocument(true).addRoot("body")
                .addTag("DCIM_NumericSensor")
                    .addTag("CurrentReading").setText("260")
                    .addTag("ElementName").setText("System Board Inlet Temp")
                    .gotoRoot()
                .addTag("DCIM_NumericSensor")
                    .addTag("CurrentReading").setText("370")
                    .addTag("ElementName").setText("System Board Exhaust Temp");

        List<Node> nodes = xmlTag.gotoRoot().getChildElement().stream()
            .map(el -> (Node)el)
            .collect(Collectors.toList());

        WsManCollector.processEnumerationResults(group, builder, resourceSupplier, nodes);

        // Verify
        Map<String, CollectionAttribute> attributesByName = CollectionSetUtils.getAttributesByName(builder.build());
        assertEquals(Double.valueOf(260), attributesByName.get("sysBoardInletTemp").getNumericValue());
        assertEquals(Double.valueOf(370), attributesByName.get("sysBoardExhaustTemp").getNumericValue());
    }

    @Test
    public void canSuccesfullyCollectFromGroupWithNoAttributes() throws CollectionInitializationException, CollectionException {
        OnmsNode node = mock(OnmsNode.class);
        NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.get(0)).thenReturn(node);

        WSManConfigDao configDao = mock(WSManConfigDao.class);
        when(configDao.getAgentConfig(any())).thenReturn(new Definition());

        Collection collection = new Collection();
        WSManDataCollectionConfigDao dataCollectionConfigDao = mock(WSManDataCollectionConfigDao.class);
        when(dataCollectionConfigDao.getCollectionByName("default")).thenReturn(collection);

        WsManCollector collector = new WsManCollector();
        collector.setWSManClientFactory(mock(WSManClientFactory.class));
        collector.setWSManConfigDao(configDao);
        collector.setWSManDataCollectionConfigDao(dataCollectionConfigDao);
        collector.setNodeDao(nodeDao);

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getAddress()).thenReturn(InetAddressUtils.getLocalHostAddress());
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get());

        Map<String, Object> collectionParams = Maps.newHashMap();
        collectionParams.put("collection", "default");
        collectionParams.putAll(collector.getRuntimeAttributes(agent, collectionParams));
        CollectionSet collectionSet = collector.collect(agent, collectionParams);

        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        assertEquals(0, CollectionSetUtils.getAttributesByName(collectionSet).size());
    }


    /**
     * NMS-8924: Verifies that the generated collection set includes a resource
     * for every node (XML) in the response.
     */
    @Test
    public void canGenerateManyResources() {
        // Define our resource type, and create a supplier that returns a new instance on every call
        NodeLevelResource node = mock(NodeLevelResource.class);
        ResourceType rt = new ResourceType();
        rt.setName("wsProcIndex");
        rt.setLabel("Processor (wsman)");
        rt.setResourceLabel("Processor (${wmiOSCpuName})");
        StorageStrategy strategy = new StorageStrategy();
        strategy.setClazz(SiblingColumnStorageStrategy.class.getCanonicalName());
        strategy.addParameter(new Parameter("sibling-column-name", "wmiOSCpuName"));
        rt.setStorageStrategy(strategy);
        PersistenceSelectorStrategy pstrategy = new PersistenceSelectorStrategy();
        pstrategy.setClazz(PersistAllSelectorStrategy.class.getCanonicalName());
        rt.setPersistenceSelectorStrategy(pstrategy);
        final AtomicInteger instanceId = new AtomicInteger();
        Supplier<Resource> resourceSupplier = () -> {
            return new GenericTypeResource(node, rt, Integer.toString(instanceId.getAndIncrement()));
        };

        // Define our group
        Group group = new Group();
        group.setName("windows-os-wmi-processor");
        addAttribute(group, "Name", "wmiOSCpuName", AttributeType.STRING);
        addAttribute(group, "InterruptsPersec", "wmiOSCpuIntsPerSec", AttributeType.GAUGE);
        addAttribute(group, "PercentProcessorTime", "wmiOSCpuPctProcTime", AttributeType.GAUGE);
        addAttribute(group, "PercentDPCTime", "wmiOSCpuPctDPCTime", AttributeType.GAUGE);
        addAttribute(group, "PercentInterruptTime", "wmiOSCpuPctIntrTime", AttributeType.GAUGE);
        addAttribute(group, "PercentUserTime", "wmiOSCpuPctUserTime", AttributeType.GAUGE);

        // Mock the agent
        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get());
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);

        // Sample data
        XMLTag xmlTag = XMLDoc.newDocument(true).addRoot("body")
                .addTag("Win32_PerfFormattedData_PerfOS_Processor")
                    .addTag("Name").setText("c0")
                    .addTag("InterruptsPersec").setText("95")
                    .gotoRoot()
                .addTag("Win32_PerfFormattedData_PerfOS_Processor")
                    .addTag("Name").setText("c1")
                    .addTag("InterruptsPersec").setText("100");

        List<Node> nodes = xmlTag.gotoRoot().getChildElement().stream()
            .map(el -> (Node)el)
            .collect(Collectors.toList());

        // Process the data and generate the collection set
        WsManCollector.processEnumerationResults(group, builder, resourceSupplier, nodes);

        // Verify the result
        CollectionSet collectionSet = builder.build();
        assertEquals(Arrays.asList(
                "wsProcIndex/c0/windows-os-wmi-processor/wmiOSCpuName[c0,null]",
                "wsProcIndex/c0/windows-os-wmi-processor/wmiOSCpuIntsPerSec[null,95.0]",
                "wsProcIndex/c1/windows-os-wmi-processor/wmiOSCpuName[c1,null]",
                "wsProcIndex/c1/windows-os-wmi-processor/wmiOSCpuIntsPerSec[null,100.0]"),
                CollectionSetUtils.flatten(collectionSet));
        assertEquals(Sets.newHashSet("c0", "c1"), CollectionSetUtils.getResourcesByLabel(collectionSet).keySet());
    }

    private static void addAttribute(Group group, String name, String alias, AttributeType type) {
        Attrib attr = new Attrib();
        attr.setName(name);
        attr.setAlias(alias);
        attr.setType(type);
        group.addAttrib(attr);
    }
}
