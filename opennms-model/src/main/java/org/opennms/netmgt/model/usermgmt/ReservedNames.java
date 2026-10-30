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

import java.util.List;

/**
 * The names no user or group may have. The v2 API addresses a user or group by its name as a path segment
 * ({@code /users/{userId}}, {@code /groups/{name}}): a client resolves {@code .} and {@code ..} away before
 * sending, and the other names are endpoints of their own. The v2 API refuses them as new names, and the
 * migration of users.xml and groups.xml stops on them. The match is exact, as the path match is.
 */
public final class ReservedNames {

    public static final List<String> NAMES = List.of(".", "..", "item", "available-roles", "available-categories");

    private ReservedNames() {
    }

    public static boolean isReserved(final String name) {
        return NAMES.contains(name);
    }
}
