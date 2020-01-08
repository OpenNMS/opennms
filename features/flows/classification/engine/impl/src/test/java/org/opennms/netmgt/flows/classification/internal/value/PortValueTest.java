/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal.value;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PortValueTest {

    @Test
    public void verifySingleValue() {
        final PortValue portValue = new PortValue("5");
        assertThat(portValue.getPorts(), hasItems(5));
        assertThat(portValue.matches(5), is(true));
        assertThat(portValue.matches(1), is(false));
    }

    @Test
    public void verifyMultipleValues() {
        final PortValue portValue = new PortValue("1,2,3");
        assertThat(portValue.getPorts(), hasItems(1,2,3));
        assertThat(portValue.matches(1), is(true));
        assertThat(portValue.matches(2), is(true));
        assertThat(portValue.matches(3), is(true));
        assertThat(portValue.matches(4), is(false));
        assertThat(portValue.matches(5), is(false));
    }

    @Test
    public void verifyRange() {
        final PortValue portValue = new PortValue("10-13");
        assertThat(portValue.getPorts(), hasItems(10, 11, 12, 13));
        assertThat(portValue.matches(10), is(true));
        assertThat(portValue.matches(11), is(true));
        assertThat(portValue.matches(12), is(true));
        assertThat(portValue.matches(1), is(false));
        assertThat(portValue.matches(2), is(false));
        assertThat(portValue.matches(3), is(false));
    }
}
