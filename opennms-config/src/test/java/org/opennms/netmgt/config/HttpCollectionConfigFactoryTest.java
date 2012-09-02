/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class HttpCollectionConfigFactoryTest extends TestCase {

    public void testHttpCollectionConfigFactoryReader() throws MarshalException, ValidationException, UnsupportedEncodingException {
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
                "</http-datacollection-config>").getBytes("UTF-8"));
        new HttpCollectionConfigFactory(rdr);
        assertNotNull(HttpCollectionConfigFactory.getConfig());
    }

}
