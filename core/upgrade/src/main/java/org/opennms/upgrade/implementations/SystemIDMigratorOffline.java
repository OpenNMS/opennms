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
import java.util.UUID;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

public class SystemIDMigratorOffline extends AbstractOnmsUpgrade {

    public SystemIDMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        return 15;
    }

    @Override
    public String getDescription() {
        return "Updates OpenNMS system ID to a random UUID.";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        try (final Connection connection = DataSourceFactory.getInstance().getConnection();
             final Statement preExecutionStatement = connection.createStatement();
             final ResultSet resultSet = preExecutionStatement.executeQuery("SELECT id FROM monitoringsystems WHERE location='Default' AND type='OpenNMS'"))
        {
            if (resultSet.next()) {
                String systemID = resultSet.getString("id");
                if (systemID == null || !systemID.equals("00000000-0000-0000-0000-000000000000")) {
                    throw new OnmsUpgradeException("id is not set to the default, not updating");
                }
            }
            else {
                throw new OnmsUpgradeException("No entries found in monitoringsystems table");
            }
        } catch (SQLException e) {
            throw new OnmsUpgradeException("Error opening database connection", e);
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        try (final Connection c = DataSourceFactory.getInstance().getConnection();
             final Statement st = c.createStatement())
        {
            String newUUID = UUID.randomUUID().toString();
            try {
                c.setAutoCommit(false);
                // For existing databases, we need to temporarily alter a foreign key
                // constraint to cascade updates into the alarm table
                st.addBatch("ALTER TABLE alarms DROP CONSTRAINT fk_alarms_systemid");
                st.addBatch("ALTER TABLE alarms ADD CONSTRAINT fk_alarms_systemid FOREIGN KEY (systemId) REFERENCES monitoringsystems (id) ON UPDATE CASCADE ON DELETE CASCADE");
                st.addBatch("UPDATE monitoringsystems SET id='" + newUUID + "' WHERE id='00000000-0000-0000-0000-000000000000' AND location='Default' AND type='OpenNMS'");
                st.addBatch("ALTER TABLE alarms DROP CONSTRAINT fk_alarms_systemid");
                st.addBatch("ALTER TABLE alarms ADD CONSTRAINT fk_alarms_systemid FOREIGN KEY (systemId) REFERENCES monitoringsystems (id) ON DELETE CASCADE");
                st.addBatch("UPDATE events SET systemid='" + newUUID + "' WHERE systemid = '00000000-0000-0000-0000-000000000000'");
                st.executeBatch();
                c.commit();
            }
            catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
        catch (SQLException e) {
            throw new OnmsUpgradeException("unable to update systemId", e);
        }
    }
}
