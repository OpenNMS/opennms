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
import org.opennms.netmgt.model.discovery.IPAddress;

class ConfigRange implements Comparable<ConfigRange> {
    
    private IPAddress m_begin;
    private IPAddress m_end;
    
    public ConfigRange(IPAddress begin, IPAddress end) {
        m_begin = begin;
        m_end = end;
        if (getBeginAddress().isGreaterThan(getEndAddress())) {
            throw new IllegalArgumentException("Invalid range ["+getBeginAddress()+", "+getEndAddress()+"]");
        }
    }
    
    public ConfigRange(String beginAddr, String endAddr) {
        this(new IPAddress(beginAddr), new IPAddress(endAddr));
    }
    
    public ConfigRange(Range r) {
        this(r.getBegin(), r.getEnd());
    }

    public ConfigRange(String specific) {
        this(specific, specific);
    }
    
    private IPAddress getBeginAddress() {
        return m_begin;
    }
    
    private IPAddress getEndAddress() {
        return m_end;
    }
    
    public String getBegin() {
        return getBeginAddress().toString();
    }

    public String getEnd() {
        return getEndAddress().toString();
    }
    
    public String getSpecificString() {
        if (!isSpecific()) {
            throw new IllegalStateException("Cannot get the specific string unless the range as only a single addrs: " + this);
        }
        return getBegin();
    }
    
    public boolean isSpecific() {
        return getBeginAddress().equals(getEndAddress());
    }
    
    private boolean contains(IPAddress address) {
        return getBeginAddress().isLessThanOrEqualTo(address) && address.isLessThanOrEqualTo(getEndAddress());
    }

    public boolean preceeds(ConfigRange r) {
        return getEndAddress().isLessThan(r.getBeginAddress());
    }

    public boolean follows(ConfigRange r) {
        return r.preceeds(this);
    }
    
    public boolean overlaps(ConfigRange r) {
        return getBeginAddress().isLessThanOrEqualTo(r.getEndAddress()) && r.getBeginAddress().isLessThanOrEqualTo(getEndAddress());
    }

    public boolean contains(ConfigRange r) {
        return getBeginAddress().isLessThanOrEqualTo(r.getBeginAddress()) && r.getEndAddress().isLessThanOrEqualTo(getEndAddress());
    }
    
    public boolean combinable(ConfigRange r) {
        return overlaps(r) || adjacent(r);
    }
    
    public ConfigRange combine(ConfigRange r) {
        if (!combinable(r)) {
            throw new IllegalArgumentException(String.format("Range %s is not combinable with range %s", this, r));
        }
        return new ConfigRange(IPAddress.min(r.getBeginAddress(), getBeginAddress()), IPAddress.max(getEndAddress(), r.getEndAddress()));
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
            return getBeginAddress().equals(r.getBeginAddress()) && getEndAddress().equals(r.getEndAddress());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getBeginAddress().hashCode()*31+getEndAddress().hashCode();
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
        return contains(new IPAddress(address));
    }

    public boolean adjacent(ConfigRange r) {
        return r.getEndAddress().isPredecessorOf(getBeginAddress()) || r.getBeginAddress().isSuccessorOf(getEndAddress());
    }

    public ConfigRange[] remove(ConfigRange r) {
        if (r.contains(this)) {
            return new ConfigRange[0];
        } else if (!overlaps(r)) {
            return new ConfigRange[] { this };
        } else {
            List<ConfigRange> ranges = new ArrayList<ConfigRange>(2);
            if (getBeginAddress().isLessThan(r.getBeginAddress())) {
                ranges.add(new ConfigRange(getBeginAddress(), r.getBeginAddress().decr()));
            }
            if (r.getEndAddress().isLessThan(getEndAddress())) {
                ranges.add(new ConfigRange(r.getEndAddress().incr(), getEndAddress()));
            }
            return ranges.toArray(new ConfigRange[ranges.size()]);
            
        }
    }
    
}