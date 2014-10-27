/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;

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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.OspfElement.TruthValue;


@Entity
@Table(name="cdpElement")
public final class CdpElement implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3134355798509685991L;


    private Integer m_id;	
    private TruthValue m_cdpGlobalRun;
    private String m_cdpGlobalDeviceId;
    private Date m_cdpNodeCreateTime = new Date();
    private Date m_cdpNodeLastPollTime;
	private OnmsNode m_node;

    public CdpElement() {}

    public CdpElement(OnmsNode node, String cdpGlobalDeviceId) {
        setNode(node);
        setCdpGlobalDeviceId(cdpGlobalDeviceId);
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
    @Type(type="org.opennms.netmgt.model.TruthValueUserType")
	public TruthValue getCdpGlobalRun() {
		return m_cdpGlobalRun;
	}

    @Column(name="cdpGlobalDeviceId" , length=256, nullable = false)
	public String getCdpGlobalDeviceId() {
		return m_cdpGlobalDeviceId;
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
		return new ToStringBuilder(this)
			.append("Nodeid", m_node.getId())
			.append("cdpGlobalDeviceId", m_cdpGlobalDeviceId)
			.append("cdpNodeCreateTime", m_cdpNodeCreateTime)
			.append("cdpNodeLastPollTime", m_cdpNodeLastPollTime)
			.toString();
	}
	
	public void merge(CdpElement element) {
		if (element == null)
			return;
		setCdpGlobalRun(element.getCdpGlobalRun());
		setCdpGlobalDeviceId(element.getCdpGlobalDeviceId());
		setCdpNodeLastPollTime(element.getCdpNodeCreateTime());
	}

}
