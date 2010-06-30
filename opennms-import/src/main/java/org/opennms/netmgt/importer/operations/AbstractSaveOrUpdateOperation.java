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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.IfSnmpCollector;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.capsd.snmp.IpAddrTableEntry;
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
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;
import org.opennms.netmgt.xml.event.Event;

/**
 * <p>Abstract AbstractSaveOrUpdateOperation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractSaveOrUpdateOperation extends AbstractImportOperation implements SaveOrUpdateOperation {

	private OnmsNode m_node;
    private NodeDao m_nodeDao;
    private DistPollerDao m_distPollerDao;
    private OnmsIpInterface m_currentInterface;
    private ServiceTypeDao m_svcTypeDao;
    private CategoryDao m_categoryDao;
    private ThreadLocal<HashMap<String, OnmsServiceType>> m_types;
    private ThreadLocal<HashMap<String, OnmsCategory>> m_categories;
    
    IfSnmpCollector m_collector;
    
    protected Boolean m_nonIpInterfaces;
    protected String m_nonIpSnmpPrimary;

    /**
     * <p>Constructor for AbstractSaveOrUpdateOperation.</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @param nonIpInterfaces a {@link java.lang.Boolean} object.
     * @param nonIpSnmpPrimary a {@link java.lang.String} object.
     */
    public AbstractSaveOrUpdateOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city,
            Boolean nonIpInterfaces, String nonIpSnmpPrimary) {
		this(null, foreignSource, foreignId, nodeLabel, building, city, nonIpInterfaces, nonIpSnmpPrimary);
	}

	/**
	 * <p>Constructor for AbstractSaveOrUpdateOperation.</p>
	 *
	 * @param nodeId a {@link java.lang.Integer} object.
	 * @param foreignSource a {@link java.lang.String} object.
	 * @param foreignId a {@link java.lang.String} object.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param building a {@link java.lang.String} object.
	 * @param city a {@link java.lang.String} object.
	 * @param nonIpInterfaces a {@link java.lang.Boolean} object.
	 * @param nonIpSnmpPrimary a {@link java.lang.String} object.
	 */
	public AbstractSaveOrUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city,
	        Boolean nonIpInterfaces, String nonIpSnmpPrimary) {
        m_node = new OnmsNode();
        m_node.setId(nodeId);
		m_node.setLabel(nodeLabel);
		m_node.setLabelSource("U");
		m_node.setType("A");
        m_node.setForeignSource(foreignSource);
        m_node.setForeignId(foreignId);
        m_node.getAssetRecord().setBuilding(building);
        m_node.getAssetRecord().setCity(city);
        m_nonIpInterfaces = nonIpInterfaces;
        m_nonIpSnmpPrimary = nonIpSnmpPrimary;
	}

	/** {@inheritDoc} */
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
        if (log().isDebugEnabled()) log().debug("foundInterface: Set snmpPrimary to '" + m_nonIpSnmpPrimary + "' for ipInterface with ifIndex " + m_currentInterface.getIfIndex());
        m_currentInterface.setIpStatus(status == 3 ? new Integer(3) : new Integer(1));
        
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
	
	/**
	 * <p>gatherAdditionalData</p>
	 */
	public void gatherAdditionalData() {
    	updateSnmpData();
	}
	
    /**
     * <p>persist</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Event> persist() {
    	return doPersist();
	}

    /**
     * <p>doPersist</p>
     *
     * @return a {@link java.util.List} object.
     */
    protected abstract List<Event> doPersist();

	/**
	 * <p>updateSnmpData</p>
	 */
	protected void updateSnmpData() {
		if (m_collector != null) 
			m_collector.run();
		
		updateSnmpDataForNode();

		updateSnmpDataForSnmpInterfaces();

		for (OnmsIpInterface ipIf : m_node.getIpInterfaces()) {
            resolveIpHostname(ipIf);
            updateSnmpDataForIpInterface(ipIf);
		}
	}
	
	private void updateSnmpDataForSnmpInterfaces() {
	    Set<Integer> ipIfIndexes = new HashSet<Integer>();
	    
	    if (m_collector == null || !m_collector.hasIfTable()) {
	        log().debug("Not finding non-IP interfaces for this node because the node does not support SNMP (wrong community string?) or lacks an ifTable (lame SNMP agent?)");
	        return;
	    }
	    
	    if (!m_nonIpInterfaces) {
	        log().debug("Not finding non-IP interfaces for this operation because 'non-ip-interfaces' is false");
	        return;
	    }
	    
	    for (IpAddrTableEntry ipadEnt : m_collector.getIpAddrTable().getEntries()) {
	        ipIfIndexes.add(ipadEnt.getIpAdEntIfIndex());
	    }
	    
        for(IfTableEntry entry : m_collector.getIfTable().getEntries()) {
            
            Integer ifIndex = entry.getIfIndex();
            if (ipIfIndexes.contains(ifIndex)) {
                if (log().isDebugEnabled()) log().debug("Not creating a non-IP interface for ifIndex " + ifIndex + " because it appears in the ipAddrTable");
                continue;
            }
            
            if (ifIndex == null) continue;
            
            log().debug("Updating SNMP Interface with ifIndex "+ifIndex);
            
            // first look to see if an snmpIf was created already
            OnmsSnmpInterface newSnmpIf = m_node.getSnmpInterfaceWithIfIndex(ifIndex);
            
            if (newSnmpIf == null) {
                // if not then create one
                newSnmpIf = new OnmsSnmpInterface("0.0.0.0", ifIndex, m_node);
            }
            
            newSnmpIf.setIfAlias(m_collector.getIfAlias(ifIndex));
            newSnmpIf.setIfName(m_collector.getIfName(ifIndex));
            newSnmpIf.setIfType(getIfType(ifIndex));
            newSnmpIf.setNetMask(getNetMask(ifIndex));
            newSnmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
            newSnmpIf.setIfDescr(m_collector.getIfDescr(ifIndex));
            newSnmpIf.setIfSpeed(m_collector.getInterfaceSpeed(ifIndex));
            newSnmpIf.setPhysAddr(m_collector.getPhysAddr(ifIndex));

            OnmsIpInterface newIpIf = null;
            for (OnmsIpInterface existingIpIf : m_node.getIpInterfaces()) {
                if (existingIpIf.getIfIndex() != null && existingIpIf.getIfIndex() == newSnmpIf.getIfIndex()) {
                    newIpIf = existingIpIf;
                    break;
                }
            }
            if (newIpIf == null) { 
                newIpIf = new OnmsIpInterface("0.0.0.0", m_node);
            }
            
            newIpIf.setSnmpInterface(newSnmpIf);
            newIpIf.setIpStatus(2);
            newIpIf.setIsManaged("U");
            newIpIf.setIsSnmpPrimary(CollectionType.get(m_nonIpSnmpPrimary));
            if (log().isDebugEnabled()) log().debug("updateSnmpDataForSnmpInterfaces: Set snmpPrimary to '" + m_nonIpSnmpPrimary + "' for ipInterface with ifIndex " + newIpIf.getIfIndex());
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
	
	/**
	 * <p>isSnmpDataForNodeUpToDate</p>
	 *
	 * @return a boolean.
	 */
	protected boolean isSnmpDataForNodeUpToDate() {
		return m_collector != null && m_collector.hasSystemGroup();
	}
	
	/**
	 * <p>isSnmpDataForInterfacesUpToDate</p>
	 *
	 * @return a boolean.
	 */
	protected boolean isSnmpDataForInterfacesUpToDate() {
		return m_collector != null && m_collector.hasIfTable() && m_collector.hasIpAddrTable();
	}

    private void updateSnmpDataForIpInterface(OnmsIpInterface ipIf) {
    	if (m_collector == null || !m_collector.hasIpAddrTable() || !m_collector.hasIfTable()) return;

    	String ipAddr = ipIf.getIpAddress();
    	log().debug("Creating SNMP info for interface "+ipAddr);

    	InetAddress inetAddr = ipIf.getInetAddress();

    	int ifIndex = m_collector.getIfIndex(inetAddr);
    	if (ifIndex == -1) return;

        // first look to see if an snmpIf was created already
        OnmsSnmpInterface snmpIf = m_node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf == null) {
            // if not then create one
            snmpIf = new OnmsSnmpInterface(ipAddr, new Integer(ifIndex), m_node);
        }
        
        snmpIf.setIfAlias(m_collector.getIfAlias(ifIndex));
        snmpIf.setIfName(m_collector.getIfName(ifIndex));
        snmpIf.setIfType(getIfType(ifIndex));
        snmpIf.setNetMask(getNetMask(ifIndex));
        snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
        snmpIf.setIfDescr(m_collector.getIfDescr(ifIndex));
        snmpIf.setIfSpeed(m_collector.getInterfaceSpeed(ifIndex));
        snmpIf.setPhysAddr(m_collector.getPhysAddr(ifIndex));
        
        if (ipIf.getIsSnmpPrimary() == CollectionType.PRIMARY) {
            // make sure the snmpIf has the ipAddr of the primary interface
            snmpIf.setIpAddress(ipAddr);
        }
    	
    	ipIf.setSnmpInterface(snmpIf);

    	//FIXME: Improve OpenNMS to provide these values
    	// ifOperStatus

	}

	/**
	 * <p>getAdminStatus</p>
	 *
	 * @param ifIndex a int.
	 * @return a {@link java.lang.Integer} object.
	 */
	protected Integer getAdminStatus(int ifIndex) {
		int adminStatus = m_collector.getAdminStatus(ifIndex);
		return (adminStatus == -1 ? null : new Integer(adminStatus));
	}

	/**
	 * <p>getIfType</p>
	 *
	 * @param ifIndex a int.
	 * @return a {@link java.lang.Integer} object.
	 */
	protected Integer getIfType(int ifIndex) {
		int ifType = m_collector.getIfType(ifIndex);
		return (ifType == -1 ? null : new Integer(ifType));
	}

	/**
	 * <p>getNetMask</p>
	 *
	 * @param ifIndex a int.
	 * @return a {@link java.lang.String} object.
	 */
	protected String getNetMask(int ifIndex) {
		InetAddress[] ifAddressAndMask = m_collector.getIfAddressAndMask(ifIndex);
		if (ifAddressAndMask != null && ifAddressAndMask.length > 1 && ifAddressAndMask[1] != null)
			return ifAddressAndMask[1].getHostAddress();
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

	/** {@inheritDoc} */
	public void foundMonitoredService(String serviceName) {
        OnmsServiceType svcType = getServiceType(serviceName);
        OnmsMonitoredService service = new OnmsMonitoredService(m_currentInterface, svcType);
        service.setStatus("A");
        m_currentInterface.getMonitoredServices().add(service);
    
    }

    /** {@inheritDoc} */
    public void foundCategory(String name) {
        OnmsCategory category = getCategory(name);
        m_node.getCategories().add(category);
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
            for (Iterator<OnmsServiceType> it = m_svcTypeDao.findAll().iterator(); it.hasNext();) {
                OnmsServiceType svcType = it.next();
                getTypes().put(svcType.getName(), svcType);
            }
        }
    }
    
    private void preloadExistingCategories() {
        if (getCategories() == null) {
            setCategories(new HashMap<String, OnmsCategory>());
            for(Iterator<OnmsCategory> it = m_categoryDao.findAll().iterator(); it.hasNext();) {
                OnmsCategory category = it.next();
                getCategories().put(category.getName(), category);
            }
        }
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    protected OnmsNode getNode() {
        return m_node;
    }
    
    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    protected NodeDao getNodeDao() {
        return m_nodeDao;
    }
    
    /**
     * <p>getDistPollerDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.DistPollerDao} object.
     */
    protected DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }

    private OnmsCategory getCategory(String name) {
        preloadExistingCategories();
        
        OnmsCategory category = (OnmsCategory)getCategories().get(name);
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

    /**
     * <p>getIpAddrToInterfaceMap</p>
     *
     * @param imported a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link java.util.Map} object.
     */
    protected Map<String, OnmsIpInterface> getIpAddrToInterfaceMap(OnmsNode imported) {
        Map<String, OnmsIpInterface> ipAddrToIface = new HashMap<String, OnmsIpInterface>();
        for (OnmsIpInterface iface : imported.getIpInterfaces()) {
            if (! m_nonIpInterfaces) {
                // If we're not doing non-IP interfaces, include every ipInterface
                ipAddrToIface.put(iface.getIpAddress(), iface);
            } else if (! "0.0.0.0".equals(iface.getIpAddress())) {
                // Otherwise include only non-zero ones
                ipAddrToIface.put(iface.getIpAddress(), iface);
            }
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

    /**
     * <p>getCategoryDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    /**
     * <p>setServiceTypeDao</p>
     *
     * @param svcTypeDao a {@link org.opennms.netmgt.dao.ServiceTypeDao} object.
     */
    public void setServiceTypeDao(ServiceTypeDao svcTypeDao) {
        m_svcTypeDao = svcTypeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>setDistPollerDao</p>
     *
     * @param distPollerDao a {@link org.opennms.netmgt.dao.DistPollerDao} object.
     */
    public void setDistPollerDao(DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }
    
    /**
     * <p>setTypeCache</p>
     *
     * @param typeCache a {@link java.lang.ThreadLocal} object.
     */
    public void setTypeCache(ThreadLocal<HashMap<String, OnmsServiceType>> typeCache) {
        m_types = typeCache;
    }
    
    /**
     * <p>setCategoryCache</p>
     *
     * @param categoryCache a {@link java.lang.ThreadLocal} object.
     */
    public void setCategoryCache(ThreadLocal<HashMap<String, OnmsCategory>> categoryCache) {
        m_categories = categoryCache;
    }
    
    /**
     * <p>setNonIpInterfaces</p>
     *
     * @param nonIpInterfaces a {@link java.lang.Boolean} object.
     */
    public void setNonIpInterfaces(Boolean nonIpInterfaces) {
        m_nonIpInterfaces = nonIpInterfaces;
    }
    
    /**
     * <p>getNonIpInterfaces</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean getNonIpInterfaces() {
        return m_nonIpInterfaces;
    }
    
    /**
     * <p>setNonIpSnmpPrimary</p>
     *
     * @param nonIpSnmpPrimary a {@link java.lang.String} object.
     */
    public void setNonIpSnmpPrimary(String nonIpSnmpPrimary) {
        m_nonIpSnmpPrimary = nonIpSnmpPrimary;
    }
    
    /**
     * <p>getNonIpSnmpPrimary</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNonIpSnmpPrimary() {
        return m_nonIpSnmpPrimary;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    protected Category log() {
        return ThreadCategory.getInstance(getClass());
    }

	/**
	 * <p>nullSafeEquals</p>
	 *
	 * @param o1 a {@link java.lang.Object} object.
	 * @param o2 a {@link java.lang.Object} object.
	 * @return a boolean.
	 */
	public boolean nullSafeEquals(Object o1, Object o2) {
	    return (o1 == null ? o2 == null : o1.equals(o2));
	}

}
