/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.jrobin.core.RrdDb;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.JMXDataCollectionConfigFactory;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.collectd.Service;
import org.opennms.upgrade.api.OnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class JRB Migrator for JMX Collector.
 * 
 * <p>The fix for the following issues is going to break existing collected data specially for JRBs.
 * For this reason, these files must be updated too.</p>
 * 
 * <ul>
 * <li>NMS-1539</li>
 * <li>NMS-3485</li>
 * <li>NMS-4592</li>
 * <li>NMS-4612</li>
 * <li>NMS-5247</li>
 * <li>NMS-5279</li>
 * <li>NMS-5824</li>
 * </ul>
 * 
 * FIXME: Implement preExecute, postExecute and roll-back properly.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class JmxJrbMigrator implements OnmsUpgrade {

    /** The main properties. */
    private Properties mainProperties;

    /** The RRD properties. */
    private Properties rrdProperties;

    /** The JMX resource directories. */
    private List<File> jmxResourceDirectories;

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 1;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getId()
     */
    @Override
    public String getId() {
        return getClass().getName();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Fix the JRB names for the new JMX Collector: NMS-1539, NMS-3485, NMS-4592, NMS-4612, NMS-5247, NMS-5279, NMS-5824";
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
        File versionFile = new File(ConfigFileConstants.getHome(), "jetty-webapps/opennms/WEB-INF/version.properties");
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(versionFile));
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't load " + versionFile);
        }
        String version = properties.getProperty("version.display");
        log("Installed version: %s\n", version);
        if (version == null) {
            throw new OnmsUpgradeException("Can't retrive OpenNMS version");
        }
        String[] a = version.split("\\.");
        boolean isValid = false;
        try {
            isValid = Integer.parseInt(a[0]) == 1 && Integer.parseInt(a[1]) == 12 && Integer.parseInt(a[2].replaceFirst("[^\\d].+", "")) >= 2;
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't process the OpenNMS version");
        }
        if (isValid) {
            for (File jmxResourceDir : getJmxResourceDirectories()) {
                log("backing up %s\n", jmxResourceDir);
                zipDir(jmxResourceDir.getAbsolutePath() + ".zip", jmxResourceDir);
            }
        } else {
            throw new OnmsUpgradeException("This upgrade procedure requires at least OpenNMS 1.12.2, the current version is " + version);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() throws OnmsUpgradeException {
        log("Removing backup files");
        for (File jmxResourceDir : getJmxResourceDirectories()) {
            File zip = new File(jmxResourceDir.getAbsolutePath() + ".zip");
            zip.delete();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        try {
            for (File jmxResourceDir : getJmxResourceDirectories()) {
                File zip = new File(jmxResourceDir.getAbsolutePath() + ".zip");
                FileUtils.deleteDirectory(jmxResourceDir);
                jmxResourceDir.mkdirs();
                unzipDir(zip, jmxResourceDir);
                zip.delete();
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
        String opennmsHome = System.getProperty("opennms.home");
        if (opennmsHome == null) {
            log("Warning: opennms.home is not configured, using /opt/opennms instead\n");
            System.setProperty("opennms.home", "/opt/opennms");
        }
        try {
            final boolean isRrdtool = isRrdToolEnabled();
            final boolean storeByGroup = isStoreByGroupEnabled();
            log("Is RRDtool enabled ? %s\n", isRrdtool);
            log("Is storeByGroup enabled ? %s\n", storeByGroup);
            for (File jmxResourceDir : getJmxResourceDirectories()) {
                if (storeByGroup) {
                    processGroupFiles(jmxResourceDir, isRrdtool);
                } else {
                    processSingleFiles(jmxResourceDir, isRrdtool);
                }
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't upgrade the JRBs because " + e.getMessage(), e);
        }
    }

    /**
     * Gets the JMX resource directories.
     *
     * @return the JMX resource directories
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private List<File> getJmxResourceDirectories() throws OnmsUpgradeException {
        if (jmxResourceDirectories == null) {
            jmxResourceDirectories = new ArrayList<File>();
            CollectdConfiguration config;
            try {
                config = getCollectdConfiguration();
            } catch (Exception e) {
                throw new OnmsUpgradeException("Can't upgrade the JRBs because " + e.getMessage(), e);
            }
            List<String> services = getJmxServices(config);
            List<String> jmxFriendlyNames = new ArrayList<String>();
            for (String service : services) {
                Service svc = getServiceObject(config, service);
                String friendlyName = getSvcPropertyValue(svc, "friendly-name");
                jmxFriendlyNames.add(friendlyName);
            }
            File rrdDir = new File(JMXDataCollectionConfigFactory.getInstance().getRrdPath());
            findJmxDirectories(rrdDir, jmxFriendlyNames, jmxResourceDirectories);
        }
        return jmxResourceDirectories;
    }

    /**
     * Find JMX directories.
     *
     * @param rrdDir the RRD directory
     * @param jmxfriendlyNames the JMX friendly names
     * @param jmxDirectories the target list for JMX directories
     */
    private void findJmxDirectories(final File rrdDir, final List<String> jmxfriendlyNames, final List<File> jmxDirectories) {
        File[] files = rrdDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                boolean valid = false;
                for (String friendlyName : jmxfriendlyNames) {
                    if (file.getName().equals(friendlyName)) {
                        valid = true;
                    }
                }
                if (valid) {
                    jmxDirectories.add(file);
                }
                findJmxDirectories(file, jmxfriendlyNames, jmxDirectories);
            }
        }
    }

    /**
     * Gets the Collectd configuration.
     *
     * @return the Collectd configuration
     * @throws Exception the exception
     */
    private CollectdConfiguration getCollectdConfiguration() throws Exception {
        CollectdConfigFactory.init();
        JMXDataCollectionConfigFactory.init();
        CollectdConfiguration config = CollectdConfigFactory.getInstance().getCollectdConfig().getConfig();
        return config;
    }

    /**
     * Process single files.
     *
     * @param resourceDir the resource directory
     * @param isRrdtool the is RRDtool enabled
     * @throws Exception the exception
     */
    private void processSingleFiles(File resourceDir, boolean isRrdtool) throws Exception {
        // JRBs
        final String rrdExt = getRrdExtension();
        for (final File jrbFile : getFiles(resourceDir, rrdExt)) {
            log("Processing %s %s\n", rrdExt.toUpperCase(), jrbFile);
            String dsName = jrbFile.getName().replaceFirst(rrdExt, "");
            String newName = getFixedDsName(dsName);
            File newFile = new File(jrbFile.getParentFile(), newName + rrdExt);
            if (!dsName.equals(newName)) {
                try {
                    log("Renaming %s to %s\n", rrdExt.toUpperCase(), newFile);
                    FileUtils.moveFile(jrbFile, newFile); // TODO It should be copyFile in order to do a roll-back
                } catch (Exception e) {
                    log("Warning: Can't move file because: %s", e.getMessage());
                    continue;
                }
            }
            if (!isRrdtool) {
                updateJrb(newFile);
            }
        }
        // META
        final String metaExt = ".meta";
        for (final File metaFile : getFiles(resourceDir, metaExt))  {
            log("Processing META %s\n", metaFile);
            String dsName = metaFile.getName().replaceFirst(metaExt, "");
            String newName = getFixedDsName(dsName);
            if (!dsName.equals(newName)) {
                Properties meta = new Properties();
                Properties newMeta = new Properties();
                meta.load(new FileInputStream(metaFile));
                for (Object k : meta.keySet()) {
                    String key = (String) k;
                    String newKey = key.replaceAll(dsName, newName);
                    newMeta.put(newKey, newName);
                }
                File newFile = new File(metaFile.getParentFile(), newName + metaExt);
                log("Re-creating META into %s\n", newFile);
                newMeta.store(new FileWriter(newFile), null);
                if (!metaFile.equals(newFile))
                    metaFile.delete();
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
        updateDsProperties(resourceDir);
        // JRBs
        final String rrdExt = getRrdExtension();
        for (final File jrbFile : getFiles(resourceDir, rrdExt)) {
            log("Processing %s %s\n", rrdExt.toUpperCase(), jrbFile);
            File newFile = new File(jrbFile.getParentFile(), getFixedFileName(jrbFile.getName().replaceFirst(rrdExt, "")) + rrdExt);
            if (!jrbFile.equals(newFile)) {
                try {
                    log("Renaming %s to %s\n", rrdExt.toUpperCase(), newFile);
                    FileUtils.moveFile(jrbFile, newFile); // TODO It should be copyFile in order to do a roll-back
                } catch (Exception e) {
                    log("Warning: Can't move file because: %s", e.getMessage());
                    continue;
                }
            }
            if (!isRrdtool) {
                updateJrb(newFile);
            }
        }
        // META
        final String metaExt = ".meta";
        for (final File metaFile : getFiles(resourceDir, metaExt))  {
            log("Processing META %s\n", metaFile);
            Properties meta = new Properties();
            Properties newMeta = new Properties();
            meta.load(new FileInputStream(metaFile));
            for (Object k : meta.keySet()) {
                String key = (String) k;
                String dsName = meta.getProperty(key);
                String newName = getFixedDsName(dsName);
                String newKey = key.replaceAll(dsName, newName);
                newMeta.put(newKey, newName);
            }
            File newFile = new File(metaFile.getParentFile(), getFixedFileName(metaFile.getName().replaceFirst(metaExt, "")) + metaExt);
            log("Recreating META into %s\n", newFile);
            newMeta.store(new FileWriter(newFile), null);
            if (!metaFile.equals(newFile))
                metaFile.delete();
        }
    }

    /**
     * Update DS properties.
     *
     * @param resourceDir the resource directory
     * @throws Exception the exception
     */
    private void updateDsProperties(File resourceDir) throws Exception {
        File dsFile = new File(resourceDir, "ds.properties");
        log("Processing DS %s\n", dsFile);
        if (dsFile.exists()) {
            Properties dsProperties = new Properties();
            Properties newDsProperties = new Properties();
            dsProperties.load(new FileInputStream(dsFile));
            for (Object key : dsProperties.keySet()) {
                String oldName = (String) key;
                String newName = getFixedDsName(oldName);
                String oldFile = dsProperties.getProperty(oldName);
                String newFile = getFixedFileName(oldFile);
                newDsProperties.put(newName, newFile);
            }
            newDsProperties.store(new FileWriter(dsFile), null);
        }
    }

    /**
     * Gets the fixed file name.
     *
     * @param oldFile the old file
     * @return the fixed file name
     */
    private String getFixedFileName(String oldFile) {
        return AlphaNumeric.parseAndReplace(oldFile, '_');
    }

    /**
     * Gets the files.
     *
     * @param resourceDir the resource directory
     * @param ext the file extension
     * @return the files
     */
    private File[] getFiles(final File resourceDir, final String ext) {
        return resourceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(ext);
            }
        });
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
     * Gets the fixed DS name.
     *
     * @param dsName the DS name
     * @return the fixed DS name
     */
    private String getFixedDsName(String dsName) {
        if (dsName.contains(".")) {
            String parts[] = dsName.split("\\.");
            return parts[0] +  parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
        }
        return dsName;
    }

    /**
     * Load properties.
     *
     * @param properties the properties
     * @param fileName the file name
     * @throws OnmsUpgradeException 
     */
    private void loadProperties(Properties properties, String fileName) throws OnmsUpgradeException {
        try {
            File propertiesFile = ConfigFileConstants.getConfigFileByName(fileName);
            properties.load(new FileInputStream(propertiesFile));
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't load " + fileName);
        }
    }

    /**
     * Gets the main properties.
     *
     * @return the main properties
     * @throws OnmsUpgradeException 
     */
    private Properties getMainProperties() throws OnmsUpgradeException {
        if (mainProperties == null) {
            mainProperties = new Properties();
            loadProperties(mainProperties, "opennms.properties");
        }
        return mainProperties;
    }

    /**
     * Gets the RRD properties.
     *
     * @return the RRD properties
     * @throws OnmsUpgradeException 
     */
    private Properties getRrdProperties() throws OnmsUpgradeException {
        if (rrdProperties == null) {
            rrdProperties = new Properties();
            loadProperties(rrdProperties, "rrd-configuration.properties");
        }
        return mainProperties;
    }

    /**
     * Checks if is storeByGroup enabled.
     *
     * @return true, if is storeByGroup enabled
     */
    private boolean isStoreByGroupEnabled() {
        try {
            return Boolean.parseBoolean(getMainProperties().getProperty("org.opennms.rrd.storeByGroup", "false"));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if is RRDtool enabled.
     *
     * @return true, if is RRDtool enabled
     */
    private boolean isRrdToolEnabled() {
        try {
            String strategy = getRrdProperties().getProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
            return strategy.endsWith(".JniRrdStrategy");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the RRD extension.
     *
     * @return the RRD extension
     */
    private String getRrdExtension() {
        try {
            return getRrdProperties().getProperty("org.opennms.rrd.fileExtension", ".jrb");
        } catch (Exception e) {
            return ".jrb";
        }
    }

    /**
     * Gets the JMX services.
     *
     * @param config the Collectd's configuration
     * @return the list of JMX services
     */
    private List<String> getJmxServices(CollectdConfiguration config) {
        List<String> services = new ArrayList<String>();
        for (Collector c : config.getCollectorCollection()) {
            // The following code has been made that way to avoid a dependency with opennms-services
            if (c.getClassName().matches(".*(JBoss|JMXSecure|Jsr160|MX4J)Collector$")) {
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
        for (Package pkg : config.getPackageCollection()) {
            for (Service svc : pkg.getServiceCollection()) {
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
        for (org.opennms.netmgt.config.collectd.Parameter p : svc.getParameterCollection()) {
            if (p.getKey().equals(propertyName)) {
                return p.getValue();
            }
        }
        return null;
    }

    /**
     * ZIP a directory.
     *
     * @param zipFileName the ZIP file name
     * @param sourceFolder the source folder object
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void zipDir(String zipFileName, File sourceFolder) throws OnmsUpgradeException {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
            log("Creating %s\n", zipFileName);
            addDir(sourceFolder, out);
            out.close();
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't create " + zipFileName + " because " + e.getMessage());
        }
    }

    /**
     * UNZIP a directory.
     *
     * @param zipFileName the ZIP file name
     * @param outputFolder the output folder object
     */
    private void unzipDir(File zipFileName, File outputFolder) throws OnmsUpgradeException {
        byte[] buffer = new byte[1024];
        try {
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder, fileName);
                log("  Unzip %s\n", newFile.getAbsoluteFile());
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't unzip files because " + e.getMessage()); 
        }
    }

    /**
     * Adds a directory to a ZIP file.
     *
     * @param dirObj the directory object
     * @param out the ZIP output stream
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void addDir(File dirObj, ZipOutputStream out) throws IOException {
        File[] files = dirObj.listFiles();
        byte[] tmpBuf = new byte[1024];
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addDir(files[i], out);
                continue;
            }
            FileInputStream in = new FileInputStream(files[i]);
            log("  Adding: %s\n", files[i].getName());
            out.putNextEntry(new ZipEntry(files[i].getName()));
            int len;
            while ((len = in.read(tmpBuf)) > 0) {
                out.write(tmpBuf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
    }

    /**
     * Log.
     *
     * @param msgFormat the message format
     * @param args the message's arguments
     */
    protected void log(String msgFormat, Object... args) {
        System.out.printf("  " + msgFormat, args);
    }

}
