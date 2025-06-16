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
package org.opennms.netmgt.jmx;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.management.remote.JMXServiceURL;

import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.jmx.connection.JmxConnectionConfig;
import org.opennms.netmgt.jmx.connection.JmxConnectionConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JmxUtils {

    private static final Logger LOG = LoggerFactory.getLogger(JmxUtils.class);

    /**
     * Converts the map, so that it only contains String values. All non String values will be removed (null values included).
     * <p/>
     * The returned map is not modifiable.
     * <p/>
     * If the input map is null, null is also returned.
     *
     * @param map The map to be converted. May be null.
     * @return An unmodifiable map containing only String values from the input map, or null if input map was null.
     */
    public static Map<String, String> convertToUnmodifiableStringMap(final Map<String, Object> map) {
        if (map != null) {
            Map<String, String> convertedProperties = new HashMap<>();
            for (Map.Entry<String, Object> eachEntry : map.entrySet()) {
                if (eachEntry.getValue() != null) {
                    convertedProperties.put(eachEntry.getKey(), eachEntry.getValue().toString());
                }
            }
            return Collections.unmodifiableMap(convertedProperties);
        }
        return null;
    }

    /**
     * Converts the map, so that it only contains String values. All non String values will be removed (null values included).
     * <p/>
     * The returned map is modifiable.
     * <p/>
     * If the input map is null, null is also returned.
     *
     * @param map The map to be converted. May be null.
     * @return An unmodifiable map containing only String values from the input map, or null if input map was null.
     */
    public static Map<String, String> convertToStringMap(final Map<String, Object> map) {
        if (map != null) {
            Map<String, String> convertedProperties = new HashMap<>();
            for (Map.Entry<String, Object> eachEntry : map.entrySet()) {
                if (eachEntry.getValue() != null) {
                    convertedProperties.put(eachEntry.getKey(), eachEntry.getValue().toString());
                }
            }
            return convertedProperties;
        }
        return null;
    }

    public static String getCollectionDirectory(final Map<String, String> map, final String friendlyName, final String serviceName) {
        Objects.requireNonNull(map, "Map must be initialized!");

        if (friendlyName != null && !friendlyName.isEmpty()) {
            return friendlyName;
        }
        if (serviceName != null && !serviceName.isEmpty()) {
            return serviceName.toLowerCase();
        }
        final String port = map.get(ParameterName.PORT.toString());
        return port;
    }

    public static String getGroupName(final Map<String, String> map, final Mbean mbean) {
        final boolean useMbeanForRrds = Boolean.valueOf(map.get(ParameterName.USE_MBEAN_NAME_FOR_RRDS.toString()));
        final String groupName = useMbeanForRrds ? mbean.getName() : mbean.getObjectname();
        return groupName;
    }

    public static MBeanServer getMBeanServer(JmxConfigDao jmxConfigDao, String address, Map<String, String> parameters) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(parameters);
        if (jmxConfigDao != null && jmxConfigDao.getConfig() != null) {
            try {
                final JmxConnectionConfig config = JmxConnectionConfigBuilder.buildFrom(address, parameters).build();
                final int port = new JMXServiceURL(config.getUrl()).getPort();
                final MBeanServer mBeanServer = jmxConfigDao.getConfig().lookupMBeanServer(address, port);
                return mBeanServer;
            } catch (MalformedURLException e) {
                LOG.warn("Unexpected exception: {}", e.getMessage(), e);
            }
        }
        return null; // not found or exception
    }

    public static Map<String, String> getRuntimeAttributes(JmxConfigDao jmxConfigDao, String address, Map<String, String> parameters) {
        MBeanServer mBeanServer = getMBeanServer(jmxConfigDao, address, parameters);
        if (mBeanServer != null) {
            return new HashMap<>(mBeanServer.getParameterMap());
        }
        return Collections.emptyMap();
    }

    private JmxUtils() {

    }
}
