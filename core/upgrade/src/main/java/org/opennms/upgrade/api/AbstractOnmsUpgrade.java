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
package org.opennms.upgrade.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.rrd.RrdUtils;

/**
 * The Abstract class for OpenNMS Upgrade Implementations.
 * <p>This contains the basic methods that may be required for several implementations.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public abstract class AbstractOnmsUpgrade implements OnmsUpgrade {

    /** The main properties. */
    private Properties mainProperties;

    /** The RRD properties. */
    private Properties rrdProperties;

    /** The OpenNMS version. */
    private String onmsVersion;

    /**
     * Instantiates a new abstract OpenNMS upgrade.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public AbstractOnmsUpgrade() throws OnmsUpgradeException {
        registerProperties(getMainProperties());
        registerProperties(getRrdProperties());
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getId()
     */
    @Override
    public String getId() {
        return getClass().getName();
    }

    /**
     * Gets the home directory.
     *
     * @return the home directory
     */
    protected String getHomeDirectory() {
        return ConfigFileConstants.getHome();
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

    /**
     * Gets the files.
     *
     * @param resourceDir the resource directory
     * @param ext the file extension
     * @return the files
     */
    protected File[] getFiles(final File resourceDir, final String ext) {
        return resourceDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(ext);
            }
        });
    }

    /**
     * Register properties.
     *
     * @param properties the properties
     */
    protected void registerProperties(Properties properties) {
        if (properties == null) {
            return;
        }
        for (Object o : properties.keySet()) {
            String key = (String) o;
            System.setProperty(key, properties.getProperty(key));
        }
    }

    /**
     * Load properties.
     *
     * @param properties the properties
     * @param fileName the file name
     * @throws OnmsUpgradeException 
     */
    protected void loadProperties(Properties properties, String fileName) throws OnmsUpgradeException {
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
    protected Properties getMainProperties() throws OnmsUpgradeException {
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
    protected Properties getRrdProperties() throws OnmsUpgradeException {
        if (rrdProperties == null) {
            rrdProperties = new Properties();
            loadProperties(rrdProperties, "rrd-configuration.properties");
        }
        return rrdProperties;
    }

    /**
     * Checks if is storeByGroup enabled.
     *
     * @return true, if is storeByGroup enabled
     */
    protected boolean isStoreByGroupEnabled() {
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
    protected boolean isRrdToolEnabled() {
        try {
            RrdUtils.getStrategy().getClass().getSimpleName().equals("JniRrdStrategy");
            String strategy = getRrdProperties().getProperty("org.opennms.rrd.strategyClass", "org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy");
            return !strategy.endsWith(".JRobinRrdStrategy");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Gets the RRD extension.
     *
     * @return the RRD extension
     */
    protected String getRrdExtension() {
        try {
            return RrdUtils.getExtension();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Gets the DB connection.
     *
     * @return the DB connection
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected Connection getDbConnection() throws OnmsUpgradeException {
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
            DataSourceConfiguration dsc = null;
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(cfgFile);
                dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, fileInputStream);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
            } 
            for (JdbcDataSource ds : dsc.getJdbcDataSourceCollection()) {
                if (ds.getName().equals("opennms")) {
                    return DriverManager.getConnection(ds.getUrl(), ds.getUserName(), ds.getPassword());
                }
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't connect to OpenNMS Database");
        }
        throw new OnmsUpgradeException("Databaseconnection cannot be null");
    }

    /**
     * ZIP a directory.
     *
     * @param zipFileName the ZIP file name
     * @param sourceFolder the source folder object
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected void zipDir(String zipFileName, File sourceFolder) throws OnmsUpgradeException {
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
    protected void unzipDir(File zipFileName, File outputFolder) throws OnmsUpgradeException {
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
     * Gets the currently installed OpenNMS version.
     *
     * @return the OpenNMS version
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected String getOpennmsVersion() throws OnmsUpgradeException {
        if (onmsVersion == null) {
            File versionFile = new File(getHomeDirectory(), "jetty-webapps/opennms/WEB-INF/version.properties");
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(versionFile));
            } catch (Exception e) {
                throw new OnmsUpgradeException("Can't load " + versionFile);
            }
            final String version = properties.getProperty("version.display");
            if (version == null) {
                throw new OnmsUpgradeException("Can't retrive OpenNMS version");
            }
            onmsVersion = version;
        }
        return onmsVersion;
    }

    /**
     * Checks if the OpenNMS version is valid.
     *
     * @param mayor the mayor
     * @param minor the minor
     * @param release the release
     * @return true, if the current installed version is greater or equals than $major.$minor.$release</p>
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected boolean isOnmsVersionValid(int mayor, int minor, int release) throws OnmsUpgradeException {
        String version = getOpennmsVersion();
        String[] a = version.split("\\.");
        boolean isValid = false;
        try {
            isValid = Integer.parseInt(a[0]) >= mayor && Integer.parseInt(a[1]) >= minor && Integer.parseInt(a[2].replaceFirst("[^\\d].+", "")) >= release;
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't process the OpenNMS version");
        }
        return isValid;
    }

    /**
     * Prints the settings.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected void printMainSettings() throws OnmsUpgradeException {
        log("OpenNMS Home: %s\n", getHomeDirectory());
        log("OpenNMS Version: %s\n", getOpennmsVersion());
        log("Is RRDtool enabled? %s\n", isRrdToolEnabled());
        log("Is storeByGroup enabled? %s\n", isStoreByGroupEnabled());
        log("RRD Extension: %s\n", getRrdExtension());
        log("RRD Strategy: %s\n", RrdUtils.getStrategy().getClass().getName());
    }

    /**
     * Prints the full settings.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected void printFullSettings() throws OnmsUpgradeException {
        printMainSettings();
        printProperties("Main Properties", getMainProperties());
        printProperties("RRD Properties", getRrdProperties());
    }

    /**
     * Prints the properties.
     *
     * @param title the title
     * @param properties the properties
     */
    private void printProperties(String title, Properties properties) {
        List<String> keys = new ArrayList<String>();
        for (Object k : properties.keySet()) {
            keys.add((String) k);
        }
        Collections.sort(keys);
        log("%s\n", title);
        for (String key : keys) {
            log("  %s = %s\n", key, properties.getProperty(key));
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
