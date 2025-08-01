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
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * Used to modify package names within the data collection configuration files, following a refactor.
 *
 * <p>See:</p>
 * <ul>
 * <li>NMS-7612</li>
 * </ul>
 * 
 * @author Jesse White <jesse@opennms.org>
 */
public class DataCollectionConfigMigratorOffline extends AbstractOnmsUpgrade {

    final File dataCollectionConfigDir;

    final File dataCollectionConfigDirBackupZip;

    final Map<String, String> substitutionMap = new HashMap<String, String>();

    public DataCollectionConfigMigratorOffline() throws OnmsUpgradeException {
        super();

        // Substitutions to make
        substitutionMap.put("org.opennms.netmgt.collectd.PersistAllSelectorStrategy", "org.opennms.netmgt.collection.support.PersistAllSelectorStrategy");
        substitutionMap.put("org.opennms.netmgt.dao.support.IndexStorageStrategy", "org.opennms.netmgt.collection.support.IndexStorageStrategy");

        // Grab the data collection configuration directory
        dataCollectionConfigDir = Paths.get(ConfigFileConstants.getHome(), "etc", "datacollection").toFile();

        // Make sure it's a valid directory before continuing
        if (!dataCollectionConfigDir.isDirectory()) {
            throw new OnmsUpgradeException(String.format("Failed to determine the data collection configuration directory. "
                    + "%s is not a directory", dataCollectionConfigDir.getAbsolutePath()));
        }

        dataCollectionConfigDirBackupZip = new File(dataCollectionConfigDir.getAbsolutePath() + ZIP_EXT);
    }

    @Override
    public String getDescription() {
        return "Modifies the packages names used in etc/datacollection/*.xml. See NMS-7612.";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public int getOrder() {
        return 7;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        log("Backing up %s\n", dataCollectionConfigDir);
        zipDir(dataCollectionConfigDirBackupZip, dataCollectionConfigDir);
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        log("Patching files...\n");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataCollectionConfigDir.toPath(), "*.xml")) {
            for (Path entry: stream) {
                migrateDataCollectionConfig(entry.toFile());
            }
        } catch (IOException e) {
            throw new OnmsUpgradeException("Failed to list files in folder.", e);
        }
    }

    private void migrateDataCollectionConfig(File cfgFile) throws OnmsUpgradeException {
        try {
            log("  %s\n", cfgFile.getAbsolutePath());

            // Read the file into memory
            String cfgFileContents = FileUtils.readFileToString(cfgFile);

            // Perform the required substitutions
            for (final Map.Entry<String, String> substitution : substitutionMap.entrySet()) {
                cfgFileContents = cfgFileContents.replace(substitution.getKey(), substitution.getValue());
            }

            // Write the (modified) file back to disk
            FileUtils.write(cfgFile, cfgFileContents);
        } catch (IOException e) {
            throw new OnmsUpgradeException(String.format("Failed to update %s", cfgFile), e);
        }
    }

    @Override
    public void postExecute() {
        if (dataCollectionConfigDirBackupZip.exists()) {
            log("Removing backup %s\n", dataCollectionConfigDirBackupZip);
            FileUtils.deleteQuietly(dataCollectionConfigDirBackupZip);
        }
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        if (!dataCollectionConfigDirBackupZip.exists()) {
            throw new OnmsUpgradeException(String.format("Backup %s not found. Can't rollback.", dataCollectionConfigDirBackupZip));
        }

        log("Unziping backup %s to %s\n", dataCollectionConfigDirBackupZip, dataCollectionConfigDir);
        unzipFile(dataCollectionConfigDirBackupZip, dataCollectionConfigDir);

        log("Rollback succesful. The backup file %s will be kept.\n", dataCollectionConfigDirBackupZip);
    }
}
