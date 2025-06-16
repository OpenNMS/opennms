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
package org.opennms.netmgt.enlinkd.service.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

public class CompositeKeyTest {
    @Test(expected = IllegalArgumentException.class)
    public void shouldRejectNoKeysInvocation() {
        new CompositeKey();
    }

    @Test
    public void equalsAndHashCodeShouldWork() {
        CompositeKey same1 = new CompositeKey("aa", 33);
        CompositeKey same2 = new CompositeKey("aa", 33);
        CompositeKey different = new CompositeKey("aa", 31);
        assertEquals(same1, same2);
        assertEquals(same1.hashCode(), same2.hashCode());
        assertNotEquals(same1, different);
    }
}
