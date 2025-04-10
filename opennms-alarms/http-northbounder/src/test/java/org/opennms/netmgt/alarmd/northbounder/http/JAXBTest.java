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
package org.opennms.netmgt.alarmd.northbounder.http;

import static org.junit.Assert.assertNotNull;
import static org.opennms.core.test.xml.XmlTest.assertXmlEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

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
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class JAXBTest {

    /**
     * The Class TestNorthBoundAlarm.
     */
    @XmlRootElement(name="test-alarm")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class TestNorthBoundAlarm {

        /** The ID. */
        @XmlElement(name="id")
        private String m_id;

        /** The name. */
        private String m_name;

        /** The status. */
        private String m_status;

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return m_id;
        }

        /**
         * Sets the id.
         *
         * @param id the new id
         */
        public void setId(String id) {
            m_id = id;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return m_name;
        }

        /**
         * Sets the name.
         *
         * @param name the new name
         */
        public void setName(String name) {
            m_name = name;
        }

        /**
         * Gets the status.
         *
         * @return the status
         */
        public String getStatus() {
            return m_status;
        }

        /**
         * Sets the status.
         *
         * @param status the new status
         */
        public void setStatus(String status) {
            m_status = status;
        }
    }

    /**
     * Test marshall.
     *
     * @throws Exception the exception
     */
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

        String result = new String(utf8, StandardCharsets.UTF_8);
        assertXmlEquals(expectedXML, result);

        System.err.println(result);

        // unmarshall the generated XML

        Unmarshaller unmarshaller = context.createUnmarshaller();
        //		unmarshaller.setSchema(schema);
        Source source = new StreamSource(new ByteArrayInputStream(utf8));
        TestNorthBoundAlarm read = unmarshaller.unmarshal(source, TestNorthBoundAlarm.class).getValue();

        assertNotNull(read);

        // round trip back to XML and make sure we get the same thing
        ByteArrayOutputStream reout = new ByteArrayOutputStream();
        marshaller.marshal(read, reout);

        String roundTrip = new String(reout.toByteArray(), StandardCharsets.UTF_8);

        assertXmlEquals(expectedXML, roundTrip);
    }

}
