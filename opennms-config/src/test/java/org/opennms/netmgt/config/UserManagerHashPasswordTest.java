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
package org.opennms.netmgt.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.junit.Test;

public class UserManagerHashPasswordTest {

    @Test
    public void testHashIsSaltedAndChecksWithTheStrongEncryptor() {
        final String hash = UserManager.hashPassword("s3cret");
        assertNotEquals(hash, UserManager.hashPassword("s3cret"));
        assertNotEquals("s3cret", hash);
        // the format the database-backed users and the legacy password check have always shared
        final StrongPasswordEncryptor encryptor = new StrongPasswordEncryptor();
        assertTrue(encryptor.checkPassword("s3cret", hash));
        assertFalse(encryptor.checkPassword("other", hash));
    }
}
