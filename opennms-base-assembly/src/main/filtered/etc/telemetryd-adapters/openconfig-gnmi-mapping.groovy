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
