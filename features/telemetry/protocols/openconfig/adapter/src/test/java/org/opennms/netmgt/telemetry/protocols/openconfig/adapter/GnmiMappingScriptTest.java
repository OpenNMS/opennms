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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;

import com.google.protobuf.TextFormat;

import groovy.lang.GroovyClassLoader;

public class GnmiMappingScriptTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private String previousHome;

    @Before
    public void pointHomeAtTempDir() {
        previousHome = System.getProperty("opennms.home");
        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());
    }

    @After
    public void restoreHome() {
        if (previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousHome);
        }
    }

    private static Gnmi.SubscribeResponse oneCounter() {
        return Gnmi.SubscribeResponse.newBuilder()
                .setUpdate(Gnmi.Notification.newBuilder()
                        .addUpdate(Gnmi.Update.newBuilder()
                                .setPath(Gnmi.Path.newBuilder()
                                        .addElem(Gnmi.PathElem.newBuilder().setName("system"))
                                        .addElem(Gnmi.PathElem.newBuilder().setName("uptime")))
                                .setVal(Gnmi.TypedValue.newBuilder().setUintVal(3))))
                .build();
    }

    @Test
    public void scriptHasNoMavenFilterTokens() throws Exception {
        String text = Files.readString(GnmiMappingScript.file().toPath());
        assertFalse("the script must not contain a dollar sign", text.contains("$"));
        assertFalse("the script must not contain an @word@ token", Pattern.compile("@[A-Za-z0-9._-]+@").matcher(text).find());
    }

    @Test
    public void noMappingEmitsNothing() throws Exception {
        assertEquals("", GnmiMappingScript.render(GnmiMappingScript.run(oneCounter())));
    }

    @Test
    public void nullAgentEmitsNothing() throws Exception {
        GnmiMappingScript.writeFile(new File(tmp.getRoot(), GnmiMappingScript.MAPPING_DIR), "sys.yaml",
                "version: 1",
                "groups:",
                "  - name: sys",
                "    match: /system",
                "    resource-type: node",
                "    attributes: { uptime: { type: gauge } }");
        GroovyClassLoader gcl = GnmiMappingScript.load();
        CollectionSetBuilder builder = new CollectionSetBuilder(Mockito.mock(CollectionAgent.class));
        GnmiMappingScript.call(gcl, "GnmiMapping", "apply", null, builder, oneCounter());
        assertEquals(0, builder.getNumAttributes());
    }

    @Test
    public void gnmiNanosecondTimestampBecomesMilliseconds() throws Exception {
        GnmiMappingScript.writeFile(new File(tmp.getRoot(), GnmiMappingScript.MAPPING_DIR), "sys.yaml",
                "version: 1",
                "groups:",
                "  - name: sys",
                "    match: /system",
                "    resource-type: node",
                "    attributes: { uptime: { type: gauge } }");
        long nanos = 1751293808127192960L;
        Gnmi.SubscribeResponse message = oneCounter().toBuilder()
                .setUpdate(oneCounter().getUpdate().toBuilder().setTimestamp(nanos))
                .build();
        CollectionAgent agent = Mockito.mock(CollectionAgent.class);
        Mockito.when(agent.getNodeId()).thenReturn(1);
        // OpenConfigAdapter passes the notification timestamp straight through as milliseconds
        CollectionSet set = new ScriptedCollectionSetBuilder(GnmiMappingScript.file()).build(agent, message, nanos);
        assertEquals(1751293808127L, set.getCollectionTimestamp().getTime());
    }

    @Test
    public void shippedMappingsAreValid() throws Exception {
        List<Path> shipped = shippedMappings();
        assertThat(shipped, hasSize(5));
        GroovyClassLoader gcl = GnmiMappingScript.load();
        for (Path sample : shipped) {
            Object result = GnmiMappingScript.call(gcl, "MappingLoader", "load", sample.toFile());
            assertThat(sample.toString(), (List<?>) GnmiMappingScript.prop(result, "errors"), is(empty()));
            assertThat(sample.toString(), (List<?>) GnmiMappingScript.prop(result, "warnings"), is(empty()));
        }
    }

    /** The shipped mapping files, copied into the test classpath by the POM. */
    private static List<Path> shippedMappings() throws Exception {
        Path dir = Paths.get(GnmiMappingScript.class.getResource("/openconfig-interfaces.yaml").toURI()).getParent();
        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(f -> {
                String name = f.getFileName().toString();
                return (name.startsWith("openconfig-") || name.startsWith("junos-")) && name.endsWith(".yaml");
            }).sorted().collect(Collectors.toList());
        }
    }

    @Test
    public void shippedMappingsMapJuniperMx10004() throws Exception {
        Path target = tmp.getRoot().toPath().resolve(GnmiMappingScript.MAPPING_DIR);
        Files.createDirectories(target);
        for (Path sample : shippedMappings()) {
            Files.copy(sample, target.resolve(sample.getFileName()));
        }

        // Notifications captured from an MX10004 with JSON encoding, a few leaves each, separated by ---
        Path samples = Paths.get("src/test/resources/gnmi-mapping-samples");
        List<String> lines = new ArrayList<>();
        for (String block : Files.readString(samples.resolve("juniper-mx10004.textproto")).split("\n---\n")) {
            Gnmi.SubscribeResponse.Builder response = Gnmi.SubscribeResponse.newBuilder();
            TextFormat.merge(block, response);
            String rendered = GnmiMappingScript.render(GnmiMappingScript.run(response.build()));
            if (!rendered.isEmpty()) {
                Collections.addAll(lines, rendered.split("\n"));
            }
        }
        Collections.sort(lines);
        assertEquals(Files.readString(samples.resolve("juniper-mx10004.expected.txt")).trim(), String.join("\n", lines));
    }
}
