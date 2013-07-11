/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.operations;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.provision.service.ProvisionService;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

public abstract class SaveOrUpdateOperation extends ImportOperation {
    private static final Logger LOG = LoggerFactory.getLogger(SaveOrUpdateOperation.class);

    private final OnmsNode m_node;
    private OnmsIpInterface m_currentInterface;
    
    private ScanManager m_scanManager;
    
    /**
     * <p>Constructor for SaveOrUpdateOperation.</p>
     *
     * @param foreignSource a {@link java.lang.String} object.
     * @param foreignId a {@link java.lang.String} object.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param building a {@link java.lang.String} object.
     * @param city a {@link java.lang.String} object.
     * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
     */
    public SaveOrUpdateOperation(String foreignSource, String foreignId, String nodeLabel, String building, String city, ProvisionService provisionService) {
		this(null, foreignSource, foreignId, nodeLabel, building, city, provisionService);
	}

	/**
	 * <p>Constructor for SaveOrUpdateOperation.</p>
	 *
	 * @param nodeId a {@link java.lang.Integer} object.
	 * @param foreignSource a {@link java.lang.String} object.
	 * @param foreignId a {@link java.lang.String} object.
	 * @param nodeLabel a {@link java.lang.String} object.
	 * @param building a {@link java.lang.String} object.
	 * @param city a {@link java.lang.String} object.
	 * @param provisionService a {@link org.opennms.netmgt.provision.service.ProvisionService} object.
	 */
	public SaveOrUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city, ProvisionService provisionService) {
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
	
	/**
	 * <p>getScanManager</p>
	 *
	 * @return a {@link org.opennms.netmgt.provision.service.operations.ScanManager} object.
	 */
	public ScanManager getScanManager() {
	    return m_scanManager;
	}

	/**
	 * <p>foundInterface</p>
	 *
	 * @param ipAddr a {@link java.lang.String} object.
	 * @param descr a {@link java.lang.Object} object.
	 * @param primaryType a {@link InterfaceSnmpPrimaryType} object.
	 * @param managed a boolean.
	 * @param status a int.
	 */
	public void foundInterface(String ipAddr, Object descr, final PrimaryType primaryType, boolean managed, int status) {
		
		if (ipAddr == null || "".equals(ipAddr.trim())) {
		    LOG.error(String.format("Found interface on node {} with an empty ipaddr! Ignoring!", m_node.getLabel()));
			return;
		}

        m_currentInterface = new OnmsIpInterface(ipAddr, m_node);
        m_currentInterface.setIsManaged(status == 3 ? "U" : "M");
        m_currentInterface.setIsSnmpPrimary(primaryType);
        
        if (PrimaryType.PRIMARY.equals(primaryType)) {
        	final InetAddress addr = InetAddressUtils.addr(ipAddr);
        	if (addr == null) {
        		LOG.error("Unable to resolve address of snmpPrimary interface for node {} with address '{}'", m_node.getLabel(), ipAddr);
        	} else {
        		m_scanManager = new ScanManager(addr);
        	}
        }
        
        //FIXME: verify this doesn't conflict with constructor.  The constructor already adds this
        //interface to the node.
        m_node.addIpInterface(m_currentInterface);
    }
	
    /**
     * <p>scan</p>
     */
    @Override
    public void scan() {
    	updateSnmpData();
	}
	
    /**
     * <p>updateSnmpData</p>
     */
    protected void updateSnmpData() {
        if (m_scanManager != null) {
            m_scanManager.updateSnmpData(m_node);
        }
	}

    /**
     * <p>foundMonitoredService</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     */
    public void foundMonitoredService(String serviceName) {
        // current interface may be null if it has no ipaddr
        if (m_currentInterface != null) {
            OnmsServiceType svcType = getProvisionService().createServiceTypeIfNecessary(serviceName);
            OnmsMonitoredService service = new OnmsMonitoredService(m_currentInterface, svcType);
            service.setStatus("A"); // DbIfServiceEntry.STATUS_ACTIVE
            m_currentInterface.getMonitoredServices().add(service);
        }
    
    }

    /**
     * <p>foundCategory</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void foundCategory(String name) {
        OnmsCategory category = getProvisionService().createCategoryIfNecessary(name);
        m_node.getCategories().add(category);
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
     * <p>foundAsset</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void foundAsset(final String name, final String value) {
        final BeanWrapper w = PropertyAccessorFactory.forBeanPropertyAccess(m_node.getAssetRecord());
        try {
            w.setPropertyValue(name, value);
        } catch (final BeansException e) {
            LOG.warn("Could not set property on object of type {}: {}", m_node.getClass().getName(), name, e);
        }
    }
}
