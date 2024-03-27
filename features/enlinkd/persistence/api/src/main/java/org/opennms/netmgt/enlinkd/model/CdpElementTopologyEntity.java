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
package org.opennms.netmgt.enlinkd.model;

import java.util.Optional;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ReadOnlyEntity;

@ReadOnlyEntity
public class CdpElementTopologyEntity {
    private final Integer id;
    private final String cdpGlobalDeviceId;
    private final Integer nodeId;

    public CdpElementTopologyEntity(Integer id, String cdpGlobalDeviceId, Integer nodeId){
        this.id = id;
        this.cdpGlobalDeviceId = cdpGlobalDeviceId;
        this.nodeId = nodeId;
    }

    public static CdpElementTopologyEntity create(CdpElement element){
        return new CdpElementTopologyEntity(
                element.getId(),
                element.getCdpGlobalDeviceId(),
                Optional.ofNullable(element.getNode()).map(OnmsNode::getId).orElse(null)
                );
    }

    public Integer getId() {
        return id;
    }

    public String getCdpGlobalDeviceId() {
        return cdpGlobalDeviceId;
    }

    public Integer getNodeId() {
        return nodeId;
    }
}
