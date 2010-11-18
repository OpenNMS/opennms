package org.opennms.core.utils;

/**
 * <p>IPLike class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class IPLike {

    private interface RangeMatcher {
        boolean match(String value, String range);
    }

    private static class HexRangeMatcher implements RangeMatcher {
        public boolean match(String value, String range) {
            return matchRangeHex(value, range);
        }
    }

    private static class DecimalRangeMatcher implements RangeMatcher {
        public boolean match(String value, String range) {
            return matchRange(value, range);
        }
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
            String[] patternAndScope = pattern.split("%");
            pattern = patternAndScope[0];
            String[] addressAndScope = address.split("%");
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

    public static boolean matchNumericListOrRange(String value, String patterns) {
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
    public static boolean matchNumericListOrRange(String value, String patterns, RangeMatcher matcher) {

        String patternList[] = patterns.split(",", 0);
        for (String element : patternList) {
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
    public static boolean matchRange(String value, String pattern) {
        int dashCount = countChar('-', pattern);

        if ("*".equals(pattern)) {
            return true;
        } else if (dashCount == 0) {
            return Long.parseLong(pattern, 10) ==  Long.parseLong(value, 10);
        } else if (dashCount > 1) {
            return false;
        } else if (dashCount == 1) {
            String ar[] = pattern.split("-");
            long rangeBegin = Long.parseLong(ar[0]);
            long rangeEnd = Long.parseLong(ar[1]);
            long ip = Long.parseLong(value);
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
    public static boolean matchRangeHex(String value, String pattern) {
        int dashCount = countChar('-', pattern);

        if ("*".equals(pattern)) {
            return true;
        } else if (dashCount == 0) {
            // Convert values to hex integers and compare
            return Long.parseLong(pattern, 16) ==  Long.parseLong(value, 16);
        } else if (dashCount > 1) {
            return false;
        } else if (dashCount == 1) {
            String ar[] = pattern.split("-");
            long rangeBegin = Long.parseLong(ar[0], 16);
            long rangeEnd = Long.parseLong(ar[1], 16);
            long ip = Long.parseLong(value, 16);
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
    public static int countChar(char charIn, String stingIn) {

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
