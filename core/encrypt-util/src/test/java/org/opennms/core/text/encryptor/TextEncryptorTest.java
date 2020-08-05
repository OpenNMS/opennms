/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
