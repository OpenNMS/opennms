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
import org.opennms.netmgt.model.EventConfEventDto;

import java.util.List;

/** Describes the JSON that the event-filter endpoint assembles; it is not returned as a type. */
@Schema(name = "EventConfEventPage", description = "One page of EventConf events plus the total match count.")
public class EventConfEventPageResponse {

    @Schema(description = "Total number of events matching the filter, ignoring pagination.", example = "132")
    public Integer totalRecords;

    @Schema(description = "The requested page of events. Named after sources for historical reasons."
            + " Each entry's createdTime and lastModified come back as epoch milliseconds.")
    public List<EventConfEventDto> eventConfSourceList;
}
