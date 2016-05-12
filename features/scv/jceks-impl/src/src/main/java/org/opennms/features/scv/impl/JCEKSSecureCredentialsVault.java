/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.scv.impl;

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
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collections;
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

    public JCEKSSecureCredentialsVault(String keystoreFile, String password) {
        this(keystoreFile, password, new byte[] {0x0, 0xd, 0xd, 0xb, 0xa, 0x1, 0x1});
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
                LOG.info("No existing keystore found at: {}. Using empty keystore.");
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
    
    @Override
    public Credentials getCredentials(String alias) {
        try {
            KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(m_password);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
    
            KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry)m_keystore.getEntry(alias, keyStorePP);
            if (ske == null)  {
                return null;
            }
    
            PBEKeySpec keySpec = (PBEKeySpec)factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);
            return fromBase64EncodedByteArray(new String(keySpec.getPassword()).getBytes());
        } catch (KeyStoreException | InvalidKeySpecException | NoSuchAlgorithmException | IOException | ClassNotFoundException | UnrecoverableEntryException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void setCredentials(String alias, Credentials credentials) {
        try {
            byte[] credentialBytes = toBase64EncodedByteArray(credentials);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
            SecretKey generatedSecret = factory.generateSecret(
                    new PBEKeySpec(new String(credentialBytes).toCharArray(), m_salt, m_iterationCount, m_keyLength));

            KeyStore.PasswordProtection keyStorePP = new KeyStore.PasswordProtection(m_password);
            m_keystore.setEntry(alias, new KeyStore.SecretKeyEntry(generatedSecret), keyStorePP);
            writeKeystoreToDisk();
        } catch (KeyStoreException | InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
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
        T o = (T)in.readObject();
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
}
