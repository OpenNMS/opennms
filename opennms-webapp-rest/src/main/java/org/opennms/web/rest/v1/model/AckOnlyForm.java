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

/** Describes the form body the single-event and single-notification acknowledge handlers read; it is not bound as a type. */
@Schema(name = "V1AckOnlyForm",
        description = "Form-encoded body carrying only an acknowledge flag.")
public class AckOnlyForm {

    @Schema(description = "`true` records the acknowledgement as the authenticated user, `false` removes it. "
            + "Any other value parses as `false`. The single-resource operations reject a body without it with a "
            + "400; the collection operations default it to `false`.",
            example = "true")
    public Boolean ack;
}
