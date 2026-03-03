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
package org.opennms.features.scv.jceks;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.scv.utils.ScvUtils;

public class ScvUtilsTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private String opennmsHome;

    @Before
    public void setUp() throws IOException {
        opennmsHome = tempFolder.getRoot().getAbsolutePath();
        tempFolder.newFolder("etc");
    }

    @After
    public void tearDown() {
        System.clearProperty(ScvUtils.KEYSTORE_KEY_PROPERTY);
        System.clearProperty(ScvUtils.SCV_KEYSTORE_TYPE_PROPERTY);
    }

    // --- readKeyFromFile tests ---

    @Test
    public void testReadKeyFromFile_fileExists() throws IOException {
        Files.writeString(Path.of(opennmsHome, "etc", ScvUtils.SCV_KEY_FILENAME), "mySecretKey123");
        assertEquals("mySecretKey123", ScvUtils.readKeyFromFile(opennmsHome));
    }

    @Test
    public void testReadKeyFromFile_fileDoesNotExist() {
        assertNull(ScvUtils.readKeyFromFile(opennmsHome));
    }

    @Test
    public void testReadKeyFromFile_emptyFile() throws IOException {
        Files.writeString(Path.of(opennmsHome, "etc", ScvUtils.SCV_KEY_FILENAME), "");
        assertNull(ScvUtils.readKeyFromFile(opennmsHome));
    }

    @Test
    public void testReadKeyFromFile_whitespaceOnly() throws IOException {
        Files.writeString(Path.of(opennmsHome, "etc", ScvUtils.SCV_KEY_FILENAME), "  \n\t  ");
        assertNull(ScvUtils.readKeyFromFile(opennmsHome));
    }

    @Test
    public void testReadKeyFromFile_trimmed() throws IOException {
        Files.writeString(Path.of(opennmsHome, "etc", ScvUtils.SCV_KEY_FILENAME), "  myKey  \n");
        assertEquals("myKey", ScvUtils.readKeyFromFile(opennmsHome));
    }

    @Test
    public void testReadKeyFromFile_nullHome() {
        assertNull(ScvUtils.readKeyFromFile(null));
    }

    // --- writeKeyToFile tests ---

    @Test
    public void testWriteKeyToFile() throws IOException {
        ScvUtils.writeKeyToFile(opennmsHome, "newPassword");
        String content = Files.readString(Path.of(opennmsHome, "etc", ScvUtils.SCV_KEY_FILENAME));
        assertEquals("newPassword", content);
    }

    // --- clearKeyFromSystemProperties tests ---

    @Test
    public void testClearKeyFromSystemProperties() {
        System.setProperty(ScvUtils.KEYSTORE_KEY_PROPERTY, "testKey");
        assertEquals("testKey", System.getProperty(ScvUtils.KEYSTORE_KEY_PROPERTY));

        ScvUtils.clearKeyFromSystemProperties();
        assertNull(System.getProperty(ScvUtils.KEYSTORE_KEY_PROPERTY));
    }

    // --- loadScvProperties priority tests ---

    @Test
    public void testLoadScvProperties_keyFileTakesPriority() throws IOException {
        // Set both key file and system property with different values
        Files.writeString(Path.of(opennmsHome, "etc", ScvUtils.SCV_KEY_FILENAME), "fromKeyFile");
        System.setProperty(ScvUtils.KEYSTORE_KEY_PROPERTY, "fromSystemProperty");

        Properties props = ScvUtils.loadScvProperties(opennmsHome);
        assertEquals("fromKeyFile", props.getProperty(ScvUtils.KEYSTORE_KEY_PROPERTY));
    }

    @Test
    public void testLoadScvProperties_systemPropertyFallback() {
        // No key file, system property set
        System.setProperty(ScvUtils.KEYSTORE_KEY_PROPERTY, "fromSystemProperty");

        Properties props = ScvUtils.loadScvProperties(opennmsHome);
        assertEquals("fromSystemProperty", props.getProperty(ScvUtils.KEYSTORE_KEY_PROPERTY));
    }

    @Test
    public void testLoadScvProperties_propertiesFileFallback() throws IOException {
        // No key file, no system property, but opennms.properties exists
        File propsFile = new File(opennmsHome, "etc/opennms.properties");
        Files.writeString(propsFile.toPath(), ScvUtils.KEYSTORE_KEY_PROPERTY + "=fromPropsFile\n");

        Properties props = ScvUtils.loadScvProperties(opennmsHome);
        assertEquals("fromPropsFile", props.getProperty(ScvUtils.KEYSTORE_KEY_PROPERTY));
    }

    // --- resolveSalt tests ---

    @Test
    public void testResolveSalt_fileExists() throws IOException {
        byte[] expectedSalt = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10};
        Files.writeString(Path.of(opennmsHome, "etc", ScvUtils.SCV_SALT_FILENAME),
                HexFormat.of().formatHex(expectedSalt));

        assertArrayEquals(expectedSalt, ScvUtils.resolveSalt(opennmsHome));
    }

    @Test
    public void testResolveSalt_fileMissing_returnsDefault() {
        assertArrayEquals(ScvUtils.DEFAULT_SALT, ScvUtils.resolveSalt(opennmsHome));
    }

    @Test
    public void testResolveSalt_emptyFile_returnsDefault() throws IOException {
        Files.writeString(Path.of(opennmsHome, "etc", ScvUtils.SCV_SALT_FILENAME), "");
        assertArrayEquals(ScvUtils.DEFAULT_SALT, ScvUtils.resolveSalt(opennmsHome));
    }

    @Test
    public void testResolveSalt_nullHome_returnsDefault() {
        assertArrayEquals(ScvUtils.DEFAULT_SALT, ScvUtils.resolveSalt(null));
    }

    // --- generateAndSaveSalt tests ---

    @Test
    public void testGenerateAndSaveSalt() throws IOException {
        byte[] salt = ScvUtils.generateAndSaveSalt(opennmsHome);

        assertNotNull(salt);
        assertEquals(16, salt.length);

        // Verify file was written
        String hex = Files.readString(Path.of(opennmsHome, "etc", ScvUtils.SCV_SALT_FILENAME));
        assertArrayEquals(salt, HexFormat.of().parseHex(hex));
    }

    @Test
    public void testGenerateAndSaveSalt_isRandom() throws IOException {
        byte[] salt1 = ScvUtils.generateAndSaveSalt(opennmsHome);
        byte[] salt2 = ScvUtils.generateAndSaveSalt(opennmsHome);

        // Two generated salts should be different (extremely unlikely to collide)
        boolean different = false;
        for (int i = 0; i < salt1.length; i++) {
            if (salt1[i] != salt2[i]) {
                different = true;
                break;
            }
        }
        assertEquals("Generated salts should be different", true, different);
    }
}
