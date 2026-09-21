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

import io.swagger.v3.oas.annotations.media.Schema;

/** Describes the JSON that the create-source endpoint assembles; it is not returned as a type. */
@Schema(name = "EventConfSourceCreated", description = "Identifies a newly created EventConf source.")
public class EventConfSourceCreatedResponse {

    @Schema(description = "Database identifier assigned to the source.", example = "42")
    public Long id;

    @Schema(description = "Source name as stored.", example = "Cisco.syslog.events")
    public String name;

    @Schema(description = "Search order assigned to the source. New sources take the highest value.", example = "25")
    public Integer fileOrder;
}
