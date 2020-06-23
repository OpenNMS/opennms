/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2016 The OpenNMS Group, Inc.
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
