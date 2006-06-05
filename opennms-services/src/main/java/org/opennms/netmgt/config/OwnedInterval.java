//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class OwnedInterval extends TimeInterval {
    List m_owners;
    
    public OwnedInterval(OwnedInterval owned) {
        this(owned.getOwners(), owned.getStart(), owned.getEnd());
    }

    public OwnedInterval(TimeInterval interval) {
        this(interval.getStart(), interval.getEnd());
    }
    
    public OwnedInterval(Object owner, TimeInterval interval) {
        this(owner, interval.getStart(), interval.getEnd());
    }
    
    public OwnedInterval(List owners, TimeInterval interval) {
        this(owners, interval.getStart(), interval.getEnd());
    }
    
    public OwnedInterval(Date start, Date end) {
        this(Collections.EMPTY_LIST, start, end);
    }
    
    public OwnedInterval(Object owner, Date start, Date end) {
        this(Collections.singletonList(owner), start, end);
    }
    
    public OwnedInterval(List owners, Date start, Date end) {
        super(start, end);
        m_owners = new ArrayList(owners);
        Collections.sort(m_owners);
    }
    
    public List getOwners() { return m_owners; }
    
    public void addOwner(Object owner) { m_owners.add(owner); Collections.sort(m_owners); }
    
    public void removeOwner(Object owner) { m_owners.remove(owner); }
    
    public void addOwners(List owners) { m_owners.addAll(owners); Collections.sort(m_owners); }
    
    public void removeOwners(List owners) { m_owners.removeAll(owners); }
    
    public boolean isOwner(Object owner) { return m_owners.contains(owner); }
    
    public boolean isOwned() { return !m_owners.isEmpty(); }
    
    public String toString() {
        String ownerString = "";
        if (m_owners.isEmpty()) {
            ownerString = "UNOWNED";
        } else {
            for(int i = 0; i < m_owners.size(); i++) {
                if (i != 0) ownerString += ",";
                ownerString += m_owners.get(i);
            }
        }
        return ownerString+super.toString();
    }
    
    public int hashCode() { return 123; }
    
    public boolean equals(Object o) {
        if (o instanceof OwnedInterval) {
            OwnedInterval owned = (OwnedInterval) o;
            return super.equals(owned) && m_owners.equals(owned.m_owners);
        }
        return false;
    }
    
}