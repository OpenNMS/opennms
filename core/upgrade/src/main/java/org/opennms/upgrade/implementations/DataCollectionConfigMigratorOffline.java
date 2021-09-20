/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
