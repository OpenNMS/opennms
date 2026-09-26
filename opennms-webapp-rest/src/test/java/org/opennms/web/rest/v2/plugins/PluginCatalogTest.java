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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PluginCatalogTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void missingFileYieldsTheBuiltInCatalog() {
        final PluginCatalog catalog = new PluginCatalog(folder.getRoot().toPath().resolve("plugin-catalog.json"));

        final List<PluginCatalog.Entry> entries = catalog.entries();

        assertEquals(Arrays.asList("alec", "prometheus-remotewrite", "pagerduty"), ids(entries));
        assertEquals("OpenNMS-Plugins/alec", catalog.find("alec").orElseThrow().getRepository());
        assertEquals(Arrays.asList("alec-opennms-standalone"), catalog.find("alec").orElseThrow().getBootFeatures());
        assertEquals(Arrays.asList("opennms-plugins-prometheus-remotewrite"), catalog.find("prometheus-remotewrite").orElseThrow().getBootFeatures());
        assertEquals(Arrays.asList("opennms-plugins-pagerduty"), catalog.find("pagerduty").orElseThrow().getBootFeatures());
        assertEquals(Arrays.asList("alec-opennms-standalone"), catalog.bootFeaturesFor("OpenNMS-Plugins/alec"));
        assertTrue(catalog.bootFeaturesFor("acme/other").isEmpty());
        assertTrue(catalog.bootFeaturesFor(null).isEmpty());
        assertEquals(PluginCatalog.DEFAULT_ASSET_PATTERN, catalog.find("alec").orElseThrow().getAssetPattern());
        assertTrue(catalog.find("alec").orElseThrow().assetPattern().matcher("opennms-alec-plugin.kar").matches());
        assertFalse(catalog.find("alec").orElseThrow().assetPattern().matcher("checksums.txt").matches());
        assertFalse(catalog.find("nope").isPresent());
        for (final PluginCatalog.Entry entry : entries) {
            assertTrue(entry.getId(), GitHubReleases.isValidRepository(entry.getRepository()));
            assertFalse(entry.getId(), entry.getDescription().isBlank());
            assertTrue(entry.getId(), entry.getDocsUrl().startsWith("https://"));
        }
    }

    @Test
    public void shippedCatalogFileMatchesTheBuiltInDefaults() throws IOException {
        final Path shipped = Path.of(System.getProperty("basedir", "."), "..", "opennms-base-assembly", "src", "main", "filtered", "etc", "plugin-catalog.json");
        assertTrue(shipped + " must ship", Files.isRegularFile(shipped));

        final List<PluginCatalog.Entry> entries = new PluginCatalog(shipped).entries();

        assertEquals(ids(PluginCatalog.defaults()), ids(entries));
        for (int i = 0; i < entries.size(); i++) {
            assertEquals(PluginCatalog.defaults().get(i).getRepository(), entries.get(i).getRepository());
            assertEquals(PluginCatalog.defaults().get(i).getBootFeatures(), entries.get(i).getBootFeatures());
        }
    }

    @Test
    public void fileEntriesReplaceTheDefaultsAndInvalidOnesAreSkipped() throws IOException {
        final Path file = folder.getRoot().toPath().resolve("plugin-catalog.json");
        Files.write(file, ("{\"entries\":[\n"
                + "  {\"id\":\"mine\",\"name\":\"Mine\",\"description\":\"d\",\"repository\":\"me/mine\",\"assetPattern\":\"mine-.*\\\\.kar\",\"docsUrl\":\"https://example.org\",\"bootFeatures\":[\" mine-core \",\"\",null,\"mine-core\",\"mine-ui\"],\"extra\":1},\n"
                + "  {\"id\":\"bad regex\",\"repository\":\"me/x\"},\n"
                + "  {\"id\":\"badregex\",\"repository\":\"me/x\",\"assetPattern\":\"(\"},\n"
                + "  {\"id\":\"norepo\",\"repository\":\"just-a-name\"},\n"
                + "  {\"id\":\"mine\",\"repository\":\"me/dup\"},\n"
                + "  {\"id\":\"nameless\",\"repository\":\"me/nameless\"}\n"
                + "]}").getBytes(StandardCharsets.UTF_8));
        final PluginCatalog catalog = new PluginCatalog(file);

        final List<PluginCatalog.Entry> entries = catalog.entries();

        assertEquals(Arrays.asList("mine", "badregex", "nameless"), ids(entries));
        assertEquals("mine-.*\\.kar", entries.get(0).getAssetPattern());
        assertEquals(PluginCatalog.DEFAULT_ASSET_PATTERN, entries.get(1).getAssetPattern());
        assertEquals("nameless", entries.get(2).getName());
        assertEquals("me/mine", catalog.find("mine").orElseThrow().getRepository());
        assertEquals(Arrays.asList("mine-core", "mine-ui"), catalog.find("mine").orElseThrow().getBootFeatures());
        assertTrue(catalog.find("nameless").orElseThrow().getBootFeatures().isEmpty());
    }

    @Test
    public void unreadableFileFallsBackToTheDefaults() throws IOException {
        final Path file = folder.getRoot().toPath().resolve("plugin-catalog.json");
        Files.write(file, "{not json".getBytes(StandardCharsets.UTF_8));

        assertEquals(ids(PluginCatalog.defaults()), ids(new PluginCatalog(file).entries()));
    }

    @Test
    public void fileWithoutUsableEntriesFallsBackToTheDefaults() throws IOException {
        final Path file = folder.getRoot().toPath().resolve("plugin-catalog.json");
        for (final String content : new String[] { "{\"entries\":[]}", "{}", "{\"entries\":null}", "{\"plugins\":[{\"id\":\"x\",\"repository\":\"a/b\"}]}", "{\"entries\":[{\"id\":\"bad id\",\"repository\":\"a/b\"}]}" }) {
            Files.write(file, content.getBytes(StandardCharsets.UTF_8));
            assertEquals(content, ids(PluginCatalog.defaults()), ids(new PluginCatalog(file).entries()));
        }
    }

    @Test
    public void docsUrlMustBeHttp() throws IOException {
        final Path file = folder.getRoot().toPath().resolve("plugin-catalog.json");
        Files.write(file, ("{\"entries\":[\n"
                + "  {\"id\":\"a\",\"repository\":\"me/a\",\"docsUrl\":\"https://docs.example.org/a\"},\n"
                + "  {\"id\":\"b\",\"repository\":\"me/b\",\"docsUrl\":\"HTTP://docs.example.org/b\"},\n"
                + "  {\"id\":\"c\",\"repository\":\"me/c\",\"docsUrl\":\"javascript:alert(1)\"},\n"
                + "  {\"id\":\"d\",\"repository\":\"me/d\",\"docsUrl\":\"ftp://docs.example.org/d\"},\n"
                + "  {\"id\":\"e\",\"repository\":\"me/e\",\"docsUrl\":\"https://\"},\n"
                + "  {\"id\":\"f\",\"repository\":\"me/f\"}\n"
                + "]}").getBytes(StandardCharsets.UTF_8));

        final PluginCatalog catalog = new PluginCatalog(file);

        assertEquals(Arrays.asList("a", "b", "c", "d", "e", "f"), ids(catalog.entries()));
        assertEquals("https://docs.example.org/a", catalog.find("a").orElseThrow().getDocsUrl());
        assertEquals("HTTP://docs.example.org/b", catalog.find("b").orElseThrow().getDocsUrl());
        assertNull(catalog.find("c").orElseThrow().getDocsUrl());
        assertNull(catalog.find("d").orElseThrow().getDocsUrl());
        assertNull(catalog.find("e").orElseThrow().getDocsUrl());
        assertNull(catalog.find("f").orElseThrow().getDocsUrl());
    }

    private static List<String> ids(final List<PluginCatalog.Entry> entries) {
        return entries.stream().map(PluginCatalog.Entry::getId).collect(Collectors.toList());
    }
}
