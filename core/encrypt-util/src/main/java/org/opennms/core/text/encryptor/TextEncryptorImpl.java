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
