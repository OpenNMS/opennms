/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.upgrade.implementations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

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

                    try (final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO event_parameters (eventid, name, value, type) VALUES  (?,?,?,?)");
                         final PreparedStatement nullifyStatement = connection.prepareStatement("UPDATE events SET eventparms=NULL WHERE eventid=?")) {

                        do {
                            final Integer eventId = resultSet.getInt("eventid");
                            final String eventParms = resultSet.getString("eventparms");
                            final List<Parm> parmList = EventParameterUtils.decode(eventParms);

                            if (parmList != null) {
                                final Map<String, Parm> parmMap = EventParameterUtils.normalize(parmList);

                                for (Map.Entry<String, Parm> entry : parmMap.entrySet()) {
                                    insertStatement.setInt(1, eventId);
                                    insertStatement.setString(2, entry.getValue().getParmName());
                                    insertStatement.setString(3, entry.getValue().getValue().getContent());
                                    insertStatement.setString(4, entry.getValue().getValue().getType());
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
