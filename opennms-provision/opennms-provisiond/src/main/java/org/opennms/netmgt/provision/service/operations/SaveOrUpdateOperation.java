/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.operations;

import java.net.InetAddress;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeLabelSource;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.service.ProvisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

import com.google.common.base.Strings;

public abstract class SaveOrUpdateOperation extends ImportOperation {
    private static final Logger LOG = LoggerFactory.getLogger(SaveOrUpdateOperation.class);

    private final OnmsNode m_node;
    private OnmsIpInterface m_currentInterface;
    
    private ScanManager m_scanManager;
    private boolean m_rescanExisting = Boolean.TRUE;

    protected SaveOrUpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String location, String building, String city, ProvisionService provisionService, boolean rescanExisting) {
        super(provisionService);

        m_node = new OnmsNode();

        m_node.setLocation(Strings.isNullOrEmpty(location) ?
                new OnmsMonitoringLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID) :
                new OnmsMonitoringLocation(location, location));

        m_node.setLabel(nodeLabel);
        m_node.setId(nodeId);
        m_node.setLabelSource(NodeLabelSource.USER);
        m_node.setType(NodeType.ACTIVE);
        m_node.setForeignSource(foreignSource);
        m_node.setForeignId(foreignId);
        m_node.getAssetRecord().setBuilding(building);
        m_node.getAssetRecord().setCity(city);
        m_rescanExisting = rescanExisting;
    }

	public ScanManager getScanManager() {
	    return m_scanManager;
	}

	public void foundInterface(String ipAddr, Object descr, final PrimaryType primaryType, boolean managed, int status) {
		
		if (ipAddr == null || "".equals(ipAddr.trim())) {
		    LOG.error("Found interface on node {} with an empty ipaddr! Ignoring!", m_node.getLabel());
			return;
		}

        final InetAddress addr = InetAddressUtils.addr(ipAddr);
        if (addr == null) {
            LOG.error("Unable to resolve address of snmpPrimary interface for node {} with address '{}'", m_node.getLabel(), ipAddr);
        }

        m_currentInterface = new OnmsIpInterface(ipAddr, m_node);
        m_currentInterface.setIsManaged(status == 3 ? "U" : "M");
        m_currentInterface.setIsSnmpPrimary(primaryType);

        if (addr != null && System.getProperty("org.opennms.provisiond.reverseResolveRequisitionIpInterfaceHostnames", "true").equalsIgnoreCase("true")) {
            m_currentInterface.setIpHostName(getProvisionService().getHostnameResolver().getHostname(addr, m_node.getLocation().getLocationName()));
        }

        if (PrimaryType.PRIMARY.equals(primaryType)) {
            if (addr != null) {
                m_scanManager = new ScanManager(getProvisionService().getLocationAwareSnmpClient(), addr);
            }
        }

        //FIXME: verify this doesn't conflict with constructor.  The constructor already adds this
        //interface to the node.
        m_node.addIpInterface(m_currentInterface);
    }
	
    @Override
    public void scan() {
    	updateSnmpData();
    }
	
    protected void updateSnmpData() {
        if (m_scanManager != null) {
            m_scanManager.updateSnmpData(m_node);
        }
	}

    public void foundMonitoredService(String serviceName) {
        // current interface may be null if it has no ipaddr
        if (m_currentInterface != null) {
            OnmsServiceType svcType = getProvisionService().createServiceTypeIfNecessary(serviceName);
            OnmsMonitoredService service = new OnmsMonitoredService(m_currentInterface, svcType);
            service.setStatus("A");
            m_currentInterface.getMonitoredServices().add(service);
        }
    }

    public void foundCategory(String name) {
        OnmsCategory category = getProvisionService().createCategoryIfNecessary(name);
        m_node.getCategories().add(category);
    }

    protected OnmsNode getNode() {
        return m_node;
    }

    protected boolean getRescanExisting() {
        return m_rescanExisting;
    }

    public void foundAsset(final String name, final String value) {
        final BeanWrapper w = PropertyAccessorFactory.forBeanPropertyAccess(m_node.getAssetRecord());
        try {
            w.setPropertyValue(name, value);
        } catch (final BeansException e) {
            LOG.warn("Could not set property on object of type {}: {}", m_node.getClass().getName(), name, e);
        }
    }

}
