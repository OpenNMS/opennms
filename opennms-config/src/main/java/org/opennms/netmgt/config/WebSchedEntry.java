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
import java.util.Date;
import java.util.Locale;

import org.opennms.netmgt.config.groups.Role;
import org.opennms.netmgt.config.groups.Schedule;
import org.opennms.netmgt.config.groups.Time;

/**
 * <p>WebSchedEntry class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class WebSchedEntry {
    
    private final int m_schedIndex;
    private final int m_timeIndex;
    private final String m_user;
    private final String m_type;
    private final String m_begins;
    private final String m_ends;

    /**
     * <p>Constructor for WebSchedEntry.</p>
     *
     * @param schedIndex a int.
     * @param timeIndex a int.
     * @param user a {@link java.lang.String} object.
     * @param startDate a java$util$Date object.
     * @param endDate a java$util$Date object.
     */
    public WebSchedEntry(int schedIndex, int timeIndex, String user, Date startDate, Date endDate) {
        this(schedIndex, timeIndex, user, new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ROOT).format(startDate), new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ROOT).format(endDate));
    }
    
    /**
     * <p>Constructor for WebSchedEntry.</p>
     *
     * @param schedIndex a int.
     * @param timeIndex a int.
     * @param user a {@link java.lang.String} object.
     * @param begins a {@link java.lang.String} object.
     * @param ends a {@link java.lang.String} object.
     */
    private WebSchedEntry(int schedIndex, int timeIndex, String user, String begins, String ends) {
        // this is a specific entry
        m_schedIndex = schedIndex;
        m_timeIndex = timeIndex;
        m_type = "specific";
        m_user = user;
        m_begins = begins;
        m_ends = ends;
    }
    
    /**
     * <p>isNew</p>
     *
     * @return a boolean.
     */
    public boolean isNew() {
        return m_schedIndex == -1 && m_timeIndex == -1;
    }
    
    /**
     * <p>update</p>
     *
     * @param role a {@link org.opennms.netmgt.config.groups.Role} object.
     */
    public void update(Role role) {
        if (isNew()) {
            addToRole(role);
        } else {
            modifyRole(role);
        }
    }

    private void modifyRole(Role role) {
        Schedule sched = role.getSchedules().get(m_schedIndex);
        Time time = sched.getTimes().get(m_timeIndex);
        sched.setName(m_user);
        sched.setType(m_type);
        time.setBegins(m_begins);
        time.setEnds(m_ends);
    }

    private void addToRole(Role role) {
        Schedule sched = new Schedule();
        sched.setName(m_user);
        sched.setType(m_type);
        Time time = new Time();
        time.setBegins(m_begins);
        time.setEnds(m_ends);
        sched.addTime(time);
        role.addSchedule(sched);
    }

}
