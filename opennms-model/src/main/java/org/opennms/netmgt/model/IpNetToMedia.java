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
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Type;
import org.springframework.core.style.ToStringCreator;

@Entity
@Table(name="ipNetToMedia")
public class IpNetToMedia implements Serializable {
/**
 * ipNetToMediaType OBJECT-TYPE
 *   SYNTAX     INTEGER {
 *               other(1),        -- none of the following
 *               invalid(2),      -- an invalidated mapping
 *               dynamic(3),
 *               static(4)
 *           }
 *  
 * @author antonio
 *
 */

	public enum IpNetToMediaType {
		IPNETTOMEDIA_TYPE_OTHER(1),
		IPNETTOMEDIA_TYPE_INVALID(2),
		IPNETTOMEDIA_TYPE_DYNAMIC(3),
		IPNETTOMEDIA_TYPE_STATIC(4);
		
        private int m_value;

        IpNetToMediaType(int value) {
        	m_value=value;
        }

 	    protected static final Map<Integer, String> s_typeMap = new HashMap<Integer, String>();

        static {
        	s_typeMap.put(1, "other" );
        	s_typeMap.put(2, "invalid" );
        	s_typeMap.put(3, "dynamic" );
        	s_typeMap.put(4, "static" );
        }

        public static String getTypeString(Integer code) {
            if (s_typeMap.containsKey(code))
                    return s_typeMap.get( code);
            return null;
        }
        
        public static IpNetToMediaType get(Integer code) {
            if (code == null) {
                return null;
            }
            switch (code) {
            case 1: 	return IPNETTOMEDIA_TYPE_OTHER;
            case 2: 	return IPNETTOMEDIA_TYPE_INVALID;
            case 3: 	return IPNETTOMEDIA_TYPE_DYNAMIC;
            case 4: 	return IPNETTOMEDIA_TYPE_STATIC;
            default:    return null;
            }
        }
        
        public Integer getValue() {
            return m_value;
        }

		
	}
	
    private static final long serialVersionUID = 7750043250236397014L;

    private Integer m_id;
    
    private InetAddress m_netAddress;
    private String m_physAddress;
    private IpNetToMediaType m_ipNetToMediaType;

    private OnmsNode m_sourceNode;
    private Integer m_sourceIfIndex;

    private Date m_createTime = new Date();
    private Date m_lastPollTime;

    
    /**
     * <p>Constructor for IpNetToMedia.</p>
     */
    public IpNetToMedia() {
    }

    /**
     * Unique identifier for ipInterface.
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")
    public Integer getId() {
        return m_id;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }
    
    @Column(name="netAddress", nullable=false)
    @Type(type="org.opennms.netmgt.model.InetAddressUserType")
    public InetAddress getNetAddress() {
        return m_netAddress;
    }

	public void setNetAddress(InetAddress netAddress) {
		m_netAddress = netAddress;
	}
    
    /**
     * <p>getPhysAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="physAddress", length=32, nullable=false)
    public String getPhysAddress() {
        return m_physAddress;
    }

    /**
     * <p>setPhysAddr</p>
     *
     * @param physAddr a {@link java.lang.String} object.
     */
    public void setPhysAddress(String physAddr) {
        m_physAddress = physAddr;
    }
    
	@Transient
    public IpNetToMediaType getIpNetToMediaType() {
		return m_ipNetToMediaType;
	}

	public void setIpNetToMediaType(IpNetToMediaType ipNetToMediaType) {
		m_ipNetToMediaType = ipNetToMediaType;
	}

    /**
     * <p>getSourceNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="sourceNodeId", nullable=false)
    public OnmsNode getSourceNode() {
        return m_sourceNode;
    }

    /**
     * <p>setSourceNode</p>
     *
     * @param sourceNode a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void setSourceNode(OnmsNode sourceNode) {
        m_sourceNode = sourceNode;
    }

    @Column(name="sourceIfIndex", nullable=false)
    public Integer getSourceIfIndex() {
        return m_sourceIfIndex;
    }
    
    /**
     * <p>setIfIndex</p>
     *
     * @param ifIndex a {@link java.lang.Integer} object.
     */
    public void setSourceIfIndex(Integer ifIndex) {
        m_sourceIfIndex = ifIndex;
    }

    /**
     * <p>getCreateTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createTime", nullable=false)
    public Date getCreateTime() {
        return m_createTime;
    }

    /**
     * <p>setLastPoll</p>
     *
     * @param lastPoll a {@link java.util.Date} object.
     */
    public void setCreateTime(Date lastPoll) {
        m_createTime = lastPoll;
    }


    /**
     * <p>getLastPollTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastPollTime", nullable=false)
    public Date getLastPollTime() {
        return m_lastPollTime;
    }

    /**
     * <p>setLastPoll</p>
     *
     * @param lastPoll a {@link java.util.Date} object.
     */
    public void setLastPollTime(Date lastPoll) {
        m_lastPollTime = lastPoll;
    }

	public void merge(IpNetToMedia element) {
		setSourceNode(element.getSourceNode());
		setSourceIfIndex(element.getSourceIfIndex());
		setLastPollTime(element.getCreateTime());
	}

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
        .append("ipaddr", getNetAddress())
        .append("physaddr", getPhysAddress())
        .append("sourcenode", getSourceNode())
        .append("sourceifindex", getSourceIfIndex())
        .append("createTime", getCreateTime())
        .append("lastPollTime", getLastPollTime())
        .toString();
    }

}
