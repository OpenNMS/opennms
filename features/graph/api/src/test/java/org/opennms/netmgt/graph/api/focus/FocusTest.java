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

package org.opennms.netmgt.graph.api.focus;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FocusTest {

    @Test
    public void verifyEquals() {
        assertEquals(new Focus(FocusStrategy.EMPTY), new Focus(FocusStrategy.EMPTY));
        assertEquals(new Focus(FocusStrategy.ALL), new Focus(FocusStrategy.ALL));
        assertEquals(new Focus(FocusStrategy.FIRST), new Focus(FocusStrategy.FIRST));
        assertEquals(new Focus(FocusStrategy.SELECTION), new Focus(FocusStrategy.SELECTION));

        // TODO MVR verify SelectiveFocus and vertexRef list in focus
    }

}