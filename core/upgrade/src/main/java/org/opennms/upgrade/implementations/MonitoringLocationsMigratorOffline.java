/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;

import org.apache.commons.io.FileUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.opennms.upgrade.implementations.monitoringLocations16.LocationDef;
import org.opennms.upgrade.implementations.monitoringLocations16.MonitoringLocationsConfiguration;
import org.opennms.upgrade.implementations.monitoringLocations16.Tag;

/**
 * Migrate the content from monitoring-locations.xml into the monitoringlocations tables
 * in the database.
 * 
 * @author Seth 
 */
public class MonitoringLocationsMigratorOffline extends AbstractOnmsUpgrade {

    private MonitoringLocationsConfiguration monitoringLocationsConfig;

    private File configFile;

    /**
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public MonitoringLocationsMigratorOffline() throws OnmsUpgradeException {
        super();
        try {
            // Parse the existing config file
            configFile = ConfigFileConstants.getConfigFileByName("monitoring-locations.xml");
            if (configFile.exists() && configFile.isFile()) {
                monitoringLocationsConfig = JaxbUtils.unmarshal(MonitoringLocationsConfiguration.class, configFile);
            } else {
                monitoringLocationsConfig = null;
            }
        } catch (FileNotFoundException e) {
            log("No monitoring-locations.xml file found, skipping migration to database\n");
            monitoringLocationsConfig = null;
        } catch (IOException e) {
            throw new OnmsUpgradeException("Unexpected exception while reading monitoring-locations.xml", e);
        }
    }

    @Override
    public int getOrder() {
        return 8;
    }

    @Override
    public String getDescription() {
        return "Moves monitoring locations from monitoring-locations.xml into the monitoringlocations tables in the database";
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        if (monitoringLocationsConfig == null) return;

        try {
            log("Backing up %s\n", configFile);
            zipFile(configFile);
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't backup " + configFile + " because " + e.getMessage());
        }
    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
        if (monitoringLocationsConfig == null) return;

        // Delete the original config file so that it doesn't get remigrated later
        if (configFile.exists()) {
            log("Removing original config file %s\n", configFile);
            FileUtils.deleteQuietly(configFile);
        }
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        if (monitoringLocationsConfig == null) return;

        log("Restoring backup %s\n", configFile);
        File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        FileUtils.deleteQuietly(configFile);
        unzipFile(zip, zip.getParentFile());
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        if (monitoringLocationsConfig == null) return;

        log("Moving monitoring locations into the database...\n");
        long count = 0;
        try {
            Connection connection = null;
            final DBUtils dbUtils = new DBUtils(getClass());
            try {
                connection = DataSourceFactory.getInstance().getConnection();
                dbUtils.watch(connection);

                PreparedStatement insertLocation = connection.prepareStatement("INSERT INTO monitoringlocations (id, monitoringarea, geolocation, latitude, longitude, priority) VALUES (?,?,?,?,?,?)");
                PreparedStatement insertPollingPackage = connection.prepareStatement("INSERT INTO monitoringlocationspollingpackages (monitoringlocationid, packagename) VALUES (?,?)");
                PreparedStatement insertCollectionPackage = connection.prepareStatement("INSERT INTO monitoringlocationscollectionpackages (monitoringlocationid, packagename) VALUES (?,?)");
                PreparedStatement insertTag = connection.prepareStatement("INSERT INTO monitoringlocationstags (monitoringlocationid, tag) VALUES (?,?)");

                dbUtils.watch(insertLocation);
                dbUtils.watch(insertPollingPackage);
                dbUtils.watch(insertCollectionPackage);
                dbUtils.watch(insertTag);

                for (LocationDef location : monitoringLocationsConfig.getLocations()) {
                    insertLocation.setString(1, location.getLocationName()); // id
                    insertLocation.setString(2, location.getMonitoringArea()); // monitoringarea
                    if (location.getGeolocation() != null && !"".equals(location.getGeolocation().trim())) {
                        insertLocation.setString(3, location.getGeolocation()); // geolocation
                    } else {
                        insertLocation.setNull(3, Types.VARCHAR);
                    }

                    if (location.getCoordinates() != null && !"".equals(location.getCoordinates())) {
                        String[] latLong = location.getCoordinates().split(",");
                        if (latLong.length == 2) {
                            insertLocation.setDouble(4, Double.valueOf(latLong[0])); // latitude
                            insertLocation.setDouble(5, Double.valueOf(latLong[1])); // longitude
                        } else {
                            insertLocation.setNull(4, Types.DOUBLE);
                            insertLocation.setNull(5, Types.DOUBLE);
                        }
                    } else {
                        insertLocation.setNull(4, Types.DOUBLE);
                        insertLocation.setNull(5, Types.DOUBLE);
                    }
                    if (location.getPriority() == null) {
                        insertLocation.setNull(6, Types.INTEGER); // priority
                    } else {
                        insertLocation.setLong(6, location.getPriority()); // priority
                    }
                    insertLocation.execute();
                    count++;

                    if (location.getPollingPackageName() != null && !"".equals(location.getPollingPackageName())) {
                        insertPollingPackage.setString(1, location.getLocationName()); // monitoringlocationid
                        insertPollingPackage.setString(2, location.getPollingPackageName()); // packagename
                        insertPollingPackage.execute();
                    }

                    if (location.getCollectionPackageName() != null && !"".equals(location.getCollectionPackageName())) {
                        insertCollectionPackage.setString(1, location.getLocationName()); // monitoringlocationid
                        insertCollectionPackage.setString(2, location.getCollectionPackageName()); // packagename
                        insertCollectionPackage.execute();
                    }

                    for (Tag tag : location.getTags()) {
                        if (tag.getName() != null && !"".equals(tag.getName().trim())) {
                            insertTag.setString(1, location.getLocationName()); // monitoringlocationid
                            insertTag.setString(2, tag.getName()); // tag
                            insertTag.execute();
                        }
                    }
                }
            } finally {
                dbUtils.cleanUp();
            }
        } catch (Throwable e) {
            throw new OnmsUpgradeException("Can't fix services configuration because " + e.getMessage(), e);
        }
        log("Moved %d monitoring locations into the database\n", count);
    }
}
