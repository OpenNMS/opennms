/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.rrd;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.PropertiesCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static methods for interacting with .meta files.
 */
public abstract class RrdMetaDataUtils {
    private static final Logger LOG = LoggerFactory.getLogger(RrdMetaDataUtils.class);
    private static PropertiesCache s_cache = new PropertiesCache();

    /**
     * Writes a file with the attribute to rrd track mapping next to the rrd file.
     *
     * attributMappings = Key(attributeId, for example SNMP OID or JMX bean)
     *                  = value(Name of data source, for example ifInOctets)
     *
     * @param directory
     * @param rrdName
     * @param attributeMappings a {@link Map<String, String>} that represents
     * the mapping of attributeId to rrd track names
     */
    public static void createMetaDataFile(final String directory, final String rrdName, final Map<String, String> attributeMappings) {
        final File metaFile = new File(directory + File.separator + rrdName + ".meta");

        LOG.info("createMetaDataFile: creating meta data file {} with values '{}'", metaFile, attributeMappings);

        try {
            if (metaFile.exists()) {
                s_cache.updateProperties(metaFile, attributeMappings);
            } else {
                s_cache.saveProperties(metaFile, attributeMappings);
            }
        } catch (final IOException e) {
            LOG.error("Failed to save metadata file {}", metaFile, e);
        }
    }

    public static Map<String,String> readMetaDataFile(final File directory, final String rrdName) {
        final File metaFile = new File(directory + File.separator + rrdName + ".meta");

        try {
            final Properties props = s_cache.getProperties(metaFile);
            final Map<String,String> ret = new HashMap<String,String>();
            for (final Map.Entry<Object,Object> entry : props.entrySet()) {
                final Object value = entry.getValue();
                ret.put(entry.getKey().toString(), value == null? null : value.toString());
            }
            return ret;
        } catch (final IOException e) {
            LOG.warn("Failed to retrieve metadata from {}", metaFile, e);
        }

        return Collections.emptyMap();
    }
}
