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
package org.opennms.upgrade.implementations;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.groups.Groupinfo;
import org.opennms.netmgt.config.users.Userinfo;

/**
 * The default users (admin, rtc) and group (Admin) of a new installation: the files that OpenNMS used to ship as
 * etc/users.xml and etc/groups.xml, now kept as resources of this module.
 */
final class UserMgmtDefaults {

    private static final String USERS_RESOURCE = "default-users.xml";
    private static final String GROUPS_RESOURCE = "default-groups.xml";

    private UserMgmtDefaults() {
    }

    /** @return a new copy on every call, which the caller may change */
    static Userinfo users() {
        return read(Userinfo.class, USERS_RESOURCE);
    }

    /** @return a new copy on every call, which the caller may change */
    static Groupinfo groups() {
        return read(Groupinfo.class, GROUPS_RESOURCE);
    }

    private static <T> T read(final Class<T> type, final String resource) {
        try (final InputStream in = UserMgmtDefaults.class.getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("The resource " + resource + " is missing");
            }
            return JaxbUtils.unmarshal(type, in);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
