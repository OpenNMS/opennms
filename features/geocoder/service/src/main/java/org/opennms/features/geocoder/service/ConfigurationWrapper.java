/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
