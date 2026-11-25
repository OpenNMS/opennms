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
package org.opennms.core.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * The fixtures in src/test/resources/pem were made with OpenSSL 3:
 * <pre>
 * openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out rsa-pkcs8.key
 * openssl rsa -in rsa-pkcs8.key -traditional -out rsa-pkcs1.key
 * openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-256 -out ec-pkcs8.key
 * openssl ec -in ec-pkcs8.key -out ec-sec1.key
 * openssl genpkey -algorithm ED25519 -out ed25519-pkcs8.key
 * openssl pkcs8 -topk8 -in rsa-pkcs8.key -passout pass:secret -out rsa-pkcs8-encrypted.key
 * openssl rsa -in rsa-pkcs8.key -traditional -aes256 -passout pass:secret -out rsa-pkcs1-encrypted.key
 * openssl req -x509 -key rsa-pkcs8.key -days 36500 -subj /CN=localhost -out rsa.crt
 * openssl req -x509 -key ec-pkcs8.key -days 36500 -subj /CN=localhost -out ec.crt
 * cat rsa.crt ec.crt > chain.crt
 * openssl x509 -in rsa.crt -trustout -addtrust serverAuth -out rsa-trusted.crt
 * </pre>
 */
public class PemKeysTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void readsRsaPkcs8() throws Exception {
        assertKeyMatches("rsa-pkcs8.key", "rsa.crt", "SHA256withRSA");
    }

    @Test
    public void readsRsaPkcs1() throws Exception {
        assertKeyMatches("rsa-pkcs1.key", "rsa.crt", "SHA256withRSA");
        assertArrayEquals(PemKeys.readPrivateKey(pem("rsa-pkcs8.key")).getEncoded(),
                PemKeys.readPrivateKey(pem("rsa-pkcs1.key")).getEncoded());
    }

    @Test
    public void readsEcPkcs8() throws Exception {
        assertKeyMatches("ec-pkcs8.key", "ec.crt", "SHA256withECDSA");
    }

    @Test
    public void readsEcSec1() throws Exception {
        assertKeyMatches("ec-sec1.key", "ec.crt", "SHA256withECDSA");
    }

    @Test
    public void readsEd25519Pkcs8() throws Exception {
        final PrivateKey key = PemKeys.readPrivateKey(pem("ed25519-pkcs8.key"));
        assertTrue(key.getAlgorithm(), key.getAlgorithm().startsWith("Ed"));
    }

    @Test
    public void rejectsEncryptedPkcs8() throws Exception {
        assertRejected(pem("rsa-pkcs8-encrypted.key"), "encrypted");
    }

    @Test
    public void rejectsEncryptedPkcs1() throws Exception {
        assertRejected(pem("rsa-pkcs1-encrypted.key"), "encrypted");
    }

    @Test
    public void rejectsFileWithoutKey() throws Exception {
        assertRejected(pem("rsa.crt"), "does not contain a PEM private key");
    }

    @Test
    public void rejectsUnsupportedKeyTypeNamingTheSupportedFormats() throws Exception {
        final File file = write("id_ed25519", "-----BEGIN OPENSSH PRIVATE KEY-----\nb3BlbnNzaC1rZXktdjEAAAAA\n-----END OPENSSH PRIVATE KEY-----\n");
        assertRejected(file, "Unsupported key type 'OPENSSH PRIVATE KEY'");
        assertRejected(file, "BEGIN RSA PRIVATE KEY");
    }

    @Test
    public void rejectsBase64WithAnInvalidCharacterOfAValidLength() throws Exception {
        // Without the '!' this is valid base64, which the MIME decoder would quietly accept.
        final File file = write("bang.key", "-----BEGIN PRIVATE KEY-----\nMIIB!AAAA\n-----END PRIVATE KEY-----\n");
        assertRejected(file, "not valid base64");
    }

    @Test
    public void rejectsGarbageInKeyBlock() throws Exception {
        final File file = write("bad.key", "-----BEGIN RSA PRIVATE KEY-----\nAAECAwQ=\n-----END RSA PRIVATE KEY-----\n");
        assertRejected(file, "Could not read");
    }

    @Test
    public void findsKeyAndCertificateInCombinedFile() throws Exception {
        final File combined = write("combined.pem", "subject=CN=localhost\n" + read("rsa.crt") + read("rsa-pkcs1.key"));
        assertArrayEquals(PemKeys.readPrivateKey(pem("rsa-pkcs8.key")).getEncoded(),
                PemKeys.readPrivateKey(combined).getEncoded());
        assertEquals(1, PemKeys.readCertificates(combined).length);
    }

    @Test
    public void readsCertificateChainInOrder() throws Exception {
        final X509Certificate[] chain = PemKeys.readCertificates(pem("chain.crt"));
        assertEquals(2, chain.length);
        assertEquals(PemKeys.readCertificates(pem("rsa.crt"))[0], chain[0]);
        assertEquals(PemKeys.readCertificates(pem("ec.crt"))[0], chain[1]);
    }

    @Test
    public void rejectsBadBase64InKeyBlock() throws Exception {
        final File file = write("bad64.key", "-----BEGIN PRIVATE KEY-----\n!!not base64!!\n-----END PRIVATE KEY-----\n");
        assertRejected(file, "not valid base64");
    }

    @Test
    public void rejectsOversizedDerLength() throws Exception {
        // SEQUENCE with a 4-byte length close to Integer.MAX_VALUE, which overflows pos + length.
        final String body = Base64.getEncoder().encodeToString(new byte[] {0x30, (byte) 0x84, 0x7F, (byte) 0xFF, (byte) 0xFF, (byte) 0xF0, 0x02, 0x01, 0x00});
        final File file = write("huge.key", "-----BEGIN RSA PRIVATE KEY-----\n" + body + "\n-----END RSA PRIVATE KEY-----\n");
        assertRejected(file, "DER length exceeds the data");
    }

    @Test
    public void readsTrustedAndX509CertificateLabels() throws Exception {
        final X509Certificate expected = PemKeys.readCertificates(pem("rsa.crt"))[0];
        assertEquals(expected, PemKeys.readCertificates(pem("rsa-trusted.crt"))[0]);
        final File x509 = write("x509.crt", read("rsa.crt").replace("BEGIN CERTIFICATE", "BEGIN X509 CERTIFICATE")
                .replace("END CERTIFICATE", "END X509 CERTIFICATE"));
        assertEquals(expected, PemKeys.readCertificates(x509)[0]);
    }

    @Test
    public void rejectsBadBase64InCertificateBlock() throws Exception {
        final File file = write("bad64.crt", "-----BEGIN CERTIFICATE-----\n!!not base64!!\n-----END CERTIFICATE-----\n");
        try {
            PemKeys.readCertificates(file);
            fail("expected " + file + " to be rejected");
        } catch (final CertificateException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("not valid base64"));
        }
    }

    @Test(expected = CertificateException.class)
    public void rejectsFileWithoutCertificate() throws Exception {
        PemKeys.readCertificates(pem("rsa-pkcs8.key"));
    }

    private static void assertKeyMatches(final String keyFile, final String certFile, final String signatureAlgorithm) throws Exception {
        final PrivateKey key = PemKeys.readPrivateKey(pem(keyFile));
        final X509Certificate cert = PemKeys.readCertificates(pem(certFile))[0];
        final byte[] data = "syslog".getBytes(StandardCharsets.US_ASCII);

        final Signature signer = Signature.getInstance(signatureAlgorithm);
        signer.initSign(key);
        signer.update(data);
        final byte[] signature = signer.sign();

        final Signature verifier = Signature.getInstance(signatureAlgorithm);
        verifier.initVerify(cert.getPublicKey());
        verifier.update(data);
        assertTrue("key in " + keyFile + " does not match " + certFile, verifier.verify(signature));
    }

    private static void assertRejected(final File file, final String messagePart) throws Exception {
        try {
            PemKeys.readPrivateKey(file);
            fail("expected " + file + " to be rejected");
        } catch (final InvalidKeySpecException e) {
            assertTrue(e.getMessage(), e.getMessage().contains(messagePart));
        }
    }

    private static File pem(final String name) throws Exception {
        return new File(PemKeysTest.class.getResource("/pem/" + name).toURI());
    }

    private static String read(final String name) throws Exception {
        return new String(Files.readAllBytes(pem(name).toPath()), StandardCharsets.US_ASCII);
    }

    private File write(final String name, final String content) throws Exception {
        final File file = tmp.newFile(name);
        Files.write(file.toPath(), content.getBytes(StandardCharsets.US_ASCII));
        return file;
    }
}
