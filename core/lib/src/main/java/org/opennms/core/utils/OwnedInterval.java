/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

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
     * @param owned a {@link org.opennms.core.utils.OwnedInterval} object.
     */
    public OwnedInterval(OwnedInterval owned) {
        this(owned.getOwners(), owned.getStart(), owned.getEnd());
    }

    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     */
    public OwnedInterval(TimeInterval interval) {
        this(interval.getStart(), interval.getEnd());
    }
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param owner a {@link org.opennms.core.utils.Owner} object.
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
     */
    public OwnedInterval(Owner owner, TimeInterval interval) {
        this(owner, interval.getStart(), interval.getEnd());
    }
    
    /**
     * <p>Constructor for OwnedInterval.</p>
     *
     * @param owners a {@link java.util.List} object.
     * @param interval a {@link org.opennms.core.utils.TimeInterval} object.
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
     * @param owner a {@link org.opennms.core.utils.Owner} object.
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
     * @param owner a {@link org.opennms.core.utils.Owner} object.
     */
    public void addOwner(Owner owner) { m_owners.add(owner); Collections.sort(m_owners); }
    
    /**
     * <p>removeOwner</p>
     *
     * @param owner a {@link org.opennms.core.utils.Owner} object.
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
     * @param owner a {@link org.opennms.core.utils.Owner} object.
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
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
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
