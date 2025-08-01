/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.xml.collector;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.Resource;
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
    public static String getLastFilename(BlobStore blobStore, String serviceName, ResourcePath path, String targetPath) throws Exception {
        String filename = null;
        try {
            String key = getCacheId(path, serviceName, targetPath);
            filename = blobStore.get(key, Sftp3gppUtils.class.getName())
                    .map(String::new)
                    .orElse(null);
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
    public static void setLastFilename(BlobStore blobStore, String serviceName, ResourcePath path, String targetPath, String filename) throws Exception {
        String key = getCacheId(path, serviceName, targetPath);
        blobStore.put(key, filename.getBytes(StandardCharsets.UTF_8), Sftp3gppUtils.class.getName());
    }

    /**
     * Gets the cache id.
     *
     * @param serviceName the service name
     * @param targetPath the target path
     * @return the cache id
     */
    public static String getCacheId(ResourcePath path, String serviceName, String targetPath) {
        return String.join("/", path.elements()) + XML_LAST_FILENAME + '.' + serviceName + targetPath.replaceAll("/", "_");
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
