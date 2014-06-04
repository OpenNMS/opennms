/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.http;

import static org.junit.Assert.assertNotNull;
import static org.opennms.core.test.xml.XmlTest.assertXmlEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;

/**
 * Tests Marshaling of North bound Alarm
 * 
 * FIXME: This is just a stub for getting started.  Needs lots of work.
 * 
 * @author <a mailto:brozow@opennms.org>Matt Brozowski</a>
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
public class JAXBTest {
	
    @XmlRootElement(name="test-alarm")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestNorthBoundAlarm {
        
        @XmlElement(name="id")
        private String m_id;
        private String m_name;
        private String m_status;
        
        public String getId() {
            return m_id;
        }
        public void setId(String id) {
            m_id = id;
        }
        public String getName() {
            return m_name;
        }
        public void setName(String name) {
            m_name = name;
        }
        public String getStatus() {
            return m_status;
        }
        public void setStatus(String status) {
            m_status = status;
        }
    }


	@Test
	public void testMarshall() throws Exception {
		
		final String expectedXML = "" +
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
				"<test-alarm>\n" + 
				"    <id>23</id>\n" + 
				"</test-alarm>\n" +
				"";

		TestNorthBoundAlarm nba = new TestNorthBoundAlarm();
		nba.setId("23");
		
		// Create a Marshaller
		JAXBContext context = JAXBContext.newInstance(TestNorthBoundAlarm.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		// save the output in a byte array
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		// marshall the output
		marshaller.marshal(nba, out);

		// verify its matches the expected results
		byte[] utf8 = out.toByteArray();

		String result = new String(utf8, "UTF-8");
		assertXmlEquals(expectedXML, result);
		
		System.err.println(result);
		
		// unmarshall the generated XML
		
//		URL xsd = getClass().getResource("/ncs-model.xsd");
//		
//		assertNotNull(xsd);
//		
//		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
//		Schema schema = schemaFactory.newSchema(xsd);
		
		Unmarshaller unmarshaller = context.createUnmarshaller();
//		unmarshaller.setSchema(schema);
		Source source = new StreamSource(new ByteArrayInputStream(utf8));
		TestNorthBoundAlarm read = unmarshaller.unmarshal(source, TestNorthBoundAlarm.class).getValue();
		
		assertNotNull(read);
		
		// round trip back to XML and make sure we get the same thing
		ByteArrayOutputStream reout = new ByteArrayOutputStream();
		marshaller.marshal(read, reout);
		
		String roundTrip = new String(reout.toByteArray(), "UTF-8");
		
		assertXmlEquals(expectedXML, roundTrip);
	}

}
