package org.opennms.netmgt.alarmd;

public interface Resolver {
    
    /**
     * Return the hostname associated with the address provided
     * @param address as a X.X.X.X formatted ipaddress string
     * @return the hostname associated with that address or the address itself if no
     *  hostname could be found
     */
    String resolveAddress(String address);

}