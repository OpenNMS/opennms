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

import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.model.ReadOnlyEntity;

import com.google.common.base.MoreObjects;
import org.springframework.util.Assert;

@ReadOnlyEntity
public class LldpLinkTopologyEntity {

    private final Integer id;
    private final Integer nodeId;
    private final String lldpRemChassisId;
    private final String lldpRemSysname;
    private final String lldpRemPortId;
    private final String lldpRemPortDescr;
    private final LldpUtils.LldpPortIdSubType lldpRemPortIdSubType;
    private final String lldpPortId;
    private final LldpUtils.LldpPortIdSubType lldpPortIdSubType;
    private final String lldpPortDescr;
    private final Integer lldpPortIfindex;

    public LldpLinkTopologyEntity(Integer id, Integer nodeId, String lldpRemChassisId, String lldpRemSysname, String lldpRemPortId,
                                  LldpUtils.LldpPortIdSubType lldpRemPortIdSubType,
                                  String remportdescr,
                                  String lldpPortId,
                                  LldpUtils.LldpPortIdSubType lldpPortIdSubType, String lldpPortDescr, Integer lldpPortIfindex) {
        Assert.notNull(remportdescr);
        this.id = id;
        this.nodeId = nodeId;
        this.lldpRemChassisId = lldpRemChassisId;
        this.lldpRemSysname = lldpRemSysname;
        this.lldpRemPortId = lldpRemPortId;
        this.lldpRemPortIdSubType = lldpRemPortIdSubType;
        this.lldpPortId = lldpPortId;
        this.lldpRemPortDescr = remportdescr;
        this.lldpPortIdSubType = lldpPortIdSubType;
        this.lldpPortDescr = lldpPortDescr;
        this.lldpPortIfindex = lldpPortIfindex;
    }

    public static LldpLinkTopologyEntity create (LldpLink link) {
        return new LldpLinkTopologyEntity(
                link.getId()
                , link.getNode().getId()
                , link.getLldpRemChassisId()
                , link.getLldpRemSysname()
                , link.getLldpRemPortId()
                , link.getLldpRemPortIdSubType()
                ,link.getLldpPortDescr()
                , link.getLldpPortId()
                , link.getLldpPortIdSubType()
                , link.getLldpPortDescr()
                , link.getLldpPortIfindex()
        );
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

    public String getLldpRemChassisId() {
        return lldpRemChassisId;
    }

    public String getLldpRemPortId() {
        return lldpRemPortId;
    }

    public LldpUtils.LldpPortIdSubType getLldpRemPortIdSubType() {
        return lldpRemPortIdSubType;
    }

    public String getLldpPortId() {
        return lldpPortId;
    }

    public LldpUtils.LldpPortIdSubType getLldpPortIdSubType() {
        return lldpPortIdSubType;
    }

    public String getLldpPortDescr() {
        return lldpPortDescr;
    }

    public Integer getLldpPortIfindex() {
        return lldpPortIfindex;
    }
    public String getLldpRemPortDescr() {
        return lldpRemPortDescr;
    }

    public String getLldpRemSysname() {
        return lldpRemSysname;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("nodeId", nodeId)
                .add("lldpRemChassisId", lldpRemChassisId)
                .add("lldpRemSysname", lldpRemSysname)
                .add("lldpRemPortId", lldpRemPortId)
                .add("lldpRemPortIdSubType", lldpRemPortIdSubType)
                .add("lldpRemPortDescr", lldpRemPortDescr)
                .add("lldpPortId", lldpPortId)
                .add("lldpPortIdSubType", lldpPortIdSubType)
                .add("lldpPortDescr", lldpPortDescr)
                .add("lldpPortIfindex", lldpPortIfindex)
                .toString();
    }

}
