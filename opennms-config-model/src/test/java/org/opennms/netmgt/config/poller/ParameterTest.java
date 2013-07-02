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
import org.opennms.netmgt.config.poller.tests.Person;
import org.w3c.dom.Node;

public class ParameterTest extends XmlTest<Parameter> {

    public ParameterTest(final Parameter sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final Parameter parameter = new Parameter();
        parameter.setKey("firstName");
        parameter.setValue("alejandro");
        
        return Arrays.asList(new Object[][] {
            {
                parameter,
                "<parameter key='firstName' value='alejandro'/>\n",
                "target/classes/xsds/poller-configuration.xsd"
            }
        });
    }

    @Test
    public void testEmbededXml() throws Exception {
    	String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                     "<parameter key=\"person\" xmlns=\"http://xmlns.opennms.org/xsd/config/poller\">\n" +
                     "    <person firstName=\"alejandro\" lastName=\"galue\"/>\n" + 
                     "</parameter>\n";
    	Parameter p = JaxbUtils.unmarshal(Parameter.class, xml, true);
    	Assert.assertNotNull(p);
    	Assert.assertEquals("person", p.getKey());
    	Assert.assertNull(p.getValue());
    	Assert.assertNotNull(p.getAnyObject());
        Assert.assertTrue(p.getAnyObject() instanceof Node);
        Node node = (Node) p.getAnyObject();
        Person person = JaxbUtils.unmarshal(Person.class, node, false);
        Assert.assertEquals("alejandro", person.getFirstName());
    	String jaxbXml = JaxbUtils.marshal(p);
    	Assert.assertEquals(xml, jaxbXml);
    }

}
