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

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class EventConfGlobalSecurityMigratorOffline extends AbstractOnmsUpgrade {

    /**
     * Instantiates a new abstract OpenNMS upgrade.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public EventConfGlobalSecurityMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public boolean runOnlyOnce() {
        return true;
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        final List<String> doNotOverrides = new ArrayList<>();

        try {
            final Events events = JaxbUtils.unmarshal(Events.class, new FileSystemResource(ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME)));
            if (events != null && events.getGlobal() != null && events.getGlobal().getSecurity() != null) {
                doNotOverrides.addAll(events.getGlobal().getSecurity().getDoNotOverrides());
            }
        } catch (IOException e) {
            throw new OnmsUpgradeException("Can't load Events Configuration file", e);
        }

        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            try (final PreparedStatement insertStatement = connection.prepareStatement("INSERT INTO eventconf_global_security (do_not_override) VALUES  (?)")) {
                for (final String doNotOverride : doNotOverrides) {
                    insertStatement.setString(1, doNotOverride);
                    insertStatement.execute();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                connection.setAutoCommit(true);
                throw e;
            }
            connection.setAutoCommit(true);
            log("Evenconf doNotOverride entries migrated.\n");
        } catch (Throwable e) {
            throw new OnmsUpgradeException("Can't move event parameters to table: " + e.getMessage(), e);
        }
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        try {
            if (!ConfigFileConstants.getFile(ConfigFileConstants.EVENT_CONF_FILE_NAME).exists()) {
                throw new OnmsUpgradeException("Configuration file 'eventconf.xml' not found");
            }
        } catch (IOException e) {
            throw new OnmsUpgradeException("Error checking whether the file ‘eventconf.xml’ exists", e);
        }

        try (final Connection connection = DataSourceFactory.getInstance().getConnection()) {
            final Statement preExecutionStatement = connection.createStatement();
            try (final ResultSet preExecutionResultSet = preExecutionStatement.executeQuery("SELECT EXISTS (SELECT oid FROM pg_class WHERE relname = 'eventconf_global_security')")) {
                preExecutionResultSet.next();
                if (!preExecutionResultSet.getBoolean(1)) {
                    throw new OnmsUpgradeException("The table 'eventconf_global_security' does not exist");
                }
            } catch (SQLException e) {
                throw new OnmsUpgradeException("Error checking for table 'eventconf_global_security'", e);
            }
        } catch (SQLException e) {
            throw new OnmsUpgradeException("Error opening database connection", e);
        }

    }

    @Override
    public void rollback() throws OnmsUpgradeException {
    }

    private void createEtcArchive() throws OnmsUpgradeException {
        final File etcArchiveFolder = Paths.get(
                ConfigFileConstants.getHome() +
                        File.separator +
                        "etc_archive").toFile();

        if (!etcArchiveFolder.exists()) {
            if (!etcArchiveFolder.mkdir()) {
                throw new OnmsUpgradeException("Error creating folder 'etc_archive'.");
            }
        }
    }

    private void moveEventConf() throws OnmsUpgradeException {
        final Path srcEventConf = Paths.get(
                ConfigFileConstants.getHome() +
                File.separator +
                "etc" +
                File.separator +
                "eventconf.xml");

        final Path dstEventConf = Paths.get(
                ConfigFileConstants.getHome() +
                File.separator +
                "etc_archive" +
                File.separator +
                "eventconf.xml");

        if (srcEventConf.toFile().exists()) {
            try {
                Files.move(srcEventConf, dstEventConf, ATOMIC_MOVE);
            } catch (IOException e) {
                throw new OnmsUpgradeException("Error moving file 'eventconf.xml' to 'etc_archive'.", e);
            }
        }
    }

    private void moveEvents() throws OnmsUpgradeException {
        final Path srcEvents = Paths.get(ConfigFileConstants.getHome() + File.separator + "etc" + File.separator + "events");
        final Path dstEvents = Paths.get(ConfigFileConstants.getHome() + File.separator + "etc_archive" + File.separator + "events");
        if (srcEvents.toFile().exists()) {
            try {
                Files.move(srcEvents, dstEvents, ATOMIC_MOVE);

                log("*********************************************************************************\n");
                log("* Please note that your 'events' folder has been moved to the 'etc_archive'     *\n");
                log("* directory. Your events have not been imported into the database. Please refer *\n");
                log("* to the OpenNMS documentation for information on how to migrate custom events  *\n");
                log("* to the database.                                                              *\n");
                log("*********************************************************************************\n");

            } catch (IOException e) {
                throw new OnmsUpgradeException("Error moving folder 'events' to 'etc_archive'.", e);
            }
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
        createEtcArchive();
        moveEventConf();
        moveEvents();
    }

    @Override
    public String getDescription() {
        return "Moves doNotOverride entries from 'eventconf.xml' to the database table 'eventconf_global_security'.";
    }

    @Override
    public int getOrder() {
        return 17;
    }
}
