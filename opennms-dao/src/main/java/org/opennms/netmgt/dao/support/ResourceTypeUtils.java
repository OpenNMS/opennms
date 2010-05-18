//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 May 09: Add a check in loadRrdAttributes in case listFiles returns null. - dj@opennms.org
// 2007 Apr 05: Move string property loading here from RrdGraphService. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
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

import org.opennms.core.utils.PropertiesCache;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.rrd.RrdUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class ResourceTypeUtils {

    public static String DS_PROPERTIES_FILE = "ds.properties";

    private static PropertiesCache s_cache = new PropertiesCache();

    /**
     * This class has only static methods.
     */
    private ResourceTypeUtils() {
    }
    
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

    private static void loadRrdAttributes(File rrdDirectory,
            String relativePath, Set<OnmsAttribute> attributes) {
        int suffixLength = RrdFileConstants.getRrdSuffix().length();
        File resourceDir = new File(rrdDirectory, relativePath);
        File[] files = resourceDir.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);
        
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            String fileName = file.getName();
            if (isStoreByGroup() && !isResponseTime(relativePath)) {
                String groupName = fileName.substring(0, fileName.length() - suffixLength);
                Properties props = getDsProperties(resourceDir);
                for (Object o : props.keySet()) {
                    String dsName = (String)o;
                    if (props.getProperty(dsName).equals(groupName)) {
                        attributes.add(new RrdGraphAttribute(dsName, relativePath, file.getName()));
                    }
                }
            } else {
                String dsName = fileName.substring(0, fileName.length() - suffixLength);
                attributes.add(new RrdGraphAttribute(dsName, relativePath, file.getName()));
            }
        }
    }
    
    public static Properties getDsProperties(File directory) {
        File propertiesFile = new File(directory, DS_PROPERTIES_FILE);
        try {
            return s_cache.getProperties(propertiesFile);
        } catch(IOException e) {
            log().error("ds.properties error: " + e, e);
            return new Properties();
        }
    }
    
    public static File getRrdFileForDs(File directory, String ds) {
        String rrdBaseName = ds;
        if (isStoreByGroup()) {
            try {
                rrdBaseName = s_cache.getProperty(new File(directory, DS_PROPERTIES_FILE), ds);
            } catch (IOException e) {
                log().error("ds.properties error: " + e, e);
                rrdBaseName = ds;
            }
        }
        return new File(directory, rrdBaseName + RrdUtils.getExtension());
    }

    public static boolean isStoreByGroup() {
        return Boolean.getBoolean("org.opennms.rrd.storeByGroup");
    }

    public static boolean isResponseTime(String relativePath) {
        return Pattern.matches("^" + DefaultResourceDao.RESPONSE_DIRECTORY + ".+$", relativePath);
    }

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

    public static Properties getProperties(File file) {
        try {
            return s_cache.findProperties(file);
        } catch (IOException e) {
            String message = "loadProperties: Error opening properties file " + file.getAbsolutePath() + ": " + e;
            log().warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        }
    }
    
    public static ThreadCategory log() {
        return ThreadCategory.getInstance();
    }

    public static void saveUpdatedProperties(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        s_cache.saveProperties(propertiesFile, props);
    }

    public static void updateDsProperties(File resourceDir, Map<String, String> dsNamesToRrdNames) {
        try {
            s_cache.updateProperties(new File(resourceDir, DS_PROPERTIES_FILE), dsNamesToRrdNames);
        } catch (IOException e) {
            log().error("Unable to save DataSource Properties file" + e, e);
        }
    }

    public static void updateStringProperty(File resourceDir, String attrVal, String attrName) throws FileNotFoundException, IOException {
        File propertiesFile = new File(resourceDir, DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME);
        s_cache.setProperty(propertiesFile, attrName, attrVal);
    }

    public static String getStringProperty(File directory, String key) {
        File file = new File(directory, DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME);
        try {
            return s_cache.getProperty(file, key);
        } catch (IOException e) {
            String message = "loadProperties: Error opening properties file " + file.getAbsolutePath() + ": " + e;
            log().warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        }
    }
}
