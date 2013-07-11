/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.PropertiesCache;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.rrd.RrdUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

/**
 * <p>ResourceTypeUtils class.</p>
 */
public abstract class ResourceTypeUtils {
    
    private static Logger LOG = LoggerFactory.getLogger(ResourceTypeUtils.class);

    /** Constant <code>DS_PROPERTIES_FILE="ds.properties"</code> */
    public static String DS_PROPERTIES_FILE = "ds.properties";

    private static PropertiesCache s_cache = new PropertiesCache();

    /**
     * <p>getAttributesAtRelativePath</p>
     *
     * @param rrdDirectory a {@link java.io.File} object.
     * @param relativePath a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    public static Set<OnmsAttribute> getAttributesAtRelativePath(File rrdDirectory, String relativePath) {
        
        Set<OnmsAttribute> attributes =  new HashSet<OnmsAttribute>();

        loadRrdAttributes(rrdDirectory, relativePath, attributes);
        loadStringAttributes(rrdDirectory, relativePath, attributes);
        
        return attributes;
        
    }

    private static void loadStringAttributes(File rrdDirectory,
            String relativePath, Set<OnmsAttribute> attributes) {
        Properties properties = getStringProperties(rrdDirectory, relativePath);
        if (properties != null) {
            for (Entry<Object,Object> entry : properties.entrySet()) {
                attributes.add(new StringPropertyAttribute(entry.getKey().toString(), entry.getValue().toString()));
            }
        }
    }

    private static void loadRrdAttributes(File rrdDirectory, String relativePath, Set<OnmsAttribute> attributes) {
        int suffixLength = RrdFileConstants.getRrdSuffix().length();
        File resourceDir = new File(rrdDirectory, relativePath);
        File[] files = resourceDir.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);
        
        if (files == null) {
            return;
        }
        
        for (final File file : files) {
            String fileName = file.getName();
            if (isStoreByGroup() && !isResponseTime(relativePath)) {
                String groupName = fileName.substring(0, fileName.length() - suffixLength);
                Properties props = getDsProperties(resourceDir);
                for (Object o : props.keySet()) {
                    String dsName = (String)o;
                    if (props.getProperty(dsName).equals(groupName)) {
                        attributes.add(new RrdGraphAttribute(dsName, relativePath, fileName));
                    }
                }
            } else {
                String dsName = fileName.substring(0, fileName.length() - suffixLength);
                attributes.add(new RrdGraphAttribute(dsName, relativePath, fileName));
            }
        }
    }
    
    /**
     * <p>getDsProperties</p>
     *
     * @param directory a {@link java.io.File} object.
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getDsProperties(File directory) {
        File propertiesFile = new File(directory, DS_PROPERTIES_FILE);
        try {
            return s_cache.getProperties(propertiesFile);
        } catch(IOException e) {
            LOG.error("ds.properties error", e);
            return new Properties();
        }
    }
    
    /**
     * <p>getRrdFileForDs</p>
     *
     * @param directory a {@link java.io.File} object.
     * @param ds a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public static File getRrdFileForDs(File directory, String ds) {
        String rrdBaseName = ds;
        if (isStoreByGroup()) {
            try {
                rrdBaseName = s_cache.getProperty(new File(directory, DS_PROPERTIES_FILE), ds);
            } catch (IOException e) {
                LOG.error("ds.properties error", e);
                rrdBaseName = ds;
            }
        }
        return new File(directory, rrdBaseName + RrdUtils.getExtension());
    }

    /**
     * <p>isStoreByGroup</p>
     *
     * @return a boolean.
     */
    public static boolean isStoreByGroup() {
        return Boolean.getBoolean("org.opennms.rrd.storeByGroup");
    }

    /**
     * <p>isResponseTime</p>
     *
     * @param relativePath a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean isResponseTime(String relativePath) {
        return Pattern.matches("^" + DefaultResourceDao.RESPONSE_DIRECTORY + ".+$", relativePath);
    }

    /**
     * <p>getStringProperties</p>
     *
     * @param rrdDirectory a {@link java.io.File} object.
     * @param relativePath a {@link java.lang.String} object.
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getStringProperties(File rrdDirectory, String relativePath) {
        Assert.notNull(rrdDirectory, "rrdDirectory argument must not be null");
        Assert.notNull(relativePath, "relativePath argument must not be null");
        
        File resourceDir = new File(rrdDirectory, relativePath);
        
        return getStringProperties(resourceDir);
    }

    private static Properties getStringProperties(File resourceDir) {
        Assert.notNull(resourceDir, "resourceDir argumnet must not be null");
        return getProperties(new File(resourceDir, DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME));
    }

    /**
     * <p>getProperties</p>
     *
     * @param file a {@link java.io.File} object.
     * @return a {@link java.util.Properties} object.
     */
    public static Properties getProperties(File file) {
        try {
            return s_cache.findProperties(file);
        } catch (IOException e) {
            String message = "loadProperties: Error opening properties file " + file.getAbsolutePath() + ": " + e;
            LOG.warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        }
    }
    
    /**
     * <p>saveUpdatedProperties</p>
     *
     * @param propertiesFile a {@link java.io.File} object.
     * @param props a {@link java.util.Properties} object.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    public static void saveUpdatedProperties(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        s_cache.saveProperties(propertiesFile, props);
    }

    /**
     * <p>updateDsProperties</p>
     *
     * @param resourceDir a {@link java.io.File} object.
     * @param dsNamesToRrdNames a {@link java.util.Map} object.
     */
    public static void updateDsProperties(File resourceDir, Map<String, String> dsNamesToRrdNames) {
        try {
            s_cache.updateProperties(new File(resourceDir, DS_PROPERTIES_FILE), dsNamesToRrdNames);
        } catch (IOException e) {
            LOG.error("Unable to save DataSource Properties file", e);
        }
    }

    /**
     * <p>updateStringProperty</p>
     *
     * @param resourceDir a {@link java.io.File} object.
     * @param attrVal a {@link java.lang.String} object.
     * @param attrName a {@link java.lang.String} object.
     * @throws java.io.FileNotFoundException if any.
     * @throws java.io.IOException if any.
     */
    public static void updateStringProperty(File resourceDir, String attrVal, String attrName) throws FileNotFoundException, IOException {
        File propertiesFile = new File(resourceDir, DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME);
        s_cache.setProperty(propertiesFile, attrName, attrVal);
    }

    /**
     * <p>getStringProperty</p>
     *
     * @param directory a {@link java.io.File} object.
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getStringProperty(File directory, String key) {
        File file = new File(directory, DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME);
        try {
            return s_cache.getProperty(file, key);
        } catch (IOException e) {
            String message = "loadProperties: Error opening properties file " + file.getAbsolutePath() + ": " + e;
            LOG.warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        }
    }
    
    /**
     * 
     * @param nodeSource a {@link java.lang.String} object.
     * @return a {@link java.io.File} object.
     */
    public static File getRelativeNodeSourceDirectory(String nodeSource) {
        String[] ident = nodeSource.split(":");
        return new File(DefaultResourceDao.FOREIGN_SOURCE_DIRECTORY, File.separator + ident[0] + File.separator + ident[1]);
    }
}
