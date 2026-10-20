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
package org.opennms.netmgt.config.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.After;
import org.junit.Test;

public class UserMgmtStoreTest {
    @After
    public void tearDown() {
        System.clearProperty(UserMgmtStore.PROPERTY);
    }

    @Test
    public void testDefaultIsDatabase() {
        assertTrue(UserMgmtStore.isDatabase(new Properties()));
    }

    @Test
    public void testXml() {
        final Properties p = new Properties();
        p.setProperty(UserMgmtStore.PROPERTY, " xml ");
        assertFalse(UserMgmtStore.isDatabase(p));
    }

    @Test
    public void testUnknownValueFails() {
        final Properties p = new Properties();
        p.setProperty(UserMgmtStore.PROPERTY, "ldap");
        final IllegalStateException e = assertThrows(IllegalStateException.class, () -> UserMgmtStore.isDatabase(p));
        assertEquals("org.opennms.usermgmt.store must be \"db\" or \"xml\", not \"ldap\"", e.getMessage());
    }
}
