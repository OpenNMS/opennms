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
import java.util.concurrent.ConcurrentHashMap;

import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jasypt.util.text.AES256TextEncryptor;
import org.opennms.core.config.api.TextEncryptor;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


public class TextEncryptorImpl implements TextEncryptor {

    private static final Logger LOG = LoggerFactory.getLogger(TextEncryptorImpl.class);
    private final SecureCredentialsVault secureCredentialsVault;
    private Map<String, Credentials> passwordsByAlias = new ConcurrentHashMap<>();

    public TextEncryptorImpl(SecureCredentialsVault secureCredentialsVault) {
        this.secureCredentialsVault = secureCredentialsVault;
    }

    @Override
    public String encrypt(String alias, String key, String text) {
        try {
            final AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
            String password = getPasswordFromCredentials(alias, key);
            if (!Strings.isNullOrEmpty(password)) {
                textEncryptor.setPassword(password);
                return textEncryptor.encrypt(text);
            }
        } catch (Exception e) {
            LOG.error("Exception while encrypting {} with key {}", text, key, e);
        }
        return text;

    }

    @Override
    public String decrypt(String alias, String key, String encrypted) {
        final AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        try {
            String password = getPasswordFromCredentials(alias, key);
            if (!Strings.isNullOrEmpty(password)) {
                textEncryptor.setPassword(password);
                return textEncryptor.decrypt(encrypted);
            }
        } catch (Exception e) {
            LOG.error("Exception while decrypting {} with key {}", encrypted, key, e);
        }
        return encrypted;
    }

    private String getPasswordFromCredentials(String alias, String key) {
        Credentials credentials = passwordsByAlias.get(alias);
        if (credentials == null) {
            credentials = secureCredentialsVault.getCredentials(alias);
            if (credentials == null) {
                return generateAndStorePassword(alias, key);
            } else {
                passwordsByAlias.put(alias, credentials);
            }
        }
        return credentials.getPassword();
    }

    // For encryption, create a new password in scv.
    private String generateAndStorePassword(String alias, String key) {
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String password = passwordEncryptor.encryptPassword(key);
        Credentials credentials = new Credentials(key, password);
        secureCredentialsVault.setCredentials(alias, credentials);
        passwordsByAlias.put(alias, credentials);
        return password;
    }
}
