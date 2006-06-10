//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.model;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.core.style.ToStringCreator;


/** 
 *        @hibernate.class
 *         table="ipinterface"
 *     
*/
public class OnmsIpInterface extends OnmsEntity implements Serializable {
	
	public static class CollectionType implements Comparable {
		private static final char[] s_order = {'N', 'C', 'S', 'P' };
		char m_collType;
		private CollectionType(char collType) {
			m_collType = collType;
		}
		
		public int compareTo(Object o) {
			CollectionType collType = (CollectionType)o;
			return getIndex(m_collType) - getIndex(collType.m_collType);
		}
		
		private static int getIndex(char code) {
			for (int i = 0; i < s_order.length; i++) {
				if (s_order[i] == code) return i;
			}
			throw new IllegalArgumentException("illegal collType code '"+code+"'");
		}
		
		public boolean equals(Object o) {
			if (o instanceof CollectionType) {
				return m_collType == ((CollectionType)o).m_collType;
			}
			return false;
		}
		
		public int hashCode() {
			return toString().hashCode();
		}
		
		public String toString() {
			return String.valueOf(m_collType);
		}
        
        public boolean isLessThan(CollectionType collType) {
            return compareTo(collType) < 0;
        }
        
        public boolean isGreaterThan(CollectionType collType) {
            return compareTo(collType) > 0;
        }
		
		public CollectionType max(CollectionType collType) {
            return this.isLessThan(collType) ? collType : this;
		}

		public CollectionType min(CollectionType collType) {
            return this.isLessThan(collType) ? this : collType;
 		}

		public static CollectionType get(char code) {
			switch (code) {
			case 'P': return PRIMARY;
			case 'S': return SECONDARY;
			case 'C': return COLLECT;
			case 'N': return NO_COLLECT;
			default:
				throw new IllegalArgumentException("Connot create collType from code "+code);
			}
		}
		
		public static CollectionType get(String code) {
			code = code.trim();
			if (code == null || code.length() < 1)
				return NO_COLLECT;
			else if (code.length() > 1)
				throw new IllegalArgumentException("Cannot convert string "+code+" to a collType");
			else
				return get(code.charAt(0));
		}
		
		public static CollectionType PRIMARY = new CollectionType('P');
		public static CollectionType SECONDARY = new CollectionType('S');
		public static CollectionType COLLECT = new CollectionType('C');
		public static CollectionType NO_COLLECT = new CollectionType('N');
		
		
	}

    private static final long serialVersionUID = 7750043250236397014L;
    
    private Integer m_id;

    private String m_ipAddress;
    
    private Integer m_ifIndex;

    private String m_ipHostName;

    private String m_isManaged;

    private Integer m_ipStatus;

    private Date m_ipLastCapsdPoll;

    private CollectionType m_isSnmpPrimary = CollectionType.NO_COLLECT;

    private OnmsNode m_node;
    
    private Set m_monitoredServices = new HashSet();
    
    public OnmsIpInterface() {
    }
    
    /** minimal constructor */
    public OnmsIpInterface(String ipAddr, OnmsNode node) {
        this.m_ipAddress = ipAddr;
        this.m_node = node;
        node.getIpInterfaces().add(this);
    }
    
    /**
     * Unique identifier for ipInterface.
     * 
     * @hibernate.id generator-class="native" column="id"
     * @hibernate.generator-param name="sequence" value="ipIfNxtId"
     *         
     */
    public Integer getId() {
        return m_id;
    }

    public void setId(Integer id) {
        m_id = id;
    }


    
    /** 
     *                @hibernate.property
     *                 column="ipaddr"
     *                 length="16"
     *             
     */
    public String getIpAddress() {
        return this.m_ipAddress;
    }

    public void setIpAddress(String ipaddr) {
        this.m_ipAddress = ipaddr;
    }
    
    /** 
     *                @hibernate.property
     *                 column="ifindex"
     *             
     */
    public Integer getIfIndex() {
        return this.m_ifIndex;
    }

    public void setIfIndex(Integer ifindex) {
        this.m_ifIndex = ifindex;
    }

    /** 
     *                @hibernate.property
     *                 column="iphostname"
     *                 length="256"
     *             
     */
    public String getIpHostName() {
        return this.m_ipHostName;
    }

    public void setIpHostName(String iphostname) {
        this.m_ipHostName = iphostname;
    }

    /** 
     *                @hibernate.property
     *                 column="ismanaged"
     *                 length="1"
     *             
     */
    public String getIsManaged() {
        return this.m_isManaged;
    }

    public void setIsManaged(String ismanaged) {
        this.m_isManaged = ismanaged;
    }

    /** 
     *                @hibernate.property
     *                 column="ipstatus"
     *                 length="4"
     *             
     */
    public Integer getIpStatus() {
        return this.m_ipStatus;
    }

    public void setIpStatus(Integer ipstatus) {
        this.m_ipStatus = ipstatus;
    }

    /** 
     *                @hibernate.property
     *                 column="iplastcapsdpoll"
     *                 length="8"
     *             
     */
    public Date getIpLastCapsdPoll() {
        return this.m_ipLastCapsdPoll;
    }

    public void setIpLastCapsdPoll(Date iplastcapsdpoll) {
        this.m_ipLastCapsdPoll = iplastcapsdpoll;
    }

    /** 
     *                @hibernate.property
     *                 column="issnmpprimary"
     *                 length="1"
     *             
     */
    public CollectionType getIsSnmpPrimary() {
        return this.m_isSnmpPrimary;
    }

    public void setIsSnmpPrimary(CollectionType issnmpprimary) {
        this.m_isSnmpPrimary = issnmpprimary;
    }

    /** 
     *            @hibernate.many-to-one
     *             not-null="true"
     *            @hibernate.column name="nodeid"         
     *         
     */
    public org.opennms.netmgt.model.OnmsNode getNode() {
        return this.m_node;
    }

    public void setNode(org.opennms.netmgt.model.OnmsNode node) {
        this.m_node = node;
    }
    
    /** 
     * The interfaces on this node
     * 
     * @hibernate.set lazy="true" inverse="true" cascade="all-delete-orphan"
     * @hibernate.key column="ipIfId"
     * @hibernate.one-to-many class="org.opennms.netmgt.model.OnmsMonitoredService"
     * 
     */
    public Set getMonitoredServices() {
        return m_monitoredServices ;
    }

    public void setMonitoredServices(Set ifServices) {
        m_monitoredServices = ifServices;
    }

    public String toString() {
        return new ToStringCreator(this)
            .append("ipaddr", getIpAddress())
            .append("ifindex", getIfIndex())
            .append("iphostname", getIpHostName())
            .append("ismanaged", getIsManaged())
            .append("ipstatus", getIpStatus())
            .append("iplastcapsdpoll", getIpLastCapsdPoll())
            .append("issnmpprimary", getIsSnmpPrimary())
            .toString();
    }

	public void visit(EntityVisitor visitor) {
		visitor.visitIpInterface(this);
		
		for (Iterator it = getMonitoredServices().iterator(); it.hasNext();) {
			OnmsMonitoredService monSvc = (OnmsMonitoredService) it.next();
			monSvc.visit(visitor);
		}
		
		visitor.visitIpInterfaceComplete(this);
	}

	public InetAddress getInetAddress() {
		String ipAddr = getIpAddress();
		if (ipAddr == null) return null;
		
		InetAddress addr = null;
		try {
			String hostName = getIpHostName() == null ? ipAddr : getIpHostName();
			addr = InetAddress.getByName(ipAddr);
			addr = InetAddress.getByAddress(hostName, addr.getAddress());
		} catch (UnknownHostException e) {
			// this can't happen here
		}
		return addr;
	}

}
