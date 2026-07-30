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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An on-call role as exposed by the v2 API. Field names mirror the role
 * element in groups.xml. The schedule list is the raw file representation
 * (schedule name = the user on call; begins/ends in the file's own formats);
 * a schedule list omitted from a request body means "preserve".
 * currently-on-call is computed and ignored on writes.
 */
@XmlRootElement(name = "on-call-role")
@XmlAccessorType(XmlAccessType.FIELD)
public class OnCallRoleDto {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "membership-group")
    private String membershipGroup;

    @XmlElement(name = "supervisor")
    private String supervisor;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "schedule")
    private List<OnCallScheduleDto> schedules;

    @XmlElement(name = "currently-on-call")
    private List<String> currentlyOnCall;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMembershipGroup() {
        return membershipGroup;
    }

    public void setMembershipGroup(final String membershipGroup) {
        this.membershipGroup = membershipGroup;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(final String supervisor) {
        this.supervisor = supervisor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<OnCallScheduleDto> getSchedules() {
        return schedules;
    }

    public void setSchedules(final List<OnCallScheduleDto> schedules) {
        this.schedules = schedules;
    }

    public List<String> getCurrentlyOnCall() {
        return currentlyOnCall;
    }

    public void setCurrentlyOnCall(final List<String> currentlyOnCall) {
        this.currentlyOnCall = currentlyOnCall;
    }

    @XmlRootElement(name = "on-call-schedule")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OnCallScheduleDto {

        @XmlElement(name = "user")
        private String user;

        @XmlElement(name = "type")
        private String type;

        @XmlElement(name = "time")
        private List<OnCallTimeDto> times;

        public String getUser() {
            return user;
        }

        public void setUser(final String user) {
            this.user = user;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        public List<OnCallTimeDto> getTimes() {
            return times;
        }

        public void setTimes(final List<OnCallTimeDto> times) {
            this.times = times;
        }
    }

    @XmlRootElement(name = "on-call-time")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class OnCallTimeDto {

        @XmlElement(name = "day")
        private String day;

        @XmlElement(name = "begins")
        private String begins;

        @XmlElement(name = "ends")
        private String ends;

        public String getDay() {
            return day;
        }

        public void setDay(final String day) {
            this.day = day;
        }

        public String getBegins() {
            return begins;
        }

        public void setBegins(final String begins) {
            this.begins = begins;
        }

        public String getEnds() {
            return ends;
        }

        public void setEnds(final String ends) {
            this.ends = ends;
        }
    }
}
