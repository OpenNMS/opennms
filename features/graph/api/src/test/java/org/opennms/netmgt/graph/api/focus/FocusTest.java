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