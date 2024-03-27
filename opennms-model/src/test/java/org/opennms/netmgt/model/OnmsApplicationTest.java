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
package org.opennms.netmgt.model;

import java.io.IOException;

import org.junit.Test;
import org.opennms.core.test.xml.JsonTest;
import org.opennms.core.test.xml.XmlTest;


public class OnmsApplicationTest {

    @Test
    public void testMarshalXml() {
        OnmsApplication application = new OnmsApplication();
        application.setId(100);
        application.setName("Dummy");

        String applicationString = XmlTest.marshalToXmlWithJaxb(application);
        XmlTest.assertXmlEquals(
                "<application id=\"100\">\n" +
                "   <name>Dummy</name>\n" +
                "</application>\n",
                applicationString);
    }

    @Test
    public void testMarshalJson() throws IOException {
        OnmsApplication application = new OnmsApplication();
        application.setId(100);
        application.setName("Dummy");

        String applicationString = JsonTest.marshalToJson(application);
        JsonTest.assertJsonEquals("{\"name\" : \"Dummy\", \"id\" : 100, \"perspectiveLocations\" : []}}", applicationString);
    }
}
