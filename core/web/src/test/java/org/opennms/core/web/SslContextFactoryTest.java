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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

import org.junit.Test;

public class SslContextFactoryTest {

    @Test
    public void canBuildFromKeyStoreAndTrustStore() throws Exception {
        assertNotNull(SslContextFactory.buildSslContext(
                tlsResource("client.p12"), "PKCS12", "client-store-pw", null,
                tlsResource("client-truststore.p12"), "PKCS12", "client-trust-pw"));
    }

    @Test
    public void canBuildWithDefaultsWhenNoStoresAreGiven() throws Exception {
        assertNotNull(SslContextFactory.buildSslContext(null, null, null, null, null, null, null));
    }

    @Test
    public void keyStoreTypeDefaultsToPkcs12() throws Exception {
        assertNotNull(SslContextFactory.buildSslContext(
                tlsResource("client.p12"), null, "client-store-pw", null,
                null, null, null));
    }

    @Test
    public void canBuildFromJksKeyStoreWithSeparateKeyPassword() throws Exception {
        assertNotNull(SslContextFactory.buildSslContext(
                tlsResource("client.jks"), "JKS", "jks-store-pw", "jks-key-pw",
                null, null, null));
    }

    @Test
    public void failsOnWrongKeyPassword() throws Exception {
        try {
            SslContextFactory.buildSslContext(tlsResource("client.jks"), "JKS", "jks-store-pw", "wrong-key-pw", null, null, null);
            fail("Expected the private key to fail to load");
        } catch (IOException | GeneralSecurityException e) {
            // expected
        }
    }

    @Test(expected = IOException.class)
    public void failsOnMissingKeyStore() throws Exception {
        SslContextFactory.buildSslContext(tlsResource("no-such-store.p12"), null, "secret", null, null, null, null);
    }

    @Test
    public void failsOnWrongKeyStorePassword() throws Exception {
        try {
            SslContextFactory.buildSslContext(tlsResource("client.p12"), null, "wrong-password", null, null, null, null);
            fail("Expected the keystore to fail to load");
        } catch (IOException | GeneralSecurityException e) {
            // expected: the JDK reports a bad keystore password as an IOException
        }
    }

    private static String tlsResource(final String filename) {
        return Paths.get("src", "test", "resources", "tls", filename).toAbsolutePath().toString();
    }
}
