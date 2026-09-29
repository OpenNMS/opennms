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

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Documentation-only description of the body returned by {@code GET /whoami}. The handler serialises
 * an ad-hoc JSON object rather than returning this type.
 */
@Schema(name = "WhoamiResponse", description = "Identity and granted roles of the authenticated principal.")
public class WhoamiResponse {

    @Schema(description = "Login name of the authenticated principal.", example = "admin", required = true)
    private String id;

    @Schema(description = "Spring Security roles the principal holds, out of the roles OpenNMS knows about.",
            example = "[\"ROLE_USER\",\"ROLE_ADMIN\"]", required = true)
    private List<String> roles;

    @Schema(description = "True when the principal is defined in users.xml. False for principals supplied by an external "
            + "authentication source, in which case `email` and `fullName` are absent.",
            example = "true", required = true)
    private boolean internal;

    @Schema(description = "Email address from users.xml. Omitted when empty or when the user is not internal.",
            example = "admin@example.org")
    private String email;

    @Schema(description = "Full name from users.xml. Omitted when empty or when the user is not internal.",
            example = "Administrator")
    private String fullName;

    public String getId() {
        return id;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean isInternal() {
        return internal;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }
}
