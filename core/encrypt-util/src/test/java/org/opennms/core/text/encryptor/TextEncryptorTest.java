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
package org.opennms.core.text.encryptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.config.api.TextEncryptor;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;


public class TextEncryptorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testEncryption() {
        File keystoreFile = new File(tempFolder.getRoot(), "scv.jce");
        SecureCredentialsVault scv = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "notRealPassword");
        TextEncryptor textEncryptor = new TextEncryptorImpl(scv);
        // No credentials exist initially
        String alias = "snmp-config";
        Credentials credentials = scv.getCredentials(alias);
        assertNull(credentials);
        // Encrypt and decrypt
        String textToEncrypt = "OpenNMS";
        String encrypted = textEncryptor.encrypt(alias, textToEncrypt);
        String result = textEncryptor.decrypt(alias, encrypted);
        assertEquals(textToEncrypt, result);
        // Should have created one key.
        credentials = scv.getCredentials(alias);
        assertNotNull(credentials);
        // Try different text to encrypt.
        textToEncrypt = "Minion-Sentinel";
        encrypted = textEncryptor.encrypt(alias, textToEncrypt);
        result = textEncryptor.decrypt(alias, encrypted);
        assertEquals(textToEncrypt, result);
        // Use different alias.
        String alias2 = "syslog-config";
        credentials = scv.getCredentials(alias2);
        assertNull(credentials);
        encrypted = textEncryptor.encrypt(alias2, textToEncrypt);
        result = textEncryptor.decrypt(alias2, encrypted);
        assertEquals(textToEncrypt, result);
        credentials = scv.getCredentials(alias2);
        assertNotNull(credentials);
    }
}
