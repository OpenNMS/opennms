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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.config.httpdatacollection.HttpDatacollectionConfig;
import org.opennms.netmgt.config.httpdatacollection.Url;
import org.opennms.web.rest.v2.model.HttpCollectionConfigDto;
import org.opennms.web.rest.v2.model.HttpUrlDto;

public class HttpCollectionConfigMapperTest {

    private static final String FULL_XML = """
            <http-datacollection-config xmlns="http://xmlns.opennms.org/xsd/config/http-datacollection" rrdRepository="/opt/opennms/share/rrd/snmp/">
              <http-collection name="full">
                <rrd step="300"><rra>RRA:AVERAGE:0.5:1:2016</rra><rra>RRA:MAX:0.5:288:366</rra></rrd>
                <uris>
                  <uri name="everything">
                    <url method="POST" http-version="1.0" user-agent="agent" virtual-host="vhost" scheme="https"
                         user-info="user:secret" host="example.org" port="8443" path="/p" query="a=b" fragment="f"
                         matches="A=([0-9]+) B=(\\w+)" response-range="200-299" canonical-equivalence="true"
                         case-insensitive="true" comments="true" dotall="true" literal="false" multiline="true"
                         unicode-case="true" unix-lines="true">
                      <parameters><parameter key="k" value="v"/></parameters>
                      <headers><header name="Authorization" value="Bearer ${scv:token:password}"/></headers>
                    </url>
                    <attributes>
                      <attrib alias="a" match-group="1" type="counter"/>
                      <attrib alias="b" match-group="2" type="string"/>
                    </attributes>
                  </uri>
                  <uri name="defaults"><url path="/d"/></uri>
                </uris>
              </http-collection>
            </http-datacollection-config>""";

    @Test
    public void roundTripsAFullyPopulatedConfig() {
        final HttpDatacollectionConfig config = JaxbUtils.unmarshal(HttpDatacollectionConfig.class, FULL_XML);
        final HttpCollectionConfigDto dto = HttpCollectionConfigMapper.toDto(config);
        final HttpDatacollectionConfig back = HttpCollectionConfigMapper.toEntity(dto);

        final Url full = back.getHttpCollection().get(0).getUris().get(0).getUrl();
        assertEquals(config.getHttpCollection().get(0).getUris().get(0).getUrl(), full);
        assertEquals(config.getHttpCollection().get(0).getUris().get(0).getAttributes(),
                back.getHttpCollection().get(0).getUris().get(0).getAttributes());
        assertEquals(config.getHttpCollection().get(0).getRrd(), back.getHttpCollection().get(0).getRrd());
        assertEquals(config.getRrdRepository(), back.getRrdRepository());
        assertEquals(AttributeType.STRING, back.getHttpCollection().get(0).getUris().get(0).getAttributes().get(1).getType());

        // Mapping the result again gives the same DTO, so ETags are stable across a save.
        assertEquals(dto.getCollections().get(0).getUris().get(1).getUrl().getPort(),
                HttpCollectionConfigMapper.toDto(back).getCollections().get(0).getUris().get(1).getUrl().getPort());
    }

    @Test
    public void showsDefaultsAndWritesThemExplicitly() {
        final HttpDatacollectionConfig config = JaxbUtils.unmarshal(HttpDatacollectionConfig.class, FULL_XML);
        final HttpUrlDto defaults = HttpCollectionConfigMapper.toDto(config).getCollections().get(0).getUris().get(1).getUrl();
        assertEquals("GET", defaults.getMethod());
        assertEquals("1.1", defaults.getHttpVersion());
        assertEquals("http", defaults.getScheme());
        assertEquals("${ipaddr}", defaults.getHost());
        assertEquals(Integer.valueOf(80), defaults.getPort());
        assertEquals("(.*)", defaults.getMatches());
        assertEquals("100-399", defaults.getResponseRange());
        assertEquals(Boolean.FALSE, defaults.getDotall());
        assertNull(defaults.getUserInfo());
        assertTrue(defaults.getHeaders().isEmpty());
    }

    @Test
    public void storesEmptyListsAsAbsentWrappers() {
        final HttpDatacollectionConfig config = JaxbUtils.unmarshal(HttpDatacollectionConfig.class, FULL_XML);
        final HttpCollectionConfigDto dto = HttpCollectionConfigMapper.toDto(config);
        dto.getCollections().get(0).getUris().get(0).setAttributes(List.of());
        dto.getCollections().get(0).getUris().get(0).getUrl().setHeaders(List.of());

        final String xml = JaxbUtils.marshal(HttpCollectionConfigMapper.toEntity(dto));
        // <attributes/> and <headers/> without children would not validate against the schema.
        assertFalse(xml, xml.contains("<attributes"));
        assertFalse(xml, xml.contains("<headers"));
        JaxbUtils.unmarshal(HttpDatacollectionConfig.class, xml, true);
    }
}
