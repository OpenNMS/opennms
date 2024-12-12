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
package org.opennms.features.scv.jceks;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

/**
 * Java Keystore based credentials store
 *
 * @author jwhite
 */
public class JCEKSSecureCredentialsVault implements SecureCredentialsVault {

    public static final Logger LOG = LoggerFactory.getLogger(JCEKSSecureCredentialsVault.class);

    private final KeyStore m_keystore;
    private final File m_keystoreFile;
    private final char[] m_password;
    private final byte[] m_salt;
    private final int m_iterationCount;
    private final int m_keyLength;
    private final HashMap<String, Credentials> m_credentialsCache = new HashMap<>();

    public static final String KEYSTORE_KEY_PROPERTY = "org.opennms.features.scv.jceks.key";

    public static final String DEFAULT_KEYSTORE_KEY = "QqSezYvBtk2gzrdpggMHvt5fJGWCdkRw";

    public JCEKSSecureCredentialsVault(String keystoreFile, String password) {
        this(keystoreFile, password, new byte[]{0x0, 0xd, 0xd, 0xb, 0xa, 0x1, 0x1});
    }

    public JCEKSSecureCredentialsVault(String keystoreFile, String password, byte[] salt) {
        this(keystoreFile, password, salt, 16, 4096);
    }

    public JCEKSSecureCredentialsVault(String keystoreFile, String password, byte[] salt, int iterationCount, int keyLength) {
        m_password = Objects.requireNonNull(password).toCharArray();
        m_salt = Objects.requireNonNull(salt);
        m_iterationCount = iterationCount;
        m_keyLength = keyLength;
        m_keystoreFile = new File(keystoreFile);
        try {
            m_keystore = KeyStore.getInstance("JCEKS");
            if (!m_keystoreFile.isFile()) {
                LOG.info("No existing keystore found at: {}. Using empty keystore.", m_keystoreFile);
                m_keystore.load(null, m_password);
            } else {
                LOG.info("Loading existing keystore from: {}", m_keystoreFile);
                try (InputStream is = new FileInputStream(m_keystoreFile)) {
                    m_keystore.load(is, m_password);
                }
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void loadCredentials() {
        synchronized (m_credentialsCache) {
            if (!m_credentialsCache.isEmpty()) {
                return;
            }
            try {
                KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(m_password);
                SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");

                for (String alias : getAliases()) {
                    KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) m_keystore.getEntry(alias, keyStorePP);
                    if (ske == null) {
                        continue;
                    }
                    PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
                    m_credentialsCache.put(alias, fromBase64EncodedByteArray(new String(keySpec.getPassword()).getBytes()));
                }
            } catch (KeyStoreException | InvalidKeySpecException | NoSuchAlgorithmException | IOException | ClassNotFoundException | UnrecoverableEntryException e) {
                throw Throwables.propagate(e);
            }
        }
    }


    @Override
    public Credentials getCredentials(String alias) {
        loadCredentials();
        reMapCredentials();
        synchronized (m_credentialsCache) {
            return m_credentialsCache.get(alias);
        }
    }

    @Override
    public void setCredentials(String alias, Credentials credentials) {
        try {
            loadCredentials();
            byte[] credentialBytes = toBase64EncodedByteArray(credentials);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
            SecretKey generatedSecret = factory.generateSecret(
                    new PBEKeySpec(new String(credentialBytes).toCharArray(), m_salt, m_iterationCount, m_keyLength));

            KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(m_password);
            m_keystore.setEntry(alias, new KeyStore.SecretKeyEntry(generatedSecret), keyStorePP);
            writeKeystoreToDisk();
            synchronized (m_credentialsCache) {
                m_credentialsCache.put(alias, credentials);
            }
        } catch (KeyStoreException | InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void deleteCredentials(final String alias) {
        try {
            synchronized (m_credentialsCache) {
                m_keystore.deleteEntry(alias);
                m_credentialsCache.remove(alias);
            }

            writeKeystoreToDisk();

        } catch (final KeyStoreException e) {
            throw Throwables.propagate(e);
        }
    }

    private void writeKeystoreToDisk() {
        try (OutputStream os = new FileOutputStream(m_keystoreFile)) {
            m_keystore.store(os, m_password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw Throwables.propagate(e);
        }
    }

    private static byte[] toBase64EncodedByteArray(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(o);
        out.close();
        return Base64.encodeBase64(baos.toByteArray());
    }

    private static <T extends Serializable> T fromBase64EncodedByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
        byte decodedBytes[] = Base64.decodeBase64(bytes);
        ByteArrayInputStream bais = new ByteArrayInputStream(decodedBytes);
        ObjectInputStream in = new ObjectInputStream(bais);
        @SuppressWarnings("unchecked")
        T o = (T) in.readObject();
        in.close();
        return o;
    }

    @Override
    public Set<String> getAliases() {
        try {
            reloadKeyStoreFile();
            return Sets.newHashSet(Collections.list(m_keystore.aliases()));
        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw Throwables.propagate(e);
        }
    }

    private static String getKeystoreFilename() {
        String opennmsHome = System.getProperty("opennms.home");
        if (opennmsHome == null) {
            try {
                System.err.println("opennms.home is not set; using a temporary directory for scv keystore. This is very likely not what you want.");
                opennmsHome = Files.createTempDirectory("opennms-home-").toString();
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to create a temporary scv keystore home!", e);
            }
        }
        return Paths.get(opennmsHome, "etc", "scv.jce").toString();
    }

    private static String getKeystorePassword() {
        return System.getProperty(KEYSTORE_KEY_PROPERTY, DEFAULT_KEYSTORE_KEY);
    }

    public static JCEKSSecureCredentialsVault defaultScv() {
        return new JCEKSSecureCredentialsVault(getKeystoreFilename(), getKeystorePassword());
    }

    private void reMapCredentials(){
        try {
            KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(m_password);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
            for (String aliass : getAliases()) {
                KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) m_keystore.getEntry(aliass, keyStorePP);
                if (ske == null) {
                    continue;
                }
                PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
                m_credentialsCache.put(aliass, fromBase64EncodedByteArray(new String(keySpec.getPassword()).getBytes()));
            }
        } catch (KeyStoreException | InvalidKeySpecException | NoSuchAlgorithmException | IOException | ClassNotFoundException | UnrecoverableEntryException e) {
            throw Throwables.propagate(e);
        }
    }
    private void reloadKeyStoreFile() throws CertificateException, IOException, NoSuchAlgorithmException {
        if (!m_keystoreFile.isFile()) {
            m_keystore.load(null, m_password);
        } else {
            try (InputStream is = new FileInputStream(m_keystoreFile)) {
                m_keystore.load(is, m_password);
            }
        }
    }
}
