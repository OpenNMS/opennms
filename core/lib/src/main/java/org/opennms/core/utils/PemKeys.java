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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads PEM certificates and unencrypted PEM private keys with the JDK alone.
 *
 * Netty reads PKCS#1 ({@code BEGIN RSA PRIVATE KEY}) and SEC1 ({@code BEGIN EC PRIVATE KEY}) keys
 * only through BouncyCastle, which is not on the class path of OpenNMS Core and which Netty does
 * not find in Karaf. This class converts those keys to PKCS#8 so that the JDK key factories
 * can read them, and the caller can give Netty a {@link PrivateKey} instead of a key file.
 */
public final class PemKeys {

    private static final Pattern PEM_BLOCK = Pattern.compile(
            "-----BEGIN ([A-Z0-9 ]+)-----\\s*(.*?)-----END \\1-----", Pattern.DOTALL);

    private static final byte TAG_INTEGER = 0x02;
    private static final byte TAG_OCTET_STRING = 0x04;
    private static final byte TAG_NULL = 0x05;
    private static final byte TAG_OID = 0x06;
    private static final byte TAG_SEQUENCE = 0x30;
    private static final byte TAG_EC_PARAMETERS = (byte) 0xA0;

    // 1.2.840.113549.1.1.1
    private static final byte[] OID_RSA_ENCRYPTION = {0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01};
    // 1.2.840.113549.1.1.10
    private static final byte[] OID_RSASSA_PSS = {0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x0A};
    // 1.2.840.10045.2.1
    private static final byte[] OID_EC_PUBLIC_KEY = {0x2A, (byte) 0x86, 0x48, (byte) 0xCE, 0x3D, 0x02, 0x01};
    // 1.2.840.10040.4.1
    private static final byte[] OID_DSA = {0x2A, (byte) 0x86, 0x48, (byte) 0xCE, 0x38, 0x04, 0x01};
    // 1.3.101.110 - 1.3.101.113
    private static final byte[] OID_X25519 = {0x2B, 0x65, 0x6E};
    private static final byte[] OID_X448 = {0x2B, 0x65, 0x6F};
    private static final byte[] OID_ED25519 = {0x2B, 0x65, 0x70};
    private static final byte[] OID_ED448 = {0x2B, 0x65, 0x71};

    private PemKeys() {
    }

    /**
     * Reads the first private key in a PEM file. PKCS#8 ({@code BEGIN PRIVATE KEY}),
     * PKCS#1 ({@code BEGIN RSA PRIVATE KEY}) and SEC1 ({@code BEGIN EC PRIVATE KEY}) keys are accepted.
     * Encrypted keys are rejected.
     */
    public static PrivateKey readPrivateKey(final File file) throws IOException, GeneralSecurityException {
        final String pem = new String(Files.readAllBytes(file.toPath()), StandardCharsets.US_ASCII);
        final Matcher m = PEM_BLOCK.matcher(pem);
        while (m.find()) {
            final String label = m.group(1);
            if (!label.endsWith("PRIVATE KEY")) {
                continue;
            }
            final String body = m.group(2);
            if ("ENCRYPTED PRIVATE KEY".equals(label) || body.contains("Proc-Type: 4,ENCRYPTED")) {
                throw new InvalidKeySpecException(file + " contains an encrypted private key."
                        + " Remove the password, for example with: openssl pkcs8 -topk8 -nocrypt -in "
                        + file.getName() + " -out <new file>");
            }
            if (body.indexOf(':') >= 0) {
                throw new InvalidKeySpecException(file + " contains a private key with PEM headers, which is not supported.");
            }
            final byte[] der = decodeBase64(body, file);
            try {
                switch (label) {
                    case "PRIVATE KEY":
                        return fromPkcs8(der);
                    case "RSA PRIVATE KEY":
                        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(rsaPkcs1ToPkcs8(der)));
                    case "EC PRIVATE KEY":
                        return KeyFactory.getInstance("EC").generatePrivate(new PKCS8EncodedKeySpec(ecSec1ToPkcs8(der)));
                    default:
                        throw new InvalidKeySpecException("Unsupported key type '" + label + "'.");
                }
            } catch (final GeneralSecurityException | IllegalArgumentException e) {
                throw new InvalidKeySpecException("Could not read the '" + label + "' block in " + file + ": " + e.getMessage(), e);
            }
        }
        throw new InvalidKeySpecException(file + " does not contain a PEM private key"
                + " (BEGIN PRIVATE KEY, BEGIN RSA PRIVATE KEY or BEGIN EC PRIVATE KEY).");
    }

    /**
     * Reads all certificates in a PEM file, in file order. Other PEM blocks and text are ignored.
     */
    public static X509Certificate[] readCertificates(final File file) throws IOException, CertificateException {
        final String pem = new String(Files.readAllBytes(file.toPath()), StandardCharsets.US_ASCII);
        final CertificateFactory factory = CertificateFactory.getInstance("X.509");
        final List<X509Certificate> certificates = new ArrayList<>();
        final Matcher m = PEM_BLOCK.matcher(pem);
        while (m.find()) {
            if ("CERTIFICATE".equals(m.group(1))) {
                final byte[] der = decodeBase64(m.group(2), file);
                certificates.add((X509Certificate) factory.generateCertificate(new ByteArrayInputStream(der)));
            }
        }
        if (certificates.isEmpty()) {
            throw new CertificateException(file + " does not contain a PEM certificate (BEGIN CERTIFICATE).");
        }
        return certificates.toArray(new X509Certificate[0]);
    }

    private static byte[] decodeBase64(final String body, final File file) {
        try {
            return Base64.getMimeDecoder().decode(body.trim());
        } catch (final IllegalArgumentException e) {
            throw new IllegalArgumentException(file + " contains a PEM block that is not valid base64.", e);
        }
    }

    private static PrivateKey fromPkcs8(final byte[] der) throws GeneralSecurityException {
        // PrivateKeyInfo ::= SEQUENCE { version INTEGER, algorithm AlgorithmIdentifier, privateKey OCTET STRING, ... }
        final Der info = new Der(der).enter(TAG_SEQUENCE);
        info.skip(TAG_INTEGER);
        final byte[] oid = info.enter(TAG_SEQUENCE).read(TAG_OID);
        final String algorithm = algorithmOf(oid);
        if (algorithm == null) {
            throw new InvalidKeySpecException("Unsupported key algorithm OID " + Arrays.toString(oid) + ".");
        }
        return KeyFactory.getInstance(algorithm).generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private static String algorithmOf(final byte[] oid) {
        if (Arrays.equals(oid, OID_RSA_ENCRYPTION)) return "RSA";
        if (Arrays.equals(oid, OID_RSASSA_PSS)) return "RSASSA-PSS";
        if (Arrays.equals(oid, OID_EC_PUBLIC_KEY)) return "EC";
        if (Arrays.equals(oid, OID_DSA)) return "DSA";
        if (Arrays.equals(oid, OID_ED25519) || Arrays.equals(oid, OID_ED448)) return "EdDSA";
        if (Arrays.equals(oid, OID_X25519) || Arrays.equals(oid, OID_X448)) return "XDH";
        return null;
    }

    /** Wraps an RSAPrivateKey (PKCS#1) in a PrivateKeyInfo (PKCS#8). */
    static byte[] rsaPkcs1ToPkcs8(final byte[] pkcs1) {
        new Der(pkcs1).enter(TAG_SEQUENCE); // fail early on garbage
        return privateKeyInfo(tlv(TAG_SEQUENCE, concat(tlv(TAG_OID, OID_RSA_ENCRYPTION), tlv(TAG_NULL, new byte[0]))), pkcs1);
    }

    /** Wraps an ECPrivateKey (SEC1) in a PrivateKeyInfo (PKCS#8), taking the named curve from the SEC1 parameters. */
    static byte[] ecSec1ToPkcs8(final byte[] sec1) throws InvalidKeySpecException {
        // ECPrivateKey ::= SEQUENCE { version INTEGER, privateKey OCTET STRING, parameters [0] ECParameters OPTIONAL, publicKey [1] BIT STRING OPTIONAL }
        final Der key = new Der(sec1).enter(TAG_SEQUENCE);
        key.skip(TAG_INTEGER);
        key.skip(TAG_OCTET_STRING);
        if (!key.hasNext() || key.peekTag() != TAG_EC_PARAMETERS) {
            throw new InvalidKeySpecException("The EC key does not name its curve. Convert it to PKCS#8 with: openssl pkcs8 -topk8 -nocrypt");
        }
        final Der parameters = key.enter(TAG_EC_PARAMETERS);
        if (parameters.peekTag() != TAG_OID) {
            throw new InvalidKeySpecException("The EC key uses explicit curve parameters; only named curves are supported.");
        }
        final byte[] curve = parameters.read(TAG_OID);
        return privateKeyInfo(tlv(TAG_SEQUENCE, concat(tlv(TAG_OID, OID_EC_PUBLIC_KEY), tlv(TAG_OID, curve))), sec1);
    }

    private static byte[] privateKeyInfo(final byte[] algorithmIdentifier, final byte[] privateKey) {
        return tlv(TAG_SEQUENCE, concat(tlv(TAG_INTEGER, new byte[] {0}), algorithmIdentifier, tlv(TAG_OCTET_STRING, privateKey)));
    }

    private static byte[] tlv(final byte tag, final byte[] value) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(value.length + 6);
        out.write(tag);
        final int length = value.length;
        if (length < 0x80) {
            out.write(length);
        } else {
            int bytes = 0;
            for (int l = length; l > 0; l >>>= 8) {
                bytes++;
            }
            out.write(0x80 | bytes);
            for (int i = bytes - 1; i >= 0; i--) {
                out.write(length >>> (8 * i));
            }
        }
        out.write(value, 0, value.length);
        return out.toByteArray();
    }

    private static byte[] concat(final byte[]... parts) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (final byte[] part : parts) {
            out.write(part, 0, part.length);
        }
        return out.toByteArray();
    }

    /** A minimal reader for the DER elements of one constructed value. */
    private static final class Der {
        private final byte[] data;
        private int pos;
        private final int end;

        Der(final byte[] data) {
            this(data, 0, data.length);
        }

        private Der(final byte[] data, final int start, final int end) {
            this.data = data;
            this.pos = start;
            this.end = end;
        }

        boolean hasNext() {
            return pos < end;
        }

        byte peekTag() {
            if (pos >= end) {
                throw new IllegalArgumentException("Unexpected end of DER data.");
            }
            return data[pos];
        }

        /** Reads the next element, which must have the given tag, and returns a reader for its content. */
        Der enter(final byte tag) {
            final int[] range = next(tag);
            return new Der(data, range[0], range[1]);
        }

        byte[] read(final byte tag) {
            final int[] range = next(tag);
            return Arrays.copyOfRange(data, range[0], range[1]);
        }

        void skip(final byte tag) {
            next(tag);
        }

        private int[] next(final byte tag) {
            if (peekTag() != tag) {
                throw new IllegalArgumentException(String.format("Expected DER tag 0x%02X but found 0x%02X.", tag & 0xFF, data[pos] & 0xFF));
            }
            pos++;
            if (pos >= end) {
                throw new IllegalArgumentException("Unexpected end of DER data.");
            }
            int length = data[pos++] & 0xFF;
            if (length >= 0x80) {
                final int bytes = length & 0x7F;
                if (bytes == 0 || bytes > 4 || pos + bytes > end) {
                    throw new IllegalArgumentException("Invalid DER length.");
                }
                length = 0;
                for (int i = 0; i < bytes; i++) {
                    length = (length << 8) | (data[pos++] & 0xFF);
                }
            }
            if (length < 0 || pos + length > end) {
                throw new IllegalArgumentException("DER length exceeds the data.");
            }
            final int[] range = {pos, pos + length};
            pos += length;
            return range;
        }
    }
}
