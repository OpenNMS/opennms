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
package org.opennms.web.rest.v2.plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PluginRegistryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path registryFile;
    private Path deployDir;
    private FakeKarafBridge bridge;
    private long bootTime;
    private PluginRegistry registry;

    @Before
    public void setUp() throws IOException {
        registryFile = folder.newFolder("etc").toPath().resolve(PluginRegistry.FILE_NAME);
        deployDir = folder.newFolder("deploy").toPath();
        bridge = new FakeKarafBridge();
        bootTime = System.currentTimeMillis() - 60_000;
        registry = new PluginRegistry(registryFile, deployDir, bridge, bootTime);
    }

    private static boolean restartRequired(final List<PluginEntry> entries) {
        return entries.stream().anyMatch(PluginEntry::isPendingRestart);
    }

    @Test
    public void emptyRegistryListsNothing() throws IOException {
        assertTrue(registry.list().isEmpty());
        assertFalse(Files.exists(registryFile));
    }

    @Test
    public void pluginWithoutAutoStartWaitsForARestart() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });

        final PluginEntry entry = registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), "etc/featuresBoot.d/example.boot", false);

        assertEquals("example", entry.getKarName());
        assertEquals("example-1.0.kar", entry.getFileName());
        assertEquals("abc", entry.getSha256());
        assertEquals("admin", entry.getUploadedBy());
        assertNotNull(entry.getUploadedAt());
        assertEquals("etc/featuresBoot.d/example.boot", entry.getBootFile());
        assertFalse(entry.isAutoStart());
        assertTrue(entry.isManaged());
        assertTrue(entry.isDeployed());
        assertFalse(entry.isKarLoaded());
        assertEquals("Uninstalled", entry.getFeatureStates().get("example-feature"));
        assertEquals(PluginEntry.STATUS_STAGED, entry.getStatus());
        assertTrue(entry.isPendingRestart());
        assertTrue(restartRequired(registry.list()));
        assertTrue(Files.exists(registryFile));
        final String json = new String(Files.readAllBytes(registryFile), StandardCharsets.UTF_8);
        assertTrue(json, json.contains("\"karName\" : \"example\""));
        assertTrue(json, json.contains("\"pendingRestartSince\" : 1"));

        // Karaf extracts the KAR right away but the features wait for the boot file.
        bridge.installedKars.add("example");
        bridge.feature("example-feature", "1.0.0", "Installed");
        final PluginEntry extracted = registry.find("example").orElseThrow();
        assertEquals(PluginEntry.STATUS_STAGED, extracted.getStatus());
        assertTrue(extracted.isPendingRestart());

        final PluginRegistry afterRestart = new PluginRegistry(registryFile, deployDir, bridge, System.currentTimeMillis() + 1);
        assertFalse(afterRestart.find("example").orElseThrow().isPendingRestart());
        assertFalse(restartRequired(afterRestart.list()));
    }

    @Test
    public void autoStartPluginNeedsNoRestart() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });

        final PluginEntry entry = registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature", "example-other"), "etc/featuresBoot.d/example.boot", true);

        assertTrue(entry.isAutoStart());
        assertFalse(entry.isPendingRestart());
        assertEquals(PluginEntry.STATUS_STAGED, entry.getStatus());
        assertFalse(restartRequired(registry.list()));
        final String json = new String(Files.readAllBytes(registryFile), StandardCharsets.UTF_8);
        assertTrue(json, json.contains("\"pendingRestartSince\" : null"));

        bridge.installedKars.add("example");
        bridge.feature("example-feature", "1.0.0", "Started").feature("example-other", "1.0.0", "Started");
        final PluginEntry started = registry.find("example").orElseThrow();
        assertEquals(PluginEntry.STATUS_INSTALLED, started.getStatus());
        assertTrue(started.isKarLoaded());
        assertEquals("Started", started.getFeatureStates().get("example-other"));
        assertFalse(started.isPendingRestart());
    }

    @Test
    public void featureNotStartedInTheInstallingJvmIsStillStaged() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), null, true);
        bridge.installedKars.add("example");
        bridge.feature("example-feature", "1.0.0", "Resolved");

        final PluginEntry entry = registry.find("example").orElseThrow();
        assertEquals(PluginEntry.STATUS_STAGED, entry.getStatus());
        assertEquals("Resolved", entry.getFeatureStates().get("example-feature"));
    }

    @Test
    public void featureNotStartedAfterARestartIsFailed() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature", "example-other"), null, false);
        bridge.installedKars.add("example");
        bridge.feature("example-feature", "1.0.0", "Started").feature("example-other", "1.0.0", "Resolved");

        final PluginRegistry afterRestart = new PluginRegistry(registryFile, deployDir, bridge, System.currentTimeMillis() + 1);
        final PluginEntry entry = afterRestart.find("example").orElseThrow();
        assertEquals(PluginEntry.STATUS_FAILED, entry.getStatus());
        assertEquals("Resolved", entry.getFeatureStates().get("example-other"));
        assertFalse(entry.isPendingRestart());
        assertFalse(restartRequired(afterRestart.list()));
    }

    @Test
    public void karNotListedAfterARestartIsStaged() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), null, true);

        final PluginRegistry afterRestart = new PluginRegistry(registryFile, deployDir, bridge, System.currentTimeMillis() + 1);
        final PluginEntry entry = afterRestart.find("example").orElseThrow();
        assertEquals(PluginEntry.STATUS_STAGED, entry.getStatus());
        assertFalse(entry.isPendingRestart());
    }

    @Test
    public void unavailableContainerYieldsUnknownStatus() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), null, true);
        bridge.available = false;

        final PluginEntry entry = registry.find("example").orElseThrow();
        assertEquals(PluginEntry.STATUS_UNKNOWN, entry.getStatus());
        assertTrue(entry.getFeatureStates().isEmpty());
    }

    @Test
    public void unloadKeepsTheRecordAndFlagsRestart() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), null, true);
        Files.delete(deployDir.resolve("example.kar"));

        final PluginEntry entry = registry.recordUnload("example", "operator", "example-1.0.kar");

        assertEquals(PluginEntry.STATUS_UNLOADED, entry.getStatus());
        assertEquals("operator", entry.getUnloadedBy());
        assertNotNull(entry.getUnloadedAt());
        assertEquals("admin", entry.getUploadedBy());
        assertTrue(entry.isPendingRestart());
        assertFalse(entry.isDeployed());
        assertTrue(restartRequired(registry.list()));

        final PluginRegistry reloaded = new PluginRegistry(registryFile, deployDir, bridge, bootTime);
        final List<PluginEntry> entries = reloaded.list();
        assertEquals(1, entries.size());
        assertEquals(PluginEntry.STATUS_UNLOADED, entries.get(0).getStatus());

        final PluginRegistry afterRestart = new PluginRegistry(registryFile, deployDir, bridge, System.currentTimeMillis() + 1);
        assertFalse(afterRestart.find("example").orElseThrow().isPendingRestart());
    }

    @Test
    public void handRemovedKarShowsAsUnloaded() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), null, true);
        Files.delete(deployDir.resolve("example.kar"));

        final PluginEntry entry = registry.find("example").orElseThrow();
        assertEquals(PluginEntry.STATUS_UNLOADED, entry.getStatus());
    }

    @Test
    public void unloadOfUnmanagedKarCreatesARecord() throws IOException {
        final PluginEntry entry = registry.recordUnload("hand-copied", "operator", "hand-copied.kar");

        assertEquals(PluginEntry.STATUS_UNLOADED, entry.getStatus());
        assertEquals("hand-copied.kar", entry.getFileName());
        assertTrue(entry.isManaged());
        assertEquals(1, registry.list().size());
    }

    @Test
    public void reinstallReplacesTheRecord() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), null, true);
        registry.recordUnload("example", "admin", "example-1.0.kar");

        final PluginEntry entry = registry.recordInstall("example", "example-1.1.kar", "def", 2, "admin", Arrays.asList("example-feature"), null, true);

        assertEquals("def", entry.getSha256());
        assertEquals("example-1.1.kar", entry.getFileName());
        assertEquals(PluginEntry.STATUS_STAGED, entry.getStatus());
        assertEquals(1, registry.list().size());
    }

    @Test
    public void revertInstallPutsThePreviousRecordBack() throws IOException {
        Files.write(deployDir.resolve("example.kar"), new byte[] { 1 });
        registry.recordInstall("example", "example-1.0.kar", "abc", 1, "admin", Arrays.asList("example-feature"), null, true);
        final PluginRegistry.Record previous = registry.record("example").orElseThrow();
        registry.recordInstall("example", "example-1.1.kar", "def", 2, "admin", Arrays.asList("example-feature"), null, true);

        registry.revertInstall("example", previous);

        final PluginEntry entry = registry.find("example").orElseThrow();
        assertEquals("abc", entry.getSha256());
        assertEquals("example-1.0.kar", entry.getFileName());
        assertEquals(1, registry.list().size());

        registry.revertInstall("example", null);
        assertTrue(registry.record("example").isEmpty());
        assertTrue(registry.list().stream().noneMatch(PluginEntry::isManaged));
    }

    @Test
    public void unmanagedKarsInDeployAreListed() throws IOException {
        Files.write(deployDir.resolve("hand-copied.kar"), new byte[] { 1, 2, 3 });
        Files.write(deployDir.resolve("README"), new byte[] { 1 });
        Files.write(deployDir.resolve("IGNORED.KAR"), new byte[] { 1 });
        Files.write(deployDir.resolve("also.Kar"), new byte[] { 1 });
        bridge.installedKars.add("hand-copied");

        final List<PluginEntry> entries = registry.list();

        assertEquals(1, entries.size());
        final PluginEntry entry = entries.get(0);
        assertEquals("hand-copied", entry.getKarName());
        assertEquals("hand-copied.kar", entry.getFileName());
        assertEquals(3, entry.getSize());
        assertFalse(entry.isManaged());
        assertTrue(entry.isDeployed());
        assertTrue(entry.isKarLoaded());
        assertEquals(PluginEntry.STATUS_UNMANAGED, entry.getStatus());
        assertFalse(entry.isPendingRestart());
        assertFalse(restartRequired(entries));
    }

    @Test
    public void unknownFieldsInRegistryFileAreIgnored() throws IOException {
        Files.write(registryFile, "{\"plugins\":[{\"karName\":\"legacy\",\"fileName\":\"legacy.kar\",\"future\":true}],\"schema\":2}".getBytes(StandardCharsets.UTF_8));

        final List<PluginEntry> entries = registry.list();

        assertEquals(1, entries.size());
        assertEquals("legacy", entries.get(0).getKarName());
        assertEquals(PluginEntry.STATUS_UNLOADED, entries.get(0).getStatus());
    }
}
