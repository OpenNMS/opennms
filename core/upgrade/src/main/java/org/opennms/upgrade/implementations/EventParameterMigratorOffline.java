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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.events.api.EventParameterUtils;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

public class EventParameterMigratorOffline extends AbstractOnmsUpgrade {

    public EventParameterMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public int getOrder() {
        return 8;
    }

    @Override
    public String getDescription() {
        return "Moves event parameters from eventparms column to table";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        ;
        // check for eventparms column
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

            final Statement preMigrationStatement = connection.createStatement();
            final ResultSet preMigrationResultSet = preMigrationStatement.executeQuery("SELECT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='public' AND table_name='events' AND column_name='eventparms')");

            if (preMigrationResultSet.next() && Boolean.TRUE.equals(preMigrationResultSet.getBoolean(1))) {
                connection.setAutoCommit(false);

                try (final Statement selectStatement = connection.createStatement();
                     final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO event_parameters (eventid, name, value, type) VALUES  (?,?,?,?)");
                     final PreparedStatement nullifyStatement = connection.prepareStatement("UPDATE events SET eventparms=NULL WHERE eventid=?")) {
                    try (final ResultSet resultSet = selectStatement.executeQuery("SELECT eventid, eventparms FROM events WHERE eventparms IS NOT NULL")) {
                        while (resultSet.next()) {
                            final Integer eventId = resultSet.getInt("eventid");
                            final String eventParms = resultSet.getString("eventparms");
                            final List<Parm> parmList = EventParameterUtils.decode(eventParms);

                            if (parmList != null) {
                                Map<String, Parm> parmMap = parmList.stream().collect(
                                        Collectors.toMap(Parm::getParmName, Function.identity(), (p1, p2) -> p1));

                                for (Map.Entry<String, Parm> entry : parmMap.entrySet()) {
                                    insertStatement.setInt(1, eventId);
                                    insertStatement.setString(2, entry.getValue().getParmName());
                                    insertStatement.setString(3, entry.getValue().getValue().getContent());
                                    insertStatement.setString(4, entry.getValue().getValue().getType());
                                    insertStatement.execute();

                                    nullifyStatement.setInt(1, eventId);
                                    nullifyStatement.execute();
                                    parameterCount++;
                                }
                            }

                            eventCount++;

                            if (eventCount % 10000 == 0) {
                                log("Processed %d eventparms entries, %d event parameters inserted...\n", eventCount, parameterCount);
                                connection.commit();
                            }
                        }

                        log("Processed %d eventparms entries, %d event parameters inserted...\n", eventCount, parameterCount);
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    throw e;
                }

                connection.commit();
                connection.setAutoCommit(true);

                log("Rows migrated. Dropping column 'eventparms'...\n");

                final Statement postMigrationStatement = connection.createStatement();
                postMigrationStatement.execute("ALTER TABLE events DROP COLUMN eventparms");
            } else {
                log("Column 'eventparms' already dropped. Nothing left to do!\n");
            }
        } catch (Throwable e) {
            throw new OnmsUpgradeException("Can't move event parameters to table: " + e.getMessage(), e);
        }
/*
        try {
            Connection connection = null;
            final DBUtils dbUtils = new DBUtils(getClass());

            try {
                connection = DataSourceFactory.getInstance().getConnection();
                connection.setAutoCommit(false);
                dbUtils.watch(connection);

                Statement selectStatement = connection.createStatement();
                dbUtils.watch(selectStatement);

                ResultSet resultSet = selectStatement.executeQuery("SELECT eventid, eventparms FROM events");
                dbUtils.watch(resultSet);

                PreparedStatement insertParameter = connection.prepareStatement("INSERT INTO event_parameters (eventid, name, value, type) VALUES  (?,?,?,?)");
                dbUtils.watch(insertParameter);

                long eventCount = 0, parameterCount = 0;

                while (resultSet.next()) {

                    Integer eventId = resultSet.getInt("eventid");
                    String eventParms = resultSet.getString("eventparms");

                    List<Parm> parmList = EventParameterUtils.decode(eventParms);

                    if (parmList != null) {
                        for (Parm parm : parmList) {
                            insertParameter.setInt(1, eventId);
                            insertParameter.setString(2, parm.getParmName());
                            insertParameter.setString(3, parm.getValue().getContent());
                            insertParameter.setString(4, parm.getValue().getType());
                            insertParameter.execute();
                            parameterCount++;
                        }
                    }

                    eventCount++;

                    if (eventCount % 10000 == 0) {
                        log("Processed %d event(s) and inserted %d event parameter(s)...\n", eventCount, parameterCount);
                    }
                }

                log("Processed %d event(s) and inserted %d event parameter(s)...\n", eventCount, parameterCount);

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();

                throw new OnmsUpgradeException("Can't move event parameters to table " + e.getMessage(), e);
            } finally {
                dbUtils.cleanUp();
            }
        } catch (Throwable e) {
            throw new OnmsUpgradeException("Can't move event parameters to table " + e.getMessage(), e);
        }
        */
    }
}
