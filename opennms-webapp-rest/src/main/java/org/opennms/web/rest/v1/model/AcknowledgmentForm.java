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

/** Describes the form body the acknowledge handler reads out of a MultivaluedMap; it is not bound as a type. */
@Schema(name = "V1AcknowledgmentForm",
        description = "Form-encoded body for creating an acknowledgement. Exactly one of `alarmId` and `notifId` "
                + "has to be present.")
public class AcknowledgmentForm {

    @Schema(description = "Id of the alarm to act on. Mutually exclusive with `notifId`.", example = "4547")
    public String alarmId;

    @Schema(description = "Id of the notification to act on. Mutually exclusive with `alarmId`.", example = "2615")
    public String notifId;

    @Schema(description = "Action to record. Defaults to `ack` when absent.",
            example = "ack", allowableValues = {"ack", "unack", "clear", "esc"})
    public String action;

    @Schema(description = "User the acknowledgement is recorded against. Defaults to the authenticated user. "
            + "A non-admin caller may only name themselves.",
            example = "admin")
    public String ackUser;
}
