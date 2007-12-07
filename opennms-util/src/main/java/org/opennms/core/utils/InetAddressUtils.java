package org.opennms.core.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

abstract public class InetAddressUtils {

    public static InetAddress getInetAddress(byte[] ipAddrOctets) {
        try {
            return InetAddress.getByAddress(ipAddrOctets);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress "+ipAddrOctets+" with length "+ipAddrOctets.length);
        }
        
    }

    public static InetAddress getInetAddress(String dottedNotation) {
        try {
            return InetAddress.getByName(dottedNotation);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid IPAddress "+dottedNotation);
        }
    }

    public static InetAddress getInetAddress(long ipAddrAs32bitNumber) {
        return getInetAddress(toIpAddrBytes(ipAddrAs32bitNumber));
    }
    
    public static byte[] toIpAddrBytes(long address) {
    
        byte[] octets = new byte[4];
        octets[0] = ((byte) ((address >>> 24) & 0xff));
        octets[1] = ((byte) ((address >>> 16) & 0xff));
        octets[2] = ((byte) ((address >>> 8) & 0xff));
        octets[3] = ((byte) (address & 0xff));
        
        return octets;
    }
    
    public static byte[] toIpAddrBytes(String dottedNotation) {
        return getInetAddress(dottedNotation).getAddress();
    }

    public static byte[] toIpAddrBytes(InetAddress addr) {
        return addr.getAddress();
    }
    
    public static long toIpAddrLong(byte[] address) {
        if (address.length != 4) {
            throw new IllegalArgumentException("address "+address+" has the wrong length "+address.length);
        }
        long[] octets = new long[address.length];
        octets[0] = unsignedByteToLong(address[0]);
        octets[1] = unsignedByteToLong(address[1]);
        octets[2] = unsignedByteToLong(address[2]);
        octets[3] = unsignedByteToLong(address[3]);
        
        long result = octets[0] << 24 
            | octets[1] << 16
            | octets[2] << 8
            | octets[3];
        
        return result;
        
    }
    
    public static long toIpAddrLong(String dottedNotation) {
        return toIpAddrLong(toIpAddrBytes(dottedNotation));
    }
    
    public static long toIpAddrLong(InetAddress addr) {
        return toIpAddrLong(addr.getAddress());
    }

    
    private static long unsignedByteToLong(byte b) {
        return b < 0 ? ((long)b)+256 : ((long)b);
    }

    public static String toIpAddrString(long ipAddr) {
        return getInetAddress(ipAddr).getHostAddress();
    }
    
    public static String toIpAddrString(byte[] addr) {
        return getInetAddress(addr).getHostAddress();
    }
    
    

}
