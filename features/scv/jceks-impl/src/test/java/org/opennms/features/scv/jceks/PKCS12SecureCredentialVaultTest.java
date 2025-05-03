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

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;
import static org.opennms.features.scv.jceks.JCEKSSecureCredentialsVault.KEYSTORE_KEY_PROPERTY;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.features.scv.api.Credentials;
import org.opennms.features.scv.api.SecureCredentialsVault;

import com.google.common.collect.Sets;

public class PKCS12SecureCredentialVaultTest{

    private final String SCV_KEYSTORE_TYPE="PKCS12";
    private final String SCV_KEYSTORE_FILE_NAME="scv.pk12";
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() {
        System.setProperty("karaf.etc", tempFolder.getRoot().getAbsolutePath());
        System.setProperty(SecureCredentialsVault.SCV_KEYSTORE_PROPERTY, SCV_KEYSTORE_TYPE);
    }

    @Test
    public void canSetAndGetCredentials() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        File keystoreFile = new File(tempFolder.getRoot(),  SCV_KEYSTORE_FILE_NAME);

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


    @Test
    public void cachingCredentials() {
        File keystoreFile = new File(tempFolder.getRoot(), SCV_KEYSTORE_FILE_NAME);
        // Create a new vault
        SecureCredentialsVault scv = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "testing123");
        // Store some creds
        Map<String, String> attributes = Map.of("key1", "value1", "key2", "value2");
        Credentials credentials1 = new Credentials("adm1n", "p@ssw0rd", attributes);
        scv.setCredentials("http", credentials1);
        Credentials credentials2 = new Credentials("adm2n", "p@ssw0rd2", attributes);
        scv.setCredentials("ssh", credentials2);

        keystoreFile.delete();
        assertFalse(Files.exists(Paths.get(keystoreFile.getAbsolutePath())));
        assertEquals(2, scv.getAliases().size());
        assertEquals(Sets.newHashSet("http", "ssh"), scv.getAliases());
        assertEquals(credentials1, scv.getCredentials("http"));
        assertEquals(credentials2, scv.getCredentials("ssh"));

    }

    @Test
    public void multiThreadSvcAccess() throws InterruptedException {
        File keystoreFile = new File(tempFolder.getRoot(), SCV_KEYSTORE_FILE_NAME);
        int numberOfThreads = 20;

        // Create a new vault and save some credentials
        final SecureCredentialsVault scv = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "testing123");
        final Map<String, String> attributes = Map.of("key1", "value1", "key2", "value2");
        final Credentials credentials1 = new Credentials("adm1n", "p@ssw0rd", attributes);
        scv.setCredentials("http", credentials1);

        final ExecutorService service = Executors.newFixedThreadPool(10);
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);
        final AtomicLong idCounter = new AtomicLong();

        //Create a new vault to access existing credentials and to add new credentials
        final SecureCredentialsVault scv2 = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "testing123");
        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                final Credentials credentials2 = new Credentials("adm2n", "p@ssw0rd2", attributes);
                final String alias = String.valueOf(idCounter.getAndIncrement());
                //scenario when adding new credentials before calling getCredentials for the first time
                scv2.setCredentials(alias, credentials2);
                //scenario on first load and caching credentials
                scv2.getCredentials("http");
                latch.countDown();
            });
        }
        latch.await();
        assertEquals(numberOfThreads + 1, scv2.getAliases().size());
        assertEquals(credentials1, scv2.getCredentials("http")) ;
    }

    @Test
    public void testScvAccessFromDifferentInstances() throws IOException {
        File keystoreFile = new File(tempFolder.newFolder("etc"), SCV_KEYSTORE_FILE_NAME);
        System.setProperty(KEYSTORE_KEY_PROPERTY, "testing123");
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());

        // Load scv indirectly through default system properties ( which is what scvcli does)
        final SecureCredentialsVault scv = JCEKSSecureCredentialsVault.defaultScv();
        final Map<String, String> attributes = Map.of("key1", "value1", "key2", "value2");
        final Credentials credentials1 = new Credentials("adm1n", "p@ssw0rd", attributes);
        scv.setCredentials("http", credentials1);

        //Add a new vault by directly loading keystore file ( as Opennms UI does), this is using watcher.
        final SecureCredentialsVault scv2 = new JCEKSSecureCredentialsVault(keystoreFile.getAbsolutePath(), "testing123", true);
        assertEquals(credentials1, scv2.getCredentials("http"));

        // Update credentials from default scv ( assuming scvcli updates credentials)
        final Credentials credentials2 = new Credentials("adm2n", "p@ssw0rd2", attributes);
        scv.setCredentials("http", credentials2);

        // watcher is asynchronous, so wait for few secs to validate loading of valid credentials
        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> scv2.getCredentials("http"), Matchers.is(credentials2));
    }
}

