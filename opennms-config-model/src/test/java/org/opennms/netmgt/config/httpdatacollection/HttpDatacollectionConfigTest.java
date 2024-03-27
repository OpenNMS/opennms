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
package org.opennms.netmgt.config.httpdatacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.netmgt.collection.api.AttributeType;

public class HttpDatacollectionConfigTest extends XmlTestNoCastor<HttpDatacollectionConfig> {

    public HttpDatacollectionConfigTest(final HttpDatacollectionConfig sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        HttpDatacollectionConfig config = new HttpDatacollectionConfig();
        config.setRrdRepository("/opt/opennms/rrd/snmp/");

        Rrd rrd = new Rrd();
        rrd.setStep(300);
        rrd.getRra().add("RRA:AVERAGE:0.5:1:2016");

        HttpCollection httpCollection = new HttpCollection();
        httpCollection.setName("doc-count");
        httpCollection.setRrd(rrd);
        
        Url url = new Url();
        url.setPath("/test/resources/httpcolltest.html");
        url.setUserAgent("Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/412 (KHTML, like Gecko) Safari/412");
        url.setMatches(".*([0-9]+).*");
        url.setResponseRange("100-399");

        Uri uri = new Uri();
        uri.setName("document-counts");
        uri.setUrl(url);

        Attrib attrib = new Attrib();
        attrib.setAlias("documentCount");
        attrib.setMatchGroup(1);
        attrib.setType(AttributeType.COUNTER);
        
        uri.setAttributes(Collections.singletonList(attrib));

        httpCollection.setUris(Collections.singletonList(uri));

        config.getHttpCollection().add(httpCollection);

        return Arrays.asList(new Object[][] { {
            config,
            "<http-datacollection-config rrdRepository=\"/opt/opennms/rrd/snmp/\">" +
                 "<http-collection name=\"doc-count\">" +
                     "<rrd step=\"300\">" +
                         "<rra>RRA:AVERAGE:0.5:1:2016</rra>" +
                     "</rrd>" +
                     "<uris>" +
                         "<uri name=\"document-counts\">" +
                             "<url path=\"/test/resources/httpcolltest.html\" " +
                                 "user-agent=\"Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/412 (KHTML, like Gecko) Safari/412\" " +
                                 "matches=\".*([0-9]+).*\" response-range=\"100-399\" />" +
                          "<attributes>" +
                              "<attrib alias=\"documentCount\" match-group=\"1\" type=\"counter\"/>" +
                          "</attributes>" +
                         "</uri>" +
                     "</uris>" +
                 "</http-collection>" +
            "</http-datacollection-config>",
            "target/classes/xsds/http-datacollection-config.xsd", }, });
    }
}
