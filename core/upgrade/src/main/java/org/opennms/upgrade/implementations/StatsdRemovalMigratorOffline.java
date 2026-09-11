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
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Removes the Statsd daemon, retired in NMS-20313, from an upgraded installation:
 * its service-configuration.xml entry goes away and statsd-configuration.xml
 * moves to etc_archive.
 */
public class StatsdRemovalMigratorOffline extends AbstractOnmsUpgrade {

    static final String STATSD_SERVICE = "OpenNMS:Name=Statsd";
    static final List<String> RETIRED_FILES = List.of("statsd-configuration.xml");

    private final File configFile;

    public StatsdRemovalMigratorOffline() throws OnmsUpgradeException {
        super();
        try {
            configFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        } catch (final IOException e) {
            throw new OnmsUpgradeException("Can't find Services Configuration file", e);
        }
    }

    @Override
    public int getOrder() {
        return 11;
    }

    @Override
    public String getDescription() {
        return "Removes the retired Statsd daemon from service-configuration.xml and archives its configuration file: NMS-20313";
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
    public void preExecute() throws OnmsUpgradeException {
        try {
            log("Backing up %s\n", configFile);
            zipFile(configFile);
        } catch (final Exception e) {
            throw new OnmsUpgradeException("Can't backup service-configuration.xml because " + e.getMessage(), e);
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
        final File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        if (zip.exists()) {
            log("Removing backup %s\n", zip);
            FileUtils.deleteQuietly(zip);
        }
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        log("Restoring backup %s\n", configFile);
        final File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        FileUtils.deleteQuietly(configFile);
        unzipFile(zip, zip.getParentFile());
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        try {
            final ServiceConfiguration currentCfg = JaxbUtils.unmarshal(ServiceConfiguration.class, configFile);
            if (currentCfg.getServices().removeIf(s -> STATSD_SERVICE.equals(s.getName()))) {
                log("Removing retired service: Statsd\n");
                final StringWriter sw = new StringWriter();
                sw.write("<?xml version=\"1.0\"?>\n");
                JaxbUtils.marshal(currentCfg, sw);
                try (FileWriter fw = new FileWriter(configFile)) {
                    fw.write(sw.toString());
                }
            }
        } catch (final Exception e) {
            throw new OnmsUpgradeException("Can't remove Statsd from the services configuration because " + e.getMessage(), e);
        }
        archiveRetiredFiles();
    }

    private void archiveRetiredFiles() throws OnmsUpgradeException {
        final Path etc = Paths.get(ConfigFileConstants.getHome(), "etc");
        final Path archive = Paths.get(ConfigFileConstants.getHome(), "etc_archive");
        for (final String name : RETIRED_FILES) {
            final Path source = etc.resolve(name);
            if (!Files.exists(source)) {
                continue;
            }
            try {
                Files.createDirectories(archive);
                Files.move(source, archive.resolve(name), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                log("Moved %s to etc_archive\n", name);
            } catch (final IOException e) {
                throw new OnmsUpgradeException("Error moving '" + name + "' to 'etc_archive'.", e);
            }
        }
    }
}
