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
 * Describes the form parameters the memo and journal handlers read out of a
 * {@code MultivaluedMap}; it is not deserialised as a type.
 */
@Schema(name = "AlarmMemoRequest",
        description = """
                Form parameters accepted by the sticky memo and journal (reduction key memo) updates.
                Each call replaces the whole note; there is no append.""")
public class AlarmMemoRequest {

    @Schema(description = "Memo text. Required: a request without it is rejected with 400.",
            required = true,
            example = "Waiting on the carrier.")
    public String body;

    @Schema(description = "Author recorded on the memo. Defaults to the authenticated user.",
            example = "admin")
    public String user;
}
