# Declarative gNMI Mapping (Phase B) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ship a Groovy adapter script, `openconfig-gnmi-mapping.groovy`, that maps gNMI `SubscribeResponse` messages to OpenNMS collection sets from a declarative YAML (or JSON) mapping file, and validate it in the nl6 lab.

**Architecture:** One Groovy script holds the whole engine as static nested classes (`MappingLoader`, `LeafFlattener`, `LeafMatcher`, `ResourceEmitter`, `MappingHolder`), structured for a later line-by-line Java port (phase A). Java tests in the adapter module load the shipped script straight from `opennms-base-assembly`: unit tests reach the nested classes through a `GroovyClassLoader`, and golden fixture tests run the whole script through `ScriptedCollectionSetBuilder`. The mapping file is found under `opennms.home`, cached, and reloaded when its mtime or size changes.

**Tech Stack:** Groovy 3.0.21 (JSR-223), protobuf-java (`TextFormat` for fixtures), SnakeYAML 2.5 and `groovy.json.JsonSlurper` loaded reflectively, JUnit 4 + Hamcrest + Mockito, Maven via `./compile.pl`.

**Spec:** `docs/superpowers/specs/2026-10-12-gnmi-declarative-mapping-design.md`

## Global Constraints

- The script lives at `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy`. That directory is Maven-filtered. The script must contain no `$` character and no `@word@` sequence. A test enforces this.
- The mapping file is `System.getProperty("opennms.home") + "/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml"`. If it does not exist, `openconfig-gnmi-mapping.json` in the same directory is used.
- Schema `version` must be `1`.
- `type` is one of `counter`, `gauge`, `string`. `default-type` is `counter` or `gauge`.
- RRD datasource name limit: 19 characters (warning only).
- The shipped `openconfig-gnmi-telemetry.groovy` stays unchanged.
- No Java changes outside tests. The adapter, connector and `ScriptedCollectionSetBuilder` are not touched.
- New Java files begin with the TOG AGPL header block exactly as in `OpenConfigAdapterTest.java` (no SPDX line, no author line). The Groovy script uses the same block. `license-maven-plugin` enforces it.
- Write the script in Java-like Groovy: typed locals, explicit loops, no metaprogramming. The only dynamic calls allowed are on the reflectively loaded YAML and JSON parsers.
- Commits use Conventional Commits, `git commit -s`, and end with `Assisted-by: ClaudeCode:claude-opus-5-5`.

Clarifications of the spec made by this plan:

- All JSON arrays are skipped with a one-time warning, not only arrays of objects. A leaf-list of scalars has no stable attribute name either.
- An unexpected exception while mapping a message is logged with the node id. The group is not known at that level.
- A null `agent` (source address not matched to a node) is skipped quietly. The shipped script throws a `NullPointerException` in that case.

## Review Focus

The inputs below are implied by the spec but easy to miss. Each one has a test in the task that owns the code.

1. A key predicate whose value contains `/` in a mapping path, such as `match: /interfaces/interface[name=Ethernet1/1]/state`. It must parse as one element, not be split. Test: Task 2, `slashInsideKeyPredicateIsOneElement`.
2. Attribute keys written with module prefixes, such as `openconfig-interfaces:counters/in-octets`. They must match the stripped leaf path. Test: Task 2, `attributeKeysAreNormalized`.
3. One malformed `json_ietf_val` in a message. That update is skipped and the other updates in the same message still map. Test: Task 3, `malformedJsonSkipsOnlyThatUpdate`.
4. A leaf whose path equals the group's `match` path exactly. There is no remainder to name an attribute, so it does not match and does not throw. Test: Task 4, `leafEqualToMatchPathDoesNotMatch`.
5. A message from an unknown source address, where the adapter passes a null `agent`. Nothing is emitted and nothing throws. Test: Task 5, `nullAgentEmitsNothing`.

## File Structure

| File | Responsibility |
|---|---|
| `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy` (create) | The engine and the script entry point |
| `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml` (create) | Shipped sample mapping |
| `opennms-base-assembly/src/main/filtered/etc/resource-types.d/openconfig-gnmi-resource.xml` (create) | `ocComponent` generic resource type used by the sample |
| `features/telemetry/protocols/openconfig/adapter/pom.xml` (modify) | Test resources from the assembly, SnakeYAML test dependency |
| `features/telemetry/protocols/openconfig/adapter/src/test/java/org/opennms/netmgt/telemetry/protocols/openconfig/adapter/GnmiMappingScript.java` (create) | Test helper: load the script, call nested classes, run it, render collection sets |
| `.../adapter/GnmiMappingScriptTest.java` (create) | Whole-script checks: no filter tokens, no mapping, null agent, shipped sample |
| `.../adapter/GnmiMappingLoaderTest.java` (create) | `PathParser` and `MappingLoader` validation |
| `.../adapter/GnmiLeafFlattenerTest.java` (create) | `LeafFlattener` |
| `.../adapter/GnmiLeafMatcherTest.java` (create) | `LeafMatcher`, templates, sanitizing |
| `.../adapter/GnmiMappingHolderTest.java` (create) | File location, reload, keep-last-good |
| `.../adapter/GnmiMappingFixtureTest.java` (create) | Parameterized golden fixture runner |
| `features/telemetry/protocols/openconfig/adapter/src/test/resources/gnmi-mapping/<case>/` (create) | Golden fixtures: `mapping.yaml` or `mapping.json`, `input.textproto`, `expected.txt` |
| `docs/modules/reference/pages/telemetryd/protocols/openconfig.adoc` (modify) | New "Declarative mapping" section |

All test classes live in package `org.opennms.netmgt.telemetry.protocols.openconfig.adapter` under `features/telemetry/protocols/openconfig/adapter/src/test/java/`. The task text abbreviates that directory as `.../adapter/`.

## Build commands

Run from the repository root.

- One test class: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest=<ClassName>`
- All module tests: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test`
- Header check: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter license:check`
- If offline resolution fails because sibling SNAPSHOT modules are missing from `~/.m2`, run once: `./compile.pl -DskipTests -am -pl features/telemetry/protocols/openconfig/adapter install`

---

### Task 1: Probe parser visibility in Karaf

This decides whether the customer writes YAML or JSON in the lab. It does not block Tasks 2 to 6, because the script loads both parsers reflectively and reports which one is missing.

**Files:**
- Create: `triage/gnmi-fixes-verify-2026-09-28/results/D-declarative-mapping.md` (lab notes, not committed)

**Interfaces:**
- Consumes: nothing
- Produces: a recorded verdict, "YAML", "JSON only", or "neither", used in Task 7

- [ ] **Step 1: Start the lab Core on stock 36.0.4**

```bash
cd triage/gnmi-fixes-verify-2026-09-28
IMAGE_TAG=36.0.4 docker compose up -d database kafka core
```

Wait until `curl -sf -u admin:admin http://localhost:8980/opennms/rest/info` returns JSON.

- [ ] **Step 2: List the Groovy and SnakeYAML bundles**

```bash
ssh -o StrictHostKeyChecking=no -p 8101 admin@localhost 'bundle:list -s -t 0 | grep -iE "groovy|snakeyaml"'
```

Password: `admin`. Note the bundle ids of `org.codehaus.groovy` (core), `org.codehaus.groovy.jsr223` and `org.codehaus.groovy.json`, and whether any `org.yaml.snakeyaml` bundle is listed.

- [ ] **Step 3: Check the dynamic imports and the exported packages**

```bash
ssh -p 8101 admin@localhost 'bundle:headers <groovy-core-id> | grep -A2 -i DynamicImport'
ssh -p 8101 admin@localhost 'package:exports | grep -E "org.yaml.snakeyaml |groovy.json "'
```

- [ ] **Step 4: Record the verdict**

Write `results/D-declarative-mapping.md` with the raw output and one verdict line:

- "YAML": the Groovy core bundle has `DynamicImport-Package: *` and `org.yaml.snakeyaml` is exported.
- "JSON only": `groovy.json` is exported but `org.yaml.snakeyaml` is not.
- "neither": stop and report to Ronny before Task 7. The script would need a different parser source.

The verdict is a strong signal, not proof. Task 7 Step 5 confirms it from the script's own log line.

---

### Task 2: Test harness, path parser and mapping loader

**Files:**
- Modify: `features/telemetry/protocols/openconfig/adapter/pom.xml`
- Create: `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy`
- Create: `.../adapter/GnmiMappingScript.java`
- Create: `.../adapter/GnmiMappingScriptTest.java`
- Create: `.../adapter/GnmiMappingLoaderTest.java`

**Interfaces:**
- Consumes: nothing
- Produces (Groovy classes in the script, used by Tasks 3 to 5):
  - `PathElem(String name, Map<String,String> keys)`, fields `name`, `keys`, `toString()` renders `name[k=v]` with keys sorted
  - `Names.stripModule(String) -> String`
  - `PathParser.parse(String) -> List<PathElem>`, throws `IllegalArgumentException`
  - `Glob.compile(String) -> Pattern`
  - `PatternElem(PathElem)`, `boolean matches(PathElem)`
  - `AttrSpec(String type, String name)`
  - `Group` with fields `name`, `match` (`List<PatternElem>`), `resourceType`, `instance`, `all`, `defaultType`, `attributes` (`Map<String,AttrSpec>`)
  - `CompiledMapping(List<Group> groups)`
  - `LoadResult` with `mapping`, `errors` (`List<String>`), `warnings` (`List<String>`)
  - `Parsers.parseYaml(String)`, `Parsers.parseJson(String)`, `Parsers.parseJsonValue(String)`
  - `AttributeKeys.normalize(String) -> String`
  - `MappingLoader.load(File) -> LoadResult`, `MappingLoader.compile(Object doc) -> LoadResult`
- Produces (Java test helper `GnmiMappingScript`, used by all later test classes):
  - `static File file()`
  - `static GroovyClassLoader load() throws IOException`
  - `static Object call(GroovyClassLoader gcl, String className, String method, Object... args) throws ClassNotFoundException`
  - `static Object newInstance(GroovyClassLoader gcl, String className, Object... args) throws ClassNotFoundException`
  - `static Object prop(Object target, String name)`
  - `static File writeFile(File dir, String name, String... lines) throws IOException`

- [ ] **Step 1: Add the test resources and the SnakeYAML test dependency to the adapter pom**

In `features/telemetry/protocols/openconfig/adapter/pom.xml`, insert at the top of `<build>`, before `<plugins>`:

```xml
    <testResources>
      <testResource>
        <directory>src/test/resources</directory>
      </testResource>
      <testResource>
        <!-- Test the shipped files, not copies -->
        <directory>${project.basedir}/../../../../../opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters</directory>
        <includes>
          <include>openconfig-gnmi-mapping.groovy</include>
          <include>openconfig-gnmi-mapping.yaml</include>
        </includes>
      </testResource>
    </testResources>
```

Add to `<dependencies>`:

```xml
    <dependency>
      <groupId>org.yaml</groupId>
      <artifactId>snakeyaml</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.hamcrest</groupId>
      <artifactId>hamcrest-library</artifactId>
      <scope>test</scope>
    </dependency>
```

Both versions are managed in the root pom (`snakeyamlVersion` 2.5, `hamcrest-library` 1.3). If Maven reports a missing version, add `<version>${snakeyamlVersion}</version>` or `<version>1.3</version>`.

- [ ] **Step 2: Create the test helper `GnmiMappingScript.java`**

```java
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.GroovyClassLoader;

/**
 * Loads the shipped openconfig-gnmi-mapping.groovy and gives tests access to its nested classes.
 */
final class GnmiMappingScript {

    private GnmiMappingScript() {
    }

    static File file() {
        try {
            return new File(GnmiMappingScript.class.getResource("/openconfig-gnmi-mapping.groovy").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Compiles the script into a fresh class loader, so static state never leaks between tests. */
    static GroovyClassLoader load() throws IOException {
        GroovyClassLoader gcl = new GroovyClassLoader(GnmiMappingScript.class.getClassLoader());
        gcl.parseClass(Files.readString(file().toPath()), "GnmiMappingScriptUnderTest.groovy");
        return gcl;
    }

    static Object call(GroovyClassLoader gcl, String className, String method, Object... args) throws ClassNotFoundException {
        return InvokerHelper.invokeStaticMethod(gcl.loadClass(className), method, args);
    }

    static Object newInstance(GroovyClassLoader gcl, String className, Object... args) throws ClassNotFoundException {
        return InvokerHelper.invokeConstructorOf(gcl.loadClass(className), args);
    }

    static Object prop(Object target, String name) {
        return InvokerHelper.getProperty(target, name);
    }

    static File writeFile(File dir, String name, String... lines) throws IOException {
        File file = new File(dir, name);
        Files.createDirectories(dir.toPath());
        Files.writeString(file.toPath(), String.join("\n", lines) + "\n");
        return file;
    }
}
```

- [ ] **Step 3: Write the failing tests**

`GnmiMappingScriptTest.java` (TOG header, then):

```java
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.junit.Assert.assertFalse;

import java.nio.file.Files;
import java.util.regex.Pattern;

import org.junit.Test;

public class GnmiMappingScriptTest {

    @Test
    public void scriptHasNoMavenFilterTokens() throws Exception {
        String text = Files.readString(GnmiMappingScript.file().toPath());
        assertFalse("the script must not contain a dollar sign", text.contains("$"));
        assertFalse("the script must not contain an @word@ token", Pattern.compile("@[A-Za-z0-9._-]+@").matcher(text).find());
    }
}
```

`GnmiMappingLoaderTest.java` (TOG header, then):

```java
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
```

- [ ] **Step 4: Run the tests to verify they fail**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest='GnmiMappingScriptTest,GnmiMappingLoaderTest'`
Expected: FAIL. `GnmiMappingScript.file()` throws a `NullPointerException` because `/openconfig-gnmi-mapping.groovy` is not on the test classpath yet.

- [ ] **Step 5: Create the script with the loader classes**

Create `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy`:

```groovy
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

// Maps gNMI SubscribeResponse messages to collection sets using a declarative
// mapping file: etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml, or
// openconfig-gnmi-mapping.json when no YAML file exists.
//
// Maven filters this file when it is packaged. Never use the dollar sign in it,
// and never write a word between two at signs.

import groovy.util.logging.Slf4j
import org.opennms.features.openconfig.proto.gnmi.Gnmi
import org.opennms.netmgt.collection.api.AttributeType
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource
import org.opennms.netmgt.collection.support.builder.NodeLevelResource
import org.opennms.netmgt.collection.support.builder.Resource

import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

class PathElem {
    final String name
    final Map<String, String> keys

    PathElem(String name, Map<String, String> keys) {
        this.name = name
        this.keys = keys
    }

    String toString() {
        StringBuilder sb = new StringBuilder(name)
        for (Map.Entry<String, String> e : new TreeMap<String, String>(keys).entrySet()) {
            sb.append('[').append(e.getKey()).append('=').append(e.getValue()).append(']')
        }
        return sb.toString()
    }
}

class Names {
    static String stripModule(String name) {
        int colon = name.lastIndexOf(':')
        return colon < 0 ? name : name.substring(colon + 1)
    }
}

class PathParser {
    static List<PathElem> parse(String path) {
        String p = path.trim()
        List<PathElem> elems = new ArrayList<>()
        int i = p.startsWith('/') ? 1 : 0
        while (i < p.length()) {
            StringBuilder name = new StringBuilder()
            while (i < p.length() && p.charAt(i) != ('/' as char) && p.charAt(i) != ('[' as char)) {
                name.append(p.charAt(i))
                i++
            }
            Map<String, String> keys = new LinkedHashMap<>()
            while (i < p.length() && p.charAt(i) == ('[' as char)) {
                int close = p.indexOf(']', i)
                if (close < 0) {
                    throw new IllegalArgumentException('unclosed [ in path ' + path)
                }
                String kv = p.substring(i + 1, close)
                int eq = kv.indexOf('=')
                if (eq <= 0) {
                    throw new IllegalArgumentException('key predicate must look like [key=value] in path ' + path)
                }
                keys.put(Names.stripModule(kv.substring(0, eq)), kv.substring(eq + 1))
                i = close + 1
            }
            if (name.length() == 0) {
                throw new IllegalArgumentException('empty element in path ' + path)
            }
            elems.add(new PathElem(Names.stripModule(name.toString()), keys))
            if (i < p.length()) {
                if (p.charAt(i) != ('/' as char)) {
                    throw new IllegalArgumentException('unexpected character after ] in path ' + path)
                }
                i++
            }
        }
        if (elems.isEmpty()) {
            throw new IllegalArgumentException('empty path')
        }
        return elems
    }
}

class Glob {
    static Pattern compile(String glob) {
        String[] parts = glob.split('\\*', -1)
        StringBuilder re = new StringBuilder()
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                re.append('.*')
            }
            re.append(Pattern.quote(parts[i]))
        }
        return Pattern.compile(re.toString())
    }
}

class PatternElem {
    final String name
    final Map<String, Pattern> keyGlobs = new LinkedHashMap<>()

    PatternElem(PathElem elem) {
        this.name = elem.name
        for (Map.Entry<String, String> e : elem.keys.entrySet()) {
            keyGlobs.put(e.getKey(), Glob.compile(e.getValue()))
        }
    }

    boolean matches(PathElem elem) {
        if (name != '*' && name != elem.name) {
            return false
        }
        for (Map.Entry<String, Pattern> e : keyGlobs.entrySet()) {
            String actual = elem.keys.get(e.getKey())
            if (actual == null || !e.getValue().matcher(actual).matches()) {
                return false
            }
        }
        return true
    }
}

class AttrSpec {
    final String type
    final String name

    AttrSpec(String type, String name) {
        this.type = type
        this.name = name
    }
}

class Group {
    String name
    List<PatternElem> match
    String resourceType
    String instance
    boolean all
    String defaultType
    Map<String, AttrSpec> attributes = new LinkedHashMap<>()
}

class CompiledMapping {
    final List<Group> groups

    CompiledMapping(List<Group> groups) {
        this.groups = groups
    }
}

class LoadResult {
    CompiledMapping mapping
    final List<String> errors = new ArrayList<>()
    final List<String> warnings = new ArrayList<>()
}

class Parsers {
    static Object parseYaml(String text) {
        Class<?> yamlClass
        try {
            yamlClass = Class.forName('org.yaml.snakeyaml.Yaml', true, Parsers.class.getClassLoader())
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException('SnakeYAML is not available to scripts, write the mapping as openconfig-gnmi-mapping.json instead')
        }
        def yaml = yamlClass.getDeclaredConstructor().newInstance()
        return yaml.load(text)
    }

    static Object parseJson(String text) {
        Class<?> slurperClass = Class.forName('groovy.json.JsonSlurper', true, Parsers.class.getClassLoader())
        def slurper = slurperClass.getDeclaredConstructor().newInstance()
        return slurper.parseText(text)
    }

    // Parses any JSON value, including bare scalars such as 15 or "UP"
    static Object parseJsonValue(String text) {
        List wrapped = (List) parseJson('[' + text + ']')
        return wrapped.get(0)
    }
}

class AttributeKeys {
    static String normalize(String key) {
        List<String> parts = new ArrayList<>()
        for (String part : key.split('/')) {
            if (!part.isEmpty()) {
                parts.add(Names.stripModule(part))
            }
        }
        return parts.join('/')
    }
}

class MappingLoader {
    static final Set<String> TYPES = ['counter', 'gauge', 'string'] as Set
    static final Set<String> NUMERIC_TYPES = ['counter', 'gauge'] as Set
    static final int RRD_NAME_LIMIT = 19

    static LoadResult load(File file) {
        Object doc
        try {
            String text = file.getText('UTF-8')
            doc = file.getName().endsWith('.json') ? Parsers.parseJson(text) : Parsers.parseYaml(text)
        } catch (Exception e) {
            LoadResult failed = new LoadResult()
            failed.errors.add('cannot parse ' + file + ': ' + e.getMessage())
            return failed
        }
        return compile(doc)
    }

    static LoadResult compile(Object doc) {
        LoadResult result = new LoadResult()
        if (!(doc instanceof Map)) {
            result.errors.add('the top level must be a map')
            return result
        }
        Map top = (Map) doc
        Object version = top.get('version')
        if (!(version instanceof Number) || ((Number) version).intValue() != 1) {
            result.errors.add('version must be 1')
        }
        Map<String, List<PathElem>> aliases = compileAliases(top.get('aliases'), result)
        Object rawGroups = top.get('groups')
        if (!(rawGroups instanceof List) || ((List) rawGroups).isEmpty()) {
            result.errors.add('groups must be a non-empty list')
            return result
        }
        List<Group> groups = new ArrayList<>()
        int index = 0
        for (Object rawGroup : (List) rawGroups) {
            index++
            if (!(rawGroup instanceof Map)) {
                result.errors.add('group #' + index + ': must be a map')
                continue
            }
            Group group = compileGroup((Map) rawGroup, index, aliases, result)
            if (group != null) {
                groups.add(group)
            }
        }
        if (result.errors.isEmpty()) {
            result.mapping = new CompiledMapping(groups)
        }
        return result
    }

    private static Map<String, List<PathElem>> compileAliases(Object rawAliases, LoadResult result) {
        Map<String, List<PathElem>> aliases = new LinkedHashMap<>()
        if (rawAliases == null) {
            return aliases
        }
        if (!(rawAliases instanceof Map)) {
            result.errors.add('aliases must be a map of name to path')
            return aliases
        }
        for (Map.Entry e : ((Map) rawAliases).entrySet()) {
            try {
                aliases.put(String.valueOf(e.getKey()), PathParser.parse(String.valueOf(e.getValue())))
            } catch (IllegalArgumentException ex) {
                result.errors.add('alias \'' + e.getKey() + '\': ' + ex.getMessage())
            }
        }
        return aliases
    }

    private static Group compileGroup(Map raw, int index, Map<String, List<PathElem>> aliases, LoadResult result) {
        int errorsBefore = result.errors.size()
        Group group = new Group()
        group.name = raw.get('name') == null ? null : String.valueOf(raw.get('name'))
        String label = group.name == null ? 'group #' + index : 'group \'' + group.name + '\''
        if (group.name == null) {
            result.errors.add(label + ': name is required')
        }

        Object match = raw.get('match')
        if (match == null) {
            result.errors.add(label + ': match is required')
        } else {
            String m = String.valueOf(match)
            List<PathElem> path = aliases.get(m)
            if (path == null && m.startsWith('/')) {
                try {
                    path = PathParser.parse(m)
                } catch (IllegalArgumentException ex) {
                    result.errors.add(label + ': ' + ex.getMessage())
                }
            } else if (path == null) {
                result.errors.add(label + ': unknown alias \'' + m + '\'')
            }
            if (path != null) {
                List<PatternElem> patterns = new ArrayList<>()
                for (PathElem elem : path) {
                    patterns.add(new PatternElem(elem))
                }
                group.match = patterns
            }
        }

        group.resourceType = raw.get('resource-type') == null ? null : String.valueOf(raw.get('resource-type'))
        if (group.resourceType == null) {
            result.errors.add(label + ': resource-type is required')
        }
        group.instance = raw.get('instance') == null ? null : String.valueOf(raw.get('instance'))
        if (group.resourceType == 'node' && group.instance != null) {
            result.errors.add(label + ': instance is not allowed for resource-type node')
        }
        if (group.resourceType != null && group.resourceType != 'node' && group.instance == null) {
            result.errors.add(label + ': instance is required for resource-type ' + group.resourceType)
        }

        Object attributes = raw.get('attributes')
        if (attributes == 'all') {
            group.all = true
            Object defaultType = raw.get('default-type')
            if (!NUMERIC_TYPES.contains(defaultType)) {
                result.errors.add(label + ': attributes: all requires default-type counter or gauge')
            } else {
                group.defaultType = (String) defaultType
            }
        } else if (attributes instanceof Map && !((Map) attributes).isEmpty()) {
            for (Map.Entry e : ((Map) attributes).entrySet()) {
                String key = AttributeKeys.normalize(String.valueOf(e.getKey()))
                Map spec = e.getValue() instanceof Map ? (Map) e.getValue() : new LinkedHashMap()
                Object type = spec.get('type')
                if (!TYPES.contains(type)) {
                    result.errors.add(label + ': attribute \'' + key + '\': unknown type \'' + type + '\'')
                    continue
                }
                String name = spec.get('name') == null ? key.replace('/', '-') : String.valueOf(spec.get('name'))
                if (name.length() > RRD_NAME_LIMIT) {
                    result.warnings.add(label + ': attribute name \'' + name + '\' is longer than ' + RRD_NAME_LIMIT + ' characters (RRD limit)')
                }
                group.attributes.put(key, new AttrSpec((String) type, name))
            }
        } else {
            result.errors.add(label + ': attributes must be a map or the word all')
        }
        return result.errors.size() == errorsBefore ? group : null
    }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest='GnmiMappingScriptTest,GnmiMappingLoaderTest'`
Expected: PASS, 15 tests.

If `GnmiMappingScript.call` fails with `ClassNotFoundException: MappingLoader`, the `GroovyClassLoader` did not cache the nested classes. Replace the body of `call` and `newInstance` with a lookup over `gcl.getLoadedClasses()` by `getName()`, and rerun.

- [ ] **Step 7: Commit**

```bash
git add features/telemetry/protocols/openconfig/adapter/pom.xml \
  opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy \
  features/telemetry/protocols/openconfig/adapter/src/test/java/org/opennms/netmgt/telemetry/protocols/openconfig/adapter/GnmiMappingScript.java \
  features/telemetry/protocols/openconfig/adapter/src/test/java/org/opennms/netmgt/telemetry/protocols/openconfig/adapter/GnmiMappingScriptTest.java \
  features/telemetry/protocols/openconfig/adapter/src/test/java/org/opennms/netmgt/telemetry/protocols/openconfig/adapter/GnmiMappingLoaderTest.java
git commit -s -m "feat(telemetry): load declarative gNMI mapping files" -m "Assisted-by: ClaudeCode:claude-opus-5-5"
```

---

### Task 3: Leaf flattener

**Files:**
- Modify: `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy` (append classes)
- Create: `.../adapter/GnmiLeafFlattenerTest.java`

**Interfaces:**
- Consumes: `PathElem`, `Names.stripModule`, `Parsers.parseJsonValue` (Task 2)
- Produces:
  - `Leaf(List<PathElem> path, Object value)`, fields `path`, `value`, `toString()` renders `/a/b[k=v]/c = <value> (<SimpleClassName>)`
  - `RateLimitedLog.allow(String key) -> boolean`, true at most once per key per 60 s
  - `LeafFlattener.flatten(Gnmi.SubscribeResponse) -> List<Leaf>`
  - Leaf value types: `String`, `Long`, `BigInteger` (uint64 above `Long.MAX_VALUE`), `Boolean`, `Double` (from float), `BigDecimal` (decimal64), and from JSON whatever `JsonSlurper` returns (`String`, `Integer`, `Long`, `BigInteger`, `BigDecimal`, `Boolean`)

- [ ] **Step 1: Write the failing test**

`GnmiLeafFlattenerTest.java` (TOG header, then):

```java
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;

import com.google.protobuf.ByteString;

import groovy.lang.GroovyClassLoader;

public class GnmiLeafFlattenerTest {

    private GroovyClassLoader gcl;

    @Before
    public void setUp() throws Exception {
        gcl = GnmiMappingScript.load();
    }

    private static Gnmi.PathElem elem(String name) {
        return Gnmi.PathElem.newBuilder().setName(name).build();
    }

    private static Gnmi.PathElem elem(String name, String key, String value) {
        return Gnmi.PathElem.newBuilder().setName(name).putKey(key, value).build();
    }

    private static Gnmi.Path path(Gnmi.PathElem... elems) {
        return Gnmi.Path.newBuilder().addAllElem(Arrays.asList(elems)).build();
    }

    private static Gnmi.Update update(Gnmi.Path path, Gnmi.TypedValue value) {
        return Gnmi.Update.newBuilder().setPath(path).setVal(value).build();
    }

    private static Gnmi.SubscribeResponse response(Gnmi.Path prefix, Gnmi.Update... updates) {
        return Gnmi.SubscribeResponse.newBuilder()
                .setUpdate(Gnmi.Notification.newBuilder().setPrefix(prefix).addAllUpdate(Arrays.asList(updates)))
                .build();
    }

    private static Gnmi.TypedValue json(String text) {
        return Gnmi.TypedValue.newBuilder().setJsonVal(ByteString.copyFromUtf8(text)).build();
    }

    private static Gnmi.TypedValue jsonIetf(String text) {
        return Gnmi.TypedValue.newBuilder().setJsonIetfVal(ByteString.copyFromUtf8(text)).build();
    }

    private List<String> flatten(Gnmi.SubscribeResponse response) throws Exception {
        List<?> leaves = (List<?>) GnmiMappingScript.call(gcl, "LeafFlattener", "flatten", response);
        return leaves.stream().map(Object::toString).sorted().collect(Collectors.toList());
    }

    @Test
    public void joinsPrefixAndUpdatePath() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("interfaces"), elem("interface", "name", "E1")),
                update(path(elem("state"), elem("counters"), elem("in-octets")), Gnmi.TypedValue.newBuilder().setUintVal(5).build()));
        assertThat(flatten(r), contains("/interfaces/interface[name=E1]/state/counters/in-octets = 5 (Long)"));
    }

    @Test
    public void stripsModulePrefixes() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(),
                update(path(elem("openconfig-interfaces:interfaces"), elem("interface", "name", "E1"), elem("openconfig-interfaces:state"), elem("mtu")),
                        Gnmi.TypedValue.newBuilder().setUintVal(1500).build()));
        assertThat(flatten(r), contains("/interfaces/interface[name=E1]/state/mtu = 1500 (Long)"));
    }

    @Test
    public void convertsScalarTypes() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("v")),
                update(path(elem("a-int")), Gnmi.TypedValue.newBuilder().setIntVal(-3).build()),
                update(path(elem("b-uint-max")), Gnmi.TypedValue.newBuilder().setUintVal(-1L).build()),
                update(path(elem("c-bool")), Gnmi.TypedValue.newBuilder().setBoolVal(true).build()),
                update(path(elem("d-float")), Gnmi.TypedValue.newBuilder().setFloatVal(1.5f).build()),
                update(path(elem("e-decimal")), Gnmi.TypedValue.newBuilder()
                        .setDecimalVal(Gnmi.Decimal64.newBuilder().setDigits(1234).setPrecision(2)).build()),
                update(path(elem("f-string")), Gnmi.TypedValue.newBuilder().setStringVal("up").build()),
                update(path(elem("g-bytes")), Gnmi.TypedValue.newBuilder().setBytesVal(ByteString.copyFromUtf8("x")).build()));
        assertThat(flatten(r), contains(
                "/v/a-int = -3 (Long)",
                "/v/b-uint-max = 18446744073709551615 (BigInteger)",
                "/v/c-bool = true (Boolean)",
                "/v/d-float = 1.5 (Double)",
                "/v/e-decimal = 12.34 (BigDecimal)",
                "/v/f-string = up (String)"));
    }

    @Test
    public void flattensJsonIetfObjects() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(),
                update(path(elem("interfaces"), elem("interface", "name", "E1"), elem("state")),
                        jsonIetf("{\"openconfig-interfaces:counters\":{\"in-octets\":\"5\"},\"oper-status\":\"UP\"}")));
        assertThat(flatten(r), contains(
                "/interfaces/interface[name=E1]/state/counters/in-octets = 5 (String)",
                "/interfaces/interface[name=E1]/state/oper-status = UP (String)"));
    }

    @Test
    public void parsesJsonScalars() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("s")),
                update(path(elem("number")), json("15")),
                update(path(elem("text")), json("\"UP\"")));
        assertThat(flatten(r), contains("/s/number = 15 (Integer)", "/s/text = UP (String)"));
    }

    @Test
    public void skipsJsonArrays() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("i")),
                update(path(elem("state")), jsonIetf("{\"addresses\":[{\"ip\":\"10.0.0.1\"}],\"tags\":[\"a\"],\"mtu\":1500}")));
        assertThat(flatten(r), contains("/i/state/mtu = 1500 (Integer)"));
    }

    @Test
    public void malformedJsonSkipsOnlyThatUpdate() throws Exception {
        Gnmi.SubscribeResponse r = response(
                path(elem("i")),
                update(path(elem("broken")), jsonIetf("{\"a\":")),
                update(path(elem("ok")), Gnmi.TypedValue.newBuilder().setUintVal(1).build()));
        assertThat(flatten(r), contains("/i/ok = 1 (Long)"));
    }

    @Test
    public void ignoresDeletes() throws Exception {
        Gnmi.SubscribeResponse r = Gnmi.SubscribeResponse.newBuilder()
                .setUpdate(Gnmi.Notification.newBuilder()
                        .setPrefix(path(elem("i")))
                        .addDelete(path(elem("gone")))
                        .addUpdate(update(path(elem("kept")), Gnmi.TypedValue.newBuilder().setUintVal(2).build())))
                .build();
        assertThat(flatten(r), contains("/i/kept = 2 (Long)"));
    }

    @Test
    public void syncResponseHasNoLeaves() throws Exception {
        assertThat(flatten(Gnmi.SubscribeResponse.newBuilder().setSyncResponse(true).build()), is(empty()));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest=GnmiLeafFlattenerTest`
Expected: FAIL with `ClassNotFoundException: LeafFlattener`.

- [ ] **Step 3: Append the flattener classes to the script**

Append to `openconfig-gnmi-mapping.groovy`:

```groovy
class Leaf {
    final List<PathElem> path
    final Object value

    Leaf(List<PathElem> path, Object value) {
        this.path = path
        this.value = value
    }

    String toString() {
        return '/' + path.join('/') + ' = ' + value + ' (' + value.getClass().getSimpleName() + ')'
    }
}

class RateLimitedLog {
    private static final long INTERVAL_MS = 60000L
    private static final Map<String, Long> LAST = new ConcurrentHashMap<>()

    static boolean allow(String key) {
        long now = System.currentTimeMillis()
        Long previous = LAST.get(key)
        if (previous != null && now - previous < INTERVAL_MS) {
            return false
        }
        LAST.put(key, now)
        return true
    }
}

@Slf4j
class LeafFlattener {
    private static final Set<String> WARNED_ARRAYS = ConcurrentHashMap.newKeySet()

    static List<Leaf> flatten(Gnmi.SubscribeResponse response) {
        List<Leaf> leaves = new ArrayList<>()
        // sync_response and error messages carry no data; deletes are ignored
        if (!response.hasUpdate()) {
            return leaves
        }
        Gnmi.Notification notification = response.getUpdate()
        List<PathElem> prefix = toElems(notification.getPrefix())
        for (Gnmi.Update update : notification.getUpdateList()) {
            List<PathElem> path = new ArrayList<>(prefix)
            path.addAll(toElems(update.getPath()))
            addValue(path, update.getVal(), leaves)
        }
        return leaves
    }

    static List<PathElem> toElems(Gnmi.Path path) {
        List<PathElem> elems = new ArrayList<>()
        for (Gnmi.PathElem e : path.getElemList()) {
            Map<String, String> keys = new LinkedHashMap<>()
            for (Map.Entry<String, String> k : e.getKeyMap().entrySet()) {
                keys.put(Names.stripModule(k.getKey()), k.getValue())
            }
            elems.add(new PathElem(Names.stripModule(e.getName()), keys))
        }
        return elems
    }

    private static void addValue(List<PathElem> path, Gnmi.TypedValue val, List<Leaf> leaves) {
        Gnmi.TypedValue.ValueCase c = val.getValueCase()
        if (c == Gnmi.TypedValue.ValueCase.STRING_VAL) {
            leaves.add(new Leaf(path, val.getStringVal()))
        } else if (c == Gnmi.TypedValue.ValueCase.INT_VAL) {
            leaves.add(new Leaf(path, Long.valueOf(val.getIntVal())))
        } else if (c == Gnmi.TypedValue.ValueCase.UINT_VAL) {
            long u = val.getUintVal()
            leaves.add(new Leaf(path, u >= 0 ? (Object) Long.valueOf(u) : (Object) new BigInteger(Long.toUnsignedString(u))))
        } else if (c == Gnmi.TypedValue.ValueCase.BOOL_VAL) {
            leaves.add(new Leaf(path, Boolean.valueOf(val.getBoolVal())))
        } else if (c == Gnmi.TypedValue.ValueCase.FLOAT_VAL) {
            leaves.add(new Leaf(path, Double.valueOf((double) val.getFloatVal())))
        } else if (c == Gnmi.TypedValue.ValueCase.DECIMAL_VAL) {
            leaves.add(new Leaf(path, BigDecimal.valueOf(val.getDecimalVal().getDigits(), val.getDecimalVal().getPrecision())))
        } else if (c == Gnmi.TypedValue.ValueCase.JSON_VAL || c == Gnmi.TypedValue.ValueCase.JSON_IETF_VAL) {
            String text = c == Gnmi.TypedValue.ValueCase.JSON_VAL ? val.getJsonVal().toStringUtf8() : val.getJsonIetfVal().toStringUtf8()
            Object parsed
            try {
                parsed = Parsers.parseJsonValue(text)
            } catch (Exception e) {
                String where = names(path)
                if (RateLimitedLog.allow('json:' + where)) {
                    log.warn('Skipping update at {}: invalid JSON: {}', where, e.getMessage())
                }
                return
            }
            addJson(path, parsed, leaves)
        } else if (log.isDebugEnabled() && RateLimitedLog.allow('type:' + c)) {
            log.debug('Skipping unsupported gNMI value type {} at {}', c, names(path))
        }
    }

    private static void addJson(List<PathElem> path, Object node, List<Leaf> leaves) {
        if (node instanceof Map) {
            for (Map.Entry e : ((Map) node).entrySet()) {
                List<PathElem> child = new ArrayList<>(path)
                child.add(new PathElem(Names.stripModule(String.valueOf(e.getKey())), new LinkedHashMap<String, String>()))
                addJson(child, e.getValue(), leaves)
            }
        } else if (node instanceof List) {
            String where = names(path)
            if (WARNED_ARRAYS.add(where)) {
                log.warn('Skipping JSON array at {}: arrays are not supported, subscribe to a deeper path', where)
            }
        } else if (node != null) {
            leaves.add(new Leaf(path, node))
        }
    }

    private static String names(List<PathElem> path) {
        List<String> names = new ArrayList<>()
        for (PathElem e : path) {
            names.add(e.name)
        }
        return '/' + names.join('/')
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest='GnmiLeafFlattenerTest,GnmiMappingScriptTest,GnmiMappingLoaderTest'`
Expected: PASS, 24 tests.

- [ ] **Step 5: Commit**

```bash
git add opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy \
  features/telemetry/protocols/openconfig/adapter/src/test/java/org/opennms/netmgt/telemetry/protocols/openconfig/adapter/GnmiLeafFlattenerTest.java
git commit -s -m "feat(telemetry): flatten gNMI updates into leaves" -m "Assisted-by: ClaudeCode:claude-opus-5-5"
```

---

### Task 4: Leaf matcher

**Files:**
- Modify: `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy` (append classes)
- Create: `.../adapter/GnmiLeafMatcherTest.java`

**Interfaces:**
- Consumes: `Leaf`, `PathParser.parse`, `MappingLoader.load`, `Group`, `AttrSpec`, `CompiledMapping`, `RateLimitedLog.allow` (Tasks 2 and 3)
- Produces:
  - `Values.toNumber(Object) -> Number` (null when not numeric). Integral values become `Long`, or `BigInteger` when they do not fit. Fractional values become `Double`. Booleans become `1L` or `0L`.
  - `Template.render(String template, List<PathElem> path) -> String` (null when a placeholder has no key)
  - `Sanitizer.instance(String) -> String`
  - `Match(Group group, String attrName, String type, String instance, Object value)`, `toString()` renders `group|attrName|type|instance|value`
  - `LeafMatcher.match(Leaf, CompiledMapping) -> List<Match>`

- [ ] **Step 1: Write the failing test**

`GnmiLeafMatcherTest.java` (TOG header, then):

```java
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.math.BigInteger;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import groovy.lang.GroovyClassLoader;

public class GnmiLeafMatcherTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private GroovyClassLoader gcl;

    @Before
    public void setUp() throws Exception {
        gcl = GnmiMappingScript.load();
    }

    private Object mapping(String... groupLines) throws Exception {
        String[] lines = new String[groupLines.length + 2];
        lines[0] = "version: 1";
        lines[1] = "groups:";
        System.arraycopy(groupLines, 0, lines, 2, groupLines.length);
        File file = GnmiMappingScript.writeFile(tmp.getRoot(), UUID.randomUUID() + ".yaml", lines);
        Object result = GnmiMappingScript.call(gcl, "MappingLoader", "load", file);
        assertThat((List<?>) GnmiMappingScript.prop(result, "errors"), is(empty()));
        return GnmiMappingScript.prop(result, "mapping");
    }

    private Object leaf(String path, Object value) throws Exception {
        Object elems = GnmiMappingScript.call(gcl, "PathParser", "parse", path);
        return GnmiMappingScript.newInstance(gcl, "Leaf", elems, value);
    }

    private List<String> match(Object mapping, String path, Object value) throws Exception {
        List<?> matches = (List<?>) GnmiMappingScript.call(gcl, "LeafMatcher", "match", leaf(path, value), mapping);
        return matches.stream().map(Object::toString).collect(Collectors.toList());
    }

    private static final String[] COUNTERS = {
            "  - name: g",
            "    match: /interfaces/interface/state/counters",
            "    resource-type: interface",
            "    instance: \"{interface.name}\"",
            "    attributes:",
            "      in-octets: { type: counter }",
            "      in-unicast-pkts: { type: counter, name: inUcastPkts }"};

    @Test
    public void matchesListedAttribute() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters/in-octets", 5L),
                contains("g|in-octets|counter|E1|5"));
    }

    @Test
    public void usesRename() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters/in-unicast-pkts", 5L),
                contains("g|inUcastPkts|counter|E1|5"));
    }

    @Test
    public void dropsUnlistedLeaf() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters/in-errors", 5L), is(empty()));
    }

    @Test
    public void ignoresLeavesOutsideTheMatchPath() throws Exception {
        assertThat(match(mapping(COUNTERS), "/system/state/counters/in-octets", 5L), is(empty()));
    }

    @Test
    public void leafEqualToMatchPathDoesNotMatch() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=E1]/state/counters", 5L), is(empty()));
    }

    @Test
    public void defaultNameJoinsNestedKeysWithDashes() throws Exception {
        Object mapping = mapping(
                "  - name: g",
                "    match: /interfaces/interface/state",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes:",
                "      counters/in-octets: { type: counter }");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/in-octets", "7"),
                contains("g|counters-in-octets|counter|E1|7"));
    }

    @Test
    public void wildcardMatchesOneElement() throws Exception {
        Object mapping = mapping(
                "  - name: g",
                "    match: /interfaces/*/state",
                "    resource-type: node",
                "    attributes: { mtu: { type: gauge } }");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/mtu", 1500L), contains("g|mtu|gauge|null|1500"));
        assertThat(match(mapping, "/interfaces/interface/subinterfaces/state/mtu", 1500L), is(empty()));
    }

    @Test
    public void keyPredicateSupportsGlobs() throws Exception {
        Object mapping = mapping(
                "  - name: cpu",
                "    match: /components/component[name=CPU*]/cpu/utilization/state",
                "    resource-type: ocComponent",
                "    instance: \"{component.name}\"",
                "    attributes: { avg: { type: gauge } }");
        assertThat(match(mapping, "/components/component[name=CPU0:CORE0]/cpu/utilization/state/avg", 15),
                contains("cpu|avg|gauge|CPU0_CORE0|15"));
        assertThat(match(mapping, "/components/component[name=FAN1]/cpu/utilization/state/avg", 15), is(empty()));
        assertThat(match(mapping, "/components/component/cpu/utilization/state/avg", 15), is(empty()));
    }

    @Test
    public void sanitizesInstance() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[name=TenGigE0/0/0/3]/state/counters/in-octets", 1L),
                contains("g|in-octets|counter|TenGigE0_0_0_3|1"));
    }

    @Test
    public void skipsLeafWithoutInstanceKey() throws Exception {
        assertThat(match(mapping(COUNTERS), "/interfaces/interface[id=7]/state/counters/in-octets", 1L), is(empty()));
    }

    @Test
    public void allTakesNumericLeavesOnly() throws Exception {
        Object mapping = mapping(
                "  - name: all",
                "    match: /interfaces/interface/state/counters",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes: all",
                "    default-type: counter");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/in-octets", "12"),
                contains("all|in-octets|counter|E1|12"));
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/last-clear", "2026-10-12T00:00:00Z"),
                is(empty()));
    }

    @Test
    public void everyMatchingGroupApplies() throws Exception {
        Object mapping = mapping(
                "  - name: per-if",
                "    match: /interfaces/interface/state/counters",
                "    resource-type: interface",
                "    instance: \"{interface.name}\"",
                "    attributes: { in-octets: { type: counter } }",
                "  - name: per-node",
                "    match: /interfaces/interface/state/counters",
                "    resource-type: node",
                "    attributes: { in-octets: { type: gauge, name: anyInOctets } }");
        assertThat(match(mapping, "/interfaces/interface[name=E1]/state/counters/in-octets", 9L),
                contains("per-if|in-octets|counter|E1|9", "per-node|anyInOctets|gauge|null|9"));
    }

    @Test
    public void toNumberNormalizesTypes() throws Exception {
        assertEquals(1L, GnmiMappingScript.call(gcl, "Values", "toNumber", true));
        assertEquals(0L, GnmiMappingScript.call(gcl, "Values", "toNumber", false));
        assertEquals(15L, GnmiMappingScript.call(gcl, "Values", "toNumber", 15));
        assertEquals(42L, GnmiMappingScript.call(gcl, "Values", "toNumber", " 42 "));
        assertEquals(new BigInteger("18446744073709551615"), GnmiMappingScript.call(gcl, "Values", "toNumber", "18446744073709551615"));
        assertEquals(12.5d, GnmiMappingScript.call(gcl, "Values", "toNumber", "12.5"));
        assertEquals(12.34d, GnmiMappingScript.call(gcl, "Values", "toNumber", new java.math.BigDecimal("12.34")));
        assertEquals(1.5d, GnmiMappingScript.call(gcl, "Values", "toNumber", 1.5d));
        assertNull(GnmiMappingScript.call(gcl, "Values", "toNumber", "UP"));
        assertNull(GnmiMappingScript.call(gcl, "Values", "toNumber", "NaN"));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest=GnmiLeafMatcherTest`
Expected: FAIL with `ClassNotFoundException: LeafMatcher`.

- [ ] **Step 3: Append the matcher classes to the script**

Append to `openconfig-gnmi-mapping.groovy`:

```groovy
class Values {
    static Number toNumber(Object v) {
        if (v instanceof Boolean) {
            return ((Boolean) v) ? 1L : 0L
        }
        if (v instanceof Float || v instanceof Double) {
            return ((Number) v).doubleValue()
        }
        if (v instanceof BigDecimal) {
            BigDecimal d = (BigDecimal) v
            return d.scale() <= 0 ? integral(d.toBigInteger()) : (Number) d.doubleValue()
        }
        if (v instanceof BigInteger) {
            return integral((BigInteger) v)
        }
        if (v instanceof Number) {
            return ((Number) v).longValue()
        }
        if (v instanceof String) {
            String s = ((String) v).trim()
            if (s ==~ /-?\d+/) {
                return integral(new BigInteger(s))
            }
            if (s ==~ /-?\d+(\.\d+)?([eE][-+]?\d+)?/) {
                return Double.parseDouble(s)
            }
        }
        return null
    }

    private static Number integral(BigInteger b) {
        return b.bitLength() < 64 ? (Number) b.longValue() : (Number) b
    }
}

class Template {
    private static final Pattern PLACEHOLDER = Pattern.compile('\\{([^}.]+)\\.([^}]+)\\}')

    static String render(String template, List<PathElem> path) {
        Matcher m = PLACEHOLDER.matcher(template)
        StringBuffer sb = new StringBuffer()
        while (m.find()) {
            String value = null
            for (PathElem e : path) {
                if (e.name == m.group(1) && e.keys.containsKey(m.group(2))) {
                    value = e.keys.get(m.group(2))
                    break
                }
            }
            if (value == null) {
                return null
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(value))
        }
        m.appendTail(sb)
        return sb.toString()
    }
}

class Sanitizer {
    // Same characters as GenericTypeResource.sanitizeInstance; ResourcePath rejects "/"
    static String instance(String raw) {
        return raw.replaceAll('[\\s:\\\\\\[\\]/]', '_')
    }
}

class Match {
    final Group group
    final String attrName
    final String type
    final String instance
    final Object value

    Match(Group group, String attrName, String type, String instance, Object value) {
        this.group = group
        this.attrName = attrName
        this.type = type
        this.instance = instance
        this.value = value
    }

    String toString() {
        return group.name + '|' + attrName + '|' + type + '|' + instance + '|' + value
    }
}

@Slf4j
class LeafMatcher {
    static List<Match> match(Leaf leaf, CompiledMapping mapping) {
        List<Match> matches = new ArrayList<>()
        for (Group group : mapping.groups) {
            Match m = matchGroup(leaf, group)
            if (m != null) {
                matches.add(m)
            }
        }
        return matches
    }

    private static Match matchGroup(Leaf leaf, Group group) {
        int depth = group.match.size()
        if (leaf.path.size() <= depth) {
            return null
        }
        for (int i = 0; i < depth; i++) {
            if (!group.match.get(i).matches(leaf.path.get(i))) {
                return null
            }
        }
        List<String> rest = new ArrayList<>()
        for (PathElem e : leaf.path.subList(depth, leaf.path.size())) {
            rest.add(e.name)
        }
        String relative = rest.join('/')

        String type
        String name
        AttrSpec spec = group.attributes.get(relative)
        if (spec != null) {
            type = spec.type
            name = spec.name
        } else if (group.all && Values.toNumber(leaf.value) != null) {
            type = group.defaultType
            name = relative.replace('/', '-')
        } else {
            return null
        }

        String instance = null
        if (group.instance != null) {
            String rendered = Template.render(group.instance, leaf.path)
            if (rendered == null) {
                if (log.isDebugEnabled() && RateLimitedLog.allow('instance:' + group.name)) {
                    log.debug('Group {}: leaf {} has no key for instance {}, skipped', group.name, leaf, group.instance)
                }
                return null
            }
            instance = Sanitizer.instance(rendered)
        }
        return new Match(group, name, type, instance, leaf.value)
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest='GnmiLeafMatcherTest,GnmiLeafFlattenerTest,GnmiMappingScriptTest,GnmiMappingLoaderTest'`
Expected: PASS, 37 tests.

- [ ] **Step 5: Commit**

```bash
git add opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy \
  features/telemetry/protocols/openconfig/adapter/src/test/java/org/opennms/netmgt/telemetry/protocols/openconfig/adapter/GnmiLeafMatcherTest.java
git commit -s -m "feat(telemetry): match gNMI leaves against mapping groups" -m "Assisted-by: ClaudeCode:claude-opus-5-5"
```

---

### Task 5: Emitter, mapping holder, script entry point and golden fixtures

**Files:**
- Modify: `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy` (append classes and the entry point)
- Modify: `.../adapter/GnmiMappingScript.java` (add `run`, `render`, `installMapping`)
- Modify: `.../adapter/GnmiMappingScriptTest.java` (add tests)
- Create: `.../adapter/GnmiMappingHolderTest.java`
- Create: `.../adapter/GnmiMappingFixtureTest.java`
- Create: 13 fixture directories under `features/telemetry/protocols/openconfig/adapter/src/test/resources/gnmi-mapping/`

**Interfaces:**
- Consumes: everything from Tasks 2 to 4
- Produces:
  - `ResourceEmitter.emit(List<Match>, NodeLevelResource, CollectionSetBuilder) -> int` (attributes emitted)
  - `MappingHolder.locate() -> File`, `MappingHolder.current() -> CompiledMapping` (null until a valid mapping loads)
  - `GnmiMapping.apply(Object agent, CollectionSetBuilder builder, Gnmi.SubscribeResponse response)`
  - Script globals consumed: `agent`, `builder`, `msg` (set by `ScriptedCollectionSetBuilder.build`)
  - Test helper additions: `static CollectionSet run(Object message)`, `static String render(CollectionSet)`, `static void installMapping(File home, Path caseDir)`

- [ ] **Step 1: Add the run, render and install helpers to `GnmiMappingScript.java`**

Add these imports:

```java
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mockito.Mockito;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.AbstractCollectionSetVisitor;
import org.opennms.netmgt.telemetry.protocols.collection.ScriptedCollectionSetBuilder;
```

Add these methods:

```java
    /** Runs the whole script, as the adapter does, for node id 1. */
    static CollectionSet run(Object message) throws Exception {
        CollectionAgent agent = Mockito.mock(CollectionAgent.class);
        Mockito.when(agent.getNodeId()).thenReturn(1);
        return new ScriptedCollectionSetBuilder(file()).build(agent, message, 0L);
    }

    /** One sorted line per attribute: resource-type|instance|group|name|type|value */
    static String render(CollectionSet set) {
        List<String> lines = new ArrayList<>();
        set.visit(new AbstractCollectionSetVisitor() {
            @Override
            public void visitAttribute(CollectionAttribute attribute) {
                String value = attribute.getType() == AttributeType.STRING
                        ? attribute.getStringValue()
                        : String.valueOf(attribute.getNumericValue());
                lines.add(String.join("|",
                        attribute.getResource().getResourceTypeName(),
                        attribute.getResource().getInstance(),
                        attribute.getAttributeType().getGroupType().getName(),
                        attribute.getName(),
                        attribute.getType().getName(),
                        value));
            }
        });
        Collections.sort(lines);
        return String.join("\n", lines);
    }

    /** Copies mapping.yaml or mapping.json from a fixture directory to home/etc/telemetryd-adapters/. */
    static void installMapping(File home, Path caseDir) throws IOException {
        Path target = home.toPath().resolve("etc/telemetryd-adapters");
        Files.createDirectories(target);
        Path yaml = caseDir.resolve("mapping.yaml");
        if (Files.exists(yaml)) {
            Files.copy(yaml, target.resolve("openconfig-gnmi-mapping.yaml"));
        } else {
            Files.copy(caseDir.resolve("mapping.json"), target.resolve("openconfig-gnmi-mapping.json"));
        }
    }
```

- [ ] **Step 2: Write the failing holder test**

`GnmiMappingHolderTest.java` (TOG header, then):

```java
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.io.File;
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
        dir = new File(tmp.getRoot(), "etc/telemetryd-adapters");
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

    @Test
    public void returnsNullWhenNoFileExists() throws Exception {
        assertNull(current());
    }

    @Test
    public void loadsYamlFromOpennmsHome() throws Exception {
        GnmiMappingScript.writeFile(dir, "openconfig-gnmi-mapping.yaml", ONE_GROUP);
        assertThat(groups(current()), hasSize(1));
        assertEquals(new File(dir, "openconfig-gnmi-mapping.yaml"), GnmiMappingScript.call(gcl, "MappingHolder", "locate"));
    }

    @Test
    public void fallsBackToJson() throws Exception {
        GnmiMappingScript.writeFile(dir, "openconfig-gnmi-mapping.json",
                "{\"version\": 1, \"groups\": [{\"name\": \"a\", \"match\": \"/a\", \"resource-type\": \"node\",",
                " \"attributes\": {\"b\": {\"type\": \"gauge\"}}}]}");
        assertThat(groups(current()), hasSize(1));
    }

    @Test
    public void prefersYamlWhenBothExist() throws Exception {
        GnmiMappingScript.writeFile(dir, "openconfig-gnmi-mapping.yaml", TWO_GROUPS);
        GnmiMappingScript.writeFile(dir, "openconfig-gnmi-mapping.json", "not json");
        assertThat(groups(current()), hasSize(2));
    }

    @Test
    public void cachesUntilTheFileChanges() throws Exception {
        GnmiMappingScript.writeFile(dir, "openconfig-gnmi-mapping.yaml", ONE_GROUP);
        Object first = current();
        assertSame(first, current());
        rewrite(dir, "openconfig-gnmi-mapping.yaml", TWO_GROUPS);
        assertThat(groups(current()), hasSize(2));
    }

    @Test
    public void keepsPreviousMappingWhenReloadIsInvalid() throws Exception {
        GnmiMappingScript.writeFile(dir, "openconfig-gnmi-mapping.yaml", ONE_GROUP);
        Object good = current();
        rewrite(dir, "openconfig-gnmi-mapping.yaml", "version: 1", "groups: []");
        assertSame(good, current());
    }

    @Test
    public void keepsPreviousMappingWhenFileIsDeleted() throws Exception {
        File file = GnmiMappingScript.writeFile(dir, "openconfig-gnmi-mapping.yaml", ONE_GROUP);
        Object good = current();
        file.delete();
        assertSame(good, current());
    }
}
```

- [ ] **Step 3: Add the failing whole-script tests to `GnmiMappingScriptTest.java`**

Add these imports:

```java
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;

import groovy.lang.GroovyClassLoader;
```

Add these members and tests:

```java
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
    public void noMappingEmitsNothing() throws Exception {
        assertEquals("", GnmiMappingScript.render(GnmiMappingScript.run(oneCounter())));
    }

    @Test
    public void nullAgentEmitsNothing() throws Exception {
        GnmiMappingScript.writeFile(new File(tmp.getRoot(), "etc/telemetryd-adapters"), "openconfig-gnmi-mapping.yaml",
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
```

- [ ] **Step 4: Run the tests to verify they fail**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest='GnmiMappingHolderTest,GnmiMappingScriptTest'`
Expected: FAIL. The holder tests fail with `ClassNotFoundException: MappingHolder`. `nullAgentEmitsNothing` fails with `ClassNotFoundException: GnmiMapping`. `noMappingEmitsNothing` already passes, because the script has no entry point yet and so emits nothing. It guards the entry point added in Step 5.

- [ ] **Step 5: Append the emitter, holder and entry point to the script**

Append to `openconfig-gnmi-mapping.groovy`:

```groovy
@Slf4j
class ResourceEmitter {
    static int emit(List<Match> matches, NodeLevelResource node, CollectionSetBuilder builder) {
        int emitted = 0
        for (Match m : matches) {
            Resource resource = resourceFor(m, node)
            if (m.type == 'string') {
                builder.withStringAttribute(resource, m.group.name, m.attrName, String.valueOf(m.value))
                emitted++
                continue
            }
            Number number = Values.toNumber(m.value)
            if (number == null) {
                if (log.isDebugEnabled() && RateLimitedLog.allow('value:' + m.group.name)) {
                    log.debug('Group {}: value {} of attribute {} is not numeric, skipped', m.group.name, m.value, m.attrName)
                }
                continue
            }
            AttributeType type = m.type == 'counter' ? AttributeType.COUNTER : AttributeType.GAUGE
            builder.withNumericAttribute(resource, m.group.name, m.attrName, number, type)
            emitted++
        }
        return emitted
    }

    private static Resource resourceFor(Match m, NodeLevelResource node) {
        if (m.group.resourceType == 'node') {
            return node
        }
        if (m.group.resourceType == 'interface') {
            return new InterfaceLevelResource(node, m.instance)
        }
        return new DeferredGenericTypeResource(node, m.group.resourceType, m.instance)
    }
}

@Slf4j
class MappingHolder {
    private static final Object LOCK = new Object()
    private static volatile CompiledMapping active
    private static volatile File loadedFile
    private static volatile long loadedStamp = -1L

    static File locate() {
        File dir = new File(System.getProperty('opennms.home', '/opt/opennms'), 'etc/telemetryd-adapters')
        File yaml = new File(dir, 'openconfig-gnmi-mapping.yaml')
        return yaml.exists() ? yaml : new File(dir, 'openconfig-gnmi-mapping.json')
    }

    static CompiledMapping current() {
        File file = locate()
        if (!file.exists()) {
            // Keep the last good mapping if the file disappears
            return active
        }
        long stamp = file.lastModified() * 31L + file.length()
        if (file == loadedFile && stamp == loadedStamp) {
            return active
        }
        synchronized (LOCK) {
            if (file == loadedFile && stamp == loadedStamp) {
                return active
            }
            LoadResult result = MappingLoader.load(file)
            loadedFile = file
            loadedStamp = stamp
            for (String warning : result.warnings) {
                log.warn('gNMI mapping {}: {}', file, warning)
            }
            if (result.errors.isEmpty()) {
                active = result.mapping
                log.info('Loaded gNMI mapping {} with {} groups', file, active.groups.size())
            } else if (active == null) {
                log.error('gNMI mapping {} is invalid: {}', file, result.errors.join('; '))
            } else {
                log.error('gNMI mapping {} is invalid, keeping the previous mapping: {}', file, result.errors.join('; '))
            }
            return active
        }
    }
}

@Slf4j
class GnmiMapping {
    static void apply(Object agent, CollectionSetBuilder builder, Gnmi.SubscribeResponse response) {
        if (agent == null) {
            // The adapter found no node for the source address
            return
        }
        CompiledMapping mapping = MappingHolder.current()
        if (mapping == null) {
            if (RateLimitedLog.allow('no-mapping')) {
                log.error('No valid gNMI mapping loaded from {}, no metrics are emitted', MappingHolder.locate())
            }
            return
        }
        int nodeId = agent.getNodeId()
        int seen = 0
        int matched = 0
        int emitted = 0
        try {
            List<Leaf> leaves = LeafFlattener.flatten(response)
            seen = leaves.size()
            List<Match> matches = new ArrayList<>()
            for (Leaf leaf : leaves) {
                List<Match> found = LeafMatcher.match(leaf, mapping)
                if (!found.isEmpty()) {
                    matched++
                }
                matches.addAll(found)
            }
            emitted = ResourceEmitter.emit(matches, new NodeLevelResource(nodeId), builder)
        } catch (Exception e) {
            log.warn('Failed to map gNMI message for node {}: {}', nodeId, e.getMessage(), e)
        }
        log.debug('gNMI mapping for node {}: {} leaves seen, {} matched, {} attributes emitted', nodeId, seen, matched, emitted)
    }
}

// Globals passed in by the OpenConfig adapter:
// agent: the CollectionAgent of the node that sent the message
// builder: the CollectionSetBuilder to add resources and attributes to
// msg: the decoded Gnmi.SubscribeResponse
GnmiMapping.apply(agent, (CollectionSetBuilder) builder, (Gnmi.SubscribeResponse) msg)
```

- [ ] **Step 6: Run the holder and script tests to verify they pass**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest='GnmiMappingHolderTest,GnmiMappingScriptTest'`
Expected: PASS, 10 tests.

- [ ] **Step 7: Write the fixture runner**

`GnmiMappingFixtureTest.java` (TOG header, then):

```java
package org.opennms.netmgt.telemetry.protocols.openconfig.adapter;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;

import com.google.protobuf.TextFormat;

/**
 * Golden fixtures for the declarative gNMI mapping. Phase A runs the same directory against the Java engine.
 */
@RunWith(Parameterized.class)
public class GnmiMappingFixtureTest {

    private static final Path CASES = Paths.get("src/test/resources/gnmi-mapping");

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> cases() throws IOException {
        try (Stream<Path> dirs = Files.list(CASES)) {
            return dirs.filter(Files::isDirectory)
                    .map(dir -> dir.getFileName().toString())
                    .sorted()
                    .map(name -> new Object[] {name})
                    .collect(Collectors.toList());
        }
    }

    @Parameterized.Parameter
    public String name;

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private String previousHome;

    @Before
    public void saveHome() {
        previousHome = System.getProperty("opennms.home");
    }

    @After
    public void restoreHome() {
        if (previousHome == null) {
            System.clearProperty("opennms.home");
        } else {
            System.setProperty("opennms.home", previousHome);
        }
    }

    @Test
    public void producesExpectedCollectionSet() throws Exception {
        Path dir = CASES.resolve(name);
        GnmiMappingScript.installMapping(tmp.getRoot(), dir);
        System.setProperty("opennms.home", tmp.getRoot().getAbsolutePath());

        Gnmi.SubscribeResponse.Builder response = Gnmi.SubscribeResponse.newBuilder();
        TextFormat.merge(Files.readString(dir.resolve("input.textproto")), response);

        String actual = GnmiMappingScript.render(GnmiMappingScript.run(response.build()));
        assertEquals(Files.readString(dir.resolve("expected.txt")).trim(), actual);
    }
}
```

- [ ] **Step 8: Create the fixtures**

Create each file below under `features/telemetry/protocols/openconfig/adapter/src/test/resources/gnmi-mapping/`. Interface resources render as type `if`, node resources as `node|node`.

`01-prefix-uint-counters/mapping.yaml`:

```yaml
version: 1
groups:
  - name: oc-if-counters
    match: /interfaces/interface/state/counters
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      in-octets: { type: counter }
      out-octets: { type: counter }
```

`01-prefix-uint-counters/input.textproto`:

```
update {
  timestamp: 1751293808127192960
  prefix {
    elem { name: "interfaces" }
    elem { name: "interface" key { key: "name" value: "Ethernet1" } }
  }
  update {
    path { elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" } }
    val { uint_val: 1000 }
  }
  update {
    path { elem { name: "state" } elem { name: "counters" } elem { name: "out-octets" } }
    val { uint_val: 2000 }
  }
  update {
    path { elem { name: "state" } elem { name: "counters" } elem { name: "in-errors" } }
    val { uint_val: 3 }
  }
}
```

`01-prefix-uint-counters/expected.txt`:

```
if|Ethernet1|oc-if-counters|in-octets|counter|1000
if|Ethernet1|oc-if-counters|out-octets|counter|2000
```

`02-per-update-full-paths/mapping.yaml`:

```yaml
version: 1
aliases:
  if-counters: /interfaces/interface/state/counters
groups:
  - name: oc-if-counters
    match: if-counters
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      in-octets: { type: counter }
```

`02-per-update-full-paths/input.textproto`:

```
update {
  update {
    path {
      elem { name: "interfaces" }
      elem { name: "interface" key { key: "name" value: "Ethernet1" } }
      elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" }
    }
    val { uint_val: 10 }
  }
  update {
    path {
      elem { name: "interfaces" }
      elem { name: "interface" key { key: "name" value: "Ethernet2" } }
      elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" }
    }
    val { uint_val: 20 }
  }
}
```

`02-per-update-full-paths/expected.txt`:

```
if|Ethernet1|oc-if-counters|in-octets|counter|10
if|Ethernet2|oc-if-counters|in-octets|counter|20
```

`03-json-ietf-subtree/mapping.yaml`:

```yaml
version: 1
groups:
  - name: oc-if-state
    match: /interfaces/interface/state
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      counters/in-octets: { type: counter, name: inOctets }
      counters/out-octets: { type: counter }
      oper-status: { type: string }
```

`03-json-ietf-subtree/input.textproto`:

```
update {
  update {
    path {
      elem { name: "openconfig-interfaces:interfaces" }
      elem { name: "interface" key { key: "name" value: "Ethernet1" } }
      elem { name: "state" }
    }
    val { json_ietf_val: "{\"openconfig-interfaces:counters\":{\"in-octets\":\"18446744073709551615\",\"out-octets\":\"42\",\"in-errors\":0},\"oper-status\":\"UP\"}" }
  }
}
```

`03-json-ietf-subtree/expected.txt`:

```
if|Ethernet1|oc-if-state|counters-out-octets|counter|42
if|Ethernet1|oc-if-state|inOctets|counter|18446744073709551615
if|Ethernet1|oc-if-state|oper-status|string|UP
```

`04-interface-name-with-slash/mapping.yaml`: same content as `01-prefix-uint-counters/mapping.yaml`.

`04-interface-name-with-slash/input.textproto`:

```
update {
  update {
    path {
      elem { name: "interfaces" }
      elem { name: "interface" key { key: "name" value: "TenGigE0/0/0/3" } }
      elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" }
    }
    val { uint_val: 5 }
  }
}
```

`04-interface-name-with-slash/expected.txt`:

```
if|TenGigE0_0_0_3|oc-if-counters|in-octets|counter|5
```

`05-string-and-counters-same-resource/mapping.yaml`:

```yaml
version: 1
groups:
  - name: oc-if-counters
    match: /interfaces/interface/state/counters
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      in-octets: { type: counter }
  - name: oc-if-description
    match: /interfaces/interface/state
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      description: { type: string }
```

`05-string-and-counters-same-resource/input.textproto`:

```
update {
  prefix {
    elem { name: "interfaces" }
    elem { name: "interface" key { key: "name" value: "Ethernet1" } }
  }
  update {
    path { elem { name: "state" } elem { name: "description" } }
    val { string_val: "uplink to core" }
  }
  update {
    path { elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" } }
    val { uint_val: 7 }
  }
}
```

`05-string-and-counters-same-resource/expected.txt`:

```
if|Ethernet1|oc-if-counters|in-octets|counter|7
if|Ethernet1|oc-if-description|description|string|uplink to core
```

`06-generic-resource-glob/mapping.yaml`:

```yaml
version: 1
groups:
  - name: oc-cpu
    match: /components/component[name=CPU*]/cpu/utilization/state
    resource-type: ocComponent
    instance: "{component.name}"
    attributes:
      avg: { type: gauge }
```

`06-generic-resource-glob/input.textproto`:

```
update {
  update {
    path {
      elem { name: "components" }
      elem { name: "component" key { key: "name" value: "CPU0:CORE0" } }
      elem { name: "cpu" } elem { name: "utilization" } elem { name: "state" } elem { name: "avg" }
    }
    val { json_val: "15" }
  }
  update {
    path {
      elem { name: "components" }
      elem { name: "component" key { key: "name" value: "FAN1" } }
      elem { name: "cpu" } elem { name: "utilization" } elem { name: "state" } elem { name: "avg" }
    }
    val { uint_val: 3 }
  }
}
```

`06-generic-resource-glob/expected.txt`:

```
ocComponent|CPU0_CORE0|oc-cpu|avg|gauge|15
```

`07-attributes-all/mapping.yaml`:

```yaml
version: 1
groups:
  - name: oc-if-all
    match: /interfaces/interface/state/counters
    resource-type: interface
    instance: "{interface.name}"
    attributes: all
    default-type: counter
```

`07-attributes-all/input.textproto`:

```
update {
  prefix {
    elem { name: "interfaces" }
    elem { name: "interface" key { key: "name" value: "Ethernet1" } }
    elem { name: "state" }
    elem { name: "counters" }
  }
  update { path { elem { name: "in-octets" } } val { uint_val: 1 } }
  update { path { elem { name: "out-octets" } } val { uint_val: 2 } }
  update { path { elem { name: "last-clear" } } val { string_val: "2026-10-12T00:00:00Z" } }
}
```

`07-attributes-all/expected.txt`:

```
if|Ethernet1|oc-if-all|in-octets|counter|1
if|Ethernet1|oc-if-all|out-octets|counter|2
```

`08-two-groups-one-leaf/mapping.yaml`:

```yaml
version: 1
groups:
  - name: oc-if-counters
    match: /interfaces/interface/state/counters
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      in-octets: { type: counter }
  - name: oc-node
    match: /interfaces/interface/state/counters
    resource-type: node
    attributes:
      in-octets: { type: gauge, name: anyInOctets }
```

`08-two-groups-one-leaf/input.textproto`:

```
update {
  update {
    path {
      elem { name: "interfaces" }
      elem { name: "interface" key { key: "name" value: "Ethernet1" } }
      elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" }
    }
    val { uint_val: 9 }
  }
}
```

`08-two-groups-one-leaf/expected.txt`:

```
if|Ethernet1|oc-if-counters|in-octets|counter|9
node|node|oc-node|anyInOctets|gauge|9
```

`09-missing-instance-key/mapping.yaml`: same content as `01-prefix-uint-counters/mapping.yaml`.

`09-missing-instance-key/input.textproto`:

```
update {
  update {
    path {
      elem { name: "interfaces" }
      elem { name: "interface" key { key: "id" value: "7" } }
      elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" }
    }
    val { uint_val: 1 }
  }
  update {
    path {
      elem { name: "interfaces" }
      elem { name: "interface" key { key: "name" value: "Ethernet1" } }
      elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" }
    }
    val { uint_val: 2 }
  }
}
```

`09-missing-instance-key/expected.txt`:

```
if|Ethernet1|oc-if-counters|in-octets|counter|2
```

`10-delete-ignored/mapping.yaml`: same content as `01-prefix-uint-counters/mapping.yaml`.

`10-delete-ignored/input.textproto`:

```
update {
  prefix {
    elem { name: "interfaces" }
    elem { name: "interface" key { key: "name" value: "Ethernet1" } }
  }
  delete { elem { name: "state" } elem { name: "counters" } elem { name: "out-octets" } }
  update {
    path { elem { name: "state" } elem { name: "counters" } elem { name: "in-octets" } }
    val { uint_val: 4 }
  }
}
```

`10-delete-ignored/expected.txt`:

```
if|Ethernet1|oc-if-counters|in-octets|counter|4
```

`11-sync-response/mapping.yaml`: same content as `01-prefix-uint-counters/mapping.yaml`.

`11-sync-response/input.textproto`:

```
sync_response: true
```

`11-sync-response/expected.txt`: an empty file.

`12-json-mapping-file/mapping.json`:

```json
{
  "version": 1,
  "groups": [
    {
      "name": "oc-if-counters",
      "match": "/interfaces/interface/state/counters",
      "resource-type": "interface",
      "instance": "{interface.name}",
      "attributes": { "in-octets": { "type": "counter" } }
    }
  ]
}
```

`12-json-mapping-file/input.textproto`: same content as `02-per-update-full-paths/input.textproto`.

`12-json-mapping-file/expected.txt`:

```
if|Ethernet1|oc-if-counters|in-octets|counter|10
if|Ethernet2|oc-if-counters|in-octets|counter|20
```

`13-json-array-skipped/mapping.yaml`:

```yaml
version: 1
groups:
  - name: oc-if-state
    match: /interfaces/interface/state
    resource-type: interface
    instance: "{interface.name}"
    attributes: all
    default-type: gauge
```

`13-json-array-skipped/input.textproto`:

```
update {
  update {
    path {
      elem { name: "interfaces" }
      elem { name: "interface" key { key: "name" value: "Ethernet1" } }
      elem { name: "state" }
    }
    val { json_ietf_val: "{\"mtu\":1500,\"addresses\":[{\"ip\":\"10.0.0.1\"}]}" }
  }
}
```

`13-json-array-skipped/expected.txt`:

```
if|Ethernet1|oc-if-state|mtu|gauge|1500
```

- [ ] **Step 9: Run the fixture tests**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest=GnmiMappingFixtureTest`
Expected: PASS, 13 tests.

If a case fails, compare the assertion's actual text with `expected.txt`. Fix the script when the behaviour contradicts the spec. Fix `expected.txt` only when the spec allows the actual output and the fixture was written wrong, and say which in the commit message.

- [ ] **Step 10: Run the whole module and the header check**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test`
Expected: PASS, 61 tests. That is 59 new tests plus the 2 existing `OpenConfigAdapterTest` tests.

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter license:check`
Expected: BUILD SUCCESS. Fixture files are data and carry no header. If the plugin flags them, add `src/test/resources/gnmi-mapping/**` to the plugin's excludes in this module's pom.

- [ ] **Step 11: Commit**

```bash
git add opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy \
  features/telemetry/protocols/openconfig/adapter/src/test
git commit -s -m "feat(telemetry): emit collection sets from declarative gNMI mappings" -m "Assisted-by: ClaudeCode:claude-opus-5-5"
```

---

### Task 6: Shipped sample mapping, resource type and documentation

**Files:**
- Create: `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml`
- Create: `opennms-base-assembly/src/main/filtered/etc/resource-types.d/openconfig-gnmi-resource.xml`
- Modify: `.../adapter/GnmiMappingScriptTest.java` (add one test)
- Modify: `docs/modules/reference/pages/telemetryd/protocols/openconfig.adoc` (new section at the end)

**Interfaces:**
- Consumes: `MappingLoader.load` (Task 2), `GnmiMappingScript` helpers
- Produces: the sample mapping that Task 7 deploys; resource type `ocComponent`

- [ ] **Step 1: Write the failing test for the shipped sample**

Add to `GnmiMappingScriptTest.java` (needs imports `java.util.List`, `static org.hamcrest.MatcherAssert.assertThat`, `static org.hamcrest.Matchers.empty`, `static org.hamcrest.Matchers.is`):

```java
    @Test
    public void shippedSampleMappingIsValid() throws Exception {
        File sample = new File(GnmiMappingScript.class.getResource("/openconfig-gnmi-mapping.yaml").toURI());
        Object result = GnmiMappingScript.call(GnmiMappingScript.load(), "MappingLoader", "load", sample);
        assertThat((List<?>) GnmiMappingScript.prop(result, "errors"), is(empty()));
        assertThat((List<?>) GnmiMappingScript.prop(result, "warnings"), is(empty()));
    }
```

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest=GnmiMappingScriptTest`
Expected: FAIL with a `NullPointerException`, because the sample does not exist yet.

- [ ] **Step 2: Create the sample mapping**

`opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml`:

```yaml
# Declarative gNMI mapping read by openconfig-gnmi-mapping.groovy.
# To use it, set the OpenConfig adapter's script parameter in
# telemetryd-configuration.xml to openconfig-gnmi-mapping.groovy.
# Reference: OpenConfig telemetry docs, section "Declarative mapping".
version: 1
aliases:
  if-counters: /interfaces/interface/state/counters
  if-state: /interfaces/interface/state
  cpu-utilization: /components/component/cpu/utilization/state
groups:
  - name: oc-if-counters
    match: if-counters
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      in-octets: { type: counter, name: ifHCInOctets }
      out-octets: { type: counter, name: ifHCOutOctets }
      in-unicast-pkts: { type: counter, name: ifHCInUcastPkts }
      out-unicast-pkts: { type: counter, name: ifHCOutUcastPkts }
      in-errors: { type: counter, name: ifInErrors }
      out-errors: { type: counter, name: ifOutErrors }
      in-discards: { type: counter, name: ifInDiscards }
      out-discards: { type: counter, name: ifOutDiscards }
  - name: oc-if-description
    match: if-state
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      description: { type: string, name: ifAlias }
  - name: oc-cpu
    match: cpu-utilization
    resource-type: ocComponent
    instance: "{component.name}"
    attributes:
      avg: { type: gauge, name: cpuAvg }
      instant: { type: gauge, name: cpuInstant }
```

- [ ] **Step 3: Create the resource type**

`opennms-base-assembly/src/main/filtered/etc/resource-types.d/openconfig-gnmi-resource.xml`, following `nxos-resource.xml`:

```xml
<?xml version="1.0"?>
<resource-types>
   <resourceType name="ocComponent" label="OpenConfig Component" resourceLabel="${index}">
      <persistenceSelectorStrategy class="org.opennms.netmgt.collection.support.PersistAllSelectorStrategy"/>
      <storageStrategy class="org.opennms.netmgt.collection.support.IndexStorageStrategy"/>
   </resourceType>
</resource-types>
```

`${index}` survives Maven filtering because no `index` property is defined. `nxos-resource.xml` relies on the same behaviour.

- [ ] **Step 4: Run the test to verify it passes**

Run: `./compile.pl -o -pl features/telemetry/protocols/openconfig/adapter test -Dtest=GnmiMappingScriptTest`
Expected: PASS, 4 tests.

- [ ] **Step 5: Document the feature**

Append to `docs/modules/reference/pages/telemetryd/protocols/openconfig.adoc`. Write one sentence per line:

````asciidoc
=== Declarative mapping

Instead of writing a script, you can describe how gNMI paths become resources and attributes in a mapping file.
The shipped script `openconfig-gnmi-mapping.groovy` reads the file and builds the collection set.

To use it, point the adapter's `script` parameter at the shipped script:

[source, xml]
----
<parameter key="script" value="$OPENNMS_HOME/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy"/>
----

The script reads `$OPENNMS_HOME/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml`.
If that file does not exist, it reads `openconfig-gnmi-mapping.json` in the same directory.
Changes to the mapping file take effect with the next message, without a restart.
If a changed file is invalid, the script logs the errors and keeps the previous mapping.

.Example mapping
[source, yaml]
----
version: 1
aliases:
  if-counters: /interfaces/interface/state/counters
groups:
  - name: oc-if-counters
    match: if-counters
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      in-octets: { type: counter, name: ifHCInOctets }
      out-octets: { type: counter, name: ifHCOutOctets }
  - name: oc-if-description
    match: /interfaces/interface/state
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      description: { type: string, name: ifAlias }
----

.Mapping file keys
[options="header", cols="1,1,3"]
|===
| Key
| Required
| Description

| version
| yes
| Schema version. Must be `1`.

| aliases
| no
| Map of name to gNMI path. Use an alias wherever a path is expected.

| groups[].name
| yes
| Group name. Also used as the attribute group.

| groups[].match
| yes
| Alias or path. `*` matches one element. `[key=value]` must match, and `value` may contain `*`.

| groups[].resource-type
| yes
| `node`, `interface`, or the name of a generic resource type, such as `ocComponent`.

| groups[].instance
| for `interface` and generic types
| Template such as `{interface.name}`. Each placeholder reads a key of the leaf's path. Not allowed for `node`.

| groups[].attributes
| yes
| Map of leaf path, relative to `match`, to `{ type, name }`. Or the word `all` to collect every numeric leaf.

| groups[].default-type
| with `attributes: all`
| `counter` or `gauge`.
|===

`type` is `counter`, `gauge` or `string`.
`name` renames the attribute.
The default name is the relative leaf path with `/` replaced by `-`.
Keep names at 19 characters or fewer when you persist to RRD.

==== How leaves are matched

. The leaf path is the notification prefix followed by the update path. Module prefixes such as `openconfig-interfaces:` are removed.
. A `json_val` or `json_ietf_val` that holds an object becomes one leaf per value. Numbers encoded as strings are converted.
. JSON arrays are skipped with a warning. Subscribe to a deeper path to collect their contents.
. A group matches when its `match` path is a prefix of the leaf path. Every matching group applies.
. The rest of the leaf path selects the attribute. Leaves that are not listed are dropped, unless the group uses `attributes: all`.
. Characters that resource paths do not allow, such as `/` and `:`, become `_` in instances. `TenGigE0/0/0/3` becomes `TenGigE0_0_0_3`.

To enrich metrics with a description, map the description to a `string` attribute on the same resource as the counters.
The `oc-if-description` group in the example does this.

==== Translate gNMI collector concepts

[options="header", cols="1,2"]
|===
| gNMI collector concept
| OpenNMS

| Path aliases
| `aliases`

| Subscription name and path
| A group's `name` and `match`. The subscription itself is set in the connector's `paths` parameter.

| Tag subscription, which labels metrics with a string value
| A group with a `string` attribute on the same `resource-type` and `instance`

| Guessing which part of a path names the measurement
| Not needed. Leaf paths are always the prefix plus the update path.

| Subscription mode, sample interval, heartbeat interval, maximum message size, gRPC keepalive, redial, TLS
| Connector settings, see <<Connector configuration>>.
|===

==== Troubleshooting

Set the log level of the telemetry adapter to `DEBUG` to see one line per message: `gNMI mapping for node <id>: <n> leaves seen, <n> matched, <n> attributes emitted`.
If leaves are seen but none match, compare the leaf paths with your `match` paths.
If SnakeYAML is not available, the log says so; write the same mapping as `openconfig-gnmi-mapping.json`.
````

- [ ] **Step 6: Build the docs page**

Run: `make -C docs` if a docs Makefile exists. Otherwise run `grep -n "Declarative mapping" docs/modules/reference/pages/telemetryd/protocols/openconfig.adoc` and check that the section headings nest under `== OpenConfig adapter`.
Expected: the page renders, or the heading check shows `=== Declarative mapping` after `=== Scripting`.

- [ ] **Step 7: Commit**

```bash
git add opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml \
  opennms-base-assembly/src/main/filtered/etc/resource-types.d/openconfig-gnmi-resource.xml \
  features/telemetry/protocols/openconfig/adapter/src/test/java/org/opennms/netmgt/telemetry/protocols/openconfig/adapter/GnmiMappingScriptTest.java \
  docs/modules/reference/pages/telemetryd/protocols/openconfig.adoc
git commit -s -m "docs(telemetry): ship sample gNMI mapping and document it" -m "Assisted-by: ClaudeCode:claude-opus-5-5"
```

---

### Task 7: Live validation in the nl6 lab (phase B exit gate)

**Files:**
- Modify (lab only, not committed): `triage/gnmi-fixes-verify-2026-09-28/core-overlay/etc/telemetryd-configuration.xml`
- Create (lab only): `core-overlay/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy`, `core-overlay/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml` (or `.json`), `core-overlay/etc/resource-types.d/openconfig-gnmi-resource.xml`
- Modify: `triage/gnmi-fixes-verify-2026-09-28/results/D-declarative-mapping.md`

**Interfaces:**
- Consumes: the script and the sample from Tasks 2 to 6, the verdict from Task 1
- Produces: a pass or fail record against the spec's exit criteria

- [ ] **Step 1: Copy the files into the lab overlay**

```bash
L=triage/gnmi-fixes-verify-2026-09-28
cp opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy $L/core-overlay/etc/telemetryd-adapters/
cp opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml $L/core-overlay/etc/telemetryd-adapters/
mkdir -p $L/core-overlay/etc/resource-types.d
cp opennms-base-assembly/src/main/filtered/etc/resource-types.d/openconfig-gnmi-resource.xml $L/core-overlay/etc/resource-types.d/
```

If Task 1's verdict was "JSON only", convert the sample to `openconfig-gnmi-mapping.json` in the overlay and do not copy the YAML file.

- [ ] **Step 2: Point the adapter at the new script and widen the subscription**

In `$L/core-overlay/etc/telemetryd-configuration.xml`, change the OpenConfig adapter's script parameter:

```xml
<parameter key="script" value="/opt/opennms/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy"/>
```

Change the connector's `paths` parameter, so that descriptions and CPU are requested if nl6 serves them:

```xml
<parameter key="paths" value="/interfaces/interface[name=*]/state,/components/component[name=*]/cpu"/>
```

- [ ] **Step 3: Start the lab and nl6**

```bash
cd $L && IMAGE_TAG=36.0.4 docker compose up -d
```

Ask Ronny to start nl6 with `!`, as in the 2026-09-28 run (it needs NET_ADMIN/SYS_ADMIN and host networking). Then provision a small set:

```bash
cd $L && ./scripts/provision.sh 5
```

- [ ] **Step 4: Check that resources appear**

Wait two sample intervals (about 2 minutes), then:

```bash
curl -s -u admin:admin -H 'Accept: application/json' \
  "http://localhost:8980/opennms/rest/resources/fornode/nl6:dev-00001" | python3 -m json.tool | grep -E '"(id|name)"' | head -40
```

Expected: interface child resources such as `interfaceSnmp[TenGigE0_0_0_0]` or `interfaceSnmp[<name>]`, with `ifHCInOctets` and `ifHCOutOctets` among their attributes. The baseline is the shipped script, which produced zero resources from nl6 on 2026-09-28.

- [ ] **Step 5: Check the logs**

```bash
cd $L && docker compose exec core grep -hE "Loaded gNMI mapping|gNMI mapping .* invalid|SnakeYAML|Skipping JSON array|No valid gNMI mapping" /opt/opennms/logs/telemetryd.log | tail -20
```

Expected: `Loaded gNMI mapping /opt/opennms/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml with 3 groups`. A SnakeYAML error here overrides Task 1's verdict. In that case switch to JSON (Step 1) and repeat from Step 3.

- [ ] **Step 6: Check description and CPU coverage**

If the leaf paths in the debug log include `state/description`, check that the interface resource carries the `ifAlias` string attribute:

```bash
curl -s -u admin:admin -H 'Accept: application/json' \
  "http://localhost:8980/opennms/rest/resources/fornode/nl6:dev-00001" | grep -o '"ifAlias"[^,]*' | head -3
```

If the leaf paths include `components/component`, check for `ocComponent` child resources.
If nl6 does not serve these paths, record "not testable with nl6, covered by fixtures 05 and 06". Do not mark the gate as failed for that reason.

- [ ] **Step 7: Check hot reload**

Edit the overlay copy so the counters group also maps `in-errors` under a new name, then copy it into the running container:

```bash
sed -i.bak 's/in-errors: { type: counter, name: ifInErrors }/in-errors: { type: counter, name: ocInErrors }/' \
  $L/core-overlay/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml
(cd $L && docker compose cp core-overlay/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml core:/opt/opennms/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml)
```

Expected within one sample interval: a second `Loaded gNMI mapping` log line, and the attribute `ocInErrors` on the interface resources, without restarting the Core.

- [ ] **Step 8: Record the result and stop the lab**

Append to `results/D-declarative-mapping.md`: the commands run, the log lines, the resource listing excerpts, and one verdict line per exit criterion from the spec:

1. Interface resources carry counters: PASS or FAIL
2. Interface description as a string attribute: PASS, FAIL, or not testable with nl6
3. CPU generic resource: PASS, FAIL, or not testable with nl6
4. Mapping edit applies without a Core restart: PASS or FAIL
5. Parser available in Karaf: YAML or JSON

Then run `docker compose stop` (not `down`), and ask Ronny to stop nl6.

No commit. The lab directory is not part of the repository.
