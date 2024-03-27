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

import com.google.common.base.MoreObjects;
import org.opennms.netmgt.model.ReadOnlyEntity;

import java.net.InetAddress;

@ReadOnlyEntity
public class OspfAreaTopologyEntity {

    private final Integer id;
    private final Integer nodeId;
    private final InetAddress ospfAreaId;
    private final Integer ospfAuthType;
    private final Integer ospfImportAsExtern;
    private final Integer ospfAreaBdrRtrCount;
    private final Integer ospfAsBdrRtrCount;
    private final Integer ospfAreaLsaCount;

    public OspfAreaTopologyEntity(Integer id, Integer nodeId, InetAddress ospfAreaId, Integer ospfAuthType, Integer ospfImportAsExtern, Integer ospfAreaBdrRtrCount, Integer ospfAsBdrRtrCount, Integer ospfAreaLsaCount) {
        this.id = id;
        this.nodeId = nodeId;
        this.ospfAreaId = ospfAreaId;
        this.ospfAuthType = ospfAuthType;
        this.ospfImportAsExtern = ospfImportAsExtern;
        this.ospfAreaBdrRtrCount = ospfAreaBdrRtrCount;
        this.ospfAsBdrRtrCount = ospfAsBdrRtrCount;
        this.ospfAreaLsaCount = ospfAreaLsaCount;
    }

    public static org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity create(OspfArea area) {
        return new org.opennms.netmgt.enlinkd.model.OspfAreaTopologyEntity(area.getId()
                , area.getNode().getId()
                , area.getOspfAreaId()
                , area.getOspfAuthType()
                , area.getOspfImportAsExtern()
                , area.getOspfAreaBdrRtrCount()
                , area.getOspfAsBdrRtrCount()
                , area.getOspfAreaLsaCount());
    }

    public Integer getId() {
        return id;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public String getNodeIdAsString() {
        if (getNodeId() != null) {
            return getNodeId().toString();
        }
        return null;
    }

    public InetAddress getOspfAreaId() {
        return ospfAreaId;
    }

    public Integer getOspfAuthType() {
        return ospfAuthType;
    }

    public Integer getOspfImportAsExtern() {
        return ospfImportAsExtern;
    }

    public Integer getOspfAreaBdrRtrCount() {
        return ospfAreaBdrRtrCount;
    }

    public Integer getOspfAsBdrRtrCount() {
        return ospfAsBdrRtrCount;
    }

    public Integer getOspfAreaLsaCount() {
        return ospfAreaLsaCount;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("ospfAreaId", ospfAreaId)
                .add("ospfAuthType", ospfAuthType)
                .add("ospfImportAsExtern", ospfImportAsExtern)
                .add("ospfAreaBdrRtrCount", ospfAreaBdrRtrCount)
                .add("ospfAsBdrRtrCount", ospfAsBdrRtrCount)
                .add("ospfAreaLsaCount", ospfAreaLsaCount)
                .toString();
    }
}


