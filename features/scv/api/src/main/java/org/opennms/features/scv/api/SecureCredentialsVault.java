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
package org.opennms.features.scv.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public interface SecureCredentialsVault {

    public enum KeyStoreType {
        JCEKS,
        PKCS12;

        /**
         * Returns the KeyStoreType from the system property {@code SCV_KEYSTORE_PROPERTY}.
         * <p>
         * If the system property is not set or contains a value that does not match any
         * of the defined KeyStoreType enums, the default value {@code JCEKS} will be returned.
         *
         * @return the resolved KeyStoreType, or {@code JCEKS} if invalid or unspecified
         */
        public static KeyStoreType fromSystemProperty() {
            return Arrays.stream(values())
                    .filter(t -> t.name().equalsIgnoreCase(System.getProperty(SCV_KEYSTORE_PROPERTY, "JCEKS").toUpperCase()))
                    .findFirst()
                    .orElse(JCEKS);
        }
    }
    public static final Logger LOG = LoggerFactory.getLogger(SecureCredentialsVault.class);
    public final static String SCV_KEYSTORE_PROPERTY = "org.opennms.features.scv.jceks.keystore.type";
    public final static Properties onmsProperties = new Properties();
    public static final String OPENNMS_PROPERTIES_D_NAME = "opennms.properties.d";
    public static final String OPENNMS_PROPERTIES_NAME = "opennms.properties";
    public static final String KEYSTORE_KEY_PROPERTY = "org.opennms.features.scv.jceks.key";
    public static final String DEFAULT_KEYSTORE_KEY = "QqSezYvBtk2gzrdpggMHvt5fJGWCdkRw";

    public static void loadScvProperties(String opennmsHome) {

        if (opennmsHome != null && !opennmsHome.isEmpty()) {

            loadProperties(Path.of(opennmsHome, "etc", OPENNMS_PROPERTIES_NAME).toString());
            loadProperties(Path.of(opennmsHome, "etc", OPENNMS_PROPERTIES_D_NAME).toString());

            if (onmsProperties.containsKey(SCV_KEYSTORE_PROPERTY)) {
                System.setProperty(SCV_KEYSTORE_PROPERTY, onmsProperties.getProperty(SCV_KEYSTORE_PROPERTY));
            }

            if (onmsProperties.containsKey(KEYSTORE_KEY_PROPERTY)) {
                System.setProperty(KEYSTORE_KEY_PROPERTY, onmsProperties.getProperty(KEYSTORE_KEY_PROPERTY, DEFAULT_KEYSTORE_KEY));
            }
        }
    }

    private static void loadProperties(String path) {
        File fileOrDir = new File(path);

        if (!fileOrDir.exists()) {
            LOG.info(" Path does not exist: " + path);
            return;
        }

        if (fileOrDir.isFile() && path.endsWith(".properties")) {
            loadSingleFile(fileOrDir);
        } else if (fileOrDir.isDirectory()) {
            Optional.ofNullable(fileOrDir.listFiles((dir, name) -> name.endsWith(".properties")))
                    .map(Arrays::stream)
                    .orElse(Stream.empty())
                    .forEach(SecureCredentialsVault::loadSingleFile);
        } else {
            LOG.info(" Not a valid .properties file or directory: " + path);
        }
    }

    private static  void loadSingleFile(File file) {
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

    Set<String> getAliases();

    Credentials getCredentials(String alias);

    void setCredentials(String alias, Credentials credentials);

    void deleteCredentials(String alias);

}
