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
import org.opennms.netmgt.xml.event.Correlation;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableCorrelation}'.
 */
public class ImmutableCorrelationTest {

    @Test
    public void test() {
        Correlation correlation = new Correlation();
        correlation.setState("off");
        correlation.setPath("suppressDuplicates");
        correlation.getCueiCollection().add("cancelling-uei");
        correlation.setCmin("1");
        correlation.setCmax("5");
        correlation.setCtime("");

        // Mutable to Immutable
        ICorrelation immutableCorrelation = ImmutableMapper.fromMutableCorrelation(correlation);

        // Attempt to add to immutable list.
        try {
            immutableCorrelation.getCueiCollection().add("cancelling-uei-2");
        } catch (Exception e) {
            // Expected...
        }

        // Immutable to Mutable
        Correlation convertedCorrelation = Correlation.copyFrom(immutableCorrelation);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(correlation);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedCorrelation);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }
}
