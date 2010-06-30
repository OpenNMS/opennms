/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: March 20, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.net.InetAddress;


/**
 * Convenience superclass for NSClientPeerFactory and SnmpPeerFactory, with common code used in both
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @version $Id: $
 */
public class PeerFactory {

    /**
     * <p>Constructor for PeerFactory.</p>
     */
    public PeerFactory() {
        super();
    }

    /**
     * Converts the internet address to a long value so that it can be compared
     * using simple opertions. The address is converted in network byte order
     * (big endin) and allows for comparisions like &lt;, &gt;, &lt;=, &gt;=,
     * ==, and !=.
     *
     * @param addr
     *            The address to convert to a long
     * @return The address as a long value.
     */
    protected static long toLong(InetAddress addr) {
        byte[] baddr = addr.getAddress();
        long result = ((long) baddr[0] & 0xffL) << 24 | ((long) baddr[1] & 0xffL) << 16 | ((long) baddr[2] & 0xffL) << 8 | ((long) baddr[3] & 0xffL);

        return result;
    }
    
    /**
     * <p>verifyIpMatch</p>
     *
     * @param hostAddress a {@link java.lang.String} object.
     * @param ipMatch a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean verifyIpMatch(String hostAddress, String ipMatch) {
        
        String hostOctets[] = hostAddress.split("\\.", 0);
        String matchOctets[] = ipMatch.split("\\.", 0);
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
     * @param value a {@link java.lang.String} object.
     * @param patterns a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean matchNumericListOrRange(String value, String patterns) {
        
        String patternList[] = patterns.split(",", 0);
        for (int i = 0; i < patternList.length; i++) {
            if (matchRange(value, patternList[i]))
                return true;
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
        
        if ("*".equals(pattern))
            return true;
        else if (dashCount == 0)
            return value.equals(pattern);
        else if (dashCount > 1)
            return false;
        else if (dashCount == 1) {
            String ar[] = pattern.split("-");
            int rangeBegin = Integer.parseInt(ar[0]);
            int rangeEnd = Integer.parseInt(ar[1]);
            int ip = Integer.parseInt(value);
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
