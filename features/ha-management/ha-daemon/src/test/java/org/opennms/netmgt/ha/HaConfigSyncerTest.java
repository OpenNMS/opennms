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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class HaConfigSyncerTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // -------------------------------------------------------------------------
    // Manifest format (HaSyncFiles)
    // -------------------------------------------------------------------------

    @Test
    public void manifestRoundTrips() {
        List<HaSyncFiles.Entry> entries = List.of(
                new HaSyncFiles.Entry("poller-configuration.xml", "ab12", 1234L),
                new HaSyncFiles.Entry("events/my events.xml", "cd34", 9L));
        String text = HaSyncFiles.toManifestText(entries);
        assertEquals(entries, HaSyncFiles.parseManifestText(text));
    }

    @Test
    public void manifestParserSkipsMalformedLines() {
        assertTrue(HaSyncFiles.parseManifestText("garbage\n\nno-size path\n").isEmpty());
        assertEquals(1, HaSyncFiles.parseManifestText("aa notanumber x\nbb 5 ok.xml\n").size());
    }

    @Test
    public void manifestPathsMayContainSpaces() {
        List<HaSyncFiles.Entry> parsed =
                HaSyncFiles.parseManifestText("aa 5 dir with space/file name.xml\n");
        assertEquals("dir with space/file name.xml", parsed.get(0).relativePath());
    }

    // -------------------------------------------------------------------------
    // Manifest building: binary files, exclusions
    // -------------------------------------------------------------------------

    @Test
    public void buildManifestIncludesBinaryFilesAndAppliesExclusions() throws Exception {
        Path etc = tmp.newFolder("etc").toPath();
        Files.write(etc.resolve("scv.jce"), new byte[]{0, 1, 2, (byte) 0xFF}); // binary
        Files.writeString(etc.resolve("ha-configuration.xml"), "<ha/>");       // builtin exclusion
        Files.createDirectories(etc.resolve("local"));
        Files.writeString(etc.resolve("local/keep.xml"), "<x/>");              // operator exclusion

        List<HaSyncFiles.Entry> manifest = HaSyncFiles.buildManifest(etc, List.of("local/"));
        assertEquals(1, manifest.size());
        assertEquals("scv.jce", manifest.get(0).relativePath());
        assertEquals(4, manifest.get(0).size());
        assertEquals(HaSyncFiles.sha256(etc.resolve("scv.jce")), manifest.get(0).sha256());
    }

    @Test
    public void exclusionRulesMatchExactAndSubtree() {
        assertTrue(HaSyncFiles.isExcluded("ha-configuration.xml", null));
        assertTrue(HaSyncFiles.isExcluded("examples/foo.xml", null));
        assertTrue(HaSyncFiles.isExcluded("node-local/x.pem", List.of("node-local/")));
        assertFalse(HaSyncFiles.isExcluded("poller-configuration.xml", null));
    }

    // -------------------------------------------------------------------------
    // Path safety
    // -------------------------------------------------------------------------

    @Test
    public void resolveSafeRejectsTraversal() throws Exception {
        Path etc = tmp.newFolder("etc").toPath().toAbsolutePath().normalize();
        assertThrows(java.io.IOException.class,
                () -> HaSyncFiles.resolveSafe(etc, "../outside.txt"));
        assertThrows(java.io.IOException.class,
                () -> HaSyncFiles.resolveSafe(etc, "a/../../outside.txt"));
        assertEquals(etc.resolve("a/b.xml"), HaSyncFiles.resolveSafe(etc, "a/b.xml"));
    }

    // -------------------------------------------------------------------------
    // Hashing
    // -------------------------------------------------------------------------

    @Test
    public void sha256MatchesKnownVector() throws Exception {
        Path f = tmp.newFile("v.txt").toPath();
        Files.write(f, "abc".getBytes(StandardCharsets.UTF_8));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                HaSyncFiles.sha256(f));
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
            // With a non-existent keystore file, JCEKSSecureCredentialsVault returns an empty vault
            // so getCredentials returns null → resolveScvExpression returns null
            assertNull(syncer.resolveScvExpression("${scv:hasync:password}"));
        } finally {
            System.clearProperty("opennms.home");
        }
    }

    // -------------------------------------------------------------------------
    // Sync skips: disabled, missing partner URL, ACTIVE instance
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
}
