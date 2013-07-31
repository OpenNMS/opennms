/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.importer.operations;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.capsd.IfSnmpCollector;
import org.opennms.netmgt.capsd.snmp.IfTableEntry;
import org.opennms.netmgt.importer.config.types.InterfaceSnmpPrimaryType;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * <p>Abstract AbstractSaveOrUpdateOperation class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractSaveOrUpdateOperation extends AbstractImportOperation implements SaveOrUpdateOperation {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSaveOrUpdateOperation.class);


	private final OnmsNode m_node;
    private NodeDao m_nodeDao;
    private DistPollerDao m_distPollerDao;
    private OnmsIpInterface m_currentInterface;
    private ServiceTypeDao m_svcTypeDao;
    private CategoryDao m_categoryDao;
    private ThreadLocal<HashMap<String, OnmsServiceType>> m_types;
    private ThreadLocal<HashMap<String, OnmsCategory>> m_categories;
    
    private IfSnmpCollector m_collector;

    /**
     * <p>Constructor for AbstractSaveOrUpdateOperation.</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     */
    public AbstractSaveOrUpdateOperation(final String foreignSource, final String foreignId, final String nodeLabel, final String building, final String city) {
		this(null, foreignSource, foreignId, nodeLabel, building, city);
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
	 */
	public AbstractSaveOrUpdateOperation(final Integer nodeId, final String foreignSource, final String foreignId, final String nodeLabel, final String building, final String city) {
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

	/** {@inheritDoc} */
        @Override
	public void foundInterface(final String ipAddr, final Object descr, final InterfaceSnmpPrimaryType snmpPrimary, final boolean managed, final int status) {
		
		if ("".equals(ipAddr)) {
			LOG.error("Found interface on node {} with an empty ipaddr! Ignoring!", m_node.getLabel());
			// create a bogus OnmsIpInterface and set it to current to services we run across get ignored as well
			m_currentInterface = new OnmsIpInterface();
			return;
		}

        m_currentInterface = new OnmsIpInterface(ipAddr, m_node);
        m_currentInterface.setIsManaged(status == 3 ? "U" : "M");
        m_currentInterface.setIsSnmpPrimary(PrimaryType.get(snmpPrimary.toString()));
        //m_currentInterface.setIpStatus(status == 3 ? new Integer(3) : new Integer(1));
        
        if (InterfaceSnmpPrimaryType.P.equals(snmpPrimary)) {
        	final InetAddress addr = InetAddressUtils.addr(ipAddr);
        	if (addr == null) {
        		LOG.error("Unable to resolve address of snmpPrimary interface for node {}", m_node.getLabel());
        	}
    		m_collector = new IfSnmpCollector(addr);
        }
        
        //FIXME: verify this doesn't conflict with constructor.  The constructor already adds this
        //interface to the node.
        m_node.addIpInterface(m_currentInterface);
    }
	
	/**
	 * <p>gatherAdditionalData</p>
	 */
        @Override
	public void gatherAdditionalData() {
    	updateSnmpData();
	}
	
    /**
     * <p>persist</p>
     *
     * @return a {@link java.util.List} object.
     */
        @Override
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

            for(IfTableEntry entry : m_collector.getIfTable()) {
	            
	            Integer ifIndex = entry.getIfIndex();
	            
	            if (ifIndex == null) continue;
	            
                LOG.debug("Updating SNMP Interface with ifIndex {}", ifIndex);
                
	            // first look to see if an snmpIf was created already
	            OnmsSnmpInterface snmpIf = m_node.getSnmpInterfaceWithIfIndex(ifIndex);
	            
	            if (snmpIf == null) {
	                // if not then create one
                    snmpIf = new OnmsSnmpInterface(m_node, ifIndex);
	            }
	            
	            snmpIf.setIfAlias(m_collector.getIfAlias(ifIndex));
	            snmpIf.setIfName(m_collector.getIfName(ifIndex));
	            snmpIf.setIfType(getIfType(ifIndex));
	            snmpIf.setNetMask(getNetMask(ifIndex));
	            snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
	            snmpIf.setIfDescr(m_collector.getIfDescr(ifIndex));
	            snmpIf.setIfSpeed(m_collector.getInterfaceSpeed(ifIndex));
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

    private void updateSnmpDataForInterface(OnmsIpInterface ipIf) {
    	if (m_collector == null || !m_collector.hasIpAddrTable() || !m_collector.hasIfTable()) {
            return;
        }

    	final InetAddress inetAddr = ipIf.getIpAddress();
    	final String ipAddr = InetAddressUtils.str(inetAddr);

    	LOG.debug("Creating SNMP info for interface {}", ipAddr);

    	int ifIndex = m_collector.getIfIndex(inetAddr);
    	if (ifIndex == -1) {
            return;
        }

        // first look to see if an snmpIf was created already
        OnmsSnmpInterface snmpIf = m_node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf == null) {
            // if not then create one
            snmpIf = new OnmsSnmpInterface(m_node, ifIndex);
            snmpIf.setIfAlias(m_collector.getIfAlias(ifIndex));
            snmpIf.setIfName(m_collector.getIfName(ifIndex));
            snmpIf.setIfType(getIfType(ifIndex));
            snmpIf.setNetMask(getNetMask(ifIndex));
            snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
            snmpIf.setIfDescr(m_collector.getIfDescr(ifIndex));
            snmpIf.setIfSpeed(m_collector.getInterfaceSpeed(ifIndex));
            snmpIf.setPhysAddr(m_collector.getPhysAddr(ifIndex));
        }
        
        snmpIf.setCollectionEnabled(true);

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

	private InetAddress getNetMask(int ifIndex) {
		InetAddress[] ifAddressAndMask = m_collector.getIfAddressAndMask(ifIndex);
		if (ifAddressAndMask != null && ifAddressAndMask.length > 1 && ifAddressAndMask[1] != null) {
            return ifAddressAndMask[1];
        }
		return null;
	}

	private void resolveIpHostname(final OnmsIpInterface ipIf) {
		final String ipAddress = InetAddressUtils.str(ipIf.getIpAddress());
		ipIf.setIpHostName(ipAddress);
//
//     DON'T DO THIS SINCE DNS DOESN'T RELIABLY AVOID HANGING
//
//    	log().info("Resolving Hostname for "+ipIf.getIpAddress());
//		try {
//			InetAddress addr = InetAddressUtils.addr(ipIf.getIpAddress());
//			ipIf.setIpHostName(addr.getHostName());
//		} catch (Throwable e) {
//			if (ipIf.getIpHostName() == null)
//				ipIf.setIpHostName(ipIf.getIpAddress());
//		}
	}

	/** {@inheritDoc} */
        @Override
	public void foundMonitoredService(String serviceName) {
        OnmsServiceType svcType = getServiceType(serviceName);
        OnmsMonitoredService service = new OnmsMonitoredService(m_currentInterface, svcType);
        service.setStatus("A");
        m_currentInterface.getMonitoredServices().add(service);
    
    }

    /** {@inheritDoc} */
        @Override
    public void foundCategory(String name) {
        OnmsCategory category = getCategory(name);
        m_node.getCategories().add(category);
    }

    /** {@inheritDoc} */
        @Override
    public void foundAsset(String name, String value) {
        BeanWrapper w = PropertyAccessorFactory.forBeanPropertyAccess(m_node.getAssetRecord());
        try {
            w.setPropertyValue(name, value);
        } catch (BeansException e) {
            LOG.warn("Could not set property on asset: {}", name, e);
        }
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
     * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    protected NodeDao getNodeDao() {
        return m_nodeDao;
    }
    
    /**
     * <p>getDistPollerDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.DistPollerDao} object.
     */
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

    /**
     * <p>getIpAddrToInterfaceMap</p>
     *
     * @param imported a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link java.util.Map} object.
     */
    protected Map<String, OnmsIpInterface> getIpAddrToInterfaceMap(OnmsNode imported) {
        Map<String, OnmsIpInterface> ipAddrToIface = new HashMap<String, OnmsIpInterface>();
        for (final OnmsIpInterface iface : imported.getIpInterfaces()) {
            final String ipAddress = InetAddressUtils.str(iface.getIpAddress());
			ipAddrToIface.put(ipAddress, iface);
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
     * @return a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public CategoryDao getCategoryDao() {
        return m_categoryDao;
    }

    /**
     * <p>setCategoryDao</p>
     *
     * @param categoryDao a {@link org.opennms.netmgt.dao.api.CategoryDao} object.
     */
    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    /**
     * <p>setServiceTypeDao</p>
     *
     * @param svcTypeDao a {@link org.opennms.netmgt.dao.api.ServiceTypeDao} object.
     */
    public void setServiceTypeDao(ServiceTypeDao svcTypeDao) {
        m_svcTypeDao = svcTypeDao;
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>setDistPollerDao</p>
     *
     * @param distPollerDao a {@link org.opennms.netmgt.dao.api.DistPollerDao} object.
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
