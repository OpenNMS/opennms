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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.jasypt.util.text.AES256TextEncryptor;
import org.opennms.core.config.api.TextEncryptor;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;


public class TextEncryptorImpl implements TextEncryptor {

    private final SecureCredentialsVault secureCredentialsVault;
    private Map<String, Credentials> passwordsByAlias = new ConcurrentHashMap<>();

    public TextEncryptorImpl(SecureCredentialsVault secureCredentialsVault) {
        this.secureCredentialsVault = secureCredentialsVault;
    }

    @Override
    public String encrypt(String alias, String text) {
        final AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        String password = getPasswordFromCredentials(alias);
        textEncryptor.setPassword(password);
        return textEncryptor.encrypt(text);
    }

    @Override
    public String decrypt(String alias, String encrypted) {
        final AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        String password = getPasswordFromCredentials(alias);
        textEncryptor.setPassword(password);
        return textEncryptor.decrypt(encrypted);

    }

    private String getPasswordFromCredentials(String alias) {
        Credentials credentials = passwordsByAlias.get(alias);
        if (credentials == null) {
            credentials = secureCredentialsVault.getCredentials(alias);
            if (credentials == null) {
                return generateAndStorePassword(alias);
            } else {
                passwordsByAlias.put(alias, credentials);
            }
        }
        return credentials.getPassword();
    }

    // For encryption, create a new password in scv.
    private String generateAndStorePassword(String alias) {
        String password = UUID.randomUUID().toString();
        Credentials credentials = new Credentials(alias, password);
        secureCredentialsVault.setCredentials(alias, credentials);
        passwordsByAlias.put(alias, credentials);
        return password;
    }
}
