/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.jrobin.core.RrdDb;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

/**
 * The Class RRD/JRB Migrator for F5 LTM pool & virtual-server names.
 *
 * <p>The fix for the following issues is going to break existing collected data specially for JRBs.
 * For this reason, these files must be updated too.</p>
 *
 * <p>Issues fixed:</p>
 * <ul>
 * <li>NMS-8587</li>
 * </ul>
 *
 * @author <a href="mailto:roskens@opennms.org">Ronald Roskens</a>
 */
public class F5LTMRrdMigratorOffline extends AbstractOnmsUpgrade {

    private static final Logger LOG = LoggerFactory.getLogger(F5LTMRrdMigratorOffline.class);

    private final DefaultDataCollectionConfigDao dataCollectionConfigDao = new DefaultDataCollectionConfigDao();

    /** The JMX resource directories. */
    private List<File> f5ResourceDirectories;

    /** The list of bad metrics. */
    protected List<String> badMetrics = new ArrayList<>();

    /** Backup files. */
    protected List<File> backupFiles = new ArrayList<>();

    /**
     * Instantiates a new JMX RRD migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public F5LTMRrdMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 4;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Fix the directory index names for the F5 LTM pools and virtual-servers: NMS-8587";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    @Override
    public void preExecute() throws OnmsUpgradeException {
        printMainSettings();
        if (isInstalledVersionGreaterOrEqual(19, 0, 1)) {
            for (File f5ResourceDir : getF5ResourceDirectories()) {
                log("Backing up %s\n", f5ResourceDir);
                zipDir(new File(f5ResourceDir.getAbsolutePath() + ZIP_EXT), f5ResourceDir);
            }
        } else {
            throw new OnmsUpgradeException("This upgrade procedure requires at least OpenNMS Horizon 19.0.1; the current version is " + getOpennmsVersion());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() throws OnmsUpgradeException {
        for (File f5ResourceDir : getF5ResourceDirectories()) {
            File zip = new File(f5ResourceDir.getAbsolutePath() + ZIP_EXT);
            if (zip.exists()) {
                log("Removing backup %s\n", zip);
                FileUtils.deleteQuietly(zip);
            }
        }
        for (File backupFile : backupFiles) {
            log("Removing backup %s\n", backupFile);
            FileUtils.deleteQuietly(backupFile);
            FileUtils.deleteQuietly(new File(backupFile.getAbsolutePath().replaceFirst(ZIP_EXT, ".temp")));
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        try {
            for (File f5ResourceDir : getF5ResourceDirectories()) {
                File zip = new File(f5ResourceDir.getAbsolutePath() + ZIP_EXT);
                FileUtils.deleteDirectory(f5ResourceDir);
                if(!f5ResourceDir.mkdirs()) {
                	LOG.warn("Could not make directory: {}", f5ResourceDir.getPath());
                }
                unzipFile(zip, f5ResourceDir);
                if(!zip.delete()) {
                	LOG.warn("Could not delete file: {}", zip.getPath());
                }
            }
            File configDir = new File(ConfigFileConstants.getFilePathString());
            for (File backupFile : backupFiles) {
                unzipFile(backupFile, configDir);
            }
        } catch (IOException e) {
            throw new OnmsUpgradeException("Can't restore the backup files because " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        try {
            // Fixing JRB/RRD files
            final boolean isRrdtool = isRrdToolEnabled();
            final boolean storeByGroup = isStoreByGroupEnabled();
            for (File f5ResourceDir : getF5ResourceDirectories()) {
                if (storeByGroup) {
                    processGroupFiles(f5ResourceDir, isRrdtool);
                } else {
                    processSingleFiles(f5ResourceDir, isRrdtool);
                }
            }
            // Fixing JMX Configuration File
            File jmxConfigFile = null;
            try {
                jmxConfigFile = ConfigFileConstants.getFile(ConfigFileConstants.JMX_DATA_COLLECTION_CONF_FILE_NAME);
            } catch (IOException e) {
                throw new OnmsUpgradeException("Can't find JMX Configuration file (ignoring processing)");
            }
            fixJmxConfigurationFile(jmxConfigFile);
            // List Bad Metrics:
            log("Found %s Bad Metrics: %s\n", badMetrics.size(), badMetrics);
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't upgrade the JRBs because " + e.getMessage(), e);
        }
    }

    /**
     * Fixes a JMX configuration file.
     *
     * @param jmxConfigFile the JMX configuration file
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void fixJmxConfigurationFile(File jmxConfigFile) throws OnmsUpgradeException {
        try {
            log("Updating JMX metric definitions on %s\n", jmxConfigFile);
            zipFile(jmxConfigFile);
            backupFiles.add(new File(jmxConfigFile.getAbsolutePath() + ZIP_EXT));
            File outputFile = new File(jmxConfigFile.getCanonicalFile() + ".temp");
            FileWriter w = new FileWriter(outputFile);
            Pattern extRegex = Pattern.compile("import-mbeans[>](.+)[<]");
            Pattern aliasRegex = Pattern.compile("alias=\"([^\"]+\\.[^\"]+)\"");
            List<File> externalFiles = new ArrayList<File>();
            LineIterator it = FileUtils.lineIterator(jmxConfigFile);
            while (it.hasNext()) {
                String line = it.next();
                Matcher m = extRegex.matcher(line);
                if (m.find()) {
                    externalFiles.add(new File(jmxConfigFile.getParentFile(), m.group(1)));
                }
                m = aliasRegex.matcher(line);
                if (m.find()) {
                    String badDs = m.group(1);
                    String fixedDs = getFixedDsName(badDs);
                    log("  Replacing bad alias %s with %s on %s\n", badDs, fixedDs, line.trim());
                    line = line.replaceAll(badDs, fixedDs);
                    if (badMetrics.contains(badDs) == false) {
                        badMetrics.add(badDs);
                    }
                }
                w.write(line + "\n");
            }
            LineIterator.closeQuietly(it);
            w.close();
            FileUtils.deleteQuietly(jmxConfigFile);
            FileUtils.moveFile(outputFile, jmxConfigFile);
            if (!externalFiles.isEmpty()) {
                for (File configFile : externalFiles) {
                    fixJmxConfigurationFile(configFile);
                }
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't fix " + jmxConfigFile + " because " + e.getMessage(), e);
        }
    }

    /**
     * Gets the JMX resource directories.
     *
     * @return the JMX resource directories
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private List<File> getF5ResourceDirectories() throws OnmsUpgradeException {
        if (f5ResourceDirectories == null) {
            f5ResourceDirectories = new ArrayList<>();
            CollectdConfiguration config;
            try {
                config = new CollectdConfigFactory().getCollectdConfig();
            } catch (Exception e) {
                throw new OnmsUpgradeException("Can't upgrade the JRBs because " + e.getMessage(), e);
            }
            File rrdDir = new File(dataCollectionConfigDao.getRrdPath());
            findF5LocalTMDirectories(rrdDir, f5ResourceDirectories);
            if (f5ResourceDirectories.isEmpty()) {
                log("Warning: no F5 LocalTM directories found on %s\n", rrdDir);
            }
        }
        return f5ResourceDirectories;
    }

    /**
     * Find F5 LocalTM directories.
     *
     * @param rrdDir the RRD directory
     * @param f5Directories the target list for JMX directories
     */
    private void findF5LocalTMDirectories(final File rrdDir, final List<File> f5Directories) {
        OctetString oString = new OctetString();
        File[] files = rrdDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().matches("^\\d+(\\.\\d+)+$")) {
                    try {
                        OID oid = new OID(file.getName());
                        oString.fromSubIndex(oid, true);
                        f5Directories.add(file);
                    } catch (UnsupportedOperationException e) {
                        // Ignore directories that can't be converted to from an OID subindex.
                    }
                }
                findF5LocalTMDirectories(file, f5Directories);
            }
        }
    }

    /**
     * Process single files.
     *
     * @param resourceDir the resource directory
     * @param isRrdtool the is RRDtool enabled
     * @throws Exception the exception
     */
    private void processSingleFiles(File resourceDir, boolean isRrdtool) throws Exception {
        // META
        final String metaExt = ".meta";
        File[] metaFiles = getFiles(resourceDir, metaExt);
        if (metaFiles == null) {
            log("Warning: there are no %s files on %s\n", metaExt, resourceDir);
        } else {
            for (final File metaFile : metaFiles)  {
                log("Processing META %s\n", metaFile);
                String dsName = metaFile.getName().replaceFirst(metaExt, "");
                String newName = getFixedDsName(dsName);
                if (!dsName.equals(newName)) {
                    Properties meta = new Properties();
                    Properties newMeta = new Properties();
                    try (FileReader fr = new FileReader(metaFile);) {
                        meta.load(fr);
                        for (Object k : meta.keySet()) {
                            String key = (String) k;
                            String newKey = key.replaceAll(dsName, newName);
                            newMeta.put(newKey, newName);
                        }
                        File newFile = new File(metaFile.getParentFile(), newName + metaExt);
                        log("Re-creating META into %s\n", newFile);
                        try (FileWriter fw = new FileWriter(newFile);) {
                            newMeta.store(fw, null);
                        }
                        if (!metaFile.equals(newFile)) {
                            if (!metaFile.delete()) {
                        	LOG.warn("Could not delete file {}", metaFile.getPath());
                            }
                        }
                    }
                }
            }
        }
        // JRBs
        final String rrdExt = getRrdExtension();
        File[] jrbFiles = getFiles(resourceDir, rrdExt);
        if (jrbFiles == null) {
            log("Warning: there are no %s files on %s\n", rrdExt, resourceDir);
        } else {
            for (final File jrbFile : jrbFiles) {
                log("Processing %s %s\n", rrdExt.toUpperCase(), jrbFile);
                String dsName = jrbFile.getName().replaceFirst(rrdExt, "");
                String newName = getFixedDsName(dsName);
                File newFile = new File(jrbFile.getParentFile(), newName + rrdExt);
                if (!dsName.equals(newName)) {
                    try {
                        log("Renaming %s to %s\n", rrdExt.toUpperCase(), newFile);
                        FileUtils.moveFile(jrbFile, newFile);
                    } catch (Exception e) {
                        log("Warning: Can't move file because: %s", e.getMessage());
                        continue;
                    }
                }
                if (!isRrdtool) { // Only the JRBs may contain invalid DS inside
                    updateJrb(newFile);
                }
            }
        }
    }

    /**
     * Process group files.
     *
     * @param resourceDir the resource directory
     * @param isRrdtool the is RRDtool enabled
     * @throws Exception the exception
     */
    private void processGroupFiles(File resourceDir, boolean isRrdtool) throws Exception {
        // DS
        File dsFile = new File(resourceDir, "ds.properties");
        log("Processing DS %s\n", dsFile);
        if (dsFile.exists()) {
            Properties dsProperties = new Properties();
            Properties newDsProperties = new Properties();
            try (FileReader fr = new FileReader(dsFile);) {
                dsProperties.load(fr);
                for (Object key : dsProperties.keySet()) {
                    String oldName = (String) key;
                    String newName = getFixedDsName(oldName);
                    String oldFile = dsProperties.getProperty(oldName);
                    String newFile = getFixedFileName(oldFile);
                    newDsProperties.put(newName, newFile);
                }
                try (FileWriter fw = new FileWriter(dsFile);) {
                    newDsProperties.store(new FileWriter(dsFile), null);
                }
            }
        }
        // META
        final String metaExt = ".meta";
        File[] metaFiles = getFiles(resourceDir, metaExt);
        if (metaFiles == null) {
            log("Warning: there are no %s files on %s\n", metaExt, resourceDir);
        } else {
            for (final File metaFile : metaFiles)  {
                log("Processing META %s\n", metaFile);
                Properties meta = new Properties();
                Properties newMeta = new Properties();
                try (FileReader fr = new FileReader(metaFile);) {
                    meta.load(fr);
                    for (Object k : meta.keySet()) {
                        String key = (String) k;
                        String dsName = meta.getProperty(key);
                        String newName = getFixedDsName(dsName);
                        String newKey = key.replaceAll(dsName, newName);
                        newMeta.put(newKey, newName);
                    }
                    File newFile = new File(metaFile.getParentFile(), getFixedFileName(metaFile.getName().replaceFirst(metaExt, "")) + metaExt);
                    log("Recreating META into %s\n", newFile);
                    try (FileWriter fw = new FileWriter(newFile);) {
                        newMeta.store(fw, null);
                    }
                    if (!metaFile.equals(newFile)) {
                       if (!metaFile.delete()) {
                           LOG.warn("Could not delete file: {}", metaFile.getPath());
                       }
                    }
                }
            }
        }
        // JRBs
        final String rrdExt = getRrdExtension();
        File[] jrbFiles = getFiles(resourceDir, rrdExt);
        if (jrbFiles == null) {
            log("Warning: there are no %s files on %s\n", rrdExt, resourceDir);
        } else {
            for (final File jrbFile : jrbFiles) {
                log("Processing %s %s\n", rrdExt.toUpperCase(), jrbFile);
                File newFile = new File(jrbFile.getParentFile(), getFixedFileName(jrbFile.getName().replaceFirst(rrdExt, "")) + rrdExt);
                if (!jrbFile.equals(newFile)) {
                    try {
                        log("Renaming %s to %s\n", rrdExt.toUpperCase(), newFile);
                        FileUtils.moveFile(jrbFile, newFile);
                    } catch (Exception e) {
                        log("Warning: Can't move file because: %s", e.getMessage());
                        continue;
                    }
                }
                if (!isRrdtool) {  // Only the JRBs may contain invalid DS inside
                    updateJrb(newFile);
                }
            }
        }
    }

    /**
     * Update JRB.
     *
     * @param jrbFile the JRB file
     * @throws Exception the exception
     */
    private void updateJrb(File jrbFile) throws Exception {
        RrdDb rrdDb = new RrdDb(jrbFile);
        for (String ds : rrdDb.getDsNames()) {
            String newDs = getFixedDsName(ds);
            if (!ds.equals(newDs)) {
                log("Updating internal DS name from %s to %s\n", ds, newDs);
                rrdDb.getDatasource(ds).setDsName(newDs);
            }
        }
        rrdDb.close();
    }

    /**
     * Gets the SNMP services.
     *
     * @param config the Collectd's configuration
     * @return the list of SNMP services
     */
    private List<String> getSnmpServices(CollectdConfiguration config) {
        List<String> services = new ArrayList<>();
        for (Collector c : config.getCollectors()) {
            // The following code has been made that way to avoid a dependency with opennms-services
            // TODO Depends on opennms-services is not that bad, considering that some customers could have different implementations.
            if (c.getClassName().matches(".*(SNMP)Collector$")) {
                services.add(c.getService());
            }
        }
        return services;
    }

    /**
     * Gets the service object.
     *
     * @param config the Collectd's configuration
     * @param service the service's name
     * @return the service object
     */
    private Service getServiceObject(CollectdConfiguration config, String service) {
        for (Package pkg : config.getPackages()) {
            for (Service svc : pkg.getServices()) {
                if (svc.getName().equals(service)) {
                    return svc;
                }
            }
        }
        return null;
    }

    /**
     * Gets the value of a service property.
     *
     * @param svc the service's name
     * @param propertyName the property name
     * @return the service property value
     */
    private String getSvcPropertyValue(Service svc, String propertyName) {
        if (svc.getParameters() == null) {
            return null;
        }
        for (org.opennms.netmgt.config.collectd.Parameter p : svc.getParameters()) {
            if (p.getKey().equals(propertyName)) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * Gets the fixed DS name.
     *
     * @param dsName the DS name
     * @return the fixed DS name
     */
    protected String getFixedDsName(String dsName) {
        if (dsName.contains(".")) {
            String[] parts = dsName.split("\\.");
            return parts[0] +  parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
        }
        return dsName;
    }

    /**
     * Gets the fixed file name.
     *
     * @param oldFile the old file
     * @return the fixed file name
     */
    protected String getFixedFileName(String oldFile) {
        return AlphaNumeric.parseAndReplace(oldFile, '_');
    }

}
