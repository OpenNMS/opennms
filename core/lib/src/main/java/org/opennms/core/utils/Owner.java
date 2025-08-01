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

/**
 * <p>Owner class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Owner implements Comparable<Owner> {
    
    private final String m_roleid;
    private final String m_user;
    private final int m_schedIndex;
    private final int m_timeIndex;

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
    @Override
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
    @Override
    public int hashCode() {
        return m_user.hashCode();
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.core.utils.Owner} object.
     * @return a int.
     */
    @Override
    public int compareTo(Owner o) {
        return m_user.compareTo(o.m_user);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return m_user;
    }
}
