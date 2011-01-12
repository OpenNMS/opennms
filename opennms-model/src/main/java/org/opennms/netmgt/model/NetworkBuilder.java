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
// Modifications:
//
// 2007 Apr 05: Add the ipInterface reference to an snmpInterface when we create the snmpInterface.  This should possibly be done in SnmpInterface, instead. - dj@opennms.org
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

import java.util.Date;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * <p>NetworkBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NetworkBuilder {

	OnmsDistPoller m_distPoller;

	OnmsNode m_currentNode;
	
	BeanWrapper m_assetBean;

	OnmsIpInterface m_currentIf;

	OnmsArpInterface m_currentAtIf;

	OnmsMonitoredService m_currentMonSvc;

	/**
	 * <p>Constructor for NetworkBuilder.</p>
	 *
	 * @param distPoller a {@link org.opennms.netmgt.model.OnmsDistPoller} object.
	 */
	public NetworkBuilder(OnmsDistPoller distPoller) {
		m_distPoller = distPoller;
	}
	
	/**
	 * <p>Constructor for NetworkBuilder.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param ipAddress a {@link java.lang.String} object.
	 */
	public NetworkBuilder(String name, String ipAddress) {
	    m_distPoller = new OnmsDistPoller(name, ipAddress);
	}

    /**
     * Totally bogus
     */
    public NetworkBuilder() {
        this("localhost", "127.0.0.1");
    }

    /**
     * <p>addNode</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.NetworkBuilder.NodeBuilder} object.
     */
    public NodeBuilder addNode(String label) {
		m_currentNode = new OnmsNode(m_distPoller);
		m_currentNode.setLabel(label);
		m_assetBean = new BeanWrapperImpl(m_currentNode.getAssetRecord());
		return new NodeBuilder(m_currentNode);
	}
    
    public class NodeBuilder {
        OnmsNode m_node;
        
        NodeBuilder(OnmsNode node) {
            m_node = node;
        }
        
        public OnmsNode getNode() {
            return m_node;
        }
        
        public NodeBuilder setId(Integer id) {
            m_node.setId(id);
            return this;
        }
        
        public NodeBuilder setForeignSource(String foreignSource) {
            m_node.setForeignSource(foreignSource);
            return this;
        }
        
        public NodeBuilder setForeignId(String foreignId) {
            m_node.setForeignId(foreignId);
            return this;
        }
        
        public OnmsAssetRecord getAssetRecord() {
            return m_node.getAssetRecord();
        }

        public NodeBuilder setLabelSource(String labelSource) {
            m_node.setLabelSource(labelSource);
            return this;
            
        }

        public NodeBuilder setType(String type) {
            m_node.setType(type);
            return this;
        }
        
        public NodeBuilder setSysObjectId(String sysObjectId) {
            m_node.setSysObjectId(sysObjectId);
            return this;
        }
        
        
    }
    
    /**
     * <p>addInterface</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder} object.
     */
    public InterfaceBuilder addInterface(String ipAddr) {
		m_currentIf = new OnmsIpInterface(ipAddr, m_currentNode);
//        return m_currentIf;
        return new InterfaceBuilder(m_currentIf);
	}

    public class InterfaceBuilder {
		OnmsIpInterface m_iface;

		InterfaceBuilder(OnmsIpInterface iface) {
			m_iface = iface;
		}

		public InterfaceBuilder setIsManaged(String managed) {
			m_iface.setIsManaged(managed);
			return this;
		}

		public InterfaceBuilder setIsSnmpPrimary(String isSnmpPrimary) {
			m_iface.setIsSnmpPrimary(OnmsIpInterface.PrimaryType
					.get(isSnmpPrimary));
			return this;
		}

		public OnmsIpInterface getInterface() {
			return m_iface;
		}

		public SnmpInterfaceBuilder addSnmpInterface(int ifIndex) {
		    OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(m_currentNode, ifIndex);
		    m_iface.setSnmpInterface(snmpIf);
            // TODO: Should this be done in setSnmpInterface?
            snmpIf.getIpInterfaces().add(m_iface);
		    return new SnmpInterfaceBuilder(snmpIf);

		}

		public InterfaceBuilder setId(int id) {
		    m_iface.setId(id);
            return this;
		}
	}
    
    public class AtInterfaceBuilder {
        OnmsArpInterface m_iface;

        AtInterfaceBuilder(OnmsArpInterface iface) {
            m_iface = iface;
        }

        public AtInterfaceBuilder setStatus(char managed) {
            m_iface.setStatus(StatusType.get(managed));
            return this;
        }

        public AtInterfaceBuilder setIfIndex(int ifIndex) {
            m_iface.setIfIndex(ifIndex);
            return this;
        }

        public AtInterfaceBuilder setSourceNode(OnmsNode node) {
            m_iface.setSourceNode(node);
            return this;
        }

        public OnmsArpInterface getInterface() {
            return m_iface;
        }

        public AtInterfaceBuilder setId(int id) {
            m_iface.setId(id);
            return this;
        }

        public AtInterfaceBuilder setLastPollTime(Date timestamp) {
            m_iface.setLastPoll(timestamp);
            return this;
        }
    }
    
    /**
     * <p>addInterface</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param snmpInterface a {@link org.opennms.netmgt.model.OnmsSnmpInterface} object.
     * @return a {@link org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder} object.
     */
    public InterfaceBuilder addInterface(String ipAddr, OnmsSnmpInterface snmpInterface) {
        m_currentIf = new OnmsIpInterface(ipAddr, m_currentNode);
        m_currentIf.setSnmpInterface(snmpInterface);
        return new InterfaceBuilder(m_currentIf);
    }

    /**
     */
    public AtInterfaceBuilder addAtInterface(String ipAddr, String physAddr) {
        m_currentAtIf = new OnmsArpInterface(ipAddr, physAddr, m_currentNode);
        return new AtInterfaceBuilder(m_currentAtIf);
    }

    
	/**
	 * <p>addSnmpInterface</p>
	 * @param ifIndex a int.
	 *
	 * @return a {@link org.opennms.netmgt.model.SnmpInterfaceBuilder} object.
	 */
	public SnmpInterfaceBuilder addSnmpInterface(int ifIndex) {
		OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(m_currentNode, ifIndex);
		return new SnmpInterfaceBuilder(snmpIf);

	}

	/**
	 * <p>addService</p>
	 *
	 * @param serviceType a {@link org.opennms.netmgt.model.OnmsServiceType} object.
	 * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
	 */
	public OnmsMonitoredService addService(OnmsServiceType serviceType) {
	    if (m_currentIf != null) {
	        m_currentMonSvc = new OnmsMonitoredService(m_currentIf, serviceType);
	        return m_currentMonSvc;
	    } else {
	        m_currentMonSvc = null;
	        return null;
	    }
	}

	/**
	 * <p>setDisplayCategory</p>
	 *
	 * @param displayCategory a {@link java.lang.String} object.
	 */
	public void setDisplayCategory(String displayCategory) {
		m_currentNode.getAssetRecord().setDisplayCategory(displayCategory);
	}
	
	/**
	 * <p>setBuilding</p>
	 *
	 * @param building a {@link java.lang.String} object.
	 */
	public void setBuilding(String building){
	    m_currentNode.getAssetRecord().setBuilding(building);
	}

	/**
	 * <p>getCurrentNode</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
	 */
	public OnmsNode getCurrentNode() {
		return m_currentNode;
	}

    /**
     * <p>addCategory</p>
     *
     * @param cat a {@link org.opennms.netmgt.model.OnmsCategory} object.
     */
    public void addCategory(OnmsCategory cat) {
        m_currentNode.addCategory(cat);
    }
    
    /**
     * <p>addCategory</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     */
    public void addCategory(String categoryName) {
        addCategory(new OnmsCategory(categoryName));
    }

    /**
     * <p>clearInterface</p>
     */
    public void clearInterface() {
        m_currentIf = null;
        m_currentMonSvc = null;
    }

    /**
     * <p>addService</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     */
    public OnmsMonitoredService addService(String serviceName) {
        return addService(new OnmsServiceType(serviceName));
    }

    /**
     * <p>setAssetAttribute</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setAssetAttribute(String name, String value) {
        m_assetBean.setPropertyValue(name, value);
    }

}
