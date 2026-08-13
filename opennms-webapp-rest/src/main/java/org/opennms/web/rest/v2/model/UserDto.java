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
 * A user as exposed by the v2 user management API. Field names mirror
 * users.xml where they overlap; the password hash is deliberately never part
 * of this representation, and contact types the API does not expose (XMPP,
 * microblog, phones, pager PINs) are preserved server-side on update.
 */
public class UserDto {

    private String userId;

    private String fullName;

    private String userComments;

    private String email;

    private String pagerEmail;

    private String tuiPin;

    private String timeZoneId;

    // null (not empty) defaults: a request body that omits these keys
    // deserializes to null, which update semantics treat as "preserve"
    private List<String> dutySchedules;

    private List<String> roles;

    private Boolean readOnly;

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getUserComments() {
        return userComments;
    }

    public void setUserComments(final String userComments) {
        this.userComments = userComments;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPagerEmail() {
        return pagerEmail;
    }

    public void setPagerEmail(final String pagerEmail) {
        this.pagerEmail = pagerEmail;
    }

    public String getTuiPin() {
        return tuiPin;
    }

    public void setTuiPin(final String tuiPin) {
        this.tuiPin = tuiPin;
    }

    public String getTimeZoneId() {
        return timeZoneId;
    }

    public void setTimeZoneId(final String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public List<String> getDutySchedules() {
        return dutySchedules;
    }

    public void setDutySchedules(final List<String> dutySchedules) {
        this.dutySchedules = dutySchedules;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final Boolean readOnly) {
        this.readOnly = readOnly;
    }
}
