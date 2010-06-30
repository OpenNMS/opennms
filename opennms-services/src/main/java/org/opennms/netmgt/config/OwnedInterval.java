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
// Modifications:
//
// 2007 Jul 14: Use Java 5 generics. - dj@opennms.org
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

/**
 * <p>OwnedInterval class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class OwnedInterval extends TimeInterval {
    private List<Owner> m_owners;
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param owned a {@link org.opennms.netmgt.config.OwnedInterval} object.
     */
    public OwnedInterval(OwnedInterval owned) {
        this(owned.getOwners(), owned.getStart(), owned.getEnd());
    }

    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param interval a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public OwnedInterval(TimeInterval interval) {
        this(interval.getStart(), interval.getEnd());
    }
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param owner a {@link org.opennms.netmgt.config.Owner} object.
     * @param interval a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public OwnedInterval(Owner owner, TimeInterval interval) {
        this(owner, interval.getStart(), interval.getEnd());
    }
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param owners a {@link java.util.List} object.
     * @param interval a {@link org.opennms.netmgt.config.TimeInterval} object.
     */
    public OwnedInterval(List<Owner> owners, TimeInterval interval) {
        this(owners, interval.getStart(), interval.getEnd());
    }
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     */
    public OwnedInterval(Date start, Date end) {
        this(new ArrayList<Owner>(0), start, end);
    }
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param owner a {@link org.opennms.netmgt.config.Owner} object.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     */
    public OwnedInterval(Owner owner, Date start, Date end) {
        this(Collections.singletonList(owner), start, end);
    }
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param owners a {@link java.util.List} object.
     * @param start a {@link java.util.Date} object.
     * @param end a {@link java.util.Date} object.
     */
    public OwnedInterval(List<Owner> owners, Date start, Date end) {
        super(start, end);
        m_owners = new ArrayList<Owner>(owners);
        Collections.sort(m_owners);
    }
    
    /**
     * <p>getOwners</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Owner> getOwners() { return m_owners; }
    
    /**
     * <p>addOwner</p>
     *
     * @param owner a {@link org.opennms.netmgt.config.Owner} object.
     */
    public void addOwner(Owner owner) { m_owners.add(owner); Collections.sort(m_owners); }
    
    /**
     * <p>removeOwner</p>
     *
     * @param owner a {@link org.opennms.netmgt.config.Owner} object.
     */
    public void removeOwner(Owner owner) { m_owners.remove(owner); }
    
    /**
     * <p>addOwners</p>
     *
     * @param owners a {@link java.util.List} object.
     */
    public void addOwners(List<Owner> owners) { m_owners.addAll(owners); Collections.sort(m_owners); }
    
    /**
     * <p>removeOwners</p>
     *
     * @param owners a {@link java.util.List} object.
     */
    public void removeOwners(List<Owner> owners) { m_owners.removeAll(owners); }
    
    /**
     * <p>isOwner</p>
     *
     * @param owner a {@link org.opennms.netmgt.config.Owner} object.
     * @return a boolean.
     */
    public boolean isOwner(Owner owner) { return m_owners.contains(owner); }
    
    /**
     * <p>isOwned</p>
     *
     * @return a boolean.
     */
    public boolean isOwned() { return !m_owners.isEmpty(); }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        if (m_owners.isEmpty()) {
            sb.append("UNOWNED");
        } else {
            for (int i = 0; i < m_owners.size(); i++) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(m_owners.get(i));
            }
        }
        return sb.toString()+super.toString();
    }
    
    /** {@inheritDoc} */
    @Override
    public int hashCode() { return 123; }
    
    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object o) {
        if (o == null) return false;
        if (o instanceof OwnedInterval) {
            final OwnedInterval owned = (OwnedInterval) o;
            return super.equals(owned) && m_owners.equals(owned.m_owners);
        }
        return false;
    }
    
}
