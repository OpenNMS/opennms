/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.collection.dto;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;

public class CollectionSetDTOTest extends XmlTestNoCastor<CollectionSetDTO> {

    public CollectionSetDTOTest(CollectionSetDTO sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        return Arrays.asList(new Object[][] {
            {
                getCollectionSetWithAllResourceTypes(),
                "<collection-set status=\"SUCCEEDED\" timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" + 
                "   <agent store-by-fs=\"false\" node-id=\"0\" sys-up-time=\"0\"/>\n" +
                "   <collection-resource>\n" +
                "      <node-level-resource node-id=\"1\"/>\n" +
                "      <numeric-attribute group=\"ucd-sysstat\" name=\"CpuRawIdle\" type=\"gauge\" value=\"99\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <node-level-resource node-id=\"1\" path=\"opennns-jvm\"/>\n" +
                "      <numeric-attribute group=\"opennms-jvm\" name=\"heap\" type=\"gauge\" value=\"2048\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <interface-level-resource if-name=\"eth0\">\n" +
                "         <node-level-resource node-id=\"1\"/>\n" +
                "      </interface-level-resource>\n" +
                "      <numeric-attribute group=\"mib2-X-interfaces\" name=\"ifHCInOctets\" type=\"counter\" value=\"1001\"/>\n" +
                "      <string-attribute group=\"mib2-X-interfaces\" name=\"ifDescr\" type=\"string\" value=\"LAN\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <generic-type-resource name=\"Charles\" instance=\"id\">\n" +
                "         <node-level-resource node-id=\"1\"/>\n" +
                "      </generic-type-resource>\n" +
                "      <numeric-attribute group=\"net-snmp-disk\" name=\"ns-dsk1\" type=\"gauge\" identifier=\"some-oid\" value=\"1024\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <generic-type-resource name=\"Charles\" instance=\"idx\" timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" +
                "         <node-level-resource node-id=\"1\"/>\n" +
                "      </generic-type-resource>\n" +
                "      <numeric-attribute group=\"net-snmp-disk\" name=\"ns-dskTotal\" type=\"gauge\" identifier=\"some-oid\" value=\"1024\"/>\n" +
                "   </collection-resource>\n" +
                "</collection-set>"
            },
            {
                getCollectionSetWithAllNumberTypes(),
                "<collection-set status=\"SUCCEEDED\" timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" +
                        "   <agent store-by-fs=\"false\" node-id=\"0\" sys-up-time=\"0\"/>\n" +
                        "   <collection-resource>\n" +
                        "      <node-level-resource node-id=\"1\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"zero\" type=\"gauge\" value=\"0\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"pi\" type=\"gauge\" value=\"3.141592653589793\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"max\" type=\"gauge\" value=\"2147483647\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"min\" type=\"gauge\" value=\"4.9E-324\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"+inf\" type=\"gauge\" value=\"Infinity\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"-inf\" type=\"gauge\" value=\"-Infinity\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"-inf\" type=\"gauge\" value=\"-Infinity\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"NaN\" type=\"gauge\" value=\"NaN\"/>\n" +
                        "      <numeric-attribute group=\"long\" name=\"max long\" type=\"gauge\" value=\"-1\"/>\n" +
                        "      <numeric-attribute group=\"long\" name=\"min long\" type=\"gauge\" value=\"0\"/>\n" +
                        "   </collection-resource>\n" +
                        "</collection-set>"
            }
        });
    }

    private static CollectionSet getCollectionSetWithAllResourceTypes() {
        CollectionAgent collectionAgent = mock(CollectionAgent.class);
        NodeLevelResource nodeLevelResource = new NodeLevelResource(1);
        NodeLevelResource jmxNodeLevelResource = new NodeLevelResource(1, "opennns-jvm");
        InterfaceLevelResource interfaceLevelResource = new InterfaceLevelResource(nodeLevelResource, "eth0");

        ResourceType rt = mock(ResourceType.class, RETURNS_DEEP_STUBS);
        when(rt.getName()).thenReturn("Charles");
        when(rt.getStorageStrategy().getClazz()).thenReturn(MockStorageStrategy.class.getCanonicalName());
        when(rt.getPersistenceSelectorStrategy().getClazz()).thenReturn(MockPersistenceSelectorStrategy.class.getCanonicalName());

        DeferredGenericTypeResource deferredGenericTypeResource = new DeferredGenericTypeResource(nodeLevelResource, "Charles", "id");

        GenericTypeResource genericTypeResource = new GenericTypeResource(nodeLevelResource, rt, "idx");
        genericTypeResource.setTimestamp(new Date(0));
        ResourceTypeMapper.getInstance().setResourceTypeMapper((name) -> rt);

        // For complete coverage make sure that there is at least one attribute
        // for every different resource type, and that every different type
        // of attribute is represented at least once
        return new CollectionSetBuilder(collectionAgent)
                .withTimestamp(new Date(0))
                .withNumericAttribute(nodeLevelResource, "ucd-sysstat", "CpuRawIdle", 99, AttributeType.GAUGE)
                .withNumericAttribute(jmxNodeLevelResource, "opennms-jvm", "heap", 2048, AttributeType.GAUGE)
                .withNumericAttribute(interfaceLevelResource, "mib2-X-interfaces", "ifHCInOctets", 1001, AttributeType.COUNTER)
                .withStringAttribute(interfaceLevelResource, "mib2-X-interfaces", "ifDescr", "LAN")
                .withIdentifiedNumericAttribute(deferredGenericTypeResource, "net-snmp-disk", "ns-dsk1", 1024, AttributeType.GAUGE, "some-oid")
                .withIdentifiedNumericAttribute(genericTypeResource, "net-snmp-disk", "ns-dskTotal", 1024, AttributeType.GAUGE, "some-oid")
                .build();
    }

    private static CollectionSet getCollectionSetWithAllNumberTypes() {
        CollectionAgent collectionAgent = mock(CollectionAgent.class);
        NodeLevelResource nodeLevelResource = new NodeLevelResource(1);
        return new CollectionSetBuilder(collectionAgent)
                .withTimestamp(new Date(0))
                .withNumericAttribute(nodeLevelResource, "double", "zero", 0d, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "double", "pi", Math.PI, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "double", "max", Double.MAX_VALUE, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "double", "min", Double.MIN_VALUE, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "double", "+inf", Double.POSITIVE_INFINITY, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "double", "-inf", Double.NEGATIVE_INFINITY, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "double", "-inf", Double.NEGATIVE_INFINITY, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "double", "NaN", Double.NaN, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "long", "max long", Long.MAX_VALUE, AttributeType.GAUGE)
                .withNumericAttribute(nodeLevelResource, "long", "min long", Long.MIN_VALUE, AttributeType.GAUGE)
                .build();
    }
}
