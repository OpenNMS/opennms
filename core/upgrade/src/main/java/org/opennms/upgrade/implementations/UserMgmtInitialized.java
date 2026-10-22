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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The usermgmt_initialized marker: set once the user data of a database is initialized (copied from the files, or
 * seeded with the defaults). DefaultUsersSeedOffline adds the default users only to a database without it.
 */
public final class UserMgmtInitialized {

    private UserMgmtInitialized() {
    }

    public static boolean isSet(final Connection connection) throws SQLException {
        try (final Statement statement = connection.createStatement();
             final ResultSet rs = statement.executeQuery("SELECT EXISTS (SELECT 1 FROM usermgmt_initialized)")) {
            rs.next();
            return rs.getBoolean(1);
        }
    }

    /** Sets the marker once; it runs in the connection's current transaction. */
    public static void set(final Connection connection, final String initializedBy) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO usermgmt_initialized (initialized_by) SELECT ? WHERE NOT EXISTS (SELECT 1 FROM usermgmt_initialized)")) {
            statement.setString(1, initializedBy);
            statement.executeUpdate();
        }
    }
}
