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
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;

@Entity
@Table(name="isisElement")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public final class IsIsElement implements Serializable {

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
        
        private final int m_value;

        IsisAdminState(int value) {
        	m_value=value;
        }
 	    protected static final Map<Integer, String> s_typeMap = new HashMap<>();

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
    @Type(type="org.opennms.netmgt.enlinkd.model.IsIsAdminStateUserType")
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

        return "isiselement: nodeid:[" +
                getNode().getId() +
                "], AdminState:[" +
                IsisAdminState.getTypeString(getIsisSysAdminState().getValue()) +
                "], SysID:[" +
                getIsisSysID() +
                "]";
        }

	public void merge(IsIsElement element) {
		if (element == null)
			return;
		setIsisSysID(element.getIsisSysID());
		setIsisSysAdminState(element.getIsisSysAdminState());
		
		setIsisNodeLastPollTime(element.getIsisNodeCreateTime());
	}

}
