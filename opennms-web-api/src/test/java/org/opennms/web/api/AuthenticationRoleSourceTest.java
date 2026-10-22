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
package org.opennms.web.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;

public class AuthenticationRoleSourceTest {
    @After
    public void tearDown() {
        Authentication.setRoleSource(null);
    }

    @Test
    public void testRoleSourceIsUsed() {
        Authentication.setRoleSource(() -> Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"));
        assertTrue(Authentication.isValidRole("ROLE_OPERATOR"));
        assertFalse(Authentication.isValidRole("ROLE_RTC"));
        assertEquals(Arrays.asList("ROLE_ADMIN", "ROLE_OPERATOR"), Authentication.getAvailableRoles());
    }

    @Test
    public void testRoleSourceIsCachedForASecond() {
        final AtomicInteger calls = new AtomicInteger();
        Authentication.setRoleSource(() -> {
            calls.incrementAndGet();
            return Arrays.asList("ROLE_ADMIN");
        });
        Authentication.isValidRole("ROLE_ADMIN");
        Authentication.isValidRole("ROLE_ADMIN");
        assertEquals(1, calls.get());
    }

    @Test
    public void testWithoutSourceTheBuiltinRolesAreValid() {
        assertTrue(Authentication.isValidRole(Authentication.ROLE_RTC));
    }

    @Test
    public void testHasRoleSource() {
        assertFalse(Authentication.hasRoleSource());
        Authentication.setRoleSource(() -> Arrays.asList("ROLE_ADMIN"));
        assertTrue(Authentication.hasRoleSource());
    }
}
