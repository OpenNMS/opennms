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

package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.protocols.sftp.Sftp3gppUrlConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Sftp3gppUtils.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public abstract class Sftp3gppUtils {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Sftp3gppUtils.class);

    /** The Constant PM_GROUPS_FILENAME. */
    public static final String PM_GROUPS_FILENAME = "3gpp-pmgroups.properties";

    /** The Constant XML_LAST_FILENAME. */
    public static final String XML_LAST_FILENAME = "_xmlCollectorLastFilename";

    /** The 3GPP Performance Metric Instance Formats. */
    private static final Properties m_pmGroups = new Properties();

    /**
     * Gets the last filename.
     *
     * @param serviceName the service name
     * @param resourceDir the resource directory
     * @param targetPath the target path
     * @return the last filename
     * @throws Exception the exception
     */
    public static String getLastFilename(ResourceStorageDao resourceStorageDao, String serviceName, ResourcePath path, String targetPath) throws Exception {
        String filename = null;
        try {
            filename = resourceStorageDao.getStringAttribute(path,  getCacheId(serviceName, targetPath));
        } catch (Throwable e) {
            LOG.info("getLastFilename: creating a new filename tracker on {}", path);
        }
        return filename;
    }

    /**
     * Sets the last filename.
     *
     * @param serviceName the service name
     * @param resourceDir the resource directory
     * @param targetPath the target path
     * @param filename the filename
     * @throws Exception the exception
     */
    public static void setLastFilename(ResourceStorageDao resourceStorageDao, String serviceName, ResourcePath path, String targetPath, String filename) throws Exception {
        resourceStorageDao.setStringAttribute(path, getCacheId(serviceName, targetPath), filename);
    }

    /**
     * Gets the cache id.
     *
     * @param serviceName the service name
     * @param targetPath the target path
     * @return the cache id
     */
    public static String getCacheId(String serviceName, String targetPath) {
        return XML_LAST_FILENAME + '.' + serviceName + targetPath.replaceAll("/", "_");
    }

    /**
     * Safely delete file on remote node.
     *
     * @param connection the SFTP URL Connection
     * @param fileName the file name
     */
    public static void deleteFile(Sftp3gppUrlConnection connection, String fileName) {
        try {
            connection.deleteFile(fileName);
        } catch (Exception e) {
            LOG.warn("Can't delete file {} from {} because {}", fileName, connection.getURL().getHost(), e.getMessage());
        }
    }

    /**
     * Process xml resource.
     *
     * @param resource the resource
     * @param attribGroupType the attrib group type
     */
    public static void processXmlResource(CollectionSetBuilder builder, Resource resource, String resourceTypeName, String group) {
        Map<String,String> properties = get3gppProperties(get3gppFormat(resourceTypeName), resource.getInstance());
        for (Entry<String,String> entry : properties.entrySet()) {
            builder.withStringAttribute(resource, group, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the 3GPP resource format.
     *
     * @param resourceType the resource type
     * @return the 3gpp format
     */    
    public static String get3gppFormat(String resourceType) {
        if (m_pmGroups.isEmpty()) {
            try {
                File configFile = new File(ConfigFileConstants.getFilePathString(), PM_GROUPS_FILENAME);
                if (configFile.exists()) {
                    LOG.info("Using 3GPP PM Groups format from {}", configFile);
                    m_pmGroups.load(new FileInputStream(configFile));
                } else {
                    LOG.info("Using default 3GPP PM Groups format.");
                    m_pmGroups.load(Sftp3gppUtils.class.getResourceAsStream("/" + PM_GROUPS_FILENAME));
                }
            } catch (Exception e) {
                LOG.warn("Can't load 3GPP PM Groups format because {}", e.getMessage());
            }
        }
        return m_pmGroups.getProperty(resourceType);
    }

    /**
     * Gets the 3GPP properties based on measInfoId.
     *
     * @param format the format
     * @param measInfoId the measInfoId (the resource instance)
     * @return the properties
     */
    // TODO  It may be possible to have some kind of default parsing by looking for key=value pairs.
    public static Map<String,String> get3gppProperties(String format, String measInfoId) {
        Map<String,String> properties = new LinkedHashMap<String,String>();
        if (format != null) {
            String[] groups = format.split("\\|");
            for (String group : groups) {
                String[] subgroups = group.split("/");
                for (String subgroup : subgroups) {
                    String[] pair = subgroup.split("=");
                    if (pair.length > 1) {
                        if (pair[1].matches("^[<].+[>]$")) {
                            // I'm not sure how to deal with separating by | or / and avoiding separating by \/
                            String valueRegex = pair[1].equals("<directory path>") ? "=([^|]+)" : "=([^|/]+)";
                            Matcher m = Pattern.compile(pair[0] + valueRegex).matcher(measInfoId);
                            if (m.find()) {
                                String v = pair[1].equals("<directory path>") ? m.group(1).replaceAll("\\\\/", "/") : m.group(1);
                                properties.put(pair[0], v);
                            }
                        }
                    }
                }
            }
        }
        if (properties.isEmpty() && Boolean.getBoolean("org.opennms.collectd.xml.3gpp.useSimpleParserForMeasObjLdn")) {
            String[] groups = measInfoId.split("\\|");
            for (String group : groups) {
                String[] pair = group.split("=");
                if (pair.length == 2) {
                    properties.put(pair[0], pair[1]);
                }
            }
        }
        // If the format was not found, and the default parser couldn't extract any data then,
        // the label must be equal to the instance (NMS-6365, to avoid blank descriptions). 
        properties.put("label", properties.isEmpty() ? measInfoId : properties.toString().replaceAll("[{}]", ""));
        properties.put("instance", measInfoId);
        return properties;
    }

}
