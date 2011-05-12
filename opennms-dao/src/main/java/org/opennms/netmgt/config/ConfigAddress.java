/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.net.InetAddress;

import org.opennms.netmgt.model.discovery.IPAddress;

/**
 * ConfigAddress
 *
 * @author brozow
 */
public class ConfigAddress implements Comparable<ConfigAddress>{

    private final IPAddress m_addr;
    
    public static ConfigAddress min(ConfigAddress addr1, ConfigAddress addr2) { return addr1.isLessThan(addr2) ? addr1 : addr2; } 
    public static ConfigAddress max(ConfigAddress addr1, ConfigAddress addr2) { return addr1.isGreaterThan(addr2) ? addr1 : addr2; } 

    public ConfigAddress(IPAddress addr) {
        m_addr = addr;
    }
    
    public ConfigAddress(InetAddress addr) {
        this(new IPAddress(addr));
    }
    
    public ConfigAddress(String addrString) {
        this(new IPAddress(addrString));
    }
    
    public long getLong() {
        return getIPAddress().toBigInteger().longValue();
    }

    boolean isGreaterThan(ConfigAddress other) {
        return getIPAddress().isGreaterThan(other.getIPAddress());
    }
    
    boolean isGreaterThanOrEqualTo(ConfigAddress other) {
        return getIPAddress().isGreaterThanOrEqualTo(other.getIPAddress());
    }
    
    boolean isLessThanOrEqualTo(ConfigAddress other) {
        return getIPAddress().isLessThanOrEqualTo(other.getIPAddress());
    }
    
    boolean isLessThan(ConfigAddress other) {
        return getIPAddress().isLessThan(other.getIPAddress());
    }

    ConfigAddress incr() {
        return new ConfigAddress(getIPAddress().incr());
    }
    
    ConfigAddress decr() {
        return new ConfigAddress(getIPAddress().decr());
    }
    
    boolean immediatelyFollows(ConfigAddress addr) {
        return addr.immediatelyPreceeds(this);
    }

    boolean immediatelyPreceeds(ConfigAddress addr) {
        return incr().equals(addr);
    }
    
    @Override
    public String toString() {
        return getIPAddress().toInetAddress().getHostAddress().replaceFirst("(^|:)(0:)+", "::");
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ConfigAddress) {
            ConfigAddress addr = (ConfigAddress)o;
            return getIPAddress().equals(addr.getIPAddress());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return getIPAddress().hashCode();
    }

    @Override
    public int compareTo(ConfigAddress addr) {
        return getIPAddress().compareTo(addr.getIPAddress());
    }

    /**
     * @return the addr
     */
    private IPAddress getIPAddress() {
        return m_addr;
    }

    
    

}
