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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class HttpCollectionConfigFactoryTest {

    @Test
    public void testHttpCollectionConfigFactoryReader() throws IOException {
        InputStream rdr = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
                "<http-datacollection-config  \n" + 
                "    xmlns:http-dc=\"http://xmlns.opennms.org/xsd/config/http-datacollection\" \n" + 
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" + 
                "    xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/config/http-datacollection http://www.opennms.org/xsd/config/http-datacollection-config.xsd\" \n" + 
                "    rrdRepository=\"${install.share.dir}/rrd/snmp/\" >\n" + 
                "  <http-collection name=\"default\">\n" + 
                "    <rrd step=\"300\">\n" + 
                "      <rra>RRA:AVERAGE:0.5:1:8928</rra>\n" + 
                "      <rra>RRA:AVERAGE:0.5:12:8784</rra>\n" + 
                "      <rra>RRA:MIN:0.5:12:8784</rra>\n" + 
                "      <rra>RRA:MAX:0.5:12:8784</rra>\n" + 
                "    </rrd>\n" + 
                "    <uris>\n" + 
                "      <uri name=\"test-document-count\">\n" + 
                "        <url path=\"/test/resources/httpcolltest.html\"\n" + 
                "             user-agent=\"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/412 (KHTML, like Gecko) Safari/412\" \n" + 
                "             matches=\".*([0-9]+).*\" response-range=\"100-399\" >\n" + 
                "        </url>\n" + 
                "        <attributes>\n" + 
                "          <attrib alias=\"documentCount\" match-group=\"1\" type=\"counter32\"/>\n" + 
                "        </attributes>\n" + 
                "      </uri>\n" + 
                "    </uris>\n" + 
                "  </http-collection>\n" + 
                "</http-datacollection-config>").getBytes(StandardCharsets.UTF_8));
        new HttpCollectionConfigFactory(rdr);
        assertNotNull(HttpCollectionConfigFactory.getConfig());
    }

}
