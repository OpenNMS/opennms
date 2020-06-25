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

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.core.config.api.TextEncryptor;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/META-INF/opennms/applicationContext-encrypt-util.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(tempDbClass = MockDatabase.class)
public class TextEncryptorTest {

    @Autowired
    private TextEncryptor textEncryptor;

    @Autowired
    private JsonStore jsonStore;

    @Test
    public void testEncryption() {
        // No key,value pairs exist initially
        Map<String, String> keyValuePair = jsonStore.enumerateContext("snmp-config");
        assertEquals(keyValuePair.size(), 0);
        // Encrypt and decrypt
        String textToEncrypt = "OpenNMS";
        String encrypted = textEncryptor.encrypt("snmp-config", null, textToEncrypt);
        String result = textEncryptor.decrypt("snmp-config", null, encrypted);
        assertEquals(textToEncrypt, result);
        // Should have created one key.
        keyValuePair = jsonStore.enumerateContext("snmp-config");
        assertEquals(keyValuePair.size(), 1);
        // Try different text to encrypt.
        textToEncrypt = "Minion-Sentinel";
        encrypted = textEncryptor.encrypt("snmp-config", null, textToEncrypt);
        result = textEncryptor.decrypt("snmp-config", null, encrypted);
        assertEquals(textToEncrypt, result);
        // Should have used the same key.
        keyValuePair = jsonStore.enumerateContext("snmp-config");
        assertEquals(keyValuePair.size(), 1);
        // Specify key.
        encrypted = textEncryptor.encrypt("snmp-config", "someKeyToEncrypt", textToEncrypt);
        result = textEncryptor.decrypt("snmp-config", "someKeyToEncrypt", encrypted);
        assertEquals(textToEncrypt, result);
        // Doesn't add any new key.
        keyValuePair = jsonStore.enumerateContext("snmp-config");
        assertEquals(keyValuePair.size(), 1);
        // Use different context.
        encrypted = textEncryptor.encrypt("syslog-config", "someKeyToEncrypt", textToEncrypt);
        result = textEncryptor.decrypt("syslog-config", "someKeyToEncrypt", encrypted);
        assertEquals(textToEncrypt, result);
        // Doesn't add any new key.
        keyValuePair = jsonStore.enumerateContext("syslog-config");
        assertEquals(keyValuePair.size(), 0);
        // Use previous context but with empty key
        encrypted = textEncryptor.encrypt("syslog-config", "", textToEncrypt);
        result = textEncryptor.decrypt("syslog-config", "", encrypted);
        assertEquals(textToEncrypt, result);
        // should have added new key
        keyValuePair = jsonStore.enumerateContext("syslog-config");
        assertEquals(keyValuePair.size(), 1);
    }
}
