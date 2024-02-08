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
import org.opennms.netmgt.xml.event.*;

import static org.junit.Assert.*;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableMask}'.
 */
public class ImmutableMaskTest {

    @Test
    public void test() {
        Maskelement maskElement = new Maskelement();
        maskElement.setMename("ME1");
        maskElement.getMevalueCollection().add("V1");
        maskElement.getMevalueCollection().add("V2");

        Mask mask = new Mask();
        mask.getMaskelementCollection().add(maskElement);

        // Mutable to Immutable
        IMask immutableMask = ImmutableMapper.fromMutableMask(mask);

        // Attempt to add to immutable list.
        try {
            immutableMask.getMaskelementCollection().add(ImmutableMaskElement.newBuilder().build());
            fail();
        } catch (Exception e) {
            // Expected...
        }

        // Attempt to add to immutable list.
        try {
            immutableMask.getMaskelementCollection().iterator().next().getMevalueCollection().add("V3");
            fail();
        } catch (Exception e) {
            // Expected...
        }

        // Immutable to Mutable
        Mask convertedMask = Mask.copyFrom(immutableMask);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(mask);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedMask);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }
}
