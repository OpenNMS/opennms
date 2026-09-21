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

/** Describes the form body the group update handler reads out of a MultivaluedMap; it is not bound as a type. */
@Schema(name = "V1GroupUpdateForm",
        description = """
                Form-encoded body for updating a group. Field names are the `OnmsGroup` bean property names. Keys
                that are not writable properties are skipped, and a request in which nothing was written comes back
                as 304.""")
public class GroupUpdateForm {

    @Schema(description = "Group comments.", example = "Second-line network operations")
    public String comments;

    @Schema(description = "Member user name. Stored as a single list element, which replaces the whole member "
            + "list, so a comma-separated string becomes one element containing the commas.",
            example = "admin")
    public String users;
}
