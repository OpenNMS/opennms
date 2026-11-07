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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Adds the default users (admin, rtc) and the Admin group to a database without users, with the values of
 * UserMgmtDefaults. Runs after UsersGroupsXmlMigratorOffline, on every install.
 *
 * It seeds a database at most once: not when the table usermgmt_initialized has its row, which the migrator also
 * sets (even when it copies no users). Thus a site without local users (LDAP, RADIUS or SSO only), or one that
 * deleted them, does not get admin/admin back on its next upgrade.
 *
 * It does not seed while etc/users.xml exists: the migrator copies that file into a database without the marker
 * (also a restored or new database on an upgraded host), so here it failed. The upgrade runs the other tasks before it
 * fails, and a seeded admin would make the migrator's next run copy nothing, so the users of the file would be lost.
 *
 * The task is critical, like the migrator: a database it fails to seed has no users, so no one could log in.
 */
public class DefaultUsersSeedOffline extends AbstractOnmsUpgrade {

    public DefaultUsersSeedOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        // after UsersGroupsXmlMigratorOffline (18)
        return 19;
    }

    @Override
    public String getDescription() {
        return "Adds the default admin and rtc users to a database that has no users: NMS-20443";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public boolean runOnlyOnce() {
        return false;
    }

    @Override
    public boolean isCritical() {
        return true;
    }

    /** Until the usermgmt_initialized marker is set, either the migrator or this task has to fill the database. */
    @Override
    public boolean isPending() throws OnmsUpgradeException {
        return !UserMgmtInitialized.isSet();
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            if (count(connection, "SELECT COUNT(*) FROM users") > 0) {
                log("The database already contains users, so no default users are added\n");
                return;
            }
            if (UserMgmtInitialized.isSet(connection)) {
                log("No default users are added: the user data of this database was initialized before (see the table usermgmt_initialized), and its users were removed later\n");
                return;
            }
            final File usersXml = new File(getHomeDirectory(), "etc" + File.separator + "users.xml");
            if (usersXml.exists()) {
                // the migrator runs before this task and sets the marker when it copies users.xml: it failed
                log("No default users are added: %s exists, but its users were not copied. See the messages of UsersGroupsXmlMigratorOffline.\n", usersXml);
                return;
            }
            connection.setAutoCommit(false);
            try {
                final UsersGroupsXmlMigratorOffline.Import defaults = new UsersGroupsXmlMigratorOffline.Import(connection, this::log);
                defaults.users(UserMgmtDefaults.users().getUsers());
                defaults.groups(UserMgmtDefaults.groups().getGroups());
                UserMgmtInitialized.set(connection, getId());
                connection.commit();
            } catch (final SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (final SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            }
            connection.setAutoCommit(true);
            log("Added the default users admin and rtc, and the group Admin\n");
        } catch (final SQLException | RuntimeException e) {
            throw new OnmsUpgradeException("Cannot add the default users: " + e.getMessage(), e);
        }
    }


    private static int count(final Connection connection, final String sql) throws SQLException {
        try (final Statement statement = connection.createStatement(); final ResultSet rs = statement.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
    }
}
