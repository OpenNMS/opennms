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

import java.util.Properties;

/**
 * Where users, groups and roles are stored while both implementations exist: in the database ("db", the default)
 * or in users.xml and groups.xml ("xml"). Removed with the XML implementation.
 */
public final class UserMgmtStore {
    public static final String PROPERTY = "org.opennms.usermgmt.store";
    public static final String DB = "db";
    public static final String XML = "xml";

    private UserMgmtStore() {
    }

    public static boolean isDatabase() {
        return isDatabase(System.getProperties());
    }

    public static boolean isDatabase(final Properties properties) {
        final String value = properties.getProperty(PROPERTY, DB).trim();
        if (DB.equals(value)) {
            return true;
        }
        if (XML.equals(value)) {
            return false;
        }
        throw new IllegalStateException(PROPERTY + " must be \"" + DB + "\" or \"" + XML + "\", not \"" + value + "\"");
    }
}
