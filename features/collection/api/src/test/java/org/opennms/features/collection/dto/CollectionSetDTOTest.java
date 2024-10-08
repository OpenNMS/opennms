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
                "      <numeric-attribute group=\"ucd-sysstat\" name=\"CpuRawIdle\" type=\"gauge\" value=\"99.0\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <node-level-resource node-id=\"1\" path=\"opennns-jvm\"/>\n" +
                "      <numeric-attribute group=\"opennms-jvm\" name=\"heap\" type=\"gauge\" value=\"2048.0\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <interface-level-resource if-name=\"eth0\">\n" +
                "         <node-level-resource node-id=\"1\"/>\n" +
                "      </interface-level-resource>\n" +
                "      <numeric-attribute group=\"mib2-X-interfaces\" name=\"ifHCInOctets\" type=\"counter\" value=\"1001.0\"/>\n" +
                "      <string-attribute group=\"mib2-X-interfaces\" name=\"ifDescr\" type=\"string\" value=\"LAN\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <generic-type-resource name=\"Charles\" instance=\"id\">\n" +
                "         <node-level-resource node-id=\"1\"/>\n" +
                "      </generic-type-resource>\n" +
                "      <numeric-attribute group=\"net-snmp-disk\" name=\"ns-dsk1\" type=\"gauge\" identifier=\"some-oid\" value=\"1024.0\"/>\n" +
                "   </collection-resource>\n" +
                "   <collection-resource>\n" +
                "      <generic-type-resource name=\"Charles\" instance=\"idx\" timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" +
                "         <node-level-resource node-id=\"1\"/>\n" +
                "      </generic-type-resource>\n" +
                "      <numeric-attribute group=\"net-snmp-disk\" name=\"ns-dskTotal\" type=\"gauge\" identifier=\"some-oid\" value=\"1024.0\"/>\n" +
                "   </collection-resource>\n" +
                "</collection-set>"
            },
            {
                getCollectionSetWithAllNumberTypes(),
                "<collection-set status=\"SUCCEEDED\" timestamp=\"" + StringUtils.iso8601OffsetString(new Date(0), ZoneId.systemDefault(), ChronoUnit.SECONDS) + "\">\n" +
                        "   <agent store-by-fs=\"false\" node-id=\"0\" sys-up-time=\"0\"/>\n" +
                        "   <collection-resource>\n" +
                        "      <node-level-resource node-id=\"1\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"zero\" type=\"gauge\" value=\"0.0\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"pi\" type=\"gauge\" value=\"3.141592653589793\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"max\" type=\"gauge\" value=\"1.7976931348623157E308\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"min\" type=\"gauge\" value=\"4.9E-324\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"+inf\" type=\"gauge\" value=\"Infinity\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"-inf\" type=\"gauge\" value=\"-Infinity\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"-inf\" type=\"gauge\" value=\"-Infinity\"/>\n" +
                        "      <numeric-attribute group=\"double\" name=\"NaN\" type=\"gauge\" value=\"NaN\"/>\n" +
                        "      <numeric-attribute group=\"long\" name=\"max long\" type=\"gauge\" value=\"9.223372036854776E18\"/>\n" +
                        "      <numeric-attribute group=\"long\" name=\"min long\" type=\"gauge\" value=\"-9.223372036854776E18\"/>\n" +
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
