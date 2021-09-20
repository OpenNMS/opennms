/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
