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
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableParm}'.
 */
public class ImmutableParmTest {

    @Test
    public void test() {
        Value value = new Value();
        value.setContent("test-value");
        value.setType("string");
        value.setEncoding("text");
        value.setExpand(false);

        Parm parm = new Parm();
        parm.setParmName("test");
        parm.setValue(value);

        // Mutable to Immutable
        IParm immutableParm = ImmutableMapper.fromMutableParm(parm);

        // Immutable to Mutable
        Parm convertedParm = Parm.copyFrom(immutableParm);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(parm);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedParm);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }
}
