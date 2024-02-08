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
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.enlinkd.model.IsIsElement.IsisAdminState;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;

@Entity
@Table(name="isisLink")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class IsIsLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3813247749765614567L;

    public enum IsisISAdjState {
        down(1),
        initializing(2),
        up(3),
        failed(4);
        private final int m_value;

        IsisISAdjState(int value) {
        	m_value=value;
        }
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<>();

        static {
        	s_typeMap.put(1, "down" );
        	s_typeMap.put(2, "initializing" );
        	s_typeMap.put(3, "up" );
        	s_typeMap.put(4, "failed" );
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                    return s_typeMap.get( code);
            return null;
        }
        
        public static IsisISAdjState get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create IsisISAdjState from null code");
            switch (code) {
            case 1: 	return down;
            case 2: 	return initializing;
            case 3: 	return up;
            case 4: 	return failed;
            default:
                throw new IllegalArgumentException("Cannot create IsisISAdjState from code "+code);
            }
        }
        
        public Integer getValue() {
            return m_value;
        }
        
    }
    public enum IsisISAdjNeighSysType {
        l1_IntermediateSystem(1),
        l2IntermediateSystem(2),
        l1L2IntermediateSystem(3),
        unknown(4);
        private final int m_value;

        IsisISAdjNeighSysType(int value) {
        	m_value=value;
        }
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<>();

        static {
        	s_typeMap.put(1, "l1_IntermediateSystem" );
        	s_typeMap.put(2, "l2IntermediateSystem" );
        	s_typeMap.put(3, "l1L2IntermediateSystem" );
        	s_typeMap.put(4, "unknown" );
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                    return s_typeMap.get( code);
            return null;
        }
        
        public static IsisISAdjNeighSysType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create IsisISAdjNeighSysType from null code");
            switch (code) {
            case 1: 	return l1_IntermediateSystem;
            case 2: 	return l2IntermediateSystem;
            case 3: 	return l1L2IntermediateSystem;
            case 4: 	return unknown;
            default:
                throw new IllegalArgumentException("Cannot create IsisISAdjNeighSysType from code "+code);
            }
        }
        
        public Integer getValue() {
            return m_value;
        }
        
    }


    private Integer m_id;	
	private OnmsNode m_node;
	
	private Integer m_isisCircIndex;
	private Integer m_isisISAdjIndex;
	private Integer m_isisCircIfIndex;
	private IsisAdminState m_isisCircAdminState;
    
	private IsisISAdjState m_isisISAdjState;
	private String m_isisISAdjNeighSNPAAddress;
	private IsisISAdjNeighSysType m_isisISAdjNeighSysType;
	private String m_isisISAdjNeighSysID;
	private Integer m_isisISAdjNbrExtendedCircID;
    
	private Date m_isisLinkCreateTime = new Date();
    private Date m_isisLinkLastPollTime;
	
	public IsIsLink() {
	}
		
    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
		return m_id;
	}

	public void setId(Integer id) {
		m_id = id;
	}

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
	public OnmsNode getNode() {
		return m_node;
	}


	public void setNode(OnmsNode node) {
		m_node = node;
	}

    @Column(name="isisCircIndex", nullable = false)
	public Integer getIsisCircIndex() {
		return m_isisCircIndex;
	}

	public void setIsisCircIndex(Integer isisCircIndex) {
		m_isisCircIndex = isisCircIndex;
	}

    @Column(name="isisISAdjIndex", nullable = false)
	public Integer getIsisISAdjIndex() {
		return m_isisISAdjIndex;
	}

	public void setIsisISAdjIndex(Integer isisISAdjIndex) {
		m_isisISAdjIndex = isisISAdjIndex;
	}

    @Column(name="isisCircIfIndex")
	public Integer getIsisCircIfIndex() {
		return m_isisCircIfIndex;
	}

	public void setIsisCircIfIndex(Integer isisIfCircIndex) {
		m_isisCircIfIndex = isisIfCircIndex;
	}

    @Column(name="isisCircAdminState")
    @Type(type="org.opennms.netmgt.enlinkd.model.IsIsAdminStateUserType")
	public IsisAdminState getIsisCircAdminState() {
		return m_isisCircAdminState;
	}


	public void setIsisCircAdminState(IsisAdminState isisCircAdminState) {
		m_isisCircAdminState = isisCircAdminState;
	}

    @Column(name="isisISAdjState", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.IsIsISAdjStateUserType")
	public IsisISAdjState getIsisISAdjState() {
		return m_isisISAdjState;
	}


	public void setIsisISAdjState(IsisISAdjState isisISAdjState) {
		m_isisISAdjState = isisISAdjState;
	}

    @Column(name="isisISAdjNeighSNPAAddress" , length=80, nullable = false)
	public String getIsisISAdjNeighSNPAAddress() {
		return m_isisISAdjNeighSNPAAddress;
	}


	public void setIsisISAdjNeighSNPAAddress(String isisISAdjNeighSNPAAddress) {
		m_isisISAdjNeighSNPAAddress = isisISAdjNeighSNPAAddress;
	}


    @Column(name="isisISAdjNeighSysType", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.IsIsISAdjNeighSysTypeUserType")
	public IsisISAdjNeighSysType getIsisISAdjNeighSysType() {
		return m_isisISAdjNeighSysType;
	}


	public void setIsisISAdjNeighSysType(IsisISAdjNeighSysType isisISAdjNeighSysType) {
		m_isisISAdjNeighSysType = isisISAdjNeighSysType;
	}


    @Column(name="isisISAdjNeighSysID" , length=32, nullable = false)
	public String getIsisISAdjNeighSysID() {
		return m_isisISAdjNeighSysID;
	}


	public void setIsisISAdjNeighSysID(String isisISAdjNeighSysID) {
		m_isisISAdjNeighSysID = isisISAdjNeighSysID;
	}

    @Column(name="isisISAdjNbrExtendedCircID", nullable = false)
	public Integer getIsisISAdjNbrExtendedCircID() {
		return m_isisISAdjNbrExtendedCircID;
	}

	public void setIsisISAdjNbrExtendedCircID(Integer isisISAdjNbrExtendedCircID) {
		m_isisISAdjNbrExtendedCircID = isisISAdjNbrExtendedCircID;
	}
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="isisLinkCreateTime", nullable=false)
	public Date getIsisLinkCreateTime() {
		return m_isisLinkCreateTime;
	}


	public void setIsisLinkCreateTime(Date isisLinkCreateTime) {
		m_isisLinkCreateTime = isisLinkCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="isisLinkLastPollTime", nullable=false)
	public Date getIsisLinkLastPollTime() {
		return m_isisLinkLastPollTime;
	}


	public void setIsisLinkLastPollTime(Date isisLinkLastPollTime) {
		m_isisLinkLastPollTime = isisLinkLastPollTime;
	}


	public void merge(IsIsLink link) {
		
		setIsisCircIfIndex(link.getIsisCircIfIndex());
		setIsisCircAdminState(link.getIsisCircAdminState());
		
		setIsisISAdjState(link.getIsisISAdjState());
		setIsisISAdjNeighSNPAAddress(link.getIsisISAdjNeighSNPAAddress());
		setIsisISAdjNeighSysType(link.getIsisISAdjNeighSysType());
		setIsisISAdjNeighSysID(link.getIsisISAdjNeighSysID());
		setIsisISAdjNbrExtendedCircID(link.getIsisISAdjNbrExtendedCircID());
	
		setIsisLinkLastPollTime(link.getIsisLinkCreateTime());
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {

		return "isislink: nodeid:[" +
				getNode().getId() +
				"]. circIndex:[ " +
				getIsisCircIndex() +
				"], ifindex:[" +
				getIsisCircIfIndex() +
				"], AdminState:[" +
				IsisAdminState.getTypeString(getIsisCircAdminState().getValue()) +
				"], ISAdjNeighSysID:[" +
				getIsisISAdjNeighSysID() +
				"], ISAdjNeighSNPAAddress:[" +
				getIsisISAdjNeighSNPAAddress() +
				"], ISAdjState:[" +
				IsisISAdjState.getTypeString(getIsisISAdjState().getValue()) +
				"]";
        }
}
