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

import java.util.Calendar;
import java.util.Date;

import org.opennms.netmgt.config.groups.Role;

/**
 * <p>Week class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Week {
    
    private final Calendar m_calendar;
    private final Role m_role;
    private final GroupManager m_groupManager;

    /**
     * <p>Constructor for Week.</p>
     *
     * @param date a {@link java.util.Date} object.
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     * @param groupManager a {@link org.opennms.netmgt.config.GroupManager} object.
     */
    public Week(Date date, Role role, GroupManager groupManager) {
        m_role = role;
        m_groupManager = groupManager;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        m_calendar = calendar;
    }

    /**
     * <p>getWeekOfYear</p>
     *
     * @return a int.
     */
    public int getWeekOfYear() { return m_calendar.get(Calendar.WEEK_OF_YEAR); }
    
    /**
     * <p>getDays</p>
     *
     * @return an array of {@link org.opennms.netmgt.config.Day} objects.
     */
    public Day[] getDays() {
        Calendar day = (Calendar)m_calendar.clone();
        Day[] days = new Day[7];
        for(int i = 0; i < 7; i++) {
            days[i] = new Day(day.getTime(), m_role, m_groupManager);
            day.add(Calendar.DAY_OF_YEAR, 1);
        }
        return days;
    }

}
