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

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.enlinkd.model.OspfElement.TruthValue;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;


@Entity
@Table(name="cdpElement")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public final class CdpElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3134355798509685991L;


    private Integer m_id;	
    private TruthValue m_cdpGlobalRun;
    private String m_cdpGlobalDeviceId;
    private CdpGlobalDeviceIdFormat m_cdpGlobalDeviceIdFormat;
    private Date m_cdpNodeCreateTime = new Date();
    private Date m_cdpNodeLastPollTime;
    private OnmsNode m_node;

    public enum CdpGlobalDeviceIdFormat {

        /**
         * SYNTAX     INTEGER { 
         *        serialNumber(1), 
         *        macAddress(2),
         *        other(3) 
         *      } 
         */
        serialNumber(1), macAddress(2),other(3);

        private final int m_type;

        CdpGlobalDeviceIdFormat(int type) {
            m_type = type;
        }

        protected static final Map<Integer, String> s_typeMap = new HashMap<>();

        static {
            s_typeMap.put(1, "serialNumber");
            s_typeMap.put(2, "macAddress");
            s_typeMap.put(3, "other");
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                return s_typeMap.get(code);
            return null;
        }

        public Integer getValue() {
            return m_type;
        }

        public static CdpGlobalDeviceIdFormat get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException(
                                                   "Cannot create CdpDeviceFormat from null code");
            switch (code) {
            case 1:
                return serialNumber;
            case 2:
                return macAddress;
            case 3:
                return other;
            default:
                throw new IllegalArgumentException(
                                                   "Cannot create CdpDeviceIdFormat from code "
                                                           + code);
            }
        }
    }

    public CdpElement() {}

    public CdpElement(OnmsNode node, String cdpGlobalDeviceId) {
        setNode(node);
        setCdpGlobalDeviceId(cdpGlobalDeviceId);
    }

    public CdpElement(OnmsNode node, String cdpGlobalDeviceId, CdpGlobalDeviceIdFormat format) {
        setNode(node);
        setCdpGlobalDeviceId(cdpGlobalDeviceId);
        setCdpGlobalDeviceIdFormat(format);
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * The node this asset information belongs to.
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    public OnmsNode getNode() {
        return m_node;
    }

    @Column(name="cdpGlobalRun", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.TruthValueUserType")
	public TruthValue getCdpGlobalRun() {
		return m_cdpGlobalRun;
	}

    @Column(name="cdpGlobalDeviceId" , length=256, nullable = false)
	public String getCdpGlobalDeviceId() {
		return m_cdpGlobalDeviceId;
	}

    @Column(name="cdpGlobalDeviceIdFormat")
    @Type(type="org.opennms.netmgt.enlinkd.model.CdpGlobalDeviceIdFormatUserType")
    public CdpGlobalDeviceIdFormat getCdpGlobalDeviceIdFormat() {
        return m_cdpGlobalDeviceIdFormat;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cdpNodeCreateTime", nullable=false)
    public Date getCdpNodeCreateTime() {
		return m_cdpNodeCreateTime;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cdpNodeLastPollTime", nullable=false)
	public Date getCdpNodeLastPollTime() {
		return m_cdpNodeLastPollTime;
	}

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(final Integer id) {
        m_id = id;
    }

    /**
     * Set the node associated with the Lldp Element record
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setNode(OnmsNode node) {
        m_node = node;
    }

    public void setCdpGlobalRun(TruthValue cdpGlobalRun) {
        m_cdpGlobalRun = cdpGlobalRun;
    }

    public void setCdpGlobalDeviceId(String cdpGlobalDeviceId) {
        m_cdpGlobalDeviceId = cdpGlobalDeviceId;
    }

    public void setCdpGlobalDeviceIdFormat(
            CdpGlobalDeviceIdFormat cdpGlobalDeviceIdFormat) {
        m_cdpGlobalDeviceIdFormat = cdpGlobalDeviceIdFormat;
    }

    public void setCdpNodeCreateTime(Date cdpNodeCreateTime) {
        m_cdpNodeCreateTime = cdpNodeCreateTime;
    }

    public void setCdpNodeLastPollTime(Date cdpNodeLastPollTime) {
        m_cdpNodeLastPollTime = cdpNodeLastPollTime;
    }


	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
        return "cdpelement: nodeid:[" +
                getNode().getId() +
                "], Global Device Id:[" +
                getCdpGlobalDeviceId() +
                "], Global Run:[" +
                TruthValue.getTypeString(getCdpGlobalRun().getValue()) +
                "]";
        }

	public void merge(CdpElement element) {
		if (element == null)
			return;
		setCdpGlobalRun(element.getCdpGlobalRun());
		setCdpGlobalDeviceId(element.getCdpGlobalDeviceId());
		setCdpNodeLastPollTime(element.getCdpNodeCreateTime());
	}

}
