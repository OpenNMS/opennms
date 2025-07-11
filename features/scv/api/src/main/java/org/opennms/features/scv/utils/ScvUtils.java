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
package org.opennms.features.scv.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class ScvUtils {
    public static final Logger LOG = LoggerFactory.getLogger(ScvUtils.class);
    public final static String SCV_KEYSTORE_TYPE_PROPERTY = "org.opennms.features.scv.keystore.type";
    public static final String KEYSTORE_KEY_PROPERTY = "org.opennms.features.scv.jceks.key";
    public static final String OPENNMS_PROPERTIES_D_NAME = "opennms.properties.d";
    public static final String OPENNMS_PROPERTIES_NAME = "opennms.properties";

    /**
     * Loads SCV-related properties from system properties first if not found in system properties,
     * then from properties files  under $OPENNMS_HOME/etc/opennms.properties.d
     * and $OPENNMS_HOME/etc/opennms.properties.
     *
     * @param opennmsHome The path to the OpenNMS home directory.
     * @return Properties containing SCV-related properties.
     */
    public static Properties loadScvProperties(String opennmsHome) {

        final Properties onmsProperties = new Properties();
        String keyStoreType = System.getProperty(SCV_KEYSTORE_TYPE_PROPERTY);
        String keyStoreKey = System.getProperty(KEYSTORE_KEY_PROPERTY);
        if (keyStoreType != null && !keyStoreType.isEmpty()) {
            onmsProperties.setProperty(SCV_KEYSTORE_TYPE_PROPERTY, keyStoreType);
        }
        if (keyStoreKey != null && !keyStoreKey.isEmpty()) {
            onmsProperties.setProperty(KEYSTORE_KEY_PROPERTY, keyStoreKey);
        }
        // Only load properties from files if both system properties are not set
        if ((keyStoreType == null || keyStoreType.isEmpty()) ||
                (keyStoreKey == null || keyStoreKey.isEmpty())) {
            if (opennmsHome != null && !opennmsHome.isEmpty()) {
                loadProperties(Path.of(opennmsHome, "etc", OPENNMS_PROPERTIES_D_NAME).toString(), onmsProperties);
                loadProperties(Path.of(opennmsHome, "etc", OPENNMS_PROPERTIES_NAME).toString(), onmsProperties);
            }
        }
        return onmsProperties;
    }

    private static void loadProperties(String path, Properties onmsProperties) {
        File fileOrDir = new File(path);

        if (!fileOrDir.exists()) {
            LOG.info(" Path does not exist: " + path);
            return;
        }

        if (fileOrDir.isFile() && path.endsWith(".properties")) {
            loadSingleFile(fileOrDir, onmsProperties);
        } else if (fileOrDir.isDirectory()) {
            Optional.ofNullable(fileOrDir.listFiles((dir, name) -> name.endsWith(".properties")))
                    .map(Arrays::stream)
                    .orElse(Stream.empty())
                    .forEach(file -> ScvUtils.loadSingleFile(file, onmsProperties));
        } else {
            LOG.info(" Not a valid .properties file or directory: " + path);
        }
    }

    private static void loadSingleFile(File file, Properties onmsProperties) {
        try (FileInputStream fis = new FileInputStream(file)) {
            Properties props = new Properties();
            props.load(fis);
            props.stringPropertyNames().stream()
                    .filter(key -> !onmsProperties.containsKey(key))
                    .forEach(key -> onmsProperties.setProperty(key, props.getProperty(key)));
        } catch (IOException e) {
            LOG.info("Failed to load properties from: " + file.getName());
        }
    }
}
