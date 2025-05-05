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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.opennms.core.fileutils.FileUpdateCallback;
import org.opennms.core.fileutils.FileUpdateWatcher;
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
public class JCEKSSecureCredentialsVault implements SecureCredentialsVault, FileUpdateCallback {

    public static final Logger LOG = LoggerFactory.getLogger(JCEKSSecureCredentialsVault.class);

    private final KeyStore m_keystore;
    private final File m_keystoreFile;
    private final char[] m_password;
    private final byte[] m_salt;
    private final int m_iterationCount;
    private final int m_keyLength;
    private final HashMap<String, Credentials> m_credentialsCache = new HashMap<>();
    private FileUpdateWatcher m_fileUpdateWatcher;
    private final AtomicBoolean m_fileUpdated = new AtomicBoolean(false);
    private long m_lastModified = System.currentTimeMillis();

    public JCEKSSecureCredentialsVault(String keystoreFile, String password, boolean useWatcher)  {
        this(keystoreFile, password, useWatcher, new byte[]{0x0, 0xd, 0xd, 0xb, 0xa, 0x1, 0x1});
    }

    public JCEKSSecureCredentialsVault(String keystoreFile, String password)  {
        this(keystoreFile, password, false, new byte[]{0x0, 0xd, 0xd, 0xb, 0xa, 0x1, 0x1});
    }

    public JCEKSSecureCredentialsVault(String keystoreFile, String password, boolean useWatcher, byte[] salt) {
        this(keystoreFile, password, useWatcher, salt, 16, 4096);
    }

    public JCEKSSecureCredentialsVault(String keystoreFile, String password, boolean useWatcher, byte[] salt, int iterationCount, int keyLength) {
        m_password = Objects.requireNonNull(password).toCharArray();
        m_salt = Objects.requireNonNull(salt);
        m_iterationCount = iterationCount;
        m_keyLength = keyLength;
        m_keystoreFile = new File(getKeyStoreFileName(keystoreFile));
        try {
            m_keystore = KeyStore.getInstance(KeyStoreType.fromSystemProperty().toString());
            if (!m_keystoreFile.isFile()) {
                LOG.info("No existing keystore found at: {}. Using empty keystore.", m_keystoreFile);
                m_keystore.load(null, m_password);
            } else {
                LOG.info("Loading existing keystore from: {}", m_keystoreFile);
                try (InputStream is = new FileInputStream(m_keystoreFile)) {
                    m_keystore.load(is, m_password);
                }
            }
            // Enable watcher to load changes to keystore file that happens outside OpenNMS
            if (useWatcher) {
                createFileUpdateWatcher();
            }

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private String getKeyStoreFileName(String keystoreFile){

        String fileName = keystoreFile;

        if (KeyStoreType.fromSystemProperty().equals(KeyStoreType.PKCS12)) {
            fileName = keystoreFile.replaceAll(".jce",".pk12");
        }

        return fileName;
    }

    public static JCEKSSecureCredentialsVault createInstance(String keystoreFile, String keyStoreType, String password, boolean useWatcher) {
        // Set the system property before creating the instance
        if(keyStoreType != null && !keyStoreType.isEmpty()) {
            System.setProperty(SCV_KEYSTORE_PROPERTY, keyStoreType);
        }

        return new JCEKSSecureCredentialsVault(keystoreFile, password, useWatcher);
    }

    private void createFileUpdateWatcher() {
        if (m_fileUpdateWatcher == null) {
            try {
                m_fileUpdateWatcher = new FileUpdateWatcher(m_keystoreFile.getAbsolutePath(), this, true);
            } catch (IOException e) {
                LOG.warn("Failed to create file update watcher", e);
            }
        }
    }

    private void loadCredentials() {
        synchronized (m_credentialsCache) {
            if (!m_credentialsCache.isEmpty() && !m_fileUpdated.get()) {
                return;
            }
            if (m_fileUpdated.get()) {
                m_fileUpdated.set(false);
                m_credentialsCache.clear();
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
            synchronized (m_credentialsCache) {
                writeKeystoreToDisk();
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
                writeKeystoreToDisk();
            }


        } catch (final KeyStoreException e) {
            throw Throwables.propagate(e);
        }
    }

    public void destroy() {
        if (m_fileUpdateWatcher != null) {
            m_fileUpdateWatcher.destroy();
        }
    }

    private void writeKeystoreToDisk() {
        try (OutputStream os = new FileOutputStream(m_keystoreFile)) {
            m_keystore.store(os, m_password);
            m_lastModified = m_keystoreFile.lastModified();
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
            return Sets.newHashSet(Collections.list(m_keystore.aliases()));
        } catch (KeyStoreException e) {
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

    /*
       This instantiates scv indirectly, not from spring/karaf container.
       Should be mostly used for read-only access, short-lived and instantiate for each access.
     */
    public static JCEKSSecureCredentialsVault defaultScv() {
        SecureCredentialsVault.loadScvProperties(System.getProperty("opennms.home",""));
        return new JCEKSSecureCredentialsVault(getKeystoreFilename(), getKeystorePassword());
    }

    @Override
    public void reload() {
        synchronized (m_credentialsCache) {
            // If the keystore file got updated by us, no need to reload
            if (m_keystoreFile.lastModified() == m_lastModified) {
                return;
            }
            // Reload the keystore file when file gets updated.
            try (InputStream is = new FileInputStream(m_keystoreFile)) {
                m_keystore.load(is, m_password);
            } catch (NoSuchAlgorithmException | CertificateException | IOException e) {
                LOG.error("Exception while loading keystore file {}", m_keystoreFile, e);
            }
            m_fileUpdated.set(true);
        }
    }

}
