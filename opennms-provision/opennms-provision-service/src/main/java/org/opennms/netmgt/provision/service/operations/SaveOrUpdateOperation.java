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
package org.opennms.netmgt.provision.service.operations;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.provision.service.DefaultProvisionService;

public abstract class SaveOrUpdateOperation extends ImportOperation {

    private OnmsNode m_node;
    private OnmsIpInterface m_currentInterface;
    
    private ScanManager m_scanManager;
    
    public SaveOrUpdateOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city, DefaultProvisionService provisionService) {
		this(null, foreignSource, foreignId, nodeLabel, building, city, provisionService);
	}

	public SaveOrUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city, DefaultProvisionService provisionService) {
	    super(provisionService);
	    
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
	
	public ScanManager getScanManager() {
	    return m_scanManager;
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
        m_currentInterface.setIsSnmpPrimary(CollectionType.get(snmpPrimary));
        m_currentInterface.setIpStatus(status == 3 ? new Integer(3) : new Integer(1));
        
        if ("P".equals(snmpPrimary)) {
        }

        try {
            m_scanManager = new ScanManager(InetAddress.getByName(ipAddr));
        } catch (UnknownHostException e) {
            log().error("Unable to resolve address of snmpPrimary interface for node "+m_node.getLabel(), e);
        }
        
        
        //FIXME: verify this doesn't conflict with constructor.  The constructor already adds this
        //interface to the node.
        m_node.addIpInterface(m_currentInterface);
    }
	
	public void gatherAdditionalData() {
    	updateSnmpData();
	}
	
    protected void updateSnmpData() {
        m_scanManager.updateSnmpData(m_node);
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

    private OnmsServiceType getServiceType(String serviceName) {
        preloadExistingTypes();
        OnmsServiceType type = getTypes().get(serviceName);
        if (type == null) {
            type = getServiceTypeDao().findByName(serviceName);
            
            if (type == null) {
                type = new OnmsServiceType(serviceName);
                getServiceTypeDao().save(type);
            }
            
            getTypes().put(serviceName, type);
        }
        return type;
    }
    
    private void preloadExistingTypes() {
        
        if (getTypes() == null) {
            setTypes(new HashMap<String, OnmsServiceType>());
            for (Iterator<OnmsServiceType> it = getServiceTypeDao().findAll().iterator(); it.hasNext();) {
                OnmsServiceType svcType = it.next();
                getTypes().put(svcType.getName(), svcType);
            }
        }
    }
    
    private void preloadExistingCategories() {
        if (getCategories() == null) {
            setCategories(new HashMap<String, OnmsCategory>());
            for(Iterator<OnmsCategory> it = getCategoryDao().findAll().iterator(); it.hasNext();) {
                OnmsCategory category = it.next();
                getCategories().put(category.getName(), category);
            }
        }
    }

    protected OnmsNode getNode() {
        return m_node;
    }
    
    protected NodeDao getNodeDao() {
        return getProvisionService().getNodeDao();
    }
    
    protected DistPollerDao getDistPollerDao() {
        return getProvisionService().getDistPollerDao();
    }

    private OnmsCategory getCategory(String name) {
        preloadExistingCategories();
        
        OnmsCategory category = (OnmsCategory)getCategories().get(name);
        if (category == null) {    
            category = getCategoryDao().findByName(name);
            if (category == null) {
                category = new OnmsCategory(name);
                getCategoryDao().save(category);
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
        return getTypeCache().get();
    }

    private void setTypes(HashMap<String, OnmsServiceType> types) {
        getTypeCache().set(types);
    }

    private void setCategories(HashMap<String, OnmsCategory> categories) {
        getCategoryCache().set(categories);
    }

    private HashMap<String, OnmsCategory> getCategories() {
        return getCategoryCache().get();
    }

    public CategoryDao getCategoryDao() {
        return getProvisionService().getCategoryDao();
    }

    public ThreadLocal<HashMap<String, OnmsServiceType>> getTypeCache() {
        return getProvisionService().getTypeCache();
    }
    
	public boolean nullSafeEquals(Object o1, Object o2) {
	    return (o1 == null ? o2 == null : o1.equals(o2));
	}

    /**
     * @return the svcTypeDao
     */
    private ServiceTypeDao getServiceTypeDao() {
        return getProvisionService().getServiceTypeDao();
    }

    /**
     * @return the categories
     */
    private ThreadLocal<HashMap<String, OnmsCategory>> getCategoryCache() {
        return getProvisionService().getCategoryCache();
    }

}
