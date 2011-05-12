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

import org.opennms.netmgt.config.ConfigRange;
import org.opennms.netmgt.config.snmp.Range;

class ConfigRange implements Comparable<ConfigRange> {
    
    private ConfigAddress m_beginAddr;
    private ConfigAddress m_endAddr;
    
    public ConfigRange(ConfigAddress beginAddr, ConfigAddress endAddr) {
        m_beginAddr = beginAddr;
        m_endAddr   = endAddr;
        if (m_beginAddr.isGreaterThan(m_endAddr)) {
            throw new IllegalArgumentException("Invalid range ["+m_beginAddr+", "+m_endAddr+"]");
        }
    }

    public ConfigRange(String beginAddr, String endAddr) {
        this(new ConfigAddress(beginAddr), new ConfigAddress(endAddr));
    }
    
    public ConfigRange(Range r) {
        this(r.getBegin(), r.getEnd());
    }

    public ConfigRange(String specific) {
        this(specific, specific);
    }
    
    public String getBegin() {
        return m_beginAddr.toString();
    }

    public String getEnd() {
        return m_endAddr.toString();
    }
    
    public ConfigAddress getBeginAddr() {
        return m_beginAddr;
    }

    public ConfigAddress getEndAddr() {
        return m_endAddr;
    }
    
    public String getSpecificString() {
        if (!isSpecific()) {
            throw new IllegalStateException("Cannot get the specific string unless the range as only a single addrs: " + this);
        }
        return getBegin();
    }
    
    public boolean isSpecific() {
        return m_beginAddr.equals(m_endAddr);
    }
    
    public boolean contains(ConfigAddress addr) {
        return m_beginAddr.isLessThanOrEqualTo(addr) && addr.isLessThanOrEqualTo(m_endAddr);
    }

    public boolean preceeds(ConfigRange r) {
        return m_endAddr.isLessThan(r.getBeginAddr());
    }
    
    public boolean follows(ConfigRange r) {
        return r.preceeds(this);
    }
    
    public boolean overlaps(ConfigRange r) {
        return m_beginAddr.isLessThanOrEqualTo(r.getEndAddr()) && r.getBeginAddr().isLessThanOrEqualTo(m_endAddr);
    }
    
    public boolean contains(ConfigRange r) {
        return m_beginAddr.isLessThanOrEqualTo(r.m_beginAddr) && r.m_endAddr.isLessThanOrEqualTo(m_endAddr);
    }
    
    public boolean combinable(ConfigRange r) {
        return overlaps(r) || adjacent(r);
    }
    
    public ConfigRange combine(ConfigRange r) {
        if (!combinable(r)) {
            throw new IllegalArgumentException(String.format("Range %s is not combinable with range %s", this, r));
        }
        
        return new ConfigRange(ConfigAddress.min(m_beginAddr, r.m_beginAddr), ConfigAddress.max(m_endAddr, r.m_endAddr));
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
            return m_beginAddr.equals(r.m_beginAddr) && m_endAddr.equals(r.m_endAddr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return m_beginAddr.hashCode()*31+m_endAddr.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        b.append(getBegin());
        b.append(",");
        b.append(getEnd());
        b.append("]");
        return b.toString();
    }

    public boolean contains(String address) {
        return contains(new ConfigAddress(address));
    }

    public boolean adjacent(ConfigRange r) {
        return r.m_endAddr.immediatelyPreceeds(m_beginAddr) || r.m_beginAddr.immediatelyFollows(m_endAddr);
    }

    public ConfigRange[] remove(ConfigRange r) {
        if (r.contains(this)) {
            return new ConfigRange[0];
        } else if (!overlaps(r)) {
            return new ConfigRange[] { this };
        } else {
            List<ConfigRange> ranges = new ArrayList<ConfigRange>(2);
            if (m_beginAddr.isLessThan(r.m_beginAddr)) {
                ranges.add(new ConfigRange(m_beginAddr, r.m_beginAddr.decr()));
            }
            if (r.m_endAddr.isLessThan(m_endAddr)) {
                ranges.add(new ConfigRange(r.m_endAddr.incr(), m_endAddr));
            }
            return ranges.toArray(new ConfigRange[ranges.size()]);
            
        }
    }
    
}