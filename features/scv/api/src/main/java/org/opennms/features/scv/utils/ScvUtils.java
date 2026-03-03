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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

public class ScvUtils {
    public static final Logger LOG = LoggerFactory.getLogger(ScvUtils.class);
    public final static String SCV_KEYSTORE_TYPE_PROPERTY = "org.opennms.features.scv.keystore.type";
    public static final String KEYSTORE_KEY_PROPERTY = "org.opennms.features.scv.jceks.key";
    public static final String OPENNMS_HOME_PROPERTY = "opennms.home";
    public static final String OPENNMS_HOME_ENV = "OPENNMS_HOME";
    public static final String OPENNMS_PROPERTIES_D_NAME = "opennms.properties.d";
    public static final String OPENNMS_PROPERTIES_NAME = "opennms.properties";
    public static final String SCV_KEY_FILENAME = "scv.key";
    public static final String SCV_SALT_FILENAME = "scv.salt";
    public static final byte[] DEFAULT_SALT = new byte[]{0x0, 0xd, 0xd, 0xb, 0xa, 0x1, 0x1};
    private static final int SALT_LENGTH = 16;

    /**
     * Resolves the OpenNMS home directory from system property or environment variable.
     *
     * @return the OpenNMS home path, or null if not set
     */
    public static String resolveOpennmsHome() {
        String home = System.getProperty(OPENNMS_HOME_PROPERTY);
        if (home != null && !home.isEmpty()) {
            return home;
        }
        home = System.getenv(OPENNMS_HOME_ENV);
        if (home != null && !home.isEmpty()) {
            return home;
        }
        return null;
    }

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

        // Check dedicated key file first ($OPENNMS_HOME/etc/scv.key)
        String keyFromFile = readKeyFromFile(opennmsHome);
        if (keyFromFile != null) {
            onmsProperties.setProperty(KEYSTORE_KEY_PROPERTY, keyFromFile);
        }

        // Then check system properties (may already be loaded by Karaf/Spring)
        String keyStoreType = System.getProperty(SCV_KEYSTORE_TYPE_PROPERTY);
        String keyStoreKey = System.getProperty(KEYSTORE_KEY_PROPERTY);
        if (keyStoreType != null && !keyStoreType.isEmpty()) {
            onmsProperties.setProperty(SCV_KEYSTORE_TYPE_PROPERTY, keyStoreType);
        }
        if (keyStoreKey != null && !keyStoreKey.isEmpty()
                && !onmsProperties.containsKey(KEYSTORE_KEY_PROPERTY)) {
            onmsProperties.setProperty(KEYSTORE_KEY_PROPERTY, keyStoreKey);
        }

        // Fall back to properties files for anything still missing.
        // During scvcli or early bootstrap, system properties from these files
        // may not have been loaded into the JVM yet.
        if (!onmsProperties.containsKey(SCV_KEYSTORE_TYPE_PROPERTY) ||
                !onmsProperties.containsKey(KEYSTORE_KEY_PROPERTY)) {
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

    /**
     * Reads the SCV keystore password from the dedicated key file.
     * The file should contain just the password (whitespace-trimmed).
     *
     * @param opennmsHome path to the OpenNMS home directory
     * @return the password, or null if the file does not exist or is empty
     */
    public static String readKeyFromFile(String opennmsHome) {
        if (opennmsHome == null || opennmsHome.isEmpty()) {
            return null;
        }
        Path keyFile = Path.of(opennmsHome, "etc", SCV_KEY_FILENAME);
        if (!Files.isRegularFile(keyFile)) {
            return null;
        }
        try {
            String content = Files.readString(keyFile).trim();
            if (content.isEmpty()) {
                LOG.warn("SCV key file exists but is empty: {}", keyFile);
                return null;
            }
            LOG.info("Loaded SCV keystore password from dedicated key file: {}", keyFile);
            return content;
        } catch (IOException e) {
            LOG.warn("Failed to read SCV key file: {}", keyFile, e);
            return null;
        }
    }

    /**
     * Writes the SCV keystore password to the dedicated key file.
     *
     * @param opennmsHome path to the OpenNMS home directory
     * @param password the password to write
     * @throws IOException if writing fails
     */
    public static void writeKeyToFile(String opennmsHome, String password) throws IOException {
        Path keyFile = Path.of(opennmsHome, "etc", SCV_KEY_FILENAME);
        writeRestrictedFile(keyFile, password);
    }

    /**
     * Clears the SCV key from system properties to prevent runtime access
     * by other code in the JVM.
     */
    public static void clearKeyFromSystemProperties() {
        System.clearProperty(KEYSTORE_KEY_PROPERTY);
    }

    /**
     * Resolves the PBE salt from the dedicated salt file.
     * Falls back to the hardcoded default salt if the file does not exist.
     *
     * @param opennmsHome path to the OpenNMS home directory
     * @return the salt bytes
     */
    public static byte[] resolveSalt(String opennmsHome) {
        if (opennmsHome == null || opennmsHome.isEmpty()) {
            return DEFAULT_SALT.clone();
        }
        Path saltFile = Path.of(opennmsHome, "etc", SCV_SALT_FILENAME);
        if (!Files.isRegularFile(saltFile)) {
            return DEFAULT_SALT.clone();
        }
        try {
            String hex = Files.readString(saltFile).trim();
            if (hex.isEmpty()) {
                LOG.warn("SCV salt file exists but is empty: {}, using default salt", saltFile);
                return DEFAULT_SALT.clone();
            }
            return HexFormat.of().parseHex(hex);
        } catch (IOException | IllegalArgumentException e) {
            LOG.warn("Failed to read SCV salt file: {}, using default salt", saltFile, e);
            return DEFAULT_SALT.clone();
        }
    }

    /**
     * Generates a random 16-byte salt, saves it to the salt file, and returns it.
     *
     * @param opennmsHome path to the OpenNMS home directory
     * @return the generated salt bytes
     * @throws IOException if writing the salt file fails
     */
    public static byte[] generateAndSaveSalt(String opennmsHome) throws IOException {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        writeSaltToFile(opennmsHome, salt);
        return salt;
    }

    /**
     * Writes the salt bytes as hex to the salt file.
     *
     * @param opennmsHome path to the OpenNMS home directory
     * @param salt the salt bytes to write
     * @throws IOException if writing fails
     */
    public static void writeSaltToFile(String opennmsHome, byte[] salt) throws IOException {
        Path saltFile = Path.of(opennmsHome, "etc", SCV_SALT_FILENAME);
        writeRestrictedFile(saltFile, HexFormat.of().formatHex(salt));
    }

    private static final Set<PosixFilePermission> OWNER_ONLY_PERMISSIONS =
            PosixFilePermissions.fromString("rw-------");

    /**
     * Writes content to a file with owner-only (600) permissions.
     * The file owner is set to match the parent directory owner (typically the opennms user),
     * so the file remains accessible to the OpenNMS service regardless of who runs scvcli.
     */
    private static void writeRestrictedFile(Path file, String content) throws IOException {
        Files.writeString(file, content);
        try {
            Files.setPosixFilePermissions(file, OWNER_ONLY_PERMISSIONS);

            // Match owner to the parent directory (etc/) which is owned by the opennms user.
            // This ensures that even if scvcli is run as root, the file is readable by the
            // OpenNMS service user on next startup.
            Path parentDir = file.getParent();
            if (parentDir != null) {
                FileOwnerAttributeView parentOwnerView = Files.getFileAttributeView(
                        parentDir, FileOwnerAttributeView.class);
                FileOwnerAttributeView fileOwnerView = Files.getFileAttributeView(
                        file, FileOwnerAttributeView.class);
                if (parentOwnerView != null && fileOwnerView != null) {
                    UserPrincipal parentOwner = parentOwnerView.getOwner();
                    fileOwnerView.setOwner(parentOwner);
                }
            }
        } catch (UnsupportedOperationException e) {
            // Non-POSIX filesystem (e.g., Windows) — skip permission/ownership setting
            LOG.warn("Cannot set POSIX file permissions on {}: {}", file, e.getMessage());
        }
    }

    private static final int RANDOM_PASSWORD_LENGTH = 32;
    private static final String PASSWORD_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Generates a cryptographically secure random password.
     *
     * @return a 32-character random alphanumeric password
     */
    public static String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(RANDOM_PASSWORD_LENGTH);
        for (int i = 0; i < RANDOM_PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
