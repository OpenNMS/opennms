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
// 2007 Jun 24: Use Java 5 generics. - dj@opennms.org
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
package org.opennms.netmgt.importer.operations;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.IfSnmpCollector;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface.PrimaryType;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public abstract class AbstractSaveOrUpdateOperation extends AbstractImportOperation implements SaveOrUpdateOperation {

	private final OnmsNode m_node;
    private NodeDao m_nodeDao;
    private DistPollerDao m_distPollerDao;
    private OnmsIpInterface m_currentInterface;
    private ServiceTypeDao m_svcTypeDao;
    private CategoryDao m_categoryDao;
    private ThreadLocal<HashMap<String, OnmsServiceType>> m_types;
    private ThreadLocal<HashMap<String, OnmsCategory>> m_categories;
    
    private IfSnmpCollector m_collector;

    public AbstractSaveOrUpdateOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city) {
		this(null, foreignSource, foreignId, nodeLabel, building, city);
	}

	public AbstractSaveOrUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city) {
        m_node = new OnmsNode();
        m_node.setId(nodeId);
		m_node.setLabel(nodeLabel);
		m_node.setLabelSource("U");
		m_node.setType("A");
        m_node.setForeignSource(foreignSource);
        m_node.setForeignId(foreignId);
        m_node.getAssetRecord().setBuilding(building);
        m_node.getAssetRecord().setCity(city);
	}

	public void foundInterface(String ipAddr, Object descr, String snmpPrimary, boolean managed, int status) {
		
		if ("".equals(ipAddr)) {
			log().error("Found interface on node "+m_node.getLabel()+" with an empty ipaddr! Ignoring!");
			// create a bogus OnmsIpInterface and set it to current to services we run across get ignored as well
			m_currentInterface = new OnmsIpInterface();
			return;
		}

        m_currentInterface = new OnmsIpInterface(ipAddr, m_node);
        m_currentInterface.setIsManaged(status == 3 ? "U" : "M");
        m_currentInterface.setIsSnmpPrimary(PrimaryType.get(snmpPrimary));
        //m_currentInterface.setIpStatus(status == 3 ? new Integer(3) : new Integer(1));
        
        if ("P".equals(snmpPrimary)) {
        	try {
        		m_collector = new IfSnmpCollector(InetAddress.getByName(ipAddr));
        	} catch (UnknownHostException e) {
        		log().error("Unable to resolve address of snmpPrimary interface for node "+m_node.getLabel(), e);
        	}
        }
        
        //FIXME: verify this doesn't conflict with constructor.  The constructor already adds this
        //interface to the node.
        m_node.addIpInterface(m_currentInterface);
    }
	
	public void gatherAdditionalData() {
    	updateSnmpData();
	}
	
    public List<Event> persist() {
    	return doPersist();
	}

    protected abstract List<Event> doPersist();

	protected void updateSnmpData() {
		if (m_collector != null) {
            m_collector.run();
        }
		
		updateSnmpDataForNode();
		
		updateSnmpDataForSnmpInterfaces();
		
		for (OnmsIpInterface ipIf : m_node.getIpInterfaces()) {
            resolveIpHostname(ipIf);
            updateSnmpDataForInterface(ipIf);
		}
	}
	
	private void updateSnmpDataForSnmpInterfaces() {
	    if (m_collector != null && m_collector.hasIfTable()) {
            String ipAddress = m_node.getPrimaryInterface().getIpAddress();

            for(IfTableEntry entry : m_collector.getIfTable().getEntries()) {
	            
	            Integer ifIndex = entry.getIfIndex();
	            
	            if (ifIndex == null) continue;
	            
                log().debug("Updating SNMP Interface with ifIndex "+ifIndex);
                
	            // first look to see if an snmpIf was created already
	            OnmsSnmpInterface snmpIf = m_node.getSnmpInterfaceWithIfIndex(ifIndex);
	            
	            if (snmpIf == null) {
	                // if not then create one
                    snmpIf = new OnmsSnmpInterface(ipAddress, ifIndex, m_node);
	            }
	            
	            snmpIf.setIfAlias(m_collector.getIfAlias(ifIndex));
	            snmpIf.setIfName(m_collector.getIfName(ifIndex));
	            snmpIf.setIfType(getIfType(ifIndex));
	            snmpIf.setNetMask(getNetMask(ifIndex));
	            snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
	            snmpIf.setIfDescr(m_collector.getIfDescr(ifIndex));
	            snmpIf.setIfSpeed(m_collector.getIfSpeed(ifIndex));
	            snmpIf.setPhysAddr(m_collector.getPhysAddr(ifIndex));
	            
	        }
	    }
	}

	private void updateSnmpDataForNode() {
        if (m_collector != null && m_collector.hasSystemGroup()) {
            m_node.setSysContact(m_collector.getSystemGroup().getSysContact());
            m_node.setSysDescription(m_collector.getSystemGroup().getSysDescr());
            m_node.setSysLocation(m_collector.getSystemGroup().getSysLocation());
            m_node.setSysObjectId(m_collector.getSystemGroup().getSysObjectID());
        }
	}
	
	protected boolean isSnmpDataForNodeUpToDate() {
		return m_collector != null && m_collector.hasSystemGroup();
	}
	
	protected boolean isSnmpDataForInterfacesUpToDate() {
		return m_collector != null && m_collector.hasIfTable() && m_collector.hasIpAddrTable();
	}

    private void updateSnmpDataForInterface(OnmsIpInterface ipIf) {
    	if (m_collector == null || !m_collector.hasIpAddrTable() || !m_collector.hasIfTable()) {
            return;
        }

    	String ipAddr = ipIf.getIpAddress();
    	log().debug("Creating SNMP info for interface "+ipAddr);

    	InetAddress inetAddr = ipIf.getInetAddress();

    	int ifIndex = m_collector.getIfIndex(inetAddr);
    	if (ifIndex == -1) {
            return;
        }

        // first look to see if an snmpIf was created already
        OnmsSnmpInterface snmpIf = m_node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf == null) {
            // if not then create one
            snmpIf = new OnmsSnmpInterface(ipAddr, new Integer(ifIndex), m_node);
            snmpIf.setIfAlias(m_collector.getIfAlias(ifIndex));
            snmpIf.setIfName(m_collector.getIfName(ifIndex));
            snmpIf.setIfType(getIfType(ifIndex));
            snmpIf.setNetMask(getNetMask(ifIndex));
            snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
            snmpIf.setIfDescr(m_collector.getIfDescr(ifIndex));
            snmpIf.setIfSpeed(m_collector.getIfSpeed(ifIndex));
            snmpIf.setPhysAddr(m_collector.getPhysAddr(ifIndex));
        }
        
        if (ipIf.getIsSnmpPrimary() == PrimaryType.PRIMARY) {
            // make sure the snmpIf has the ipAddr of the primary interface
            snmpIf.setIpAddress(ipAddr);
        }
    	
    	ipIf.setSnmpInterface(snmpIf);

    	//FIXME: Improve OpenNMS to provide these values
    	// ifOperStatus

	}

	private Integer getAdminStatus(int ifIndex) {
		int adminStatus = m_collector.getAdminStatus(ifIndex);
		return (adminStatus == -1 ? null : new Integer(adminStatus));
	}

	private Integer getIfType(int ifIndex) {
		int ifType = m_collector.getIfType(ifIndex);
		return (ifType == -1 ? null : new Integer(ifType));
	}

	private String getNetMask(int ifIndex) {
		InetAddress[] ifAddressAndMask = m_collector.getIfAddressAndMask(ifIndex);
		if (ifAddressAndMask != null && ifAddressAndMask.length > 1 && ifAddressAndMask[1] != null) {
            return ifAddressAndMask[1].getHostAddress();
        }
		return null;
	}

	private void resolveIpHostname(OnmsIpInterface ipIf) {
		ipIf.setIpHostName(ipIf.getIpAddress());
//
//     DON'T DO THIS SINCE DNS DOESN'T RELIABLY AVOID HANGING
//
//    	log().info("Resolving Hostname for "+ipIf.getIpAddress());
//		try {
//			InetAddress addr = InetAddress.getByName(ipIf.getIpAddress());
//			ipIf.setIpHostName(addr.getHostName());
//		} catch (Exception e) {
//			if (ipIf.getIpHostName() == null)
//				ipIf.setIpHostName(ipIf.getIpAddress());
//		}
	}

	public void foundMonitoredService(String serviceName) {
        OnmsServiceType svcType = getServiceType(serviceName);
        OnmsMonitoredService service = new OnmsMonitoredService(m_currentInterface, svcType);
        service.setStatus("A");
        m_currentInterface.getMonitoredServices().add(service);
    
    }

    public void foundCategory(String name) {
        OnmsCategory category = getCategory(name);
        m_node.getCategories().add(category);
    }

    public void foundAsset(String name, String value) {
        BeanWrapper w = new BeanWrapperImpl(m_node.getAssetRecord());
        w.setPropertyValue(name, value);
    }
    
    private OnmsServiceType getServiceType(String serviceName) {
        preloadExistingTypes();
        OnmsServiceType type = getTypes().get(serviceName);
        if (type == null) {
            type = m_svcTypeDao.findByName(serviceName);
            
            if (type == null) {
                type = new OnmsServiceType(serviceName);
                m_svcTypeDao.save(type);
            }
            
            getTypes().put(serviceName, type);
        }
        return type;
    }
    
    private void preloadExistingTypes() {
        
        if (getTypes() == null) {
            setTypes(new HashMap<String, OnmsServiceType>());
            for (OnmsServiceType svcType : m_svcTypeDao.findAll()) {
                getTypes().put(svcType.getName(), svcType);
            }
        }
    }
    
    private void preloadExistingCategories() {
        if (getCategories() == null) {
            setCategories(new HashMap<String, OnmsCategory>());
            for (OnmsCategory category : m_categoryDao.findAll()) {
                getCategories().put(category.getName(), category);
            }
        }
    }

    protected OnmsNode getNode() {
        return m_node;
    }
    
    protected NodeDao getNodeDao() {
        return m_nodeDao;
    }
    
    protected DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    private OnmsCategory getCategory(String name) {
        preloadExistingCategories();
        
        OnmsCategory category = getCategories().get(name);
        if (category == null) {    
            category = m_categoryDao.findByName(name);
            if (category == null) {
                category = new OnmsCategory(name);
                m_categoryDao.save(category);
            }
            getCategories().put(category.getName(), category);
        }
        return category;

    }

    protected Map<String, OnmsIpInterface> getIpAddrToInterfaceMap(OnmsNode imported) {
        Map<String, OnmsIpInterface> ipAddrToIface = new HashMap<String, OnmsIpInterface>();
        for (OnmsIpInterface iface : imported.getIpInterfaces()) {
            ipAddrToIface.put(iface.getIpAddress(), iface);
        }
        return ipAddrToIface;
    }

    private HashMap<String, OnmsServiceType> getTypes() {
        return m_types.get();
    }

    private void setTypes(HashMap<String, OnmsServiceType> types) {
        m_types.set(types);
    }

    private void setCategories(HashMap<String, OnmsCategory> categories) {
        m_categories.set(categories);
    }

    private HashMap<String, OnmsCategory> getCategories() {
        return m_categories.get();
    }

    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public void setServiceTypeDao(ServiceTypeDao svcTypeDao) {
        m_svcTypeDao = svcTypeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }
    
    public void setTypeCache(ThreadLocal<HashMap<String, OnmsServiceType>> typeCache) {
        m_types = typeCache;
    }
    
    public void setCategoryCache(ThreadLocal<HashMap<String, OnmsCategory>> categoryCache) {
        m_categories = categoryCache;
    }

    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

	public boolean nullSafeEquals(Object o1, Object o2) {
	    return (o1 == null ? o2 == null : o1.equals(o2));
	}

}
