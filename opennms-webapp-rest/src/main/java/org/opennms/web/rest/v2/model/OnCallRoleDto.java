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

/**
 * An on-call role as exposed by the v2 user management API: one document with the membership group, the
 * supervisor and all schedules. Fields left out of a PUT body arrive as null and are preserved; the
 * schedules, when present, replace the stored ones. The version is also sent as the ETag and ignored in a
 * request body (the If-Match header is the contract).
 */
public class OnCallRoleDto {

    private String name;

    private String description;

    private String membershipGroup;

    private String supervisor;

    private Integer version;

    private List<OnCallScheduleDto> schedules;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }

    public List<OnCallScheduleDto> getSchedules() {
        return schedules;
    }

    public void setSchedules(final List<OnCallScheduleDto> schedules) {
        this.schedules = schedules;
    }
}
