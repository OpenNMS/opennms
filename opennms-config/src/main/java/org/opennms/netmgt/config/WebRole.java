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
package org.opennms.netmgt.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;

/**
 * <p>Abstract WebRole class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class WebRole {
    
    private String m_name;
    private String m_description;
    private WebUser m_defaultUser;
    private WebGroup m_membershipGroup;
    private final List<WebSchedEntry> m_newEntries = new ArrayList<>();
    
    
    /**
     * <p>Constructor for WebRole.</p>
     */
    public WebRole() {
    }

    /**
     * <p>Constructor for WebRole.</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public WebRole(String name) {
        m_name = name;
    }

    /**
     * <p>getDefaultUser</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebUser} object.
     */
    public WebUser getDefaultUser() {
        return m_defaultUser;
    }
    /**
     * <p>setDefaultUser</p>
     *
     * @param defaultUser a {@link org.opennms.netmgt.config.WebUser} object.
     */
    public void setDefaultUser(WebUser defaultUser) {
        m_defaultUser = defaultUser;
    }
    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription() {
        return m_description;
    }
    /**
     * <p>setDescription</p>
     *
     * @param description a {@link java.lang.String} object.
     */
    public void setDescription(String description) {
        m_description = description;
    }
    /**
     * <p>getMembershipGroup</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebGroup} object.
     */
    public WebGroup getMembershipGroup() {
        return m_membershipGroup;
    }
    /**
     * <p>setMembershipGroup</p>
     *
     * @param memberShipGroup a {@link org.opennms.netmgt.config.WebGroup} object.
     */
    public void setMembershipGroup(WebGroup memberShipGroup) {
        m_membershipGroup = memberShipGroup;
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }
    
    /**
     * <p>getSchedule</p>
     *
     * @param schedIndex a int.
     * @return a {@link org.opennms.netmgt.config.groups.Schedule} object.
     */
    public abstract Schedule getSchedule(int schedIndex);
    
    /**
     * <p>getTime</p>
     *
     * @param schedIndex a int.
     * @param timeIndex a int.
     * @return a {@link org.opennms.netmgt.config.poller.outages.common.Time} object.
     */
    public abstract Time getTime(int schedIndex, int timeIndex);

    /**
     * <p>getCurrentUsers</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public abstract Collection<WebUser> getCurrentUsers();
    
    /**
     * <p>getWeeklyCalendar</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebCalendar} object.
     */
    public WebCalendar getWeeklyCalendar() {
        return null;
    }
    
    /**
     * <p>getCalendar</p>
     *
     * @return a {@link org.opennms.netmgt.config.WebCalendar} object.
     */
    public WebCalendar getCalendar() {
        return getCalendar(new Date());
    }

    /**
     * <p>getCalendar</p>
     *
     * @param month a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.config.WebCalendar} object.
     */
    public abstract WebCalendar getCalendar(Date month);

    /**
     * <p>addEntry</p>
     *
     * @param entry a {@link org.opennms.netmgt.config.WebSchedEntry} object.
     */
    public void addEntry(WebSchedEntry entry) {
        
        m_newEntries.add(entry);
    }
    
    /**
     * <p>getNewEntries</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    public Collection<WebSchedEntry> getNewEntries() {
        return m_newEntries;
    }
    
    

}
