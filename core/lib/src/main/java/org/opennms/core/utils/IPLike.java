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
package org.opennms.core.utils;

import java.net.InetAddress;

/**
 * <p>IPLike class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class IPLike {

    private enum AddressType {
        IPv4,
        IPv6,
    }

    private static class IPv6Address {
        public final String[] fields;
        public final String scope;

        private IPv6Address(final String[] fields,
                            final String scope) {
            this.fields = fields;
            this.scope = scope;
        }
    }

    private interface RangeMatcher {
        boolean match(final String value, final String range);
    }

    private static class HexRangeMatcher implements RangeMatcher {
        @Override
        public boolean match(final String value, final String range) {
            return matchRangeHex(value, range);
        }
    }

    private static class DecimalRangeMatcher implements RangeMatcher {
        @Override
        public boolean match(final String value, final String range) {
            return matchRange(value, range);
        }
    }

    public static boolean matches(final InetAddress address, final String pattern) {
    	return matches(InetAddressUtils.str(address), pattern);
    }

    private static AddressType classifyAddress(final String address) {
        if (address.indexOf(':') != -1) {
            return AddressType.IPv6;
        }

        if (address.indexOf('.') != -1) {
            return AddressType.IPv4;
        }
        throw new IllegalArgumentException("Cannot determine whether address is a IPv4 or IPv6 address");
    }

    private static String[] parseIPv4Address(final String address) {
        // Split address in fields
        return address.split("\\.", 0);
    }

    private static IPv6Address parseIPv6Address(final String address) {
        // Split of scope identifier
        final String[] addressAndScope = address.split("%", 2);

        // Split address in fields
        final String[] fields = addressAndScope[0].split("\\:", 0);

        final String scope = addressAndScope.length == 2
                             ? addressAndScope[1]
                             : null;

        return new IPv6Address(fields, scope);
    }

    /**
     * <p>matches</p>
     *
     * @param address a {@link java.lang.String} object.
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean matches(String address, String pattern) {
        final AddressType addressType = classifyAddress(address);
        final AddressType patternType = classifyAddress(pattern);

        if (addressType != patternType) {
            // Different address types will never match
            return false;
        }

        final String[] addressFields;
        final String[] patternFields;
        final int expectedFieldCount;
        final RangeMatcher matcher;
        switch (addressType) {
            case IPv4: {
                addressFields = parseIPv4Address(address);
                patternFields = parseIPv4Address(pattern);
                expectedFieldCount = 4;
                matcher = new DecimalRangeMatcher();
                break;
            }

            case IPv6: {
                final IPv6Address parsedAddress = parseIPv6Address(address);
                final IPv6Address parsedPattern = parseIPv6Address(pattern);

                if (parsedPattern.scope != null) {
                    if (parsedAddress.scope == null) {
                        // Fail if scope is expected but does not exists
                        return false;
                    } else {
                        // Assume that scope identifiers are always decimal
                        if (!matchNumericListOrRange(parsedAddress.scope, parsedPattern.scope, new DecimalRangeMatcher())) {
                            return false;
                        }
                    }
                }

                addressFields = parsedAddress.fields;
                patternFields = parsedPattern.fields;
                expectedFieldCount = 8;
                matcher = new HexRangeMatcher();
                break;
            }

            default: throw new IllegalStateException();
        }

        if (addressFields.length != expectedFieldCount) {
            throw new IllegalArgumentException("Malformatted IP address: " + address);
        }

        if (patternFields.length != expectedFieldCount) {
            throw new IllegalArgumentException("Malformatted IPLIKE match expression: " + pattern);
        }

        for (int i = 0; i < expectedFieldCount; i++) {
            if (!matchNumericListOrRange(addressFields[i], patternFields[i], matcher)) {
                return false;
            }
        }

        return true;
    }

    public static boolean matchNumericListOrRange(final String value, final String patterns) {
        return matchNumericListOrRange(value, patterns, new DecimalRangeMatcher());
    }

    /**
     * Use this method to match ranges, lists, and specific number strings
     * such as:
     * "200-300" or "200,300,501-700"
     * "*" matches any
     * This method is commonly used for matching IP octets or ports
     *
     * @param value a {@link java.lang.String} object.
     * @param patterns a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean matchNumericListOrRange(final String value, final String patterns, final RangeMatcher matcher) {
    	final String[] patternList = patterns.split(",", 0);
        for (final String element : patternList) {
            if (matcher.match(value, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Helper method in support of matchNumericListOrRange
     *
     * @param value a {@link java.lang.String} object.
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean matchRange(final String value, final String pattern) {
    	final int dashCount = countChar('-', pattern);

        if ("*".equals(pattern)) {
            return true;
        } else if (dashCount == 0) {
            return Long.parseLong(pattern, 10) ==  Long.parseLong(value, 10);
        } else if (dashCount > 1) {
            return false;
        } else if (dashCount == 1) {
            final String[] ar = pattern.split("-");
            final long rangeBegin = Long.parseLong(ar[0]);
            final long rangeEnd = Long.parseLong(ar[1]);
            final long ip = Long.parseLong(value);
            return (ip >= rangeBegin && ip <= rangeEnd);
        }
        return false;
    }

    /**
     * Helper method in support of matchNumericListOrRange
     *
     * @param value a {@link java.lang.String} object.
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean matchRangeHex(final String value, final String pattern) {
    	final int dashCount = countChar('-', pattern);

        if ("*".equals(pattern)) {
            return true;
        } else if (dashCount == 0) {
            // Convert values to hex integers and compare
            return Long.parseLong(pattern, 16) ==  Long.parseLong(value, 16);
        } else if (dashCount > 1) {
            return false;
        } else if (dashCount == 1) {
        	final String[] ar = pattern.split("-");
            final long rangeBegin = Long.parseLong(ar[0], 16);
            final long rangeEnd = Long.parseLong(ar[1], 16);
            final long ip = Long.parseLong(value, 16);
            return (ip >= rangeBegin && ip <= rangeEnd);
        }
        return false;
    }

    /**
     * <p>countChar</p>
     *
     * @param charIn a char.
     * @param stingIn a {@link java.lang.String} object.
     * @return a int.
     */
    public static int countChar(final char charIn, final String stingIn) {

        int charCount = 0;
        int charIndex = 0;
        for (int i=0; i<stingIn.length(); i++) {
            charIndex = stingIn.indexOf(charIn, i);
            if (charIndex != -1) {
                charCount++;
                i = charIndex +1;
            }
        }
        return charCount;
    }

}
