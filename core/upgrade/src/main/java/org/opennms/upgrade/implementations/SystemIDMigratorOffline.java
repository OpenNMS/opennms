/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
