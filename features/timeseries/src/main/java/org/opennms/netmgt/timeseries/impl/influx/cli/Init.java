/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.impl.influx.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.opennms.core.db.DataSourceConfigurationFactory;
import org.opennms.core.db.install.SimpleDataSource;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class Init implements Command {

    private static final String OPENNMS_DATA_SOURCE_NAME = "opennms";

    @Option(name = "-h", aliases = {"--help"}, help = true)
    boolean showHelp = false;

    @Option(name = "-p", aliases = {"--print-only"}, usage = "Prints the statements instead of executing them.")
    boolean printOnly = false;

    @Override
    public void execute() throws Exception {
        if (showHelp) {
            System.out.println("Usage: $OPENNMS_HOME/bin/influxdb init");
            CmdLineParser parser = new CmdLineParser(new Init());
            parser.printUsage(System.out);
            return;
        }

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);

        InputStream is = new FileInputStream(cfgFile);
        final JdbcDataSource dsConfig = new DataSourceConfigurationFactory(is).getJdbcDataSource(OPENNMS_DATA_SOURCE_NAME);
        final DataSource ds = new SimpleDataSource(dsConfig);
        is.close();

        System.out.println("Checking preconditions");
        try (Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement()) {
            ResultSet result = stmt.executeQuery("select count(*) from pg_extension where extname = 'timescaledb';");
            result.next();
            if (result.getInt(1) < 1) {
                System.out.println("It looks like timescale plugin is not installed. Please install: https://docs.timescale.com/latest/getting-started/installation");
            }
            System.out.println("Installing Timescale tables");
            executeQuery(stmt,"CREATE TABLE timescale_time_series(key TEXT NOT NULL, time TIMESTAMPTZ NOT NULL, value DOUBLE PRECISION NULL)");
            executeQuery(stmt, "SELECT create_hypertable('timescale_time_series', 'time');");
            // double check:
            stmt.execute("select * from timescale_time_series;"); // will throw exception if table doesn't exist

            executeQuery(stmt,"CREATE TABLE timescale_metric(key TEXT NOT NULL)");
            executeQuery(stmt,"CREATE TABLE timescale_tag(fk_timescale_metric TEXT NOT NULL, key TEXT, value TEXT NOT NULL, type TEXT NOT NULL, UNIQUE (fk_timescale_metric, key, value, type))");
            // TODO: Patrick: add constraint and indexes

            // TODO: Patrick: the creation of the timeseries_meta table should be moved into an update script
            // executeQuery(stmt, "CREATE TABLE timeseries_meta(group VARCHAR NOT NULL, identifier VARCHAR NOT NULL, name VARCHAR NOT NULL, value VARCHAR NOT NULL, type VARCHAR NOT NULL)"); // varchar
            // executeQuery(stmt, "CREATE TABLE timeseries_meta(resourceid VARCHAR NOT NULL, name VARCHAR NOT NULL, value VARCHAR NOT NULL, UNIQUE (resourceid, name))");

            System.out.println("Done. Enjoy!");
        }
    }

    private void executeQuery(Statement stmt, final String sql) throws SQLException {
        if(printOnly) {
            System.out.println(sql);
        } else {
            stmt.execute(sql);
        }
    }
}
