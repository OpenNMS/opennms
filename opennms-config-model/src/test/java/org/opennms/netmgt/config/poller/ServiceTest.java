/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.poller;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.core.xml.JaxbUtils;

public class ServiceTest extends XmlTest<Service> {

	public ServiceTest(final Service sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		final Parameter parameter = new Parameter();
		parameter.setKey("oid");
		parameter.setValue(".1.3.6.1.2.1.1.2.0");
		final Service service = new Service();
		service.setName("SNMP");
		service.setUserDefined("false");
		service.setStatus("on");
		service.setInterval(300000);
		service.addParameter(parameter);

        return Arrays.asList(new Object[][] {
            {
            	service,
                "<service name='SNMP' interval='300000' user-defined='false' status='on'>\n" +
                "  <parameter key='oid' value='.1.3.6.1.2.1.1.2.0'/>\n" +
                "</service>\n",
                "target/classes/xsds/poller-configuration.xsd"
            }
        });
    }

    @Test
    public void testEmbededXml() throws Exception {
        String xml = "<service name='SNMP' interval='300000' user-defined='false' status='on'>\n" +
        "  <parameter key='oid' value='.1.3.6.1.2.1.1.2.0'/>\n" +
    	"  <parameter key='person'><person name='alejandro'/></parameter>\n" +
        "</service>\n";
    	Service s = JaxbUtils.unmarshal(Service.class, xml, false);
    	Assert.assertNotNull(s);
    	Assert.assertEquals("person", s.getParameterCollection().get(1).getKey());
    	Assert.assertNull(s.getParameterCollection().get(1).getValue());
    	Assert.assertEquals("<person xmlns=\"http://xmlns.opennms.org/xsd/config/poller\" name=\"alejandro\"/>", s.getParameterCollection().get(1).getAnyObject());
    }

}
