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
import java.util.List;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.events.api.EventParameterUtils;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

public class EventParameterMigratorOffline extends AbstractOnmsUpgrade {

    private final static int BATCH_SIZE = 2000;

    public EventParameterMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        return 8;
    }

    @Override
    public String getDescription() {
        return "Moves event parameters from the 'eventparms' column to the 'event_parameters' table.";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            final Statement preExecutionStatement = connection.createStatement();
            try (final ResultSet preExecutionResultSet = preExecutionStatement.executeQuery("SELECT EXISTS (SELECT 1 FROM pg_attribute WHERE attrelid = (SELECT oid FROM pg_class WHERE relname = 'events') AND attname = 'eventparms')")) {
                preExecutionResultSet.next();
                if (!preExecutionResultSet.getBoolean(1)) {
                    throw new OnmsUpgradeException("The 'eventParms' column no longer exists");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new OnmsUpgradeException("Error checking for column 'eventParms'", e);
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
        long eventCount = 0, parameterCount = 0;

        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            do {
                try (final Statement selectStatement = connection.createStatement(); final ResultSet resultSet = selectStatement.executeQuery("SELECT eventid, eventparms FROM events WHERE eventparms IS NOT NULL LIMIT " + BATCH_SIZE)) {

                    if (!resultSet.next()) {
                        break;
                    }

                    try (final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO event_parameters (eventid, name, value, type, position) VALUES  (?,?,?,?,?)");
                         final PreparedStatement nullifyStatement = connection.prepareStatement("UPDATE events SET eventparms=NULL WHERE eventid=?")) {

                        do {
                            final Integer eventId = resultSet.getInt("eventid");
                            final String eventParms = resultSet.getString("eventparms");
                            final List<Parm> parmList = EventParameterUtils.decode(eventParms);

                            if (parmList != null) {
                                final List<Parm> normalizedParms = EventParameterUtils.normalizePreserveOrder(parmList);

                                for (int i=0; i < normalizedParms.size(); i++) {
                                    Parm parm = normalizedParms.get(i);
                                    insertStatement.setInt(1, eventId);
                                    insertStatement.setString(2, parm.getParmName());
                                    insertStatement.setString(3, parm.getValue().getContent());
                                    insertStatement.setString(4, parm.getValue().getType());
                                    insertStatement.setInt(5, i);
                                    insertStatement.execute();
                                    parameterCount++;
                                }
                            }
                            nullifyStatement.setInt(1, eventId);
                            nullifyStatement.execute();

                            eventCount++;
                        } while (resultSet.next());

                        log("Processed %d eventparms entries, %d event parameters inserted...\n", eventCount, parameterCount);
                        connection.commit();
                    } catch (SQLException e) {
                        connection.rollback();
                        connection.setAutoCommit(true);
                        throw e;
                    }
                }

            } while (true);

            connection.setAutoCommit(true);
            log("Rows migrated. Dropping column 'eventparms'...\n");

            final Statement postMigrationStatement = connection.createStatement();
            postMigrationStatement.execute("ALTER TABLE events DROP COLUMN eventparms");
        } catch (Throwable e) {
            throw new OnmsUpgradeException("Can't move event parameters to table: " + e.getMessage(), e);
        }
    }
}
