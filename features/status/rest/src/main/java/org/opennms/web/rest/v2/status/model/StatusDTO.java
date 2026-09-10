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
package org.opennms.web.rest.v2.status.model;

import io.swagger.v3.oas.annotations.media.Schema;
import org.opennms.features.status.api.SeveritySupplier;
import org.opennms.netmgt.model.OnmsSeverity;

@Schema(description = "One entity and the severity rolled up onto it.")
public class StatusDTO implements SeveritySupplier {

    @Schema(description = "Database id of the entity.", example = "1")
    private Integer id;

    @Schema(description = "Node label, application name or business service name.", example = "loopback-001")
    private String name;

    @Schema(description = "Rolled-up severity, serialized as the enum constant name rather than the label.",
            example = "MINOR",
            allowableValues = {"INDETERMINATE", "CLEARED", "NORMAL", "WARNING", "MINOR", "MAJOR", "CRITICAL"})
    private OnmsSeverity severity;

    @Override
    public OnmsSeverity getSeverity() {
        return severity;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSeverity(OnmsSeverity severity) {
        this.severity = severity;
    }
}
