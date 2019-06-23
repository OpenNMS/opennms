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
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.topology.Topology;

@Entity
@Table(name="isisElement")
public final class IsIsElement implements Serializable,Topology {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3134355798509685991L;

    public enum IsisAdminState {
    	/**
    	 *    IsisAdminState ::= TEXTUAL-CONVENTION
         *    STATUS current
         *    DESCRIPTION
         *       "Type used in enabling and disabling a row."
         *    SYNTAX INTEGER
         *       {
         *            on(1),
         *            off(2)
         *        }
    	 */
        on(1),
        off(2);
        
        private int m_value;

        IsisAdminState(int value) {
        	m_value=value;
        }
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
        	s_typeMap.put(1, "on" );
        	s_typeMap.put(2, "off" );
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                    return s_typeMap.get( code);
            return null;
        }
        
        public static IsisAdminState get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create IsisAdminState from null code");
            switch (code) {
            case 1: 	return on;
            case 2: 	return off;
            default:
                throw new IllegalArgumentException("Cannot create IsisAdminState from code "+code);
            }
        }
        
        public Integer getValue() {
            return m_value;
        }
        
    }
    
    private Integer m_id;	
    private String m_isisSysID;
    private IsisAdminState m_isisSysAdminState;
    private Date m_isisNodeCreateTime = new Date();
    private Date m_isisNodeLastPollTime;
	private OnmsNode m_node;

    public IsIsElement() {}

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

    @Column(name="isisSysAdminState", nullable = false)
    @Type(type="org.opennms.netmgt.model.IsIsAdminStateUserType")
    public IsisAdminState getIsisSysAdminState() {
		return m_isisSysAdminState;
	}

    @Column(name="isisSysID" , length=32, nullable = false)
	public String getIsisSysID() {
		return m_isisSysID;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="isisNodeCreateTime", nullable=false)
    public Date getIsisNodeCreateTime() {
		return m_isisNodeCreateTime;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="isisNodeLastPollTime", nullable=false)
	public Date getIsisNodeLastPollTime() {
		return m_isisNodeLastPollTime;
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

	public void setIsisSysID(String isisSysID) {
		m_isisSysID = isisSysID;
	}

	public void setIsisSysAdminState(IsisAdminState isisSysAdminState) {
		m_isisSysAdminState = isisSysAdminState;
	}

	public void setIsisNodeCreateTime(Date isisNodeCreateTime) {
		m_isisNodeCreateTime = isisNodeCreateTime;
	}

	public void setIsisNodeLastPollTime(Date isisNodeLastPollTime) {
		m_isisNodeLastPollTime = isisNodeLastPollTime;
	}


	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return new ToStringBuilder(this)
			.append("NodeId", m_node.getId())
			.append("isisSysAdminState", IsisAdminState.getTypeString(m_isisSysAdminState.getValue()))
			.append("isisSysID", m_isisSysID)
			.append("isisNodeCreateTime", m_isisNodeCreateTime)
			.append("isisNodeLastPollTime", m_isisNodeLastPollTime)
			.toString();
	}
	
        @Transient
        public String printTopology() {
            StringBuffer strb = new StringBuffer();
                strb.append("isiselement: nodeid:["); 
                strb.append(getNode().getId());
                strb.append("], AdminState:[");
                strb.append(IsisAdminState.getTypeString(getIsisSysAdminState().getValue()));
                strb.append("], SysID:[");
                strb.append(getIsisSysID());
                strb.append("]");

            return strb.toString();
        }

	public void merge(IsIsElement element) {
		if (element == null)
			return;
		setIsisSysID(element.getIsisSysID());
		setIsisSysAdminState(element.getIsisSysAdminState());
		
		setIsisNodeLastPollTime(element.getIsisNodeCreateTime());
	}
}
