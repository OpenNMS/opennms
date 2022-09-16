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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.core.io.FileSystemResource;

/**
 * Used to fix the missing resource-types import on datacollection-config.xml
 *
 * <p>See:</p>
 * <ul>
 * <li>NMS-7816</li>
 * </ul>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class DataCollectionConfigMigrator17Offline extends AbstractOnmsUpgrade {

    /** The source file. */
    private File sourceFile;

    /** The backup file. */
    private File backupFile;

    /** The pattern. */
    private Pattern pattern = Pattern.compile("instance '([^']+)' invalid in mibObj definition for OID '.+' in collection '([^']+)'");

    /** The data collection group map. */
    private Map<File,DatacollectionGroup> dataCollectionGroupMap = new HashMap<File,DatacollectionGroup>();

    /**
     * Instantiates a new data collection configuration migrator for OpenNMS 17 offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public DataCollectionConfigMigrator17Offline() throws OnmsUpgradeException {
        super();
        sourceFile = Paths.get(ConfigFileConstants.getHome(), "etc", "datacollection-config.xml").toFile();
        backupFile= new File(sourceFile.getAbsolutePath() + ZIP_EXT);
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Fixes the missing resource types on datacollection-config.xml. See NMS-7816.";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 9;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    @Override
    public void preExecute() throws OnmsUpgradeException {
        log("Backing up %s\n", sourceFile);
        zipFile(sourceFile);
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        log("Patching %s\n", sourceFile);
        while (isConfigValid() == false) {
            log("A missing resource-type has been added, trying again\n");
        }

    }

    /**
     * Checks if is configuration valid.
     *
     * @return true, if is configuration valid
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private boolean isConfigValid() throws OnmsUpgradeException {
        File configDirectory = new File(sourceFile.getParentFile().getAbsolutePath(), "datacollection");
        try {
            DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
            dao.setConfigDirectory(configDirectory.getAbsolutePath());
            dao.setConfigResource(new FileSystemResource(sourceFile));
            dao.setReloadCheckInterval(new Long(0));
            dao.afterPropertiesSet();
        } catch (IllegalArgumentException e) {
            log("Found a problem: %s\n", e.getMessage());
            Matcher m = pattern.matcher(e.getMessage());
            if (m.find()) {
                try {
                    Iterator<Path> paths = Files.list(configDirectory.toPath()).filter(f -> f.getFileName().toString().toLowerCase().endsWith(".xml")).iterator();
                    for (; paths.hasNext(); ) {
                        String group = getGroupForResourceType(paths.next().toFile(), m.group(1));
                        if (group != null) {
                            updateDataCollectionConfig(m.group(2), group);
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    throw new OnmsUpgradeException("Can't get datacollection-group files", ex);
                }
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't process " + sourceFile, e);
        }
        return true;
    }

    /**
     * Update data collection configuration.
     *
     * @param snmpCollection the SNMP collection
     * @param dataCollectionGroup the data collection group
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void updateDataCollectionConfig(String snmpCollection, String dataCollectionGroup) throws OnmsUpgradeException {
        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, sourceFile);
        if (config != null) {
            log("Adding datacollection-group %s to snmp-collection %s", dataCollectionGroup, snmpCollection);
            IncludeCollection ic = new IncludeCollection();
            ic.setDataCollectionGroup(dataCollectionGroup);
            config.getSnmpCollections().stream().filter(s -> s.getName().equals(snmpCollection)).findFirst().get().addIncludeCollection(ic);
            try {
                JaxbUtils.marshal(config, new FileWriter(sourceFile));
            } catch (IOException e) {
                throw new OnmsUpgradeException("Can't update " + sourceFile, e);
            }
        }
    }

    /**
     * Gets the group for resource type.
     *
     * @param configFile the configuration file
     * @param resourceType the resource type
     * @return the group for resource type
     */
    private String getGroupForResourceType(File configFile, final String resourceType) {
        DatacollectionGroup g = getDataCollectionGroup(configFile);
        if (g.getResourceTypes().stream().filter(r -> r.getName().equals(resourceType)).count() > 0) {
            return g.getName();
        }
        return null;
    }

    /**
     * Gets the data collection group.
     *
     * @param configFile the configuration file
     * @return the data collection group
     */
    private DatacollectionGroup getDataCollectionGroup(File configFile) {
        if (dataCollectionGroupMap.get(configFile) == null) {
            log("Parsing datacollection-group %s\n", configFile.getAbsolutePath());
            DatacollectionGroup grp = JaxbUtils.unmarshal(DatacollectionGroup.class, configFile);
            dataCollectionGroupMap.put(configFile, grp);
        }
        return dataCollectionGroupMap.get(configFile);
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() {
        if (backupFile.exists()) {
            log("Removing backup %s\n", backupFile);
            FileUtils.deleteQuietly(backupFile);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        if (!backupFile.exists()) {
            throw new OnmsUpgradeException(String.format("Backup %s not found. Can't rollback.", backupFile));
        }

        log("Unziping backup %s to %s\n", backupFile, sourceFile.getParentFile());
        unzipFile(backupFile, sourceFile.getParentFile());

        log("Rollback succesful. The backup file %s will be kept.\n", backupFile);
    }
}
