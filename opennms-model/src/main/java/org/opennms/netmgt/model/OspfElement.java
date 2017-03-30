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
import java.net.InetAddress;
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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import static org.opennms.core.utils.InetAddressUtils.str;

@Entity
@Table(name="ospfElement")
public final class OspfElement implements Serializable {

	public enum TruthValue {
        /**
         * TruthValue ::= TEXTUAL-CONVENTION
         * 	    STATUS       current
         * 	    DESCRIPTION
         * 	            "Represents a boolean value."
         * 	    SYNTAX       INTEGER { true(1), false(2) }
         */
         TRUE(1),FALSE(2);
         
 		private int m_type;

 		TruthValue(int type) {
 			m_type=type;
 		}
 		
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

         static {
         	s_typeMap.put(1, "true" );
         	s_typeMap.put(2, "false" );
         }

         public static String getTypeString(Integer code) {
             if (s_typeMap.containsKey(code))
                     return s_typeMap.get( code);
             return null;
         }

         public Integer getValue() {
         	return m_type;
         }

         public static TruthValue get(Integer code) {
             if (code == null)
                 throw new IllegalArgumentException("Cannot create TruthValue from null code");
             switch (code) {
             case 1: 	return TRUE;
             case 2: 	return FALSE;
             default:
                 throw new IllegalArgumentException("Cannot create TruthValue from code "+code);
             }
         }
	}

	public enum Status {
    	/**
    	 *     	Status ::= TEXTUAL-CONVENTION
    	 *      STATUS      current
    	 *      DESCRIPTION
    	 *          "The status of an interface: 'enabled' indicates that
    	 *          it is willing to communicate with other OSPF Routers,
    	 *          while 'disabled' indicates that it is not."
    	 *          SYNTAX      INTEGER { enabled (1), disabled (2) }
    	 *          
         */
         enabled(1),disabled(2);
         
 		private int m_type;

 		Status(int type) {
 			m_type=type;
 		}
 		
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

         static {
         	s_typeMap.put(1, "enabled" );
         	s_typeMap.put(2, "disabled" );
         }

         public static String getTypeString(Integer code) {
             if (s_typeMap.containsKey(code))
                     return s_typeMap.get( code);
             return null;
         }

         public Integer getValue() {
         	return m_type;
         }

         public static Status get(Integer code) {
             if (code == null)
                 throw new IllegalArgumentException("Cannot create Status from null code");
             switch (code) {
             case 1: 	return enabled;
             case 2: 	return disabled;
             default:
                 throw new IllegalArgumentException("Cannot create Status from code "+code);
             }
         }
	}
    /**
	 * 
	 */
	private static final long serialVersionUID = 7820026592390162672L;
	private Integer m_id;	
	private InetAddress m_ospfRouterId;
	private Status m_ospfAdminStat;
	private Integer m_ospfVersionNumber;
	private TruthValue m_ospfBdrRtrStatus;
	private TruthValue m_ospfASBdrRtrStatus;
	private InetAddress m_ospfRouterIdNetmask;
	private Integer     m_ospfRouterIdIfindex;
    private Date m_ospfNodeCreateTime = new Date();
    private Date m_ospfNodeLastPollTime;
	private OnmsNode m_node;

	public OspfElement() {}


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

    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @Column(name="ospfRouterId",nullable=false)
	public InetAddress getOspfRouterId() {
		return m_ospfRouterId;
	}

    @Column(name="ospfAdminStat", nullable = false)
    @Type(type="org.opennms.netmgt.model.StatusUserType")
	public Status getOspfAdminStat() {
		return m_ospfAdminStat;
	}

    @Column(name="ospfVersionNumber", nullable = false)
	public Integer getOspfVersionNumber() {
		return m_ospfVersionNumber;
	}

    @Column(name="ospfBdrRtrStatus", nullable = false)
    @Type(type="org.opennms.netmgt.model.TruthValueUserType")
	public TruthValue getOspfBdrRtrStatus() {
		return m_ospfBdrRtrStatus;
	}

    @Column(name="ospfASBdrRtrStatus", nullable = false)
    @Type(type="org.opennms.netmgt.model.TruthValueUserType")
	public TruthValue getOspfASBdrRtrStatus() {
		return m_ospfASBdrRtrStatus;
	}

	@Type(type="org.opennms.netmgt.model.InetAddressUserType")
    @Column(name="ospfRouterIdNetmask",nullable=false)
    public InetAddress getOspfRouterIdNetmask() {
		return m_ospfRouterIdNetmask;
	}

    @Column(name = "ospfRouterIdIfindex", nullable = false)
	public Integer getOspfRouterIdIfindex() {
		return m_ospfRouterIdIfindex;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ospfNodeCreateTime", nullable=false)
	public Date getOspfNodeCreateTime() {
		return m_ospfNodeCreateTime;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ospfNodeLastPollTime", nullable=false)
	public Date getOspfNodeLastPollTime() {
		return m_ospfNodeLastPollTime;
	}

	public void setOspfRouterId(InetAddress ospfRouterId) {
		m_ospfRouterId = ospfRouterId;
	}
	
	public void setOspfAdminStat(Status ospfAdminStat) {
		m_ospfAdminStat = ospfAdminStat;
	}

	public void setOspfVersionNumber(Integer ospfVersionNumber) {
		m_ospfVersionNumber = ospfVersionNumber;
	}

	public void setOspfBdrRtrStatus(TruthValue ospfBdrRtrStatus) {
		m_ospfBdrRtrStatus = ospfBdrRtrStatus;
	}

	public void setOspfASBdrRtrStatus(TruthValue ospfASBdrRtrStatus) {
		m_ospfASBdrRtrStatus = ospfASBdrRtrStatus;
	}

	public void setId(Integer id) {
		m_id = id;
	}

	public void setNode(OnmsNode node) {
		m_node = node;
	}

	public void setOspfRouterIdNetmask(InetAddress ospfRouterIdNetmask) {
		m_ospfRouterIdNetmask = ospfRouterIdNetmask;
	}

	public void setOspfRouterIdIfindex(Integer ospfRouterIdIfindex) {
		m_ospfRouterIdIfindex = ospfRouterIdIfindex;
	}

	public void setOspfNodeCreateTime(Date ospfNodeCreateTime) {
		m_ospfNodeCreateTime = ospfNodeCreateTime;
	}

	public void setOspfNodeLastPollTime(Date ospfNodeLastPollTime) {
		m_ospfNodeLastPollTime = ospfNodeLastPollTime;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("NodeId", m_node.getId())
			.append("ospfRouterId", str(m_ospfRouterId))
			.append("ospfRouterIdNetmask", str(m_ospfRouterIdNetmask))
			.append("ospfRouterIdIfindex", m_ospfRouterIdIfindex)
			.append("ospfAdminStat", Status.getTypeString(m_ospfAdminStat.getValue()))
			.append("ospfVersionNumber", m_ospfVersionNumber)
			.append("ospfBdrRtrStatus", TruthValue.getTypeString(m_ospfBdrRtrStatus.getValue()))
			.append("ospfASBdrRtrStatus", TruthValue.getTypeString(m_ospfASBdrRtrStatus.getValue()))
			.append("createTime", m_ospfNodeCreateTime)
			.append("lastPollTime", m_ospfNodeLastPollTime)
			.toString();
	}

	public void merge(OspfElement element) {
		if (element == null)
			return;
		setOspfRouterId(element.getOspfRouterId());
		setOspfRouterIdIfindex(element.getOspfRouterIdIfindex());
		setOspfRouterIdNetmask(element.getOspfRouterIdNetmask());
		
		setOspfNodeLastPollTime(element.getOspfNodeCreateTime());
	}

}
