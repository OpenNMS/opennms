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
package org.opennms.core.ipc.grpc.common;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.service.cm.ConfigurationAdmin;

public class ConfigUtils {

    public static Properties getPropertiesFromConfig(ConfigurationAdmin configAdmin, String pid) {
        Properties properties = new Properties();
        try {
            final Dictionary<String, Object> config = configAdmin.getConfiguration(pid).getProperties();
            if (config != null) {
                final Enumeration<String> keys = config.keys();
                while (keys.hasMoreElements()) {
                    final String key = keys.nextElement();
                    properties.put(key, config.get(key));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config", e);
        }
        return properties;
    }
}
