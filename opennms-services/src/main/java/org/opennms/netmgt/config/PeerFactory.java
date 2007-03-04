package org.opennms.netmgt.config;

import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

/*
 * Convenience superclass for NSClientPeerFactory and SnmpPeerFactory, with common code used in both
 */
public class PeerFactory {

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
    * @param value
    * @param patterns
    * @return
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
            String ar[] = pattern.split("-");
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