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
package org.opennms.netmgt.config.users;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UserSettersTest {

    private static final String TEXT = "A & <B> 'c'";

    @Test
    public void testSettersStoreRawText() {
        final User user = new User();
        user.setFullName(TEXT);
        assertEquals(TEXT, user.getFullName().get());

        final Contact contact = new Contact();
        contact.setInfo(TEXT);
        contact.setServiceProvider(TEXT);
        assertEquals(TEXT, contact.getInfo().get());
        assertEquals(TEXT, contact.getServiceProvider().get());
    }
}
