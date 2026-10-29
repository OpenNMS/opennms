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
package org.opennms.core.password;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

/** Sets a web user's password in the users table (org.opennms.usermgmt.store=db). */
public final class DatabasePasswords {

    private DatabasePasswords() {
    }

    /**
     * Takes the usermgmt_version locks ({@link #lockAll(Connection)}) before the update, so that it cannot deadlock
     * against the managers.
     *
     * @throws IllegalArgumentException if no user has exactly this name
     */
    public static void setPassword(final DataSource dataSource, final String username, final String plainPassword) throws SQLException {
        final String hash = new StrongPasswordEncryptor().encryptPassword(plainPassword);
        try (final Connection connection = dataSource.getConnection()) {
            final boolean autoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                lockAll(connection);
                try (final PreparedStatement statement = connection.prepareStatement(
                        "UPDATE users SET password_hash = ?, password_salted = true, last_modified = NOW() WHERE username = ?")) {
                    statement.setString(1, hash);
                    statement.setString(2, username);
                    if (statement.executeUpdate() == 0) {
                        throw new IllegalArgumentException("User \"" + username + "\" does not exist.");
                    }
                }
                connection.commit();
            } catch (final SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Locks the usermgmt_version rows of users, then groups, the order the managers take them in, so that a writer
     * outside OpenNMS cannot deadlock against them. The caller has turned off auto-commit; the locks are held until
     * its transaction ends.
     */
    public static void lockAll(final Connection connection) throws SQLException {
        lock(connection, "users");
        lock(connection, "groups");
    }

    private static void lock(final Connection connection, final String name) throws SQLException {
        try (final PreparedStatement statement = connection.prepareStatement("SELECT version FROM usermgmt_version WHERE name = ? FOR UPDATE")) {
            statement.setString(1, name);
            statement.executeQuery().close();
        }
    }
}
