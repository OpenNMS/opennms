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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.password.DatabasePasswords;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Applies install -R when the admin user did not exist yet. The installer passes the password in the system property
 * DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY, because Bootstrap starts the upgrade in the same JVM and returns, so
 * the installer cannot run code after the upgrade tasks. Runs after DefaultUsersSeedOffline (19), which creates admin.
 * The password is never logged.
 *
 * The task is critical only while a password is given: install -R must not report success when the password could not
 * be set. A missing admin user is no failure; the task only logs it.
 */
public class AdminPasswordResetOffline extends AbstractOnmsUpgrade {

    /** execute() clears the property, but the upgrade asks isCritical() after execute(). */
    private boolean m_passwordGiven;

    public AdminPasswordResetOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        // after DefaultUsersSeedOffline (19)
        return 20;
    }

    @Override
    public String getDescription() {
        return "Sets the password of the admin user given to install -R, after the admin user was created: NMS-20519";
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
        return m_passwordGiven || passwordGiven();
    }

    /**
     * Pending while a password is given: skipping the task while OpenNMS runs would drop it. Not pending when the user
     * data is initialized and there is no admin user (it was deleted or renamed): no task will create it, so the task
     * has nothing to do.
     */
    @Override
    public boolean isPending() throws OnmsUpgradeException {
        if (!passwordGiven()) {
            return false;
        }
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            return !UserMgmtInitialized.isSet(connection) || adminExists(connection);
        } catch (final SQLException e) {
            throw new OnmsUpgradeException("Can't read the users: " + e.getMessage(), e);
        }
    }

    private static boolean adminExists(final Connection connection) throws SQLException {
        try (final Statement statement = connection.createStatement();
             final ResultSet rs = statement.executeQuery("SELECT EXISTS (SELECT 1 FROM users WHERE username = 'admin')")) {
            rs.next();
            return rs.getBoolean(1);
        }
    }

    private static boolean passwordGiven() {
        final String password = System.getProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY);
        return password != null && !password.isBlank();
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        if (!passwordGiven()) {
            return;
        }
        m_passwordGiven = true;
        final String password = System.getProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY);
        try {
            DatabasePasswords.setPassword(DataSourceFactory.getInstance(), "admin", password);
            log("Password for the web application admin user was set.\n");
        } catch (final IllegalArgumentException e) {
            log("The password was NOT set: the admin user does not exist.\n");
        } catch (final Exception e) {
            throw new OnmsUpgradeException("Cannot set the password of the admin user: " + e.getMessage(), e);
        } finally {
            System.clearProperty(DatabasePasswords.RESET_ADMIN_PASSWORD_PROPERTY);
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
    }
}
