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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.opennms.core.utils.OwnedInterval;
import org.opennms.core.utils.OwnedIntervalSequence;
import org.opennms.core.utils.Owner;
import org.opennms.netmgt.config.groups.Role;

/**
 * <p>Day class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Day {
    
    private final Calendar m_calendar;
    private final Role m_role;
    private final GroupManager m_groupManager;

    /**
     * <p>Constructor for Day.</p>
     *
     * @param date a java$util$Date object.
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public Day(Date date, Role role, GroupManager groupManager) {
        m_role = role;
        m_groupManager = groupManager;
        m_calendar = Calendar.getInstance();
        m_calendar.setTime(date);
    }
    
    /**
     * <p>getDate</p>
     *
     * @return a java$util$Date object.
     */
    public Date getDate() {
        return m_calendar.getTime();
    }
    
    /**
     * <p>getMonth</p>
     *
     * @return a int.
     */
    public int getMonth() { return m_calendar.get(Calendar.MONTH); }

    /**
     * <p>getDayOfMonth</p>
     *
     * @return a int.
     */
    public int getDayOfMonth() { return m_calendar.get(Calendar.DAY_OF_MONTH); }
    
    /**
     * <p>getDayOfYear</p>
     *
     * @return a int.
     */
    public int getDayOfYear() { return m_calendar.get(Calendar.DAY_OF_YEAR); }
    
    /**
     * <p>getDayOfWeek</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDayOfWeek() {
        return new SimpleDateFormat("EEEE").format(m_calendar.getTime());
    }
    
    /**
     * <p>getTime</p>
     *
     * @param hours a int.
     * @param minutes a int.
     * @return a java$util$Date object.
     */
    public Date getTime(int hours, int minutes) {
        Calendar time = Calendar.getInstance();
        time.set(m_calendar.get(Calendar.YEAR), m_calendar.get(Calendar.MONTH), m_calendar.get(Calendar.DAY_OF_MONTH), hours, minutes);
        return time.getTime();
    }
    
    /**
     * <p>getEntries</p>
     *
     * @return an array of {@link org.opennms.web.admin.roles.CalendarEntry} objects.
     */
    public CalendarEntry[] getEntries() {
        try {
            List<CalendarEntry> entries = new ArrayList<>();
            
            OwnedIntervalSequence schedEntries = m_groupManager.getRoleScheduleEntries(m_role.getName(), getTime(0,0), getTime(24,0));
            
            for (Iterator<OwnedInterval> it = schedEntries.iterator(); it.hasNext();) {
                OwnedInterval schedEntry = it.next();
                CalendarEntry entry = new CalendarEntry(schedEntry.getStart(), schedEntry.getEnd(), ownerString(schedEntry.getOwners()), schedEntry.getOwners());
                entries.add(entry);
            }
            
            return entries.toArray(new CalendarEntry[entries.size()]);
        } catch (Throwable e) {
            throw new WebRolesException("Unable to get schedule entries: " + e.getMessage(), e);
        }
    }

    private String ownerString(List<Owner> owners) {
        boolean first = true;
        final StringBuilder buf = new StringBuilder();
        for (Owner owner : owners) {
            if (first) {
                first = false;
            } else { 
                buf.append(", ");
            }
            buf.append(owner.getUser());
        }
        return buf.toString();
    }

}
