# Declarative gNMI mapping for the OpenConfig adapter

Date: 2026-10-12
Status: design approved, phase B not started

## Problem

Integrators who stream gNMI telemetry into OpenNMS must write Groovy to turn a `SubscribeResponse` into a collection set.
The shipped `etc/telemetryd-adapters/openconfig-gnmi-telemetry.groovy` is sample code.
It detects interfaces and components only from the notification prefix, handles only `uint_val` and `json_val`, and passes interface names containing `/` straight to `ResourcePath`.
Against json_ietf devices with per-update paths (nl6, Cisco IOS-XR, Nokia SR Linux) it produces empty collection sets.

Field engineers know configuration files such as YAML and TOML.
They do not know OpenNMS internals or Groovy.
They expect to describe the mapping in a configuration file instead of writing a program.

## Goal

An integrator maps gNMI paths to OpenNMS resources and attributes in a declarative file, without writing code.
Success means:

- A json_ietf device such as nl6 or SR Linux produces correctly named interface, node and generic resources from a mapping file alone.
- One mapping file can cover several vendors through path aliases.
- A field engineer who knows gNMI collector concepts (path aliases, subscriptions, tag subscriptions) finds each one in a short reference table.

## Scope

In scope: the adapter-side mapping from a received `SubscribeResponse` to a collection set.

Out of scope, each a separate piece of work:

- Connector features: `on_change` and `heartbeat_interval` subscription modes, `max_msg_size`, gRPC keepalive, `tls_min_version`. These need Java changes in `OpenConfigClientImpl`, which hard-codes `SAMPLE` mode today.
- JTI mode.
- Matching on gNMI `origin`.
- Flattening JSON arrays of objects (YANG lists inside a payload).
- An in-memory tag cache that stamps values on every data point.

## Decisions

| Decision | Choice | Reason |
|---|---|---|
| Format | YAML with OpenNMS semantics (`group`, `resource-type`, `instance`, `counter`/`gauge`/`string`) and common gNMI collector ideas where they fit (`aliases`) | Readable for field engineers who write YAML or TOML. Maps directly onto resource types used by graphs and thresholds. |
| Enrichment | A string attribute on the same resource as the counters | OpenNMS keys data by resource, so a description stored on `interface[name=X]` sits next to its counters without a cache or join. |
| Implementation | Phase B: pure Groovy script. Phase A: Java engine plus a thin script. | Phase B validates the schema with the customer on their current release. Phase A makes it a tested product feature. |

## Phasing and contract

Three artifacts are the contract. Phase A must not change them.

1. The mapping schema in `etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml`.
2. The golden fixtures described under Testing.
3. The reference documentation, including a table that maps common gNMI collector concepts to the mapping keys.

### Phase B (validation)

- New script `opennms-base-assembly/src/main/filtered/etc/telemetryd-adapters/openconfig-gnmi-mapping.groovy` contains the whole engine.
- The script locates the mapping file as `System.getProperty("opennms.home") + "/etc/telemetryd-adapters/openconfig-gnmi-mapping.yaml"`. The adapter cannot pass extra parameters to a script, so no Java change is needed.
- The shipped `openconfig-gnmi-telemetry.groovy` stays unchanged. A user opts in by pointing the adapter `script` parameter at the new file.

### Phase A (product)

- Port the engine to a Java class `GnmiMappingEngine` in the openconfig adapter module.
- Run the same fixtures against it.
- Reduce the shipped script to a delegation call. The `script` parameter keeps working for custom logic.
- Phase A gets its own implementation plan. The JSON_IETF encoding work from 2026-08-05 (connector `setEncoding`, error classification) fits there.

### YAML parser risk

The adapter compiles scripts through `OSGiScriptEngineManager` (`ScriptedCollectionSetBuilder.java:59-60`).
Whether SnakeYAML is visible to a Groovy script inside Karaf is unknown and must be probed first.
If it is not visible, phase B reads the same document as JSON through `groovy.json.JsonSlurper`.
YAML is a superset of JSON, so phase A still accepts those files.

## Mapping schema

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
      in-octets:       { type: counter }
      out-octets:      { type: counter }
      in-unicast-pkts: { type: counter, name: inUcastPkts }
  - name: oc-if-description
    match: /interfaces/interface/state
    resource-type: interface
    instance: "{interface.name}"
    attributes:
      description: { type: string }
  - name: oc-cpu
    match: /components/component[name=CPU*]/cpu/utilization
    resource-type: cpu
    instance: "{component.name}"
    attributes: all
    default-type: gauge
```

| Key | Required | Meaning |
|---|---|---|
| `version` | yes | Schema version. Only `1` is valid. |
| `aliases` | no | Map of name to path. An alias is usable wherever a path is expected. |
| `groups[].name` | yes | Group name. Also used as the OpenNMS attribute group. |
| `groups[].match` | yes | Alias or path. Supports `*` for one element and `[key=value]` predicates whose value may contain `*`. |
| `groups[].resource-type` | yes | `node`, `interface`, or the name of a generic resource type defined in the resource-types configuration. |
| `groups[].instance` | for `interface` and generic types | Template whose `{elem.key}` placeholders read path keys of the leaf. Not allowed for `node`. |
| `groups[].attributes` | yes | Map of relative leaf path to `{ type, name? }`, or the literal `all`. |
| `groups[].default-type` | with `attributes: all` | `counter` or `gauge` for every numeric leaf. |

`type` is one of `counter`, `gauge`, `string`.
`name` renames the attribute. The default name is the relative leaf path with `/` replaced by `-`.

## Matching rules

1. Normalize. The leaf path is the notification prefix followed by the update path. Module prefixes are stripped from element names, so `openconfig-interfaces:interfaces` becomes `interfaces`. Origin is ignored.
2. Flatten. A `json_val` or `json_ietf_val` holding an object becomes one leaf per scalar, with the JSON keys appended to the path. RFC 7951 numeric strings become numbers. A JSON array of objects is skipped, with one warning per group and path.
3. Match. A group matches when its `match` path is a prefix of the leaf path. Element names compare exactly. `*` matches one element. A `[key=value]` predicate must match. Keys not named in the pattern are ignored.
4. Select. The rest of the leaf path after the match point is looked up in `attributes`. A leaf that is not listed is dropped unless the group uses `attributes: all`.
5. Every matching group applies. Groups are selectors, not a first-match chain.

## Value rules

- `counter` and `gauge` accept uint, int, float, double, decimal64, bool (as 1 or 0) and numeric strings. Other values are dropped with a rate-limited debug log.
- `string` accepts any value and stores its text form.
- Characters that `ResourcePath` rejects, including `/`, are replaced with `_` in `instance`. `TenGigE0/0/0/3` becomes `TenGigE0_0_0_3`.
- A leaf whose `instance` placeholder has no matching path key is skipped and logged with the group name.
- An attribute name longer than 19 characters logs a warning at load time, because RRD datasource names are limited to 19 characters.
- `delete` entries and `sync_response` messages are ignored.

## Engine structure

The script is written as static nested classes without dynamic Groovy features, so phase A is a near line-by-line port.

| Unit | Responsibility | Depends on |
|---|---|---|
| `MappingLoader` | Read the file (YAML or JSON), validate, return a `CompiledMapping` or a list of errors | Parser |
| `CompiledMapping` | Groups with pre-split match patterns and resolved aliases | Nothing |
| `MappingHolder` | Static cache of the current mapping. Checks the file mtime per message and reloads under a lock. | `MappingLoader` |
| `LeafFlattener` | `SubscribeResponse` to a list of `Leaf(path elements with keys, value)` | Gnmi protos |
| `LeafMatcher` | `Leaf` and groups to a list of `(group, attribute name, instance)` | `CompiledMapping` |
| `ResourceEmitter` | Write matches to `CollectionSetBuilder` as `NodeLevelResource`, `InterfaceLevelResource` or `DeferredGenericTypeResource` | Collection builder API |

Data flow per message:

```
msg -> MappingHolder.current() -> LeafFlattener -> LeafMatcher -> ResourceEmitter -> builder
```

`DeferredGenericTypeResource` already works from scripts (`cisco-nxos-telemetry-interface.groovy:59`).

## Error handling

A bad message or leaf never stops the stream.

- Missing or invalid mapping on first load: log an error naming the file and group, emit nothing, repeat the log at most once per minute.
- Invalid mapping on reload: keep the last good mapping and log the errors.
- Leaf conversion failure or missing `instance` key: skip the leaf, with a rate-limited debug log per group.
- Unexpected exception inside a message: catch it in the script, log a warning with the group name, keep what was already built. The adapter's `ScriptException` handling remains the outer net.
- Debug summary per message: leaves seen, matched and emitted.

Load-time validation fails on: unknown alias, unknown `type`, `instance` on a `node` group, missing `instance` on other groups, missing `match`, `attributes: all` without `default-type`.

## Testing

### Fixture tests

Location: `features/telemetry/protocols/openconfig/adapter/src/test/`.
The test runs the script with `new ScriptedCollectionSetBuilder(file)`, as `OpenConfigAdapterTest` does.
It loads the script from `opennms-base-assembly` through a pom test-resource entry instead of a copied file, so the tested script is the shipped script.

Each case lives in `src/test/resources/gnmi-mapping/<case>/`:

- `mapping.yaml`
- `input.textproto`: a `SubscribeResponse` in protobuf text format
- `expected.txt`: one sorted line per attribute, `resource-type|instance|group|name|type|value`

A single parameterized test runs every case.
Phase A runs the same directory against the Java engine.

Initial cases:

1. Prefix-keyed `uint_val` counters (current shipped behaviour)
2. Per-update full paths with an empty prefix
3. `json_ietf_val` subtree with module prefixes and string-encoded uint64
4. Interface name containing `/`
5. String attribute and counters on the same resource
6. Generic resource type with a `[name=CPU*]` glob
7. `attributes: all`
8. Two groups matching one leaf
9. Missing `instance` key skips the leaf
10. `delete` entries and `sync_response` are ignored

Loader tests cover each validation error and the keep-last-good behaviour on reload.

### Live validation (phase B exit gate)

Lab: `triage/gnmi-fixes-verify-2026-09-28`, stock Horizon 36.0.4 with nl6 gNMI devices.

1. Probe whether SnakeYAML resolves inside the Groovy script engine in Karaf. Choose YAML or the JSON fallback.
2. Point the adapter `script` parameter at `openconfig-gnmi-mapping.groovy` with a mapping for interface counters, interface description and CPU.
3. Pass when interface resources carry counters and the description string, the CPU generic resource has values, and an edit to the mapping file takes effect without a Core restart.

The shipped script produces empty collection sets in the same lab, which is the baseline.

## Concept translation

| gNMI collector concept | This mapping |
|---|---|
| Path aliases | `aliases` |
| Subscription name and path | a group's `name` and `match`. The subscription itself stays in the connector `paths` parameter. |
| Tag subscription, which labels metrics with a string value | a group with a `string` attribute on the same `resource-type` and `instance` |
| Guessing which part of a path names the measurement | not needed. Leaf paths are always prefix plus update path. |
| Subscription mode, sample interval, heartbeat interval | connector configuration, out of scope |
| Maximum message size, gRPC keepalive, redial, TLS | connector configuration, out of scope |
