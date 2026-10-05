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
 * Describes the body of {@code GET /nodes/{nodeCriteria}/criticalPath}. The handler builds it from an
 * ad-hoc map, so this class documents the shape rather than being returned as a type.
 */
@Schema(name = "NodeCriticalPath", description = "The critical path configured for one node (its row in the "
        + "`pathoutage` table): an address and service on the network path to the node. When the node goes down "
        + "while that element is also down, the outage is treated as a path outage.")
public class NodeCriticalPathDto {

    @Schema(description = "Address of the critical path element.", example = "10.0.0.3")
    private String criticalPathIp;

    @Schema(description = "Service polled on the critical path element.", example = "ICMP")
    private String criticalPathServiceName;

    public String getCriticalPathIp() {
        return criticalPathIp;
    }

    public void setCriticalPathIp(final String criticalPathIp) {
        this.criticalPathIp = criticalPathIp;
    }

    public String getCriticalPathServiceName() {
        return criticalPathServiceName;
    }

    public void setCriticalPathServiceName(final String criticalPathServiceName) {
        this.criticalPathServiceName = criticalPathServiceName;
    }
}
