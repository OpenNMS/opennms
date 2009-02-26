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

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class NetworkBuilder {

	OnmsDistPoller m_distPoller;

	OnmsNode m_currentNode;
	
	BeanWrapper m_assetBean;

	OnmsIpInterface m_currentIf;

	OnmsMonitoredService m_currentMonSvc;

	public NetworkBuilder(OnmsDistPoller distPoller) {
		m_distPoller = distPoller;
	}
	
	public NetworkBuilder(String name, String ipAddress) {
	    m_distPoller = new OnmsDistPoller(name, ipAddress);
	}

    /**
     * Totally bogus
     */
    public NetworkBuilder() {
        this("localhost", "127.0.0.1");
    }

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

		public SnmpInterfaceBuilder addSnmpInterface(String ipAddr, int ifIndex) {
		    OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(ipAddr, ifIndex, m_currentNode);
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
    
    public InterfaceBuilder addInterface(String ipAddr, OnmsSnmpInterface snmpInterface) {
        m_currentIf = new OnmsIpInterface(ipAddr, m_currentNode);
        m_currentIf.setSnmpInterface(snmpInterface);
        return new InterfaceBuilder(m_currentIf);
    }

    
	public SnmpInterfaceBuilder addSnmpInterface(String ipAddr, int ifIndex) {
		OnmsSnmpInterface snmpIf = new OnmsSnmpInterface(ipAddr, ifIndex, m_currentNode);
		return new SnmpInterfaceBuilder(snmpIf);

	}

	public OnmsMonitoredService addService(OnmsServiceType serviceType) {
	    if (m_currentIf != null) {
	        m_currentMonSvc = new OnmsMonitoredService(m_currentIf, serviceType);
	        return m_currentMonSvc;
	    } else {
	        m_currentMonSvc = null;
	        return null;
	    }
	}

	public void setDisplayCategory(String displayCategory) {
		m_currentNode.getAssetRecord().setDisplayCategory(displayCategory);
	}
	
	public void setBuilding(String building){
	    m_currentNode.getAssetRecord().setBuilding(building);
	}

	public OnmsNode getCurrentNode() {
		return m_currentNode;
	}

    public void addCategory(OnmsCategory cat) {
        m_currentNode.addCategory(cat);
    }
    
    public void addCategory(String categoryName) {
        addCategory(new OnmsCategory(categoryName));
    }

    public void clearInterface() {
        m_currentIf = null;
        m_currentMonSvc = null;
    }

    public OnmsMonitoredService addService(String serviceName) {
        return addService(new OnmsServiceType(serviceName));
    }

    /**
     * @param name
     * @param value
     */
    public void setAssetAttribute(String name, String value) {
        m_assetBean.setPropertyValue(name, value);
    }

}
