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

package org.opennms.web.rest.v1.model;

import io.swagger.v3.oas.annotations.media.Schema;

/** Describes the form body the user update handler reads out of a MultivaluedMap; it is not bound as a type. */
@Schema(name = "V1UserUpdateForm",
        description = """
                Form-encoded body for updating a user. Field names are the `OnmsUser` bean property names, not the
                XML element names the read endpoints emit, so `fullName` and `comments` are the keys, not
                `full-name` and `user-comments`. Keys that are not writable properties are skipped, and a request in
                which nothing was written comes back as 304.""")
public class UserUpdateForm {

    @Schema(description = "Display name.", example = "Jane Roe")
    public String fullName;

    @Schema(description = "Free-text comments stored as `user-comments`.", example = "On call for the NOC")
    public String comments;

    @Schema(description = "Email contact address.", example = "jane.roe@example.org")
    public String email;

    @Schema(description = "New password. Stored verbatim unless `hashPassword` is `true`.", example = "s3cret")
    public String password;

    @Schema(description = "`true` hashes and salts `password` before storing it. Only consulted when `password` "
            + "is also present in the same request.",
            example = "true")
    public Boolean hashPassword;

    @Schema(description = "Duty schedule entry. The value is stored as a single list element, so a "
            + "comma-separated string becomes one element containing the commas rather than several entries.",
            example = "MoTuWeThFr800-1700")
    public String dutySchedule;

    @Schema(description = "Security role. Stored as a single list element, which replaces the whole role list.",
            example = "ROLE_USER")
    public String roles;
}
