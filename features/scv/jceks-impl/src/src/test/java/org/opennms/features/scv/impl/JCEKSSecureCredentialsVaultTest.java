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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

import com.google.common.collect.Sets;

public class JCEKSSecureCredentialsVaultTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        System.setProperty("karaf.etc", tempFolder.getRoot().getAbsolutePath());
    }

    @Test
    public void canSetAndGetCredentials() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        File keystoreFile = tempFolder.newFile();
        // Create a new vault
        SecureCredentialsVault scv = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "testing123");
        // Aliases should be empty
        assertEquals(0, scv.getAliases().size());
        // Retrieving from an non-existent alias should return null
        assertNull(scv.getCredentials("http"));
        // Store some creds
        Credentials creds = new Credentials("adm1n", "p@ssw0rd");
        scv.setCredentials("http", creds);
        // Aliases should contain "http"
        assertEquals(Sets.newHashSet("http"), scv.getAliases());
        // Retrieve it back
        assertEquals(creds, scv.getCredentials("http"));
        // Recreate the store
        scv = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "testing123");
        // And retrieve it again
        assertEquals(Sets.newHashSet("http"), scv.getAliases());
        assertEquals(creds, scv.getCredentials("http"));

        // Now store credentials for another alias
        Credentials sshCreds = new Credentials("n0t-adm1n", "an0th3r-p@ssw0rd");
        scv.setCredentials("ssh", sshCreds);
        // Verify
        assertEquals(creds, scv.getCredentials("http"));
        assertEquals(sshCreds, scv.getCredentials("ssh"));
    }
}
