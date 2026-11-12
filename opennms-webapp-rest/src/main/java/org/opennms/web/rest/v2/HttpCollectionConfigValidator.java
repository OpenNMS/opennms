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
package org.opennms.web.rest.v2;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.web.rest.v2.model.HttpAttribDto;
import org.opennms.web.rest.v2.model.HttpCollectionConfigDto;
import org.opennms.web.rest.v2.model.HttpCollectionDto;
import org.opennms.web.rest.v2.model.HttpCollectionSettingsDto;
import org.opennms.web.rest.v2.model.HttpHeaderDto;
import org.opennms.web.rest.v2.model.HttpRrdDto;
import org.opennms.web.rest.v2.model.HttpUriDto;
import org.opennms.web.rest.v2.model.HttpUrlDto;
import org.opennms.web.rest.v2.model.ParameterDto;

/**
 * Field-level validation for the HTTP collection configuration REST service.
 *
 * <p>Runs <em>before</em> the DTOs are mapped onto the config model: the model's setters reject empty required
 * values with an {@link IllegalArgumentException}, which would surface as a 500 rather than a 400. It also
 * checks what the XSD cannot express, such as whether {@code matches} compiles and has the groups the
 * attributes refer to.</p>
 *
 * <p>Each method returns a human-readable message, or {@code null} when the input is valid.</p>
 */
public abstract class HttpCollectionConfigValidator {

    /** RRDtool caps datasource names at 19 characters. */
    public static final int MAX_DS_NAME_LENGTH = 19;

    public static final List<String> METHODS = List.of("GET", "POST");
    public static final List<String> HTTP_VERSIONS = List.of("1.0", "1.1");

    /** Mirrors the {@code rra} pattern in http-datacollection-config.xsd, anchored as XSD patterns implicitly are. */
    private static final Pattern RRA = Pattern.compile("^RRA:(AVERAGE|MIN|MAX|LAST):.*$");
    private static final Pattern RESPONSE_RANGE = Pattern.compile("^\\s*(\\d+)\\s*-\\s*(\\d+)\\s*$");

    private HttpCollectionConfigValidator() {
    }

    public static String validateSettings(final HttpCollectionSettingsDto settings) {
        if (settings == null) {
            return "Missing settings in request body.";
        }
        if (isBlank(settings.getRrdRepository())) {
            return "rrdRepository is required.";
        }
        return null;
    }

    /** Validates a whole configuration, as uploaded, including that collection names are unique. */
    public static String validateConfig(final HttpCollectionConfigDto config) {
        if (config == null) {
            return "Missing HTTP collection configuration.";
        }
        if (isBlank(config.getRrdRepository())) {
            return "rrdRepository is required.";
        }
        if (config.getCollections() == null) {
            return null;
        }
        final Set<String> names = new HashSet<>();
        for (final HttpCollectionDto collection : config.getCollections()) {
            final String message = validateCollection(collection);
            if (message != null) {
                return message;
            }
            if (!names.add(normalizedName(collection.getName()))) {
                return "The configuration contains HTTP collection '" + collection.getName().trim() + "' more than once.";
            }
        }
        return null;
    }

    public static String validateCollection(final HttpCollectionDto collection) {
        if (collection == null) {
            return "Missing HTTP collection in request body.";
        }
        if (isBlank(collection.getName())) {
            return "HTTP collection name is required.";
        }
        final String prefix = "HTTP collection '" + collection.getName().trim() + "': ";

        final String rrdMessage = validateRrd(collection.getRrd());
        if (rrdMessage != null) {
            return prefix + rrdMessage;
        }

        if (collection.getUris() == null) {
            return null;
        }
        final Set<String> uriNames = new HashSet<>();
        for (int i = 0; i < collection.getUris().size(); i++) {
            final HttpUriDto uri = collection.getUris().get(i);
            final String message = validateUri(uri);
            if (message != null) {
                return prefix + "URI at index " + i + ": " + message;
            }
            if (!uriNames.add(uri.getName().trim())) {
                return prefix + "URI name '" + uri.getName().trim() + "' is used more than once.";
            }
        }
        return null;
    }

    static String validateRrd(final HttpRrdDto rrd) {
        if (rrd == null) {
            return "rrd is required.";
        }
        if (rrd.getStep() == null || rrd.getStep() < 1) {
            return "rrd step must be a positive number of seconds.";
        }
        if (rrd.getRras() == null || rrd.getRras().isEmpty()) {
            return "at least one RRA is required.";
        }
        for (final String rra : rrd.getRras()) {
            if (rra == null || !RRA.matcher(rra.trim()).matches()) {
                return "RRA '" + rra + "' must have the form RRA:(AVERAGE|MIN|MAX|LAST):xff:steps:rows.";
            }
        }
        return null;
    }

    static String validateUri(final HttpUriDto uri) {
        if (uri == null) {
            return "the URI is empty.";
        }
        if (isBlank(uri.getName())) {
            return "name is required.";
        }
        final HttpUrlDto url = uri.getUrl();
        if (url == null) {
            return "url is required.";
        }
        final String urlMessage = validateUrl(url);
        if (urlMessage != null) {
            return urlMessage;
        }

        if (uri.getAttributes() == null) {
            return null;
        }
        final int groupCount = groupCount(url);
        final Set<String> aliases = new HashSet<>();
        for (int i = 0; i < uri.getAttributes().size(); i++) {
            final HttpAttribDto attrib = uri.getAttributes().get(i);
            final String message = validateAttrib(attrib, groupCount);
            if (message != null) {
                return "attribute at index " + i + ": " + message;
            }
            if (!aliases.add(attrib.getAlias().trim())) {
                return "attribute alias '" + attrib.getAlias().trim() + "' is used more than once.";
            }
        }
        return null;
    }

    static String validateUrl(final HttpUrlDto url) {
        if (isBlank(url.getPath())) {
            return "url path is required.";
        }
        if (url.getMethod() != null && !METHODS.contains(url.getMethod())) {
            return "method '" + url.getMethod() + "' must be one of " + String.join(", ", METHODS) + ".";
        }
        if (url.getHttpVersion() != null && !HTTP_VERSIONS.contains(url.getHttpVersion())) {
            return "httpVersion '" + url.getHttpVersion() + "' must be one of " + String.join(", ", HTTP_VERSIONS) + ".";
        }
        if (url.getPort() != null && (url.getPort() < 1 || url.getPort() > 65535)) {
            return "port " + url.getPort() + " must be between 1 and 65535.";
        }
        if (url.getResponseRange() != null && !url.getResponseRange().isEmpty()) {
            final java.util.regex.Matcher m = RESPONSE_RANGE.matcher(url.getResponseRange());
            if (!m.matches() || Integer.parseInt(m.group(1)) > Integer.parseInt(m.group(2))) {
                return "responseRange '" + url.getResponseRange() + "' must have the form low-high, such as 100-399.";
            }
        }
        if (url.getMatches() != null && !url.getMatches().isEmpty()) {
            try {
                Pattern.compile(url.getMatches(), flags(url));
            } catch (final PatternSyntaxException e) {
                return "matches is not a valid regular expression: " + e.getDescription() + ".";
            }
        }
        if (url.getParameters() != null) {
            for (final ParameterDto parameter : url.getParameters()) {
                if (parameter == null || isBlank(parameter.getKey()) || isBlank(parameter.getValue())) {
                    return "every parameter needs a key and a value.";
                }
            }
        }
        if (url.getHeaders() != null) {
            for (final HttpHeaderDto header : url.getHeaders()) {
                if (header == null || isBlank(header.getName()) || isBlank(header.getValue())) {
                    return "every header needs a name and a value.";
                }
            }
        }
        return null;
    }

    static String validateAttrib(final HttpAttribDto attrib, final int groupCount) {
        if (attrib == null) {
            return "the attribute is empty.";
        }
        if (isBlank(attrib.getAlias())) {
            return "alias is required.";
        }
        if (attrib.getAlias().trim().length() > MAX_DS_NAME_LENGTH) {
            return "alias '" + attrib.getAlias().trim() + "' is longer than " + MAX_DS_NAME_LENGTH + " characters.";
        }
        if (attrib.getMatchGroup() == null || attrib.getMatchGroup() < 1) {
            return "matchGroup must be 1 or greater.";
        }
        if (groupCount >= 0 && attrib.getMatchGroup() > groupCount) {
            return "matchGroup " + attrib.getMatchGroup() + " does not exist; the matches expression has "
                    + groupCount + (groupCount == 1 ? " group." : " groups.");
        }
        if (isBlank(attrib.getType()) || AttributeType.parse(attrib.getType().trim()) == null) {
            return "type '" + attrib.getType() + "' must be one of gauge, counter or string.";
        }
        return null;
    }

    /** The regex flags the collector compiles {@code matches} with. */
    static int flags(final HttpUrlDto url) {
        int flags = 0;
        if (isTrue(url.getCanonicalEquivalence())) flags |= Pattern.CANON_EQ;
        if (isTrue(url.getCaseInsensitive())) flags |= Pattern.CASE_INSENSITIVE;
        if (isTrue(url.getComments())) flags |= Pattern.COMMENTS;
        if (isTrue(url.getDotall())) flags |= Pattern.DOTALL;
        if (isTrue(url.getLiteral())) flags |= Pattern.LITERAL;
        if (isTrue(url.getMultiline())) flags |= Pattern.MULTILINE;
        if (isTrue(url.getUnicodeCase())) flags |= Pattern.UNICODE_CASE;
        if (isTrue(url.getUnixLines())) flags |= Pattern.UNIX_LINES;
        return flags;
    }

    /** Number of groups in {@code matches}, with the schema default {@code (.*)} when unset; -1 if it does not compile. */
    private static int groupCount(final HttpUrlDto url) {
        final String matches = url.getMatches() == null || url.getMatches().isEmpty() ? "(.*)" : url.getMatches();
        try {
            return Pattern.compile(matches, flags(url)).matcher("").groupCount();
        } catch (final PatternSyntaxException e) {
            return -1;
        }
    }

    /** The collector looks collections up by name ignoring case, so names that differ only in case clash. */
    static String normalizedName(final String name) {
        return name == null ? null : name.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isTrue(final Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }
}
