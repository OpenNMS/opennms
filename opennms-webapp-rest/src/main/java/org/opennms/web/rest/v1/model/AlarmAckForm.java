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

/** Describes the form body the alarm update handlers read out of a MultivaluedMap; it is not bound as a type. */
@Schema(name = "V1AlarmUpdateForm",
        description = """
                Form-encoded body for the alarm update handlers. `ack`, `escalate` and `clear` are mutually
                exclusive: they are tested in that order and only the first one present is acted on. Any field
                not listed here is treated as an alarm filter, so on the collection handler `id`, `severity`,
                `node.id` and the other `OnmsAlarm` property names select which alarms are updated.""")
public class AlarmAckForm {

    @Schema(description = "`true` acknowledges, `false` unacknowledges. Takes precedence over `escalate` and `clear`.",
            example = "true")
    public Boolean ack;

    @Schema(description = "`true` raises the alarm severity one step. Only consulted when `ack` is absent.",
            example = "true")
    public Boolean escalate;

    @Schema(description = "`true` sets the alarm severity to Cleared. Only consulted when `ack` and `escalate` are absent.",
            example = "true")
    public Boolean clear;

    @Schema(description = "User the acknowledgement is recorded against. Defaults to the authenticated user. "
            + "A non-admin caller may only name themselves.",
            example = "admin")
    public String ackUser;

    @Schema(description = "Trouble ticket id to store on the alarm. Only honoured by the single-alarm handler, "
            + "and only when non-blank.",
            example = "TT-4711")
    public String ticketId;

    @Schema(description = "Trouble ticket state to store on the alarm. Only honoured by the single-alarm handler. "
            + "A value outside the enumeration is ignored rather than reported.",
            example = "OPEN",
            allowableValues = {"OPEN", "CREATE_PENDING", "CREATE_FAILED", "UPDATE_PENDING", "UPDATE_FAILED",
                    "CLOSED", "CLOSE_PENDING", "CLOSE_FAILED", "RESOLVED", "RESOLVE_PENDING", "RESOLVE_FAILED",
                    "CANCELLED", "CANCEL_PENDING", "CANCEL_FAILED"})
    public String ticketState;

    @Schema(description = "Collection handler only: restricts the update to the alarm with this id. "
            + "`alarmId` is accepted as a synonym, but supplying both is a 400.",
            example = "4547")
    public Integer id;
}
