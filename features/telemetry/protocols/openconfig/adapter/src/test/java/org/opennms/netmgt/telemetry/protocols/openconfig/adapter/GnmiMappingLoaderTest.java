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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import groovy.lang.GroovyClassLoader;

public class GnmiMappingLoaderTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private GroovyClassLoader gcl;

    @Before
    public void setUp() throws Exception {
        gcl = GnmiMappingScript.load();
    }

    private Object load(String extension, String... lines) throws Exception {
        File file = GnmiMappingScript.writeFile(tmp.getRoot(), UUID.randomUUID() + extension, lines);
        return GnmiMappingScript.call(gcl, "MappingLoader", "load", file);
    }

    private Object loadYaml(String... lines) throws Exception {
        return load(".yaml", lines);
    }

    @SuppressWarnings("unchecked")
    private static List<String> errors(Object result) {
        return (List<String>) GnmiMappingScript.prop(result, "errors");
    }

    @SuppressWarnings("unchecked")
    private static List<String> warnings(Object result) {
        return (List<String>) GnmiMappingScript.prop(result, "warnings");
    }

    @SuppressWarnings("unchecked")
    private static List<Object> groups(Object result) {
        return (List<Object>) GnmiMappingScript.prop(GnmiMappingScript.prop(result, "mapping"), "groups");
    }

    private List<String> parsePath(String path) throws Exception {
        List<?> elems = (List<?>) GnmiMappingScript.call(gcl, "PathParser", "parse", path);
        return elems.stream().map(Object::toString).collect(Collectors.toList());
    }

    private String pathError(String path) throws Exception {
        try {
            GnmiMappingScript.call(gcl, "PathParser", "parse", path);
        } catch (IllegalArgumentException e) {
            return e.getMessage();
        }
        fail("expected IllegalArgumentException for " + path);
        return null;
    }

    @Test
    public void parsesNamesAndKeyPredicates() throws Exception {
        assertThat(parsePath("/interfaces/interface[name=Ethernet1][ifIndex=25]/state"),
                contains("interfaces", "interface[ifIndex=25][name=Ethernet1]", "state"));
    }

    @Test
    public void slashInsideKeyPredicateIsOneElement() throws Exception {
        assertThat(parsePath("/interfaces/interface[name=Ethernet1/1]/state"),
                contains("interfaces", "interface[name=Ethernet1/1]", "state"));
    }

    @Test
    public void stripsModulePrefixesFromNamesNotFromValues() throws Exception {
        assertThat(parsePath("/openconfig-platform:components/component[name=CPU0:CORE0]"),
                contains("components", "component[name=CPU0:CORE0]"));
    }

    @Test
    public void rejectsBrokenPaths() throws Exception {
        assertThat(pathError("/interfaces/interface[name=x"), is("unclosed [ in path /interfaces/interface[name=x"));
        assertThat(pathError("/a//b"), is("empty element in path /a//b"));
        assertThat(pathError("/a[name]"), is("key predicate must look like [key=value] in path /a[name]"));
        assertThat(pathError("/"), is("empty path"));
    }

    @Test
    public void specExampleLoadsWithoutErrors() throws Exception {
        Object result = loadYaml(
                "version: 1",
                "aliases:",
                "  if-counters: /interfaces/interface/state/counters",
                "groups:",
                "  - name: oc-if-counters",
                "    match: if-counters",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes:",
                "      in-octets:       { type: counter }",
                "      out-octets:      { type: counter }",
                "      in-unicast-pkts: { type: counter, name: inUcastPkts }",
                "  - name: oc-if-description",
                "    match: /interfaces/interface/state",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes:",
                "      description: { type: string }",
                "  - name: oc-cpu",
                "    match: /components/component[name=CPU*]/cpu/utilization",
                "    resource-type: cpu",
                "    instance: \"{component.name}\"",
                "    attributes: all",
                "    default-type: gauge");
        assertThat(errors(result), is(empty()));
        assertThat(warnings(result), is(empty()));
        assertThat(groups(result), hasSize(3));
    }

    @Test
    public void attributeKeysAreNormalized() throws Exception {
        Object result = loadYaml(
                "version: 1",
                "groups:",
                "  - name: g",
                "    match: /interfaces/interface/state",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes:",
                "      /openconfig-interfaces:counters/in-octets/: { type: counter }");
        assertThat(errors(result), is(empty()));
        Map<?, ?> attributes = (Map<?, ?>) GnmiMappingScript.prop(groups(result).get(0), "attributes");
        Object spec = attributes.get("counters/in-octets");
        assertEquals("counters-in-octets", GnmiMappingScript.prop(spec, "name"));
    }

    @Test
    public void jsonMappingLoads() throws Exception {
        Object result = load(".json",
                "{\"version\": 1, \"groups\": [{\"name\": \"g\", \"match\": \"/system/state\",",
                " \"resource-type\": \"node\", \"attributes\": {\"uptime\": {\"type\": \"gauge\"}}}]}");
        assertThat(errors(result), is(empty()));
        assertThat(groups(result), hasSize(1));
    }

    @Test
    public void unparsableFileIsAnError() throws Exception {
        Object result = loadYaml("version: 1", "groups: [ {");
        assertThat(errors(result), hasSize(1));
        assertThat(errors(result).get(0), startsWith("cannot parse "));
        assertNull(GnmiMappingScript.prop(result, "mapping"));
    }

    @Test
    public void topLevelMustBeAMap() throws Exception {
        assertThat(errors(loadYaml("- a", "- b")), contains("the top level must be a map"));
    }

    @Test
    public void versionMustBeOne() throws Exception {
        Object result = loadYaml("version: 2", "groups:", "  - name: g", "    match: /a", "    resource-type: node",
                "    attributes: { b: { type: gauge } }");
        assertThat(errors(result), contains("version must be 1"));
    }

    @Test
    public void groupsMustBeANonEmptyList() throws Exception {
        assertThat(errors(loadYaml("version: 1", "groups: []")), contains("groups must be a non-empty list"));
    }

    @Test
    public void groupErrorsNameTheGroup() throws Exception {
        Object result = loadYaml(
                "version: 1",
                "groups:",
                "  - name: unknown-alias",
                "    match: nope",
                "    resource-type: node",
                "    attributes: { a: { type: gauge } }",
                "  - name: bad-type",
                "    match: /a",
                "    resource-type: node",
                "    attributes: { x: { type: meter } }",
                "  - name: node-with-instance",
                "    match: /a",
                "    resource-type: node",
                "    instance: \"{a.b}\"",
                "    attributes: { x: { type: gauge } }",
                "  - name: missing-instance",
                "    match: /a",
                "    resource-type: interface",
                "    attributes: { x: { type: gauge } }",
                "  - name: missing-match",
                "    resource-type: node",
                "    attributes: { x: { type: gauge } }",
                "  - name: all-without-default",
                "    match: /a",
                "    resource-type: node",
                "    attributes: all",
                "  - name: no-attributes",
                "    match: /a",
                "    resource-type: node",
                "  - match: /a",
                "    resource-type: node",
                "    attributes: { x: { type: gauge } }",
                "  - name: missing-resource-type",
                "    match: /a",
                "    attributes: { x: { type: gauge } }",
                "  - name: bad-path",
                "    match: /a[b",
                "    resource-type: node",
                "    attributes: { x: { type: gauge } }");
        List<String> errors = errors(result);
        assertThat(errors, hasItem("group 'unknown-alias': unknown alias 'nope'"));
        assertThat(errors, hasItem("group 'bad-type': attribute 'x': unknown type 'meter'"));
        assertThat(errors, hasItem("group 'node-with-instance': instance is not allowed for resource-type node"));
        assertThat(errors, hasItem("group 'missing-instance': instance is required for resource-type interface"));
        assertThat(errors, hasItem("group 'missing-match': match is required"));
        assertThat(errors, hasItem("group 'all-without-default': attributes: all requires default-type counter or gauge"));
        assertThat(errors, hasItem("group 'no-attributes': attributes must be a map or the word all"));
        assertThat(errors, hasItem("group #8: name is required"));
        assertThat(errors, hasItem("group 'missing-resource-type': resource-type is required"));
        assertThat(errors, hasItem("group 'bad-path': unclosed [ in path /a[b"));
        assertThat(errors, hasSize(10));
        assertNull(GnmiMappingScript.prop(result, "mapping"));
    }

    @Test
    public void longAttributeNameIsAWarningNotAnError() throws Exception {
        Object result = loadYaml(
                "version: 1",
                "groups:",
                "  - name: g",
                "    match: /a",
                "    resource-type: node",
                "    attributes: { b: { type: gauge, name: this-name-is-far-too-long } }");
        assertThat(errors(result), is(empty()));
        assertThat(warnings(result), contains("group 'g': attribute name 'this-name-is-far-too-long' is longer than 19 characters (RRD limit)"));
    }

    @Test
    public void malformedInstanceTemplateIsAnError() throws Exception {
        for (String instance : new String[] {"{interface}", "interface.name", "{interface.name"}) {
            Object result = loadYaml(
                    "version: 1",
                    "groups:",
                    "  - name: g",
                    "    match: /interfaces/interface/state",
                    "    resource-type: interface",
                    "    instance: \"" + instance + "\"",
                    "    attributes: { mtu: { type: gauge } }");
            assertThat(errors(result), contains("group 'g': instance '" + instance + "' must use {element.key} placeholders"));
        }
    }

    @Test
    public void placeholderElementOutsideMatchPathWarns() throws Exception {
        Object typo = loadYaml(
                "version: 1",
                "groups:",
                "  - name: g",
                "    match: /interfaces/interface/state",
                "    resource-type: interface",
                "    instance: \"{interfac.name}\"",
                "    attributes: { mtu: { type: gauge } }");
        assertThat(errors(typo), is(empty()));
        assertThat(warnings(typo), contains("group 'g': instance placeholder '{interfac.name}' names element 'interfac', which is not in the match path"));

        Object wildcard = loadYaml(
                "version: 1",
                "groups:",
                "  - name: g",
                "    match: /interfaces/*/state",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes: { mtu: { type: gauge } }");
        assertThat(warnings(wildcard), is(empty()));
    }

    @Test
    public void aliasWithBrokenPathIsAnError() throws Exception {
        Object result = loadYaml(
                "version: 1",
                "aliases: { broken: '/a[b' }",
                "groups:",
                "  - name: g",
                "    match: /a",
                "    resource-type: node",
                "    attributes: { b: { type: gauge } }");
        assertThat(errors(result), contains("alias 'broken': unclosed [ in path /a[b"));
    }
}
