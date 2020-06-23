/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.opennmsd;

import java.net.InetAddress;

public abstract class IpAddressUtils {
    
    /**
     * Converts the internet address to a long value so that it can be compared
     * using simple opertions. The address is converted in network byte order
     * (big endin) and allows for comparisions like &lt;, &gt;, &lt;=, &gt;=,
     * ==, and !=.
     * 
     * @param addr
     *            The address to convert to a long
     * 
     * @return The address as a long value.
     * 
     */
    protected static long toLong(InetAddress addr) {
        byte[] baddr = addr.getAddress();
        long result = ((long) baddr[0] & 0xffL) << 24 | ((long) baddr[1] & 0xffL) << 16 | ((long) baddr[2] & 0xffL) << 8 | ((long) baddr[3] & 0xffL);

        return result;
    }
    
    public static boolean verifyIpMatch(String hostAddress, String ipMatch) {
        
        String[] hostOctets = hostAddress.split("\\.", 0);
        String[] matchOctets = ipMatch.split("\\.", 0);
        for (int i = 0; i < 4; i++) {
            if (!matchNumericListOrRange(hostOctets[i], matchOctets[i]))
                return false;
        }
        return true;
    }

    /**
    * Use this method to match ranges, lists, and specific number strings
    * such as:
    * "200-300" or "200,300,501-700"
    * "*" matches any
    * This method is commonly used for matching IP octets or ports
    * 
    * @param value
    * @param patterns
    * @return
    */
    public static boolean matchNumericListOrRange(String value, String patterns) {
        
        String[] patternList = patterns.split(",", 0);
        for (int i = 0; i < patternList.length; i++) {
            if (matchRange(value, patternList[i]))
                return true;
        }
        return false;
    }

    /**
    * Helper method in support of matchNumericListOrRange
    * @param value
    * @param pattern
    * @return
    */
    public static boolean matchRange(String value, String pattern) {
        int dashCount = countChar('-', pattern);
        
        if ("*".equals(pattern))
            return true;
        else if (dashCount == 0)
            return value.equals(pattern);
        else if (dashCount > 1)
            return false;
        else if (dashCount == 1) {
            String[] ar = pattern.split("-");
            int rangeBegin = Integer.parseInt(ar[0]);
            int rangeEnd = Integer.parseInt(ar[1]);
            int ip = Integer.parseInt(value);
            return (ip >= rangeBegin && ip <= rangeEnd);
        }
        return false;
    }

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
