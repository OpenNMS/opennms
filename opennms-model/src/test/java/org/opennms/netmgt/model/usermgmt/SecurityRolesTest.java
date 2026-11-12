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
package org.opennms.netmgt.model.usermgmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;

public class SecurityRolesTest {

    @Test
    public void testToRoleName() {
        assertEquals("ROLE_OPERATOR", SecurityRoles.toRoleName("operator"));
        assertEquals("ROLE_STAGE", SecurityRoles.toRoleName("  Stage "));
        assertEquals("ROLE_NOC_TIER_2", SecurityRoles.toRoleName("noc_tier_2"));
        // always prefixed, as security-roles.properties has always been read
        assertEquals("ROLE_ROLE_ADMIN", SecurityRoles.toRoleName("role_admin"));
    }

    @Test
    public void testToRoleNameIgnoresTheDefaultLocale() {
        final Locale defaultLocale = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals("ROLE_ADMIN", SecurityRoles.toRoleName("admin"));
        } finally {
            Locale.setDefault(defaultLocale);
        }
    }

    @Test
    public void testToRoleNameRejectsBlankNames() {
        assertThrows(IllegalArgumentException.class, () -> SecurityRoles.toRoleName(null));
        assertThrows(IllegalArgumentException.class, () -> SecurityRoles.toRoleName(""));
        assertThrows(IllegalArgumentException.class, () -> SecurityRoles.toRoleName("  "));
    }

    @Test
    public void testBuiltinRoles() {
        assertEquals(16, SecurityRoles.BUILTIN_ROLES.size());
        assertEquals(SecurityRoles.ROLE_USER, SecurityRoles.BUILTIN_ROLES.keySet().iterator().next());
        for (final String name : SecurityRoles.BUILTIN_ROLES.keySet()) {
            assertTrue(name, name.startsWith(SecurityRoles.ROLE_PREFIX));
            assertTrue(name, SecurityRoles.isBuiltin(name));
        }
        assertFalse(SecurityRoles.isBuiltin("ROLE_OPERATOR"));
        assertThrows(UnsupportedOperationException.class, () -> SecurityRoles.BUILTIN_ROLES.put("ROLE_X", "x"));
    }
}
