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
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import groovy.lang.GroovyClassLoader;

public class GnmiMappingHolderTest {

    private static final String[] ONE_GROUP = {
            "version: 1",
            "groups:",
            "  - name: a",
            "    match: /a",
            "    resource-type: node",
            "    attributes: { b: { type: gauge } }"};

    private static final String[] TWO_GROUPS = {
            "version: 1",
            "groups:",
            "  - name: a",
            "    match: /a",
            "    resource-type: node",
            "    attributes: { b: { type: gauge } }",
            "  - name: c",
            "    match: /c",
            "    resource-type: node",
            "    attributes: { d: { type: gauge } }"};

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private GroovyClassLoader gcl;
    private String previousHome;
    private File dir;

    @Before
    public void setUp() throws Exception {
        gcl = GnmiMappingScript.load();
        previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
        dir = new File(tmp.getRoot(), GnmiMappingScript.MAPPING_DIR);
    }

    @After
    public void restoreHome() {
        if (previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousHome);
        }
    }

    private Object current() throws Exception {
        return GnmiMappingScript.call(gcl, "MappingHolder", "current");
    }

    private static List<?> groups(Object mapping) {
        return (List<?>) GnmiMappingScript.prop(mapping, "groups");
    }

    /** Rewrites the file and moves its mtime forward, so a same-size rewrite is still seen. */
    private static void rewrite(File dir, String name, String... lines) throws Exception {
        File file = GnmiMappingScript.writeFile(dir, name, lines);
        file.setLastModified(file.lastModified() + 5000);
    }

    private static List<String> groupNames(Object mapping) {
        List<String> names = new ArrayList<>();
        for (Object group : groups(mapping)) {
            names.add((String) GnmiMappingScript.prop(group, "name"));
        }
        return names;
    }

    @Test
    public void returnsNullWhenNoFileExists() throws Exception {
        assertNull(current());
    }

    @Test
    public void locatesTheConfigDirectoryUnderOpennmsHome() throws Exception {
        assertEquals(dir, GnmiMappingScript.call(gcl, "MappingHolder", "locate"));
    }

    @Test
    public void loadsYamlFromOpennmsHome() throws Exception {
        GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml", ONE_GROUP);
        assertThat(groups(current()), hasSize(1));
    }

    @Test
    public void loadsJson() throws Exception {
        GnmiMappingScript.writeFile(dir, "acme-switch.json",
                "{\"version\": 1, \"groups\": [{\"name\": \"a\", \"match\": \"/a\", \"resource-type\": \"node\",",
                " \"attributes\": {\"b\": {\"type\": \"gauge\"}}}]}");
        assertThat(groups(current()), hasSize(1));
    }

    @Test
    public void mergesGroupsFromAllFilesInNameOrder() throws Exception {
        GnmiMappingScript.writeFile(dir, "b-vendor.yml", ONE_GROUP);
        GnmiMappingScript.writeFile(dir, "a-vendor.yaml", TWO_GROUPS);
        assertEquals(Arrays.asList("a", "c", "a"), groupNames(current()));
    }

    @Test
    public void ignoresFilesWithOtherExtensions() throws Exception {
        GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml", ONE_GROUP);
        GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml.bak", TWO_GROUPS);
        GnmiMappingScript.writeFile(dir, "README.txt", "not a mapping");
        assertThat(groups(current()), hasSize(1));
    }

    @Test
    public void cachesUntilAFileChanges() throws Exception {
        GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml", ONE_GROUP);
        Object first = current();
        assertSame(first, current());
        rewrite(dir, "juniper-mx-series.yaml", TWO_GROUPS);
        assertThat(groups(current()), hasSize(2));
    }

    @Test
    public void reloadsWhenAFileIsAdded() throws Exception {
        GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml", ONE_GROUP);
        assertThat(groups(current()), hasSize(1));
        GnmiMappingScript.writeFile(dir, "cisco-nxos.yaml", TWO_GROUPS);
        assertThat(groups(current()), hasSize(3));
    }

    @Test
    public void keepsPreviousMappingWhenReloadIsInvalid() throws Exception {
        GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml", ONE_GROUP);
        Object good = current();
        rewrite(dir, "juniper-mx-series.yaml", "version: 1", "groups: []");
        assertSame(good, current());
    }

    @Test
    public void oneInvalidFileKeepsThePreviousMappingForAll() throws Exception {
        GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml", ONE_GROUP);
        Object good = current();
        GnmiMappingScript.writeFile(dir, "cisco-nxos.yaml", "not: [valid");
        assertSame(good, current());
    }

    @Test
    public void keepsPreviousMappingWhenAllFilesAreDeleted() throws Exception {
        File file = GnmiMappingScript.writeFile(dir, "juniper-mx-series.yaml", ONE_GROUP);
        Object good = current();
        file.delete();
        assertSame(good, current());
    }
}
