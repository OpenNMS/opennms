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
package org.opennms.smoketest.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Base64;

/**
 * Generates self-signed TLS material for the HTTPS smoke tests using the
 * JDK's keytool, so no extra dependencies or binary keystores in the tree
 * are needed.
 */
public final class SelfSignedTlsHelper {

    public static final String PASSWORD = "changeit";
    private static final String ALIAS = "opennms-smoke";

    private SelfSignedTlsHelper() {
    }

    /**
     * Creates a PKCS12 keystore holding a fresh self-signed certificate.
     */
    public static Path generateKeystore(Path directory, String fileName) {
        try {
            Files.createDirectories(directory);
            final Path keystore = directory.resolve(fileName);
            Files.deleteIfExists(keystore);

            final Path keytool = Path.of(System.getProperty("java.home"), "bin", "keytool");
            final Process process = new ProcessBuilder(keytool.toString(),
                    "-genkeypair",
                    "-alias", ALIAS,
                    "-keyalg", "RSA",
                    "-keysize", "2048",
                    "-validity", "3650",
                    "-dname", "CN=opennms",
                    "-ext", "san=dns:opennms,dns:nginx,dns:localhost",
                    "-keystore", keystore.toString(),
                    "-storetype", "PKCS12",
                    "-storepass", PASSWORD,
                    "-keypass", PASSWORD)
                    .redirectErrorStream(true)
                    .start();
            final String output;
            try (InputStream is = process.getInputStream()) {
                final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                is.transferTo(buffer);
                output = buffer.toString(StandardCharsets.UTF_8);
            }
            if (process.waitFor() != 0) {
                throw new IllegalStateException("keytool failed: " + output);
            }
            return keystore;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate self-signed keystore", e);
        }
    }

    /**
     * Exports the certificate and (PKCS8) private key from the given PKCS12
     * keystore as PEM files, the format nginx expects.
     */
    public static void exportPem(Path keystore, Path certPem, Path keyPem) {
        try {
            final KeyStore ks = KeyStore.getInstance("PKCS12");
            try (InputStream is = Files.newInputStream(keystore)) {
                ks.load(is, PASSWORD.toCharArray());
            }
            final Certificate certificate = ks.getCertificate(ALIAS);
            final Key key = ks.getKey(ALIAS, PASSWORD.toCharArray());
            Files.writeString(certPem, toPem("CERTIFICATE", certificate.getEncoded()));
            Files.writeString(keyPem, toPem("PRIVATE KEY", key.getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException("Failed to export PEM files from " + keystore, e);
        }
    }

    private static String toPem(String type, byte[] der) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8)).encodeToString(der)
                + "\n-----END " + type + "-----\n";
    }
}
