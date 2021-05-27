/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.core.utils.PropertiesCache;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.Assert;

public abstract class RrdResourceAttributeUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(RrdResourceAttributeUtils.class);

    /** 
     * File name to look for in a resource directory for datasource attributes.
     */
    public static final String DS_PROPERTIES_FILE = "ds.properties";

    /**
     * File name to look for in a resource directory for string attributes.
     */
    public static final String STRINGS_PROPERTIES_FILE_NAME = "strings.properties";

    private static final PropertiesCache s_cache = new PropertiesCache();

    /**
     * <p>getAttributesAtRelativePath</p>
     *
     * @param rrdDirectory a {@link java.io.File} object.
     * @param relativePath a {@link java.lang.String} object.
     * @return a {@link java.util.Set} object.
     */
    protected static Set<OnmsAttribute> getAttributesAtRelativePath(File rrdDirectory, String relativePath, String rrdFileSuffix) {
        final Set<OnmsAttribute> attributes =  new TreeSet<>(new AlphaNumericOnmsAttributeComparator());

        loadRrdAttributes(rrdDirectory, relativePath, attributes, rrdFileSuffix);
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

    private static void loadRrdAttributes(File rrdDirectory, String relativePath, Set<OnmsAttribute> attributes, final String rrdFileSuffix) {
        int suffixLength = rrdFileSuffix.length();
        File resourceDir = new File(rrdDirectory, relativePath);

        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(rrdFileSuffix);
            }
        };
        File[] files = resourceDir.listFiles(filter);

        if (files == null) {
            return;
        }
        
        for (final File file : files) {
            String fileName = file.getName();
            if (ResourceTypeUtils.isStoreByGroup() && !(ResourceTypeUtils.isResponseTime(relativePath) || ResourceTypeUtils.isStatus(relativePath))) {
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
    protected static Properties getDsProperties(File directory) {
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
    protected static File getRrdFileForDs(File directory, String ds, String extension) {
        String rrdBaseName = ds;
        if (ResourceTypeUtils.isStoreByGroup()) {
            try {
                rrdBaseName = s_cache.getProperty(new File(directory, DS_PROPERTIES_FILE), ds);
            } catch (IOException e) {
                LOG.error("ds.properties error", e);
                rrdBaseName = ds;
            }
        }
        return new File(directory, rrdBaseName + extension);
    }

    /**
     * <p>getStringProperties</p>
     *
     * @param rrdDirectory a {@link java.io.File} object.
     * @param relativePath a {@link java.lang.String} object.
     * @return a {@link java.util.Properties} object.
     */
    protected static Properties getStringProperties(File rrdDirectory, String relativePath) {
        Assert.notNull(rrdDirectory, "rrdDirectory argument must not be null");
        Assert.notNull(relativePath, "relativePath argument must not be null");
        
        File resourceDir = new File(rrdDirectory, relativePath);
        
        return getStringProperties(resourceDir);
    }

    private static Properties getStringProperties(File resourceDir) {
        Assert.notNull(resourceDir, "resourceDir argumnet must not be null");
        return getProperties(new File(resourceDir, STRINGS_PROPERTIES_FILE_NAME));
    }

    /**
     * <p>getProperties</p>
     *
     * @param file a {@link java.io.File} object.
     * @return a {@link java.util.Properties} object.
     */
    protected static Properties getProperties(File file) {
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
    protected static void saveUpdatedProperties(File propertiesFile, Properties props) throws FileNotFoundException, IOException {
        s_cache.saveProperties(propertiesFile, props);
    }

    /**
     * <p>updateDsProperties</p>
     *
     * @param resourceDir a {@link java.io.File} object.
     * @param dsNamesToRrdNames a {@link java.util.Map} object.
     */
    protected static void updateDsProperties(File resourceDir, Map<String, String> dsNamesToRrdNames) {
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
    protected static void updateStringProperty(File resourceDir, String attrVal, String attrName) throws FileNotFoundException, IOException {
        File propertiesFile = new File(resourceDir, STRINGS_PROPERTIES_FILE_NAME);
        s_cache.setProperty(propertiesFile, attrName, attrVal);
    }

    /**
     * <p>getStringProperty</p>
     *
     * @param directory a {@link java.io.File} object.
     * @param key a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    protected static String getStringProperty(File directory, String key) {
        File file = new File(directory, STRINGS_PROPERTIES_FILE_NAME);
        try {
            return s_cache.getProperty(file, key);
        } catch (IOException e) {
            String message = "loadProperties: Error opening properties file " + file.getAbsolutePath() + ": " + e;
            LOG.warn(message, e);
            throw new DataAccessResourceFailureException(message, e);
        }
    }

    /**
     * Alphanumeric sort that handles substrings with numeric components
     */
    public static class AlphaNumericOnmsAttributeComparator implements Serializable, Comparator<OnmsAttribute> {
        private static final long serialVersionUID = 1;

        public int compare(final OnmsAttribute firstAtt, final OnmsAttribute secondAtt) {
            // Sorting algorithm found here: http://blog.icodejava.com/261/how-to-sort-alpha-numeric-strings-in-java/
            final String firstString  = firstAtt.getName();
            final String secondString = secondAtt.getName();

            if (secondString == null || firstString == null) {
                return 0;
            }

            final int lengthFirstStr = firstString.length();
            final int lengthSecondStr = secondString.length();

            int index1 = 0;
            int index2 = 0;

            while (index1 < lengthFirstStr && index2 < lengthSecondStr) {
                char ch1 = firstString.charAt(index1);
                char ch2 = secondString.charAt(index2);

                final char[] space1 = new char[lengthFirstStr];
                final char[] space2 = new char[lengthSecondStr];

                int loc1 = 0;
                int loc2 = 0;

                do {
                    space1[loc1++] = ch1;
                    index1++;

                    if (index1 < lengthFirstStr) {
                        ch1 = firstString.charAt(index1);
                    } else {
                        break;
                    }
                } while (Character.isDigit(ch1) == Character.isDigit(space1[0]));

                do {
                    space2[loc2++] = ch2;
                    index2++;

                    if (index2 < lengthSecondStr) {
                        ch2 = secondString.charAt(index2);
                    } else {
                        break;
                    }
                } while (Character.isDigit(ch2) == Character.isDigit(space2[0]));

                final String str1 = new String(space1);
                final String str2 = new String(space2);

                final int result;

                if (Character.isDigit(space1[0]) && Character.isDigit(space2[0])) {
                    final Long firstNumberToCompare = Long.parseLong(str1.trim());
                    final Long secondNumberToCompare = Long.parseLong(str2.trim());
                    result = firstNumberToCompare.compareTo(secondNumberToCompare);
                } else {
                    result = str1.compareToIgnoreCase(str2);
                }

                if (result != 0) {
                    return result;
                }
            }
            return lengthFirstStr - lengthSecondStr;
        }

    }
}
