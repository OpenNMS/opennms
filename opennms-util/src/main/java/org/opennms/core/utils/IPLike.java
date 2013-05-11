/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.net.InetAddress;

/**
 * <p>IPLike class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class IPLike {

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
    
    /**
     * <p>matches</p>
     *
     * @param address a {@link java.lang.String} object.
     * @param pattern a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean matches(String address, String pattern) {
        String[] hostOctets = null;
        String[] matchOctets = null;
        RangeMatcher matcher = null;
        int numberOfOctets = 4;

        if (address.indexOf(':') >= 0) {
            // First try and match the scope identifier
            final String[] patternAndScope = pattern.split("%");
            pattern = patternAndScope[0];
            final String[] addressAndScope = address.split("%");
            address = addressAndScope[0];
            if (patternAndScope.length < 2) {
                // Do nothing; there was no pattern specified for the scope identifier
            } else if (patternAndScope.length == 2) {
                if (addressAndScope.length < 2) {
                    return false;
                } else if (addressAndScope.length == 2) {
                    // Assume that scope identifiers are always decimal
                    if (!matchNumericListOrRange(addressAndScope[1], patternAndScope[1], new DecimalRangeMatcher())) {
                        return false;
                    }
                } else {
                    throw new IllegalArgumentException("Illegal scope identifier in address: " + address);
                }
            } else {
                throw new IllegalArgumentException("Illegal scope identifier filter: " + pattern);
            }

            hostOctets = address.split("\\:", 0);
            matchOctets = pattern.split("\\:", 0);
            numberOfOctets = 8;
            matcher = new HexRangeMatcher();
        } else {
            hostOctets = address.split("\\.", 0);
            matchOctets = pattern.split("\\.", 0);
            numberOfOctets = 4;
            matcher = new DecimalRangeMatcher();
        }

        if (hostOctets.length != numberOfOctets) {
            throw new IllegalArgumentException("Malformatted IP address: " + address);
        } else if (matchOctets.length != numberOfOctets) {
            throw new IllegalArgumentException("Malformatted IPLIKE match expression: " + pattern);
        }

        for (int i = 0; i < numberOfOctets; i++) {
            if (!matchNumericListOrRange(hostOctets[i], matchOctets[i], matcher)) {
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
    	final String patternList[] = patterns.split(",", 0);
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
            final String ar[] = pattern.split("-");
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
        	final String ar[] = pattern.split("-");
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
