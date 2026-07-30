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
 * A group as exposed by the v2 user management API. Field names mirror
 * groups.xml where they overlap. The user list is ordered — the order drives
 * notification escalation. Fields the API does not expose (default-map) are
 * preserved server-side on update; list fields default to null (not empty) so
 * a request body that omits them means "preserve".
 */
@XmlRootElement(name = "group")
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupDto {

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "comments")
    private String comments;

    @XmlElement(name = "user")
    private List<String> users;

    @XmlElement(name = "duty-schedule")
    private List<String> dutySchedules;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(final List<String> users) {
        this.users = users;
    }

    public List<String> getDutySchedules() {
        return dutySchedules;
    }

    public void setDutySchedules(final List<String> dutySchedules) {
        this.dutySchedules = dutySchedules;
    }
}
