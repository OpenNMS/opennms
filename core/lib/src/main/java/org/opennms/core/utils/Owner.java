/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

/**
 * <p>Owner class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Owner implements Comparable<Owner> {
    
    private String m_roleid;
    private String m_user;
    private int m_schedIndex;
    private int m_timeIndex;

    /**
     * <p>Constructor for Owner.</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param schedIndex a int.
     */
    public Owner(String roleid, String user, int schedIndex) {
        this(roleid, user, schedIndex, -1);
    }        
    
    /**
     * <p>Constructor for Owner.</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param supervisor a {@link java.lang.String} object.
     */
    public Owner(String roleid, String supervisor) {
        this(roleid, supervisor, -1, -1);
    }

    /**
     * <p>Constructor for Owner.</p>
     *
     * @param base a {@link org.opennms.core.utils.Owner} object.
     * @param timeIndex a int.
     */
    public Owner(Owner base, int timeIndex) {
        this(base.getRoleid(), base.getUser(), base.getSchedIndex(), timeIndex);
    }
    
    /**
     * <p>Constructor for Owner.</p>
     *
     * @param roleid a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param schedIndex a int.
     * @param timeIndex a int.
     */
    public Owner(String roleid, String user, int schedIndex, int timeIndex) {
        m_roleid = roleid;
        m_user = user;
        m_schedIndex = schedIndex;
        m_timeIndex = timeIndex;
    }
    
    /**
     * <p>isSupervisor</p>
     *
     * @return a boolean.
     */
    public boolean isSupervisor() {
        return m_schedIndex == -1 && m_timeIndex == -1;
    }

    /**
     * <p>getRoleid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getRoleid() {
        return m_roleid;
    }

    /**
     * <p>getSchedIndex</p>
     *
     * @return a int.
     */
    public int getSchedIndex() {
        return m_schedIndex;
    }

    /**
     * <p>getTimeIndex</p>
     *
     * @return a int.
     */
    public int getTimeIndex() {
        return m_timeIndex;
    }

    /**
     * <p>getUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUser() {
        return m_user;
    }
    
    public Owner addTimeIndex(int timeIndex) {
        return new Owner(this, timeIndex);
    }
    
    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj instanceof Owner) {
            Owner o = (Owner) obj;
            return m_user.equals(o.m_user);
        }
        return false;
    }
    
    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return m_user.hashCode();
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.core.utils.Owner} object.
     * @return a int.
     */
    public int compareTo(Owner o) {
        return m_user.compareTo(o.m_user);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return m_user;
    }
}
