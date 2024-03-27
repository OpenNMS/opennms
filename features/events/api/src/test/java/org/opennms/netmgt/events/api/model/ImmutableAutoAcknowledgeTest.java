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
package org.opennms.netmgt.events.api.model;

import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.netmgt.xml.event.Autoacknowledge;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableAutoAcknowledge}'.
 */
public class ImmutableAutoAcknowledgeTest {

    @Test
    public void test() {
        Autoacknowledge autoacknowledge = new Autoacknowledge();
        autoacknowledge.setContent("test-value");
        autoacknowledge.setState("on");

        // Mutable to Immutable
        IAutoAcknowledge immutableAutoAcknowledge = ImmutableMapper.fromMutableAutoAcknowledge(autoacknowledge);

        // Immutable to Mutable
        Autoacknowledge convertedAutoAcknowledge = Autoacknowledge.copyFrom(immutableAutoAcknowledge);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(autoacknowledge);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedAutoAcknowledge);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }
}
