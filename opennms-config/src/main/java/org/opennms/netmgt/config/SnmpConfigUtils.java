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
package org.opennms.netmgt.config;

import com.google.common.base.Strings;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnmpConfigUtils {
    private static final String COMMA_DELIMITER = ",";
    private static final String RANGE_DELIMITER = "-";
    private static final String IPLIKE_IPV4_VALIDATION_REGEX = "(([0-9]{1,3}(([,-])[0-9]{1,3})*|\\*)\\.){3}([0-9]{1,3}(([,-])[0-9]{1,3})*|\\*)";
    private static final String IPLIKE_IPV6_VALIDATION_REGEX = "(([0-9a-fA-F]{1,4}(([,-])[0-9a-fA-F]{1,4})*|\\*):){7}([0-9a-fA-F]{1,4}(([,-])[0-9a-fA-F]{1,4})*|\\*)";

    public enum DefinitionStatsValidationStatus {
        VALID,
        MISSING_CONTENTS,
        CANNOT_MIX_RANGE_AND_IPMATCH
    }

    public enum DefinitionContentsValidationStatus {
        VALID,
        INVALID_SPECIFIC_ADDRESS,
        INVALID_RANGE,
        INVALID_RANGE_BEGIN,
        INVALID_RANGE_END,
        INVALID_EMPTY
    }

    private record DefinitionStats(boolean hasRange, boolean hasSpecific, boolean hasIpMatch) {}

    public record ValidatedDefinitionContents(
        DefinitionContentsValidationStatus status,
        String invalidItem,
        List<String> definitionIpMatches,
        List<String> definitionSpecifics,
        List<Range> definitionRanges
    ) {}

    public static DefinitionStatsValidationStatus validateDefinitionContents(Definition definition) {
        DefinitionStats stats = getDefinitionStats(definition);

        if (!stats.hasRange && !stats.hasSpecific && !stats.hasIpMatch) {
            return DefinitionStatsValidationStatus.MISSING_CONTENTS;
        }

        if (stats.hasIpMatch && (stats.hasRange || stats.hasSpecific)) {
            return DefinitionStatsValidationStatus.CANNOT_MIX_RANGE_AND_IPMATCH;
        }

        return DefinitionStatsValidationStatus.VALID;
    }

    /**
     * Validates IP addresses in the Definition's specifics and ranges.
     * @return error message if validation fails, null if valid
     */
    public static String validateDefinitionIpAddresses(final Definition definition) {
        for (final String specific : definition.getSpecifics()) {
            if (Strings.isNullOrEmpty(specific)) {
                return "Invalid specific IP address: empty value.";
            }
            InetAddress addr = null;

            try {
                addr = InetAddressUtils.addr(specific);
            } catch (IllegalArgumentException ignored) {
            }

            if (addr == null) {
                return String.format("Invalid specific IP address: %s", specific);
            }
        }

        for (final Range range : definition.getRanges()) {
            if (Strings.isNullOrEmpty(range.getBegin()) || Strings.isNullOrEmpty(range.getEnd())) {
                return String.format("Invalid range: begin and end must be specified. begin=%s, end=%s",
                        range.getBegin(), range.getEnd());
            }

            InetAddress beginAddr = null;
            InetAddress endAddr = null;

            try {
                beginAddr = InetAddressUtils.addr(range.getBegin());
                endAddr = InetAddressUtils.addr(range.getEnd());
            } catch (IllegalArgumentException ignored) {
            }

            if (beginAddr == null) {
                return String.format("Invalid range begin IP address: %s", range.getBegin());
            }
            if (endAddr == null) {
                return String.format("Invalid range end IP address: %s", range.getEnd());
            }

            // Ensure both addresses are the same IP version
            // They should be either both Inet4Address or both Inet6Address, but not one of each
            boolean beginIsV4 = beginAddr instanceof java.net.Inet4Address;
            boolean endIsV4 = endAddr instanceof java.net.Inet4Address;

            if (beginIsV4 != endIsV4) {
                return String.format("Invalid range: begin and end must be same IP version. begin=%s, end=%s",
                        range.getBegin(), range.getEnd());
            }
        }

        return null;
    }

    /**
     * Validates IP match expressions in the Definition's ipMatch list.
     * @return error message if validation fails, null if valid
     */
    public static String validateDefinitionIpMatches(final Definition definition) {
        for (final String ipMatch : definition.getIpMatches()) {
            if (Strings.isNullOrEmpty(ipMatch)) {
                return "Invalid IP match expression: empty value.";
            }

            final String normalizedIpMatch = expandIPv6Compressed(ipMatch);
            if (normalizedIpMatch == null || (!normalizedIpMatch.matches(IPLIKE_IPV4_VALIDATION_REGEX) && !normalizedIpMatch.matches(IPLIKE_IPV6_VALIDATION_REGEX))) {
                return String.format("Invalid IP match expression: '%s'.", ipMatch);
            }

            // TODO: Consider more robust validation, for example checking octets are in the 0-255 range
        }

        return null;
    }

    /**
     * Given comma-separated strings of IP ranges, specifics and ipMatches (IPLIKE expressions),
     * parse them into arrays and validate their contents.
     * The returned `status` and `invalidItem` can be used to create an error message.
     */
    public static ValidatedDefinitionContents sanitizeAndValidateDefinitionItems(
            final String specifics, final String ranges, final String ipMatches) {
        List<String> rangeItems = splitAndTrim(ranges, COMMA_DELIMITER);
        List<String> definitionIpMatches = splitAndTrim(ipMatches, COMMA_DELIMITER);
        List<String> definitionSpecifics = splitAndTrim(specifics, COMMA_DELIMITER);
        List<Range> definitionRanges = new ArrayList<>();

        for (String specific : definitionSpecifics) {
            if (safeGetInetAddress(specific) == null) {
                return new ValidatedDefinitionContents(DefinitionContentsValidationStatus.INVALID_SPECIFIC_ADDRESS,
                        specific, null, null, null);
            }
        }

        for (String range : rangeItems) {
            List<String> rangeSplit = splitAndTrim(range, RANGE_DELIMITER);

            if (rangeSplit.size() != 2) {
                return new ValidatedDefinitionContents(DefinitionContentsValidationStatus.INVALID_RANGE,
                        range, null, null, null);
            }

            Range r = new Range(rangeSplit.get(0), rangeSplit.get(1));

            if (safeGetInetAddress(r.getBegin()) == null) {
                return new ValidatedDefinitionContents(DefinitionContentsValidationStatus.INVALID_RANGE_BEGIN,
                        r.getBegin(), null, null, null);
            }

            if (safeGetInetAddress(r.getEnd()) == null) {
                return new ValidatedDefinitionContents(DefinitionContentsValidationStatus.INVALID_RANGE_END,
                        r.getEnd(), null, null, null);
            }

            definitionRanges.add(r);
        }

        if (definitionSpecifics.isEmpty() && definitionRanges.isEmpty() && definitionIpMatches.isEmpty()) {
            return new ValidatedDefinitionContents(DefinitionContentsValidationStatus.INVALID_EMPTY,
                    null, null, null, null);
        }

        return new ValidatedDefinitionContents(DefinitionContentsValidationStatus.VALID,
                null, definitionIpMatches, definitionSpecifics, definitionRanges);
    }

    /**
     * Gather statistics about a Definition. For now, just whether each part is empty or not.
     */
    private static DefinitionStats getDefinitionStats(Definition definition) {
        boolean hasRange = definition.getRanges().stream()
                .anyMatch(range -> !Strings.isNullOrEmpty(range.getBegin()) && !Strings.isNullOrEmpty(range.getEnd()));

        boolean hasSpecific = definition.getSpecifics().stream().anyMatch(spec -> !Strings.isNullOrEmpty(spec));
        boolean hasIpMatch = definition.getIpMatches().stream().anyMatch(match -> !Strings.isNullOrEmpty(match));

        return new DefinitionStats(hasRange, hasSpecific, hasIpMatch);
    }

    /** Return a valid InetAddress, or null if it could not be parsed. */
    public static InetAddress safeGetInetAddress(String ipAddress) {
        InetAddress addr = null;

        try {
            if (!Strings.isNullOrEmpty(ipAddress)) {
                addr = InetAddressUtils.addr(ipAddress);
            }
        } catch (Exception e) {
            return null;
        }

        return addr;
    }

    /**
     * Expands compressed IPv6 notation (e.g. {@code 2001:db8::1}) into its full 8-hextet form
     * (e.g. {@code 2001:db8:0:0:0:0:0:1}) so it can be validated against the IPv6 IPLIKE regex.
     * Returns the input unchanged if it contains no {@code ::}.
     * Returns {@code null} if the input is malformed (e.g. contains multiple {@code ::}).
     */
    private static String expandIPv6Compressed(final String ipMatch) {
        if (!ipMatch.contains("::")) {
            return ipMatch;
        }

        final String[] sides = ipMatch.split("::", -1);

        if (sides.length > 2) {
            return null;
        }
        final String left = sides[0];
        final String right = sides.length > 1 ? sides[1] : "";

        final int leftGroupCount = left.isEmpty() ? 0 : left.split(":").length;
        final int rightGroupCount = right.isEmpty() ? 0 : right.split(":").length;
        final int zeroGroupCount = 8 - leftGroupCount - rightGroupCount;

        final StringBuilder expanded = new StringBuilder();

        if (!left.isEmpty()) {
            expanded.append(left).append(":");
        }

        expanded.append("0:".repeat(Math.max(0, zeroGroupCount)));

        if (!right.isEmpty()) {
            expanded.append(right);
        } else {
            expanded.deleteCharAt(expanded.length() - 1);
        }

        return expanded.toString();
    }

    private static List<String> splitAndTrim(final String source, final String delimiter) {
        String trimmed = !Strings.isNullOrEmpty(source) ? source.trim() : "";

        return Arrays.stream(trimmed.split(delimiter))
            .map(String::trim)
            .filter(s -> !Strings.isNullOrEmpty(s))
            .toList();
    }
}
