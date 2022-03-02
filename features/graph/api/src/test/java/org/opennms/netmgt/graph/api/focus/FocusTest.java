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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.Test;
import org.opennms.netmgt.graph.api.VertexRef;

import com.google.common.collect.Lists;

public class FocusTest {

    @Test
    public void verifyEquals() {
        // Same id should match
        assertEquals(new Focus(FocusStrategy.EMPTY), new Focus(FocusStrategy.EMPTY));
        assertEquals(new Focus(FocusStrategy.ALL), new Focus(FocusStrategy.ALL));
        assertEquals(new Focus(FocusStrategy.FIRST), new Focus(FocusStrategy.FIRST));
        assertEquals(new Focus(FocusStrategy.SELECTION), new Focus(FocusStrategy.SELECTION));

        // Different ids, but same list should not match
        assertThat(new Focus(FocusStrategy.EMPTY, Lists.newArrayList()), not(equalTo(new Focus(FocusStrategy.SELECTION, Lists.newArrayList()))));

        // Verify same lists match
        final ArrayList<VertexRef> singleList = Lists.newArrayList(new VertexRef("dummy", "v1"));
        final ArrayList<VertexRef> singleListCopy = Lists.newArrayList(new VertexRef("dummy", "v1"));
        assertEquals(new Focus(FocusStrategy.SELECTION, singleList), new Focus(FocusStrategy.SELECTION, singleListCopy));
    }

}