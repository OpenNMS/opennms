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

package org.opennms.web.rest.v2.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.protocols.xml.config.XmlDataCollection;
import org.opennms.protocols.xml.config.XmlDataCollectionConfig;
import org.opennms.web.rest.v2.model.XmlCollectionDto;
import org.opennms.web.rest.v2.model.XmlGroupDto;

public class XmlCollectionConfigMapperTest {

    private static final String XML = """
            <xml-datacollection-config rrdRepository="/var/rrd/" xmlns="http://xmlns.opennms.org/xsd/config/xml-datacollection">
                <xml-collection name="c">
                    <rrd step="300"><rra>RRA:AVERAGE:0.5:1:2016</rra></rrd>
                    <xml-source url="http://{ipaddr}/stats">
                        <import-groups>xml-datacollection/shared.xml</import-groups>
                        <request method="POST">
                            <parameter name="timeout" value="3000"/>
                            <header name="Accept" value="application/json"/>
                            <content type="application/json"><![CDATA[{"q":1}]]></content>
                        </request>
                        <xml-group name="g" resource-type="fsType" resource-xpath="/fs" key-xpath="@name"
                                   timestamp-xpath="/ts" timestamp-format="yyyy-MM-dd">
                            <xml-object name="used" type="counter" xpath="used">
                                <xml-mapping from="a" to="1"/>
                            </xml-object>
                            <resource-key>
                                <key-xpath>@a</key-xpath>
                                <key-xpath>@b</key-xpath>
                            </resource-key>
                        </xml-group>
                    </xml-source>
                </xml-collection>
            </xml-datacollection-config>""";

    @Test
    public void roundTripsEveryAttribute() {
        final XmlDataCollection collection = JaxbUtils.unmarshal(XmlDataCollectionConfig.class, XML).getXmlDataCollections().get(0);

        final XmlCollectionDto dto = XmlCollectionConfigMapper.toDto(collection);
        assertEquals(List.of("shared"), dto.getSources().get(0).getImportGroups());
        assertEquals("POST", dto.getSources().get(0).getRequest().getMethod());
        assertEquals("{\"q\":1}", dto.getSources().get(0).getRequest().getContent());
        final XmlGroupDto group = dto.getSources().get(0).getGroups().get(0);
        assertEquals("fsType", group.getResourceType());
        assertEquals("@name", group.getKeyXpath());
        assertEquals("yyyy-MM-dd", group.getTimestampFormat());
        assertEquals(List.of("@a", "@b"), group.getResourceKeys());
        assertEquals("counter", group.getObjects().get(0).getType());
        assertEquals("1", group.getObjects().get(0).getMappings().get(0).getTo());

        final XmlDataCollection back = XmlCollectionConfigMapper.toEntity(dto);
        // Only the import-groups spelling differs: the id rather than the former file path.
        collection.getXmlSources().get(0).setImportGroupsList(List.of("shared"));
        assertEquals(collection, back);
        assertEquals(collection.getXmlSources().get(0).getRequest().toString(), back.getXmlSources().get(0).getRequest().toString());
        assertEquals(collection.getXmlSources().get(0).getXmlGroups().get(0).getXmlObjects().get(0).getXmlMappings(),
                back.getXmlSources().get(0).getXmlGroups().get(0).getXmlObjects().get(0).getXmlMappings());
    }

    @Test
    public void dropsBlankOptionalValues() {
        final XmlCollectionDto dto = XmlCollectionConfigMapper.toDto(
                JaxbUtils.unmarshal(XmlDataCollectionConfig.class, XML).getXmlDataCollections().get(0));
        final XmlGroupDto group = dto.getSources().get(0).getGroups().get(0);
        group.setKeyXpath(" ");
        group.setResourceKeys(List.of(" "));
        dto.getSources().get(0).getRequest().setContentType("");
        dto.getSources().get(0).getRequest().setContent("");

        final XmlDataCollection back = XmlCollectionConfigMapper.toEntity(dto);
        assertNull(back.getXmlSources().get(0).getXmlGroups().get(0).getKeyXpath());
        assertNull(back.getXmlSources().get(0).getXmlGroups().get(0).getXmlResourceKey());
        assertNull(back.getXmlSources().get(0).getRequest().getContent());
    }
}
