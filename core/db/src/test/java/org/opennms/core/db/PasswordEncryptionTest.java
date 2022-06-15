/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.db;

import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;

public class PasswordEncryptionTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private String SCV_FILE;
    private final String SCV_FILE_PASSWORD = "scvFilePassword";
    private final String SCV_ALIAS = "scvalias";
    private final String SCV_USERNAME = "scvUsername";
    private final String SCV_PASSWORD = "scvPassword";

    public PasswordEncryptionTest() throws IOException {
    }

    @Before
    public void before() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        SCV_FILE = temporaryFolder.newFile("test-scv.jce").getAbsolutePath();

        final File scvFile = new File(SCV_FILE);

        if (scvFile.exists()) {
            scvFile.delete();
        }

        final SecureCredentialsVault secureCredentialsVault = new JCEKSSecureCredentialsVault(SCV_FILE, SCV_FILE_PASSWORD);
        secureCredentialsVault.setCredentials(SCV_ALIAS, new Credentials(SCV_USERNAME, SCV_PASSWORD));
    }

    @Test
    public void testPasswordEncryption() {
        final JdbcDataSource jdbcDataSource = new JdbcDataSource();

        jdbcDataSource.setUserName("superUsername");
        jdbcDataSource.setPassword("superSecretPassword");
        Assert.assertEquals("superUsername", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawUserName(), SCV_FILE, SCV_FILE_PASSWORD));
        Assert.assertEquals("superSecretPassword", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawPassword(), SCV_FILE, SCV_FILE_PASSWORD));

        jdbcDataSource.setUserName("${scv:scvalias:username}");
        jdbcDataSource.setPassword("${scv:scvalias:password}");
        Assert.assertEquals(SCV_USERNAME, jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawUserName(), SCV_FILE, SCV_FILE_PASSWORD));
        Assert.assertEquals(SCV_PASSWORD, jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawPassword(), SCV_FILE, SCV_FILE_PASSWORD));

        jdbcDataSource.setUserName("${scv:unknownalias:username|defaultUsername}");
        jdbcDataSource.setPassword("${scv:unknownalias:password|defaultPassword}");
        Assert.assertEquals("defaultUsername", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawUserName(), SCV_FILE, SCV_FILE_PASSWORD));
        Assert.assertEquals("defaultPassword", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawPassword(), SCV_FILE, SCV_FILE_PASSWORD));
    }
}
