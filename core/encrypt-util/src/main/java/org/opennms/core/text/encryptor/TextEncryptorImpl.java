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

import java.util.Optional;
import java.util.UUID;

import org.jasypt.util.text.AES256TextEncryptor;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.core.config.api.TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.gson.Gson;


public class TextEncryptorImpl implements TextEncryptor {

    private static final Logger LOG = LoggerFactory.getLogger(TextEncryptorImpl.class);
    private final JsonStore jsonStore;
    private final Gson m_gson = new Gson();

    public TextEncryptorImpl(JsonStore jsonStore) {
        this.jsonStore = jsonStore;
    }

    @Override
    public String encrypt(String context, String key, String text) {
        try {
            final AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
            String password = getPasswordToEncrypt(context, key);
            textEncryptor.setPassword(password);
            return textEncryptor.encrypt(text);
        } catch (Exception e) {
            LOG.error("Exception while encrypting {} with key {}", key);
        }
        return text;

    }

    @Override
    public String decrypt(String context, String key, String encrypted) {
        final AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
        try {
            String password = getPasswordToDecrypt(context, key);
            if (!Strings.isNullOrEmpty(password)) {
                textEncryptor.setPassword(password);
                return textEncryptor.decrypt(encrypted);
            }
        } catch (Exception e) {
            LOG.error("Exception while decrypting {} with key {}", encrypted, key);
        }
        return encrypted;
    }

    // For encryption, create a new password if nothing is present in json store.
    private String getPasswordToEncrypt(String context, String key) {
        String password = key;
        if (Strings.isNullOrEmpty(key)) {
            key = context;
            Optional<String> optionalValue = jsonStore.get(key, context);
            if (optionalValue.isPresent()) {
                password = m_gson.fromJson(optionalValue.get(), String.class);
            } else {
                password = UUID.randomUUID().toString();
                String jsonString = m_gson.toJson(password);
                jsonStore.put(key, jsonString, context);
            }
        }
        return password;
    }

    // For decryption, try to get password if there is one.
    private String getPasswordToDecrypt(String context, String key) {
        String password = key;
        if (Strings.isNullOrEmpty(key)) {
            key = context;
            Optional<String> passwordInJson = jsonStore.get(key, context);
            if (passwordInJson.isPresent()) {
                password = m_gson.fromJson(passwordInJson.get(), String.class);
            }
        }
        return password;
    }
}
