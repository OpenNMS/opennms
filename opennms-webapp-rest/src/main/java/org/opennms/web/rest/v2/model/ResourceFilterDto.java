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

@Schema(description = "A regular expression matched against one field of a collected resource. "
        + "Resource filters only apply to interface-level and generic-resource-level thresholds; a "
        + "`node` datasource type ignores them entirely.")
public class ResourceFilterDto {

    @Schema(description = "Name of the resource field the expression is matched against.", example = "ifDescr", required = true)
    private String field;

    @Schema(description = "The regular expression itself. Evaluated by Java, so Java regex syntax applies.",
            example = "^eth.*")
    private String content;

    public String getField() {
        return field;
    }

    public void setField(final String field) {
        this.field = field;
    }
    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }
}
