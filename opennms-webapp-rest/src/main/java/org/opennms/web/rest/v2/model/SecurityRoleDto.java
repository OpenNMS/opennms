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

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * A security role as exposed by the v2 user management API: a Spring Security authority ({@code ROLE_X}).
 * Only the description of a custom role can be written; {@code builtin}, {@code userCount} and
 * {@code version} are read-only and ignored in a request body.
 */
public class SecurityRoleDto {

    // the full authority name, ROLE_ included
    private String name;

    private String description;

    private boolean builtin;

    private Integer userCount;

    // the row version, also sent as the ETag; ignored in a request body (the If-Match header is the contract)
    private Integer version;

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

    public boolean isBuiltin() {
        return builtin;
    }

    public void setBuiltin(final boolean builtin) {
        this.builtin = builtin;
    }

    // only in the list: a single role carries an ETag, which follows the role's own row, not the users' grants
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public Integer getUserCount() {
        return userCount;
    }

    public void setUserCount(final Integer userCount) {
        this.userCount = userCount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(final Integer version) {
        this.version = version;
    }
}
