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
package org.opennms.core.web;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

/**
 * Builds {@link SSLContext} instances from keystore/truststore files, for use
 * with HTTPS endpoints that require a custom trust anchor (private CA,
 * self-signed certificate) and/or a client certificate (mutual TLS).
 */
public abstract class SslContextFactory {

    public static final String DEFAULT_KEY_STORE_TYPE = "PKCS12";

    /**
     * Build an {@link SSLContext} from the given keystore (client key material) and
     * truststore (server trust anchors). Either may be null: a null keystore results
     * in no client certificate being presented, and a null truststore falls back to
     * the JVM's default trust anchors.
     *
     * @param keyStorePath path to the keystore containing the client certificate and private key, or null
     * @param keyStoreType keystore type (e.g. PKCS12, JKS), defaults to PKCS12 when null
     * @param keyStorePassword password for the keystore, or null
     * @param keyPassword password for the private key, defaults to the keystore password when null
     * @param trustStorePath path to the truststore containing trusted server/CA certificates, or null
     * @param trustStoreType truststore type (e.g. PKCS12, JKS), defaults to PKCS12 when null
     * @param trustStorePassword password for the truststore, or null
     */
    public static SSLContext buildSslContext(final String keyStorePath, final String keyStoreType, final String keyStorePassword, final String keyPassword,
            final String trustStorePath, final String trustStoreType, final String trustStorePassword) throws GeneralSecurityException, IOException {
        final SSLContextBuilder builder = SSLContexts.custom();
        if (keyStorePath != null) {
            final KeyStore keyStore = loadKeyStore(keyStorePath, keyStoreType, keyStorePassword);
            final String effectiveKeyPassword = keyPassword != null ? keyPassword : keyStorePassword;
            builder.loadKeyMaterial(keyStore, effectiveKeyPassword != null ? effectiveKeyPassword.toCharArray() : null);
        }
        if (trustStorePath != null) {
            final KeyStore trustStore = loadKeyStore(trustStorePath, trustStoreType, trustStorePassword);
            builder.loadTrustMaterial(trustStore, null);
        }
        return builder.build();
    }

    private static KeyStore loadKeyStore(final String path, final String type, final String password) throws GeneralSecurityException, IOException {
        final KeyStore keyStore = KeyStore.getInstance(type != null ? type : DEFAULT_KEY_STORE_TYPE);
        try (InputStream is = new FileInputStream(path)) {
            keyStore.load(is, password != null ? password.toCharArray() : null);
        }
        return keyStore;
    }
}
