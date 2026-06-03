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
package org.opennms.netmgt.ha;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class HaConfigSyncerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // -------------------------------------------------------------------------
    // JSON array parser
    // -------------------------------------------------------------------------

    @Test
    public void parsesSimpleArray() {
        List<String> result = HaConfigSyncer.parseJsonStringArray(
                "[\"events/foo.xml\",\"threshd-configuration.xml\"]");
        assertEquals(2, result.size());
        assertEquals("events/foo.xml", result.get(0));
        assertEquals("threshd-configuration.xml", result.get(1));
    }

    @Test
    public void parsesEmptyArray() {
        assertTrue(HaConfigSyncer.parseJsonStringArray("[]").isEmpty());
    }

    @Test
    public void parsesNullInput() {
        assertTrue(HaConfigSyncer.parseJsonStringArray(null).isEmpty());
    }

    @Test
    public void parsesArrayWithWhitespace() {
        List<String> result = HaConfigSyncer.parseJsonStringArray(
                "[ \"a.xml\" , \"b.xml\" ]");
        assertEquals(2, result.size());
        assertEquals("a.xml", result.get(0));
        assertEquals("b.xml", result.get(1));
    }

    @Test
    public void handlesMalformedInput() {
        assertTrue(HaConfigSyncer.parseJsonStringArray("not json").isEmpty());
        assertTrue(HaConfigSyncer.parseJsonStringArray("{\"key\":\"val\"}").isEmpty());
    }

    @Test
    public void parsesSingleElement() {
        List<String> result = HaConfigSyncer.parseJsonStringArray("[\"only.xml\"]");
        assertEquals(1, result.size());
        assertEquals("only.xml", result.get(0));
    }

    // -------------------------------------------------------------------------
    // SCV expression resolution (literal passthrough — no real keystore needed)
    // -------------------------------------------------------------------------

    @Test
    public void literalPasswordPassedThrough() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setSyncPassword("myPlainPassword");
        HaConfigSyncer syncer = new HaConfigSyncer(cfg);

        // A literal (non-SCV) password is returned as-is
        assertEquals("myPlainPassword", syncer.resolveScvExpression("myPlainPassword"));
    }

    @Test
    public void nullPasswordReturnsNull() {
        HaConfigSyncer syncer = new HaConfigSyncer(new HaConfiguration());
        assertNull(syncer.resolveScvExpression(null));
    }

    @Test
    public void scvExpressionWithMissingKeystoreReturnsNull() {
        // Point to a non-existent keystore; should log a warning and return null
        System.setProperty("opennms.home", "/tmp/no-such-dir-" + System.currentTimeMillis());
        try {
            HaConfigSyncer syncer = new HaConfigSyncer(new HaConfiguration());
            // Should not throw; should return null (or possibly the empty-keystore case)
            String result = syncer.resolveScvExpression("${scv:hasync:password}");
            // With a non-existent keystore file, JCEKSSecureCredentialsVault returns an empty vault
            // so getCredentials returns null → resolveScvExpression returns null
            assertNull(result);
        } finally {
            System.clearProperty("opennms.home");
        }
    }

    // -------------------------------------------------------------------------
    // Sync skips sync-disabled and missing partner URL configs
    // -------------------------------------------------------------------------

    @Test
    public void syncSkipsWhenDisabled() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setSyncEnabled(false);
        cfg.setPartnerRestUrl("http://partner:8980/opennms");
        // If sync is disabled this should be a no-op — just ensure no exception
        new HaConfigSyncer(cfg).sync();
    }

    @Test
    public void syncSkipsWhenNoPartnerUrl() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setSyncEnabled(true);
        cfg.setPartnerRestUrl(null);
        // Should log a warning and return without attempting HTTP
        new HaConfigSyncer(cfg).sync();
    }

    @Test
    public void syncSkipsWhenInstanceIsActive() {
        HaConfiguration cfg = new HaConfiguration();
        cfg.setSyncEnabled(true);
        cfg.setPartnerRestUrl("http://partner:8980/opennms");
        // When this instance is ACTIVE it must not attempt to sync from the partner
        HaConfigSyncer syncer = new HaConfigSyncer(cfg, () -> HaInstanceState.ACTIVE);
        syncer.sync(); // would throw/fail if HTTP was attempted against a non-existent partner
    }

    // -------------------------------------------------------------------------
    // writeLocalFile skips unchanged content
    // -------------------------------------------------------------------------

    @Test
    public void writeLocalFileSkipsUnchangedContent() throws Exception {
        Path etc = tmp.newFolder("etc").toPath();
        Path target = etc.resolve("test.xml");
        Files.writeString(target, "<config/>", StandardCharsets.UTF_8);

        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
        try {
            long beforeMtime = Files.getLastModifiedTime(target).toMillis();
            // Small sleep to ensure mtime would differ if a write occurred
            Thread.sleep(50);

            HaConfigSyncer syncer = new HaConfigSyncer(new HaConfiguration());
            // Invoke writeLocalFile via reflection (package-private visibility via same package)
            invokeWriteLocalFile(syncer, "test.xml", "<config/>");

            long afterMtime = Files.getLastModifiedTime(target).toMillis();
            assertEquals("File should not have been rewritten when content is identical",
                    beforeMtime, afterMtime);
        } finally {
            System.clearProperty("opennms.home");
        }
    }

    @Test
    public void writeLocalFileWritesWhenContentDiffers() throws Exception {
        Path etc = tmp.newFolder("etc").toPath();
        Path target = etc.resolve("test2.xml");
        Files.writeString(target, "<old/>", StandardCharsets.UTF_8);

        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
        try {
            HaConfigSyncer syncer = new HaConfigSyncer(new HaConfiguration());
            invokeWriteLocalFile(syncer, "test2.xml", "<new/>");
            assertEquals("<new/>", Files.readString(target, StandardCharsets.UTF_8));
        } finally {
            System.clearProperty("opennms.home");
        }
    }

    private static void invokeWriteLocalFile(HaConfigSyncer syncer, String filename, String content)
            throws Exception {
        java.lang.reflect.Method m = HaConfigSyncer.class
                .getDeclaredMethod("writeLocalFile", String.class, String.class);
        m.setAccessible(true);
        m.invoke(syncer, filename, content);
    }
}
