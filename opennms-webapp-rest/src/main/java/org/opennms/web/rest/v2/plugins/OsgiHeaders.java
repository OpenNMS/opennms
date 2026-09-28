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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.web.rest.v2.plugins.KarInspection.ImportDescriptor;

/**
 * Minimal parser for OSGi manifest header clauses and version strings, so the
 * inspector does not need the OSGi framework classes.
 */
final class OsgiHeaders {

    static final class Clause {
        final List<String> paths = new ArrayList<>();
        final Map<String, String> attributes = new LinkedHashMap<>();
        final Map<String, String> directives = new LinkedHashMap<>();
    }

    private static final Pattern OSGI_EE_VERSION = Pattern.compile("\\(version\\s*=\\s*([0-9.]+)\\)");
    private static final Pattern OSGI_EE_NAME = Pattern.compile("\\(osgi\\.ee\\s*=\\s*([^)]+)\\)");

    private OsgiHeaders() {
    }

    /** Splits a header into clauses, honouring quotes around attribute values. */
    static List<Clause> parse(final String header) {
        if (header == null || header.trim().isEmpty()) {
            return Collections.emptyList();
        }
        final List<Clause> clauses = new ArrayList<>();
        for (final String rawClause : splitOutsideQuotes(header, ',')) {
            final Clause clause = new Clause();
            for (final String part : splitOutsideQuotes(rawClause, ';')) {
                final String p = part.trim();
                if (p.isEmpty()) {
                    continue;
                }
                final int directiveIdx = p.indexOf(":=");
                final int attributeIdx = p.indexOf('=');
                if (directiveIdx > 0) {
                    clause.directives.put(p.substring(0, directiveIdx).trim(), unquote(p.substring(directiveIdx + 2)));
                } else if (attributeIdx > 0) {
                    clause.attributes.put(p.substring(0, attributeIdx).trim(), unquote(p.substring(attributeIdx + 1)));
                } else {
                    clause.paths.add(p);
                }
            }
            if (!clause.paths.isEmpty()) {
                clauses.add(clause);
            }
        }
        return clauses;
    }

    /** Import-Package as package name to version range ("" when unversioned) and resolution directive. */
    static Map<String, ImportDescriptor> parseImports(final String importPackage) {
        final Map<String, ImportDescriptor> imports = new LinkedHashMap<>();
        for (final Clause clause : parse(importPackage)) {
            final String version = clause.attributes.getOrDefault("version", "");
            final boolean optional = "optional".equalsIgnoreCase(clause.directives.getOrDefault("resolution", "").trim());
            for (final String pkg : clause.paths) {
                imports.put(pkg, new ImportDescriptor(version, optional));
            }
        }
        return imports;
    }

    static List<String> parseExports(final String exportPackage) {
        final List<String> exports = new ArrayList<>();
        for (final Clause clause : parse(exportPackage)) {
            exports.addAll(clause.paths);
        }
        return exports;
    }

    static String parseSymbolicName(final String bundleSymbolicName) {
        final List<Clause> clauses = parse(bundleSymbolicName);
        return clauses.isEmpty() ? null : clauses.get(0).paths.get(0);
    }

    /** The JavaSE version demanded by an osgi.ee Require-Capability clause, or null. */
    static String parseRequiredJavaVersion(final String requireCapability) {
        for (final Clause clause : parse(requireCapability)) {
            if (!clause.paths.contains("osgi.ee")) {
                continue;
            }
            final String filter = clause.directives.get("filter");
            if (filter == null) {
                continue;
            }
            final Matcher name = OSGI_EE_NAME.matcher(filter);
            if (name.find() && !name.group(1).trim().startsWith("JavaSE")) {
                continue;
            }
            final Matcher version = OSGI_EE_VERSION.matcher(filter);
            if (version.find()) {
                return version.group(1);
            }
        }
        return null;
    }

    /** Java feature release number for "1.8", "8", "17.0.1" style strings; -1 when unparseable. */
    static int javaFeatureVersion(final String version) {
        if (version == null) {
            return -1;
        }
        final String[] parts = version.trim().split("\\.");
        try {
            final int first = Integer.parseInt(parts[0]);
            if (first == 1 && parts.length > 1) {
                return Integer.parseInt(parts[1]);
            }
            return first;
        } catch (final NumberFormatException e) {
            return -1;
        }
    }

    /** OSGi version comparison on major.minor.micro.qualifier; qualifier compares as a string. */
    static int compareVersions(final String a, final String b) {
        final int[] na = numericParts(a);
        final int[] nb = numericParts(b);
        for (int i = 0; i < 3; i++) {
            if (na[i] != nb[i]) {
                return Integer.compare(na[i], nb[i]);
            }
        }
        return qualifier(a).compareTo(qualifier(b));
    }

    static int majorVersion(final String version) {
        return numericParts(version)[0];
    }

    /**
     * True when {@code version} satisfies an OSGi range such as "[1.0,2)"; a bare
     * version means "at least"; an empty range matches everything.
     */
    static boolean inRange(final String range, final String version) {
        if (range == null || range.trim().isEmpty()) {
            return true;
        }
        final String r = range.trim();
        final char first = r.charAt(0);
        if (first != '[' && first != '(') {
            return compareVersions(version, r) >= 0;
        }
        final char last = r.charAt(r.length() - 1);
        final String[] bounds = r.substring(1, r.length() - 1).split(",");
        if (bounds.length != 2) {
            return true;
        }
        final int low = compareVersions(version, bounds[0].trim());
        final int high = compareVersions(version, bounds[1].trim());
        final boolean lowOk = first == '[' ? low >= 0 : low > 0;
        final boolean highOk = last == ']' ? high <= 0 : high < 0;
        return lowOk && highOk;
    }

    static boolean looksLikeRange(final String version) {
        return version != null && !version.isEmpty() && (version.charAt(0) == '[' || version.charAt(0) == '(');
    }

    private static int[] numericParts(final String version) {
        final int[] parts = new int[3];
        if (version == null) {
            return parts;
        }
        final String[] split = version.trim().split("\\.", 4);
        for (int i = 0; i < 3 && i < split.length; i++) {
            try {
                parts[i] = Integer.parseInt(split[i].trim());
            } catch (final NumberFormatException e) {
                parts[i] = 0;
            }
        }
        return parts;
    }

    private static String qualifier(final String version) {
        if (version == null) {
            return "";
        }
        final String[] split = version.trim().split("\\.", 4);
        return split.length > 3 ? split[3] : "";
    }

    private static String unquote(final String value) {
        final String v = value.trim();
        if (v.length() >= 2 && v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    private static List<String> splitOutsideQuotes(final String value, final char separator) {
        final List<String> parts = new ArrayList<>();
        final StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if (c == '"') {
                quoted = !quoted;
                current.append(c);
            } else if (c == separator && !quoted) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts;
    }
}
