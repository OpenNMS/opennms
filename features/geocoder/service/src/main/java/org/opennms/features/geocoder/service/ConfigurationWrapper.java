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
package org.opennms.features.geocoder.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import org.osgi.service.cm.Configuration;

public class ConfigurationWrapper {

    private final Configuration configuration;

    public ConfigurationWrapper(Configuration configuration) {
        this.configuration = configuration;
    }

    public void update(Map<String, Object> newProperties) throws IOException {
        final Dictionary<String, Object> currentProperties = configuration.getProperties() == null ? new Hashtable<>() : configuration.getProperties();
        if (!Objects.equals(currentProperties, newProperties)) {
            applyProperties(currentProperties, newProperties);
            saveToDisk(configuration.getPid(), currentProperties);
            configuration.update(currentProperties);
        }
    }


    public void delete() throws IOException {
        final String configPid = configuration.getPid();
        configuration.delete();
        final Path configFile = getConfigFile(configPid);
        Files.deleteIfExists(configFile);
    }

    private static Path getConfigFile(final String configPID) {
        final Path configFile = Paths.get(System.getProperty("karaf.etc"), configPID + ".cfg");
        return configFile;
    }

    private static void saveToDisk(String configPID, Dictionary<String, Object> currentProperties) throws IOException {
        // Ensure file will be created if it does not yet exist
        if (currentProperties.get("felix.fileinstall.filename") == null) {
            final Path configFile = getConfigFile(configPID);
            final Properties persistentProperties = new Properties();
            final Enumeration<String> keyEnumerator = currentProperties.keys();
            while(keyEnumerator.hasMoreElements()) {
                final String key = keyEnumerator.nextElement();
                final Object value = currentProperties.get(key);
                persistentProperties.put(key, value == null ? value : value.toString());
            }
            persistentProperties.store(new FileOutputStream(configFile.toFile()), null);
        }
    }

    // Updates the currentProperties with values from newProperties. Deletes null values. Does not remove keys.
    private static void applyProperties(Dictionary<String, Object> currentProperties, Map<String, Object> newProperties) {
        newProperties.entrySet().forEach(e -> {
            if (e.getValue() == null) {
                currentProperties.remove(e.getKey());
            } else {
                currentProperties.put(e.getKey(), e.getValue().toString());
            }
        });
    }
}
