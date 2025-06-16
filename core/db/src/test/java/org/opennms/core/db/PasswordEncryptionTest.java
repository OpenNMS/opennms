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
        Assert.assertEquals("superUsername", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawUserName(), new JCEKSSecureCredentialsVault(SCV_FILE, SCV_FILE_PASSWORD)));
        Assert.assertEquals("superSecretPassword", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawPassword(), new JCEKSSecureCredentialsVault(SCV_FILE, SCV_FILE_PASSWORD)));

        jdbcDataSource.setUserName("${scv:scvalias:username}");
        jdbcDataSource.setPassword("${scv:scvalias:password}");
        Assert.assertEquals(SCV_USERNAME, jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawUserName(), new JCEKSSecureCredentialsVault(SCV_FILE, SCV_FILE_PASSWORD)));
        Assert.assertEquals(SCV_PASSWORD, jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawPassword(), new JCEKSSecureCredentialsVault(SCV_FILE, SCV_FILE_PASSWORD)));

        jdbcDataSource.setUserName("${scv:unknownalias:username|defaultUsername}");
        jdbcDataSource.setPassword("${scv:unknownalias:password|defaultPassword}");
        Assert.assertEquals("defaultUsername", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawUserName(), new JCEKSSecureCredentialsVault(SCV_FILE, SCV_FILE_PASSWORD)));
        Assert.assertEquals("defaultPassword", jdbcDataSource.interpolateAttribute(jdbcDataSource.getRawPassword(), new JCEKSSecureCredentialsVault(SCV_FILE, SCV_FILE_PASSWORD)));
    }
}
