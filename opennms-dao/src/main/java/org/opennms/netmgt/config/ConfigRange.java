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

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.ConfigRange;
import org.opennms.netmgt.config.common.Range;

class ConfigRange implements Comparable<ConfigRange> {
    
    long m_begin;
    long m_end;
    
    public ConfigRange(long beginAddr, long endAddr) {
        if (beginAddr > endAddr) {
            throw new IllegalArgumentException("Invalid range ["+InetAddressUtils.toIpAddrString(beginAddr)+", "+InetAddressUtils.toIpAddrString(endAddr)+"]");
        }
        m_begin = beginAddr;
        m_end = endAddr;
    }
    
    
    public ConfigRange(String beginAddr, String endAddr) {
        this(InetAddressUtils.toIpAddrLong(beginAddr), InetAddressUtils.toIpAddrLong(endAddr));
    }

    public ConfigRange(Range r) {
        this(r.getBegin(), r.getEnd());
    }

    public ConfigRange(String specific) {
        this(specific, specific);
    }
    
    public long getBeginLong() {
        return m_begin;
    }
    
    public long getEndLong() {
        return m_end;
    }
    
    public String getBegin() {
        return InetAddressUtils.toIpAddrString(m_begin);
    }
    
    public String getEnd() {
        return InetAddressUtils.toIpAddrString(m_end);
    }
    
    public String getSpecificString() {
        if (!isSpecific()) {
            throw new IllegalStateException("Cannot get the specific string unless the range as only a single addrs: " + this);
        }
        return getBegin();
    }
    
    public boolean isSpecific() {
        return m_begin == m_end;
    }
    
    public boolean contains(long addr) {
        return m_begin <= addr && addr <= m_end;
    }
    
    public boolean preceeds(ConfigRange r) {
        return m_end < r.m_begin;
    }
    
    public boolean follows(ConfigRange r) {
        return r.preceeds(this);
    }
    
    public boolean overlaps(ConfigRange r) {
        return m_begin <= r.m_end && r.m_begin <= m_end;
    }
    
    public boolean contains(ConfigRange r) {
        return m_begin <= r.m_begin && r.m_end <= m_end;
    }
    
    public boolean combinable(ConfigRange r) {
        return overlaps(r) || adjacent(r);
    }
    
    public ConfigRange combine(ConfigRange r) {
        if (!combinable(r)) {
            throw new IllegalArgumentException(String.format("Range %s is not combinable with range %s", this, r));
        }
        
        return new ConfigRange(Math.min(m_begin, r.m_begin), Math.max(m_end, r.m_end));
    }
    
    public int compareTo(ConfigRange r) {
        if (preceeds(r)) {
            // this is less than 
            return -1;
        } else if (follows(r)) {
            // this is greater than
            return 1;
        } else {
            // otherwise it overlaps
            return 0;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConfigRange) {
            ConfigRange r = (ConfigRange)obj;
            return m_begin == r.m_begin && m_end == r.m_end;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(m_begin).hashCode()*31+Long.valueOf(m_end).hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        b.append(InetAddressUtils.toIpAddrString(m_begin));
        b.append(",");
        b.append(InetAddressUtils.toIpAddrString(m_end));
        b.append("]");
        return b.toString();
    }

    public boolean contains(String address) {
        return contains(InetAddressUtils.toIpAddrLong(address));
    }

    public boolean adjacent(ConfigRange r) {
        return (r.m_end+1 == m_begin || m_end+1 == r.m_begin);
    }

    public ConfigRange[] remove(ConfigRange r) {
        if (r.contains(this)) {
            return new ConfigRange[0];
        } else if (!overlaps(r)) {
            return new ConfigRange[] { this };
        } else {
            
            List<ConfigRange> ranges = new ArrayList<ConfigRange>(2);
            if (m_begin < r.m_begin) {
                ranges.add(new ConfigRange(m_begin, r.m_begin-1));
            }
            if (r.m_end < m_end) {
                ranges.add(new ConfigRange(r.m_end+1, m_end));
            }
            
            return ranges.toArray(new ConfigRange[ranges.size()]);
            
        }
    }
    
}