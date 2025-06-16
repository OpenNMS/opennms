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

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.opennms.core.utils.InetAddressUtils.str;

@Entity
@Table(name = "ospfArea")
@Filter(name = FilterManager.AUTH_FILTER_NAME, condition = "exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class OspfArea implements Serializable {

    private final static Logger LOG = LoggerFactory.getLogger(OspfArea.class);

    private static final long serialVersionUID = 3798160983917807494L;
    private Integer m_id;
    private OnmsNode m_node;
    private InetAddress m_ospfAreaId;
    private Integer m_ospfAuthType;
    private Integer m_ospfImportAsExtern;
    private Integer m_ospfAreaBdrRtrCount;
    private Integer m_ospfAsBdrRtrCount;
    private Integer m_ospfAreaLsaCount;
    private Date m_ospfAreaCreateTime = new Date();
    private Date m_ospfAreaLastPollTime;

    public OspfArea() {
    }

    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    @Type(type = "org.opennms.netmgt.model.InetAddressUserType")
    @Column(name = "ospfAreaId")
    public InetAddress getOspfAreaId() {
        return m_ospfAreaId;
    }

    @Column(name = "ospfAuthType")
    public Integer getOspfAuthType() {
        return m_ospfAuthType;
    }

//    @Type(type="org.opennms.netmgt.enlinkd.model.ImportAsExternUserType")
    @Column(name = "ospfImportAsExtern")
    public Integer getOspfImportAsExtern() {
        return m_ospfImportAsExtern;
    }

    @Column(name = "ospfAreaBdrRtrCount")
    public Integer getOspfAreaBdrRtrCount() {
        return m_ospfAreaBdrRtrCount;
    }

    @Column(name = "ospfAsBdrRtrCount")
    public Integer getOspfAsBdrRtrCount() {
        return m_ospfAsBdrRtrCount;
    }

    @Column(name = "ospfAreaLsaCount")
    public Integer getOspfAreaLsaCount() {
        return m_ospfAreaLsaCount;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ospfAreaCreateTime", nullable=false)
    public Date getOspfAreaCreateTime() {
        return m_ospfAreaCreateTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ospfAreaLastPollTime", nullable=false)
    public Date getOspfAreaLastPollTime() {
        return m_ospfAreaLastPollTime;
    }

    public OspfArea setId(Integer id) {
        this.m_id = id;
        return this;
    }

    public OspfArea setNode(OnmsNode node) {
        this.m_node = node;
        return this;
    }

    public OspfArea setOspfAreaId(InetAddress ospfAreaId) {
        this.m_ospfAreaId = ospfAreaId;
        return this;
    }

    public OspfArea setOspfAuthType(Integer ospfAuthType) {
        this.m_ospfAuthType = ospfAuthType;
        return this;
    }

    public OspfArea setOspfImportAsExtern(Integer ospfImportAsExtern) {
        this.m_ospfImportAsExtern = ospfImportAsExtern;
        return this;
    }

    public OspfArea setOspfAreaBdrRtrCount(Integer ospfAreaBdrRtrCount) {
        this.m_ospfAreaBdrRtrCount = ospfAreaBdrRtrCount;
        return this;
    }

    public OspfArea setOspfAsBdrRtrCount(Integer ospfAsBdrRtrCount) {
        this.m_ospfAsBdrRtrCount = ospfAsBdrRtrCount;
        return this;
    }

    public OspfArea setOspfAreaLsaCount(Integer ospfAreaLsaCount) {
        this.m_ospfAreaLsaCount = ospfAreaLsaCount;
        return this;
    }

    public OspfArea setOspfAreaCreateTime(Date ospfAreaCreateTime) {
        m_ospfAreaCreateTime = ospfAreaCreateTime;
        return this;
    }

    public OspfArea setOspfAreaLastPollTime(Date ospfAreaLastPollTime) {
        m_ospfAreaLastPollTime = ospfAreaLastPollTime;
        return this;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link String} object.
     */
    public String toString() {
        return "ospfArea: nodeid:[" +
                (getNode() != null ? getNode().getId() : null) +
                "]: area [" +
                str(getOspfAreaId()) +
                "/" +
                getOspfAuthType() +
                "/" +
                getOspfImportAsExtern() +
                "/" +
                getOspfAreaBdrRtrCount() +
                "/" +
                getOspfAsBdrRtrCount() +
                "/" +
                getOspfAreaLsaCount() +
                "]";
    }

    public void merge(OspfArea area) {
        if (area == null)
            return;
        setOspfAreaId(area.getOspfAreaId());
        setOspfAuthType(area.getOspfAuthType());
        setOspfImportAsExtern(area.getOspfImportAsExtern());
        setOspfAreaBdrRtrCount(area.getOspfAreaBdrRtrCount());
        setOspfAsBdrRtrCount(area.getOspfAsBdrRtrCount());
        setOspfAreaLsaCount(area.getOspfAreaLsaCount());
        setOspfAreaCreateTime(area.getOspfAreaCreateTime());
        setOspfAreaLastPollTime(area.getOspfAreaCreateTime());
    }


    public enum ImportAsExtern {
        IMPORT_EXTERNAL(1),
        IMPORT_NO_EXTERNAL(2),
        IMPORT_NSSA(3);

        private final int value;
        private static Map map = new HashMap<>();

        private ImportAsExtern(Integer value) {
            this.value = value;
        }

        static {
            for (ImportAsExtern importAsExtern : ImportAsExtern.values()) {
                map.put(importAsExtern.value, importAsExtern);
            }
        }

        public static ImportAsExtern valueOf(int importAsExtern) {
            return (ImportAsExtern) map.get(importAsExtern);
        }

        public Integer getValue() {
            return value;
        }
    }

}
