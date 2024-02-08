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

import org.opennms.core.db.DataSourceFactory;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

public class VmwareAssetsMigratorOffline extends AbstractOnmsUpgrade {

    private final static int BATCH_SIZE = 2000;

    public VmwareAssetsMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        return 9;
    }

    @Override
    public String getDescription() {
        return "Moves VMware asset data to the node's metadata.";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            final Statement preExecutionStatement = connection.createStatement();
            try (final ResultSet preExecutionResultSet = preExecutionStatement.executeQuery("SELECT EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = (SELECT oid FROM pg_class WHERE relname = 'assets') AND attname = 'vmwaretopologyinfo')")) {
                preExecutionResultSet.next();
                if (!preExecutionResultSet.getBoolean(1)) {
                    throw new OnmsUpgradeException("The 'vmwaretopologyinfo' columns do not exist anymore");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new OnmsUpgradeException("Error checking for column 'vmwaretopologyinfo'", e);
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
        long nodeCount = 0;

        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            do {
                try (final Statement selectStatement = connection.createStatement(); final ResultSet resultSet = selectStatement.executeQuery("SELECT nodeid, vmwaremanagedobjectid, vmwaremanagedentitytype, vmwaremanagementserver, vmwaretopologyinfo, vmwarestate FROM assets WHERE vmwaremanagementserver IS NOT NULL LIMIT " + BATCH_SIZE)) {

                    if (!resultSet.next()) {
                        break;
                    }

                    try (final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO node_metadata (id, context, key, value) VALUES  (?,?,?,?)");
                         final PreparedStatement nullifyStatement = connection.prepareStatement("UPDATE assets SET vmwaremanagedobjectid=NULL, vmwaremanagedentitytype=NULL, vmwaremanagementserver=NULL, vmwaretopologyinfo=NULL, vmwarestate=NULL WHERE nodeid=?")) {

                        do {
                            final Integer nodeId = resultSet.getInt("nodeid");
                            final String vmwareManagedObjectId = resultSet.getString("vmwaremanagedobjectid");
                            final String vmwareManagedentityType = resultSet.getString("vmwaremanagedentitytype");
                            final String vmwareManagementServer = resultSet.getString("vmwaremanagementserver");
                            final String vmwareTopologyInfo = resultSet.getString("vmwaretopologyinfo");
                            final String vmwareState = resultSet.getString("vmwarestate");

                            insertStatement.setInt(1, nodeId);
                            insertStatement.setString(2, "VMware");
                            insertStatement.setString(3, "managedObjectId");
                            insertStatement.setString(4, vmwareManagedObjectId);
                            insertStatement.execute();

                            insertStatement.setInt(1, nodeId);
                            insertStatement.setString(2, "VMware");
                            insertStatement.setString(3, "managedEntityType");
                            insertStatement.setString(4, vmwareManagedentityType);
                            insertStatement.execute();

                            insertStatement.setInt(1, nodeId);
                            insertStatement.setString(2, "VMware");
                            insertStatement.setString(3, "managementServer");
                            insertStatement.setString(4, vmwareManagementServer);
                            insertStatement.execute();

                            insertStatement.setInt(1, nodeId);
                            insertStatement.setString(2, "VMware");
                            insertStatement.setString(3, "topologyInfo");
                            insertStatement.setString(4, vmwareTopologyInfo);
                            insertStatement.execute();

                            insertStatement.setInt(1, nodeId);
                            insertStatement.setString(2, "VMware");
                            insertStatement.setString(3, "state");
                            insertStatement.setString(4, vmwareState);
                            insertStatement.execute();

                            nullifyStatement.setInt(1, nodeId);
                            nullifyStatement.execute();

                            nodeCount++;
                        } while (resultSet.next());

                        log("Processed %d node entries, %d metadata entries inserted...\n", nodeCount, nodeCount * 5);
                        connection.commit();
                    } catch (SQLException e) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        throw e;
                    }
                }

            } while (true);

            connection.setAutoCommit(true);
            log("Rows migrated. Dropping VMware asset columns...\n");

            final Statement postMigrationStatement = connection.createStatement();
            postMigrationStatement.execute("ALTER TABLE assets DROP COLUMN vmwaremanagedobjectid");
            postMigrationStatement.execute("ALTER TABLE assets DROP COLUMN vmwaremanagedentitytype");
            postMigrationStatement.execute("ALTER TABLE assets DROP COLUMN vmwaremanagementserver");
            postMigrationStatement.execute("ALTER TABLE assets DROP COLUMN vmwaretopologyinfo");
            postMigrationStatement.execute("ALTER TABLE assets DROP COLUMN vmwarestate");

        } catch (Throwable e) {
            throw new OnmsUpgradeException("Can't move asset data to metadata table: " + e.getMessage(), e);
        }
    }
}
