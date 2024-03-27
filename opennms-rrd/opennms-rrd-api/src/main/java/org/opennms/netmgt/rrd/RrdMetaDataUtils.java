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
