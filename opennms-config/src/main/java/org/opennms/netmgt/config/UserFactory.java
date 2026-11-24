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

/**
 * Holds the UserManager for the static callers: the servlets, JSPs, reporting and other code without Spring
 * injection. The user management context registers the database manager here (UserMgmtFactoryRegistration);
 * tests may register a mock.
 */
public final class UserFactory {
    private static UserManager s_instance;

    private UserFactory() {
    }

    /**
     * Checks that a user manager is registered. Kept for the callers that call it before
     * {@link #getInstance()}; it never reads a file.
     *
     * @throws IllegalStateException if no manager is registered
     */
    public static synchronized void init() {
        if (s_instance == null) {
            throw new IllegalStateException("No user manager is registered: the user management context is not loaded.");
        }
    }

    /** @return the registered manager, or null if none is registered */
    public static synchronized UserManager getInstance() {
        return s_instance;
    }

    /** @param mgr the manager to hand out, or null to remove the registered one */
    public static synchronized void setInstance(final UserManager mgr) {
        s_instance = mgr;
    }
}
