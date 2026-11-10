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

// Disclaimer: This configuration is a proof of concept. It evaluates how usable it is
// to configure gNMI streaming telemetry metric processing. These files are not covered
// by commercial service agreements.

// Maps gNMI SubscribeResponse messages to collection sets using the declarative
// mapping files in etc/gnmi-datacollection-config.d. Every *.yaml, *.yml and
// *.json file in that directory is loaded, and their groups are merged in file
// name order. Name the files after the vendor and device class they cover,
// e.g. juniper-mx-series.yaml.
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
import java.util.concurrent.TimeUnit
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
            throw new IllegalStateException('SnakeYAML is not available to scripts, write the mapping as a .json file instead')
        }
        def yaml = yamlClass.getDeclaredConstructor().newInstance()
        return yaml.load(text)
    }

    static Object parseJson(String text) {
        Class<?> slurperClass = Class.forName('groovy.json.JsonSlurper', true, Parsers.class.getClassLoader())
        // JsonSlurper finds its FastStringService through the context class loader. On Core-local
        // sink threads that loader sees another groovy-json copy, so pin it to groovy-json's own.
        Thread thread = Thread.currentThread()
        ClassLoader previous = thread.getContextClassLoader()
        thread.setContextClassLoader(slurperClass.getClassLoader())
        try {
            def slurper = slurperClass.getDeclaredConstructor().newInstance()
            return slurper.parseText(text)
        } finally {
            thread.setContextClassLoader(previous)
        }
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
        if (group.resourceType != 'node' && group.instance != null) {
            checkInstance(group, label, result)
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

    // Without a well-formed placeholder every leaf lands on one literal instance
    private static void checkInstance(Group group, String label, LoadResult result) {
        Matcher m = Template.PLACEHOLDER.matcher(group.instance)
        StringBuffer rest = new StringBuffer()
        int placeholders = 0
        while (m.find()) {
            placeholders++
            String elem = m.group(1)
            if (group.match != null && !matchMentions(group.match, elem)) {
                result.warnings.add(label + ': instance placeholder \'' + m.group() + '\' names element \'' + elem + '\', which is not in the match path')
            }
            m.appendReplacement(rest, '')
        }
        m.appendTail(rest)
        if (placeholders == 0 || rest.indexOf('{') >= 0 || rest.indexOf('}') >= 0) {
            result.errors.add(label + ': instance \'' + group.instance + '\' must use {element.key} placeholders')
        }
    }

    // A wildcard could stand for any element, so it counts as a mention
    private static boolean matchMentions(List<PatternElem> match, String elem) {
        for (PatternElem p : match) {
            if (p.name == '*' || p.name == elem) {
                return true
            }
        }
        return false
    }
}

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
    static final Pattern PLACEHOLDER = Pattern.compile('\\{([^}.]+)\\.([^}]+)\\}')

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
    private static volatile String loadedStamp

    static File locate() {
        return new File(System.getProperty('opennms.home', '/opt/opennms'), 'etc/gnmi-datacollection-config.d')
    }

    // Mapping files in name order, so the merged group order is predictable
    static List<File> files() {
        List<File> files = new ArrayList<>()
        File[] found = locate().listFiles()
        if (found == null) {
            return files
        }
        for (File file : found) {
            String name = file.getName()
            if (file.isFile() && (name.endsWith('.yaml') || name.endsWith('.yml') || name.endsWith('.json'))) {
                files.add(file)
            }
        }
        Collections.sort(files)
        return files
    }

    static CompiledMapping current() {
        List<File> files = files()
        if (files.isEmpty()) {
            // Keep the last good mapping if every file disappears
            return active
        }
        StringBuilder sb = new StringBuilder()
        for (File file : files) {
            sb.append(file.getName()).append(':').append(file.lastModified()).append(':').append(file.length()).append('\n')
        }
        String stamp = sb.toString()
        if (stamp == loadedStamp) {
            return active
        }
        synchronized (LOCK) {
            if (stamp == loadedStamp) {
                return active
            }
            loadedStamp = stamp
            List<Group> groups = new ArrayList<>()
            boolean valid = true
            for (File file : files) {
                LoadResult result = MappingLoader.load(file)
                for (String warning : result.warnings) {
                    log.warn('gNMI mapping {}: {}', file, warning)
                }
                if (result.errors.isEmpty()) {
                    groups.addAll(result.mapping.groups)
                } else {
                    valid = false
                    log.error('gNMI mapping {} is invalid: {}', file, result.errors.join('; '))
                }
            }
            if (valid) {
                active = new CompiledMapping(groups)
                log.info('Loaded {} gNMI mapping file(s) from {} with {} groups', files.size(), locate(), groups.size())
            } else if (active != null) {
                log.error('Keeping the previous gNMI mapping until every file in {} is valid', locate())
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
        if (response.hasUpdate() && response.getUpdate().getTimestamp() > 0) {
            // gNMI timestamps are nanoseconds, but the adapter hands them to the builder as milliseconds
            builder.withTimestamp(new Date(TimeUnit.NANOSECONDS.toMillis(response.getUpdate().getTimestamp())))
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
