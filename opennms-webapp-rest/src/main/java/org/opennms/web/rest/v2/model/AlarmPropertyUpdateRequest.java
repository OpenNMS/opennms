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

/**
 * Describes the form parameters the alarm and situation property-update handlers read out of a
 * {@code MultivaluedMap}; it is not deserialised as a type.
 */
@Schema(name = "AlarmPropertyUpdateRequest",
        description = """
                Form parameters accepted by the alarm and situation property updates. Every field is
                optional and only the fields present are acted on.

                `ticketId` and `ticketState` are written straight onto the alarm row. `ack`,
                `escalate` and `clear` are handled as acknowledgements instead and are mutually
                exclusive: `ack` is examined first, then `escalate`, then `clear`, and the first one
                present wins. `escalate=false` and `clear=false` are accepted and do nothing.

                A `ticketState` that is not one of the enumerated names is ignored rather than
                rejected, so a misspelt value still yields 204.""")
public class AlarmPropertyUpdateRequest {

    @Schema(description = "`true` acknowledges the alarm, `false` un-acknowledges it.", example = "true")
    public Boolean ack;

    @Schema(description = """
            User credited with the acknowledgement. Defaults to the authenticated user. A caller
            without ROLE_ADMIN may only name itself.""",
            example = "admin")
    public String ackUser;

    @Schema(description = "`true` raises the severity by one step. Ignored unless `ack` is absent.",
            example = "true")
    public Boolean escalate;

    @Schema(description = "`true` sets the severity to Cleared. Ignored unless `ack` and `escalate` are absent.",
            example = "true")
    public Boolean clear;

    @Schema(description = "Trouble ticket identifier to store on the alarm.", example = "INC0012345")
    public String ticketId;

    @Schema(description = "Trouble ticket state. Unrecognised values are ignored.",
            allowableValues = {"OPEN", "CREATE_PENDING", "CREATE_FAILED", "UPDATE_PENDING", "UPDATE_FAILED",
                    "CLOSED", "CLOSE_PENDING", "CLOSE_FAILED", "RESOLVED", "RESOLVE_PENDING", "RESOLVE_FAILED",
                    "CANCELLED", "CANCEL_PENDING", "CANCEL_FAILED"},
            example = "OPEN")
    public String ticketState;
}
