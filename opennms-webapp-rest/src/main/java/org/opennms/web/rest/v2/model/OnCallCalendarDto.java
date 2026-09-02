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
package org.opennms.web.rest.v2.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * One month of on-call coverage for a role, computed server-side from the
 * same schedule resolution notifd uses. Intervals where nobody is scheduled
 * fall to the supervisor and carry the supervisor flag.
 */
@XmlRootElement(name = "on-call-calendar")
@XmlAccessorType(XmlAccessType.FIELD)
public class OnCallCalendarDto {

    @XmlElement(name = "role")
    private String role;

    @XmlElement(name = "year")
    private int year;

    @XmlElement(name = "month")
    private int month;

    /** IANA id of the zone the server evaluates schedules in; clients should render and enter times in it. */
    @XmlElement(name = "time-zone")
    private String timeZone;

    @XmlElement(name = "day")
    private List<CalendarDayDto> days = new ArrayList<>();

    /** Set when some stored entries could not be resolved; affected days render without them. */
    @XmlElement(name = "schedule-error")
    private String scheduleError;

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public int getYear() {
        return year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(final int month) {
        this.month = month;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final String timeZone) {
        this.timeZone = timeZone;
    }

    public List<CalendarDayDto> getDays() {
        return days;
    }

    public void setDays(final List<CalendarDayDto> days) {
        this.days = days;
    }

    public String getScheduleError() {
        return scheduleError;
    }

    public void setScheduleError(final String scheduleError) {
        this.scheduleError = scheduleError;
    }

    @XmlRootElement(name = "on-call-calendar-day")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CalendarDayDto {

        @XmlElement(name = "date")
        private String date;

        @XmlElement(name = "entry")
        private List<CalendarEntryDto> entries = new ArrayList<>();

        public String getDate() {
            return date;
        }

        public void setDate(final String date) {
            this.date = date;
        }

        public List<CalendarEntryDto> getEntries() {
            return entries;
        }

        public void setEntries(final List<CalendarEntryDto> entries) {
            this.entries = entries;
        }
    }

    @XmlRootElement(name = "on-call-calendar-entry")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CalendarEntryDto {

        @XmlElement(name = "start")
        private long start;

        @XmlElement(name = "end")
        private long end;

        @XmlElement(name = "user")
        private List<String> users = new ArrayList<>();

        @XmlElement(name = "supervisor")
        private boolean supervisor;

        public long getStart() {
            return start;
        }

        public void setStart(final long start) {
            this.start = start;
        }

        public long getEnd() {
            return end;
        }

        public void setEnd(final long end) {
            this.end = end;
        }

        public List<String> getUsers() {
            return users;
        }

        public void setUsers(final List<String> users) {
            this.users = users;
        }

        public boolean isSupervisor() {
            return supervisor;
        }

        public void setSupervisor(final boolean supervisor) {
            this.supervisor = supervisor;
        }
    }
}
