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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.rrd.RrdUtils;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public class ResourceTypeUtils {
	
	public static String DS_PROPERTIES_FILE = "ds.properties";
	
    /**
     * This class has only static methods.
     */
    private ResourceTypeUtils() {
    }

    public static Set<OnmsAttribute> getAttributesAtRelativePath(File rrdDirectory, String relativePath) {
        int suffixLength = RrdFileConstants.getRrdSuffix().length();
        
        File directory = new File(rrdDirectory, relativePath);
        File[] files = directory.listFiles(RrdFileConstants.RRD_FILENAME_FILTER);
        
        Set<OnmsAttribute> attributes =  new HashSet<OnmsAttribute>(files.length);
        for (File file : files) {
            String fileName = file.getName();
            if (isStoreByGroup() && !isResponseTime(relativePath)) {
                String groupName = fileName.substring(0, fileName.length() - suffixLength);
                Properties props = getDsProperties(directory);
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
        
        Properties properties = getProperties(rrdDirectory, relativePath);
        if (properties != null) {
            for (Entry<Object,Object> entry : properties.entrySet()) {
                attributes.add(new StringPropertyAttribute(entry.getKey().toString(), entry.getValue().toString()));
            }
        }
        
        return attributes;
    }

    
    public static Properties getDsProperties(File directory) {
        Properties props = new Properties();
        File propertiesFile = new File(directory, DS_PROPERTIES_FILE);
        if (propertiesFile.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(propertiesFile);
                props.load(fileInputStream);
                fileInputStream.close();
            } catch (Exception e) {
                log().error("ds.properties error: " + e, e);
            }
        } else {
        	log().error("ds.properties does not exist on directory " + directory);
        }
        return props;
    }
	
    public static File getRrdFileForDs(File directory, String ds) {
        if (isStoreByGroup()) {
            Properties props = getDsProperties(directory);
            ds = props.getProperty(ds);
        }
        return new File(directory, ds + RrdUtils.getExtension());
    }

    public static boolean isStoreByGroup() {
        return Boolean.getBoolean("org.opennms.rrd.storeByGroup");
    }

    public static boolean isResponseTime(String relativePath) {
        return Pattern.matches("^" + DefaultResourceDao.RESPONSE_DIRECTORY + ".+$", relativePath);
    }

    public static Properties getProperties(File rrdDirectory, String relativePath) {
        Assert.notNull(rrdDirectory, "rrdDirectory argument must not be null");
        Assert.notNull(relativePath, "relativePath argument must not be null");
        
        return getProperties(new File(rrdDirectory, relativePath + File.separator + DefaultResourceDao.STRINGS_PROPERTIES_FILE_NAME));
    }

    public static Properties getProperties(File file) {
        if (!file.exists()) {
            return null;
        }
        
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
        } catch (IOException e) {
            String message = "loadProperties: Error opening properties file "
                + file.getAbsolutePath() + ": " + e;
            log().warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        }
    
        Properties properties = new Properties();
        
        try {
            properties.load(fileInputStream);
        } catch (IOException e) {
            String message = "loadProperties: Error loading properties file "
                + file.getAbsolutePath() + ": " + e;
            log().warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                String message = 
                    "loadProperties: Error closing properties file "
                    + file.getAbsolutePath() + ": " + e;
                log().warn(message, e);
                throw new DataAccessResourceFailureException(message, e);
            }
        }
                
        return properties;
    }
    
    private static Category log() {
        return ThreadCategory.getInstance();
    }
}
