/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: July 16, 2010
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.SnmpAssetAdapterConfig;
import org.opennms.netmgt.config.snmpAsset.adapter.AssetField;
import org.opennms.netmgt.config.snmpAsset.adapter.MibObj;
import org.opennms.netmgt.config.snmpAsset.adapter.MibObjs;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventForwarder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.Assert;

/**
 */
@EventListener(name="SnmpAssetProvisioningAdapter")
public class SnmpAssetProvisioningAdapter extends SimplerQueuedProvisioningAdapter implements InitializingBean {

	private NodeDao m_nodeDao;
	private EventForwarder m_eventForwarder;
	private SnmpAssetAdapterConfig m_config;
	private SnmpAgentConfigFactory m_snmpConfigDao;

	/** 
	 * Constant <code>NAME="SnmpAssetProvisioningAdapter"</code> 
	 */
	public static final String NAME = "SnmpAssetProvisioningAdapter";

	public SnmpAssetProvisioningAdapter() {
		super(NAME);

		// Set the default time delay to 300 seconds
		this.setDelay(300);
		this.setTimeUnit(TimeUnit.SECONDS);
	}
	
	/**
	 * Creating a custom schedule for this adapter.  We need to make sure that the node has a system object ID set
	 * and that it has had enough time for that to have happened.
	 * @param nodeId
	 * @param adapterOperationType
	 * @return
	 */
	@Override
	AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
		AdapterOperationSchedule aos = new AdapterOperationSchedule(m_delay, 60, 3, m_timeUnit);
		log().info("createScheduleForNode: Scheduling "+adapterOperationType+" with schedule: "+aos);
		return aos;
	}
	
	@Override
	public boolean isNodeReady(AdapterOperation op) {
		boolean readyState = false;
		OnmsNode node = m_nodeDao.get(op.getNodeId());
		
		if (node != null && node.getSysObjectId() != null) {
			readyState = true;
		}
		return readyState;
	}

	/**
	 * <p>doAdd</p>
	 *
	 * @param nodeId a int.
	 * @param retry a boolean.
	 * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
	 */
	@Override
	public void doAddNode(int nodeId) throws ProvisioningAdapterException {
		log().debug("doAdd: adding nodeid: " + nodeId);

		final OnmsNode node = m_nodeDao.get(nodeId);
		Assert.notNull(node, "doAdd: failed to return node for given nodeId:"+nodeId);

		InetAddress ipaddress = m_template.execute(new TransactionCallback<InetAddress>() {
			public InetAddress doInTransaction(TransactionStatus arg0) {
				return getIpForNode(node);
			}
		});

		SnmpAgentConfig agentConfig = null;
		agentConfig = m_snmpConfigDao.getAgentConfig(ipaddress);

		OnmsAssetRecord asset = node.getAssetRecord();
		AssetField[] fields = m_config.getAssetFieldsForAddress(ipaddress, node.getSysObjectId());
		for (AssetField field : fields) {
			try {
				String value = fetchSnmpAssetString(agentConfig, field.getMibObjs(), field.getFormatString());
				if (log().isDebugEnabled()) {
					log().debug("doAdd: Setting asset field \"" + field.getName() + "\" to value: " + value);
				}
				// Use Spring bean-accessor classes to set the field value
				BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(asset);
				try {
					wrapper.setPropertyValue(field.getName(), value);
				} catch (BeansException e) {
					log().warn("doAdd: Could not set property \"" + field.getName() + "\" on asset object: " + e.getMessage(), e);
				}
			} catch (MissingFormatArgumentException e) {
				// This exception is thrown if the SNMP operation fails or an incorrect number of
				// parameters is returned by the agent or because of a misconfiguration.
				log().warn("doAdd: Could not set value for asset field \"" + field.getName() + "\": " + e.getMessage(), e);
			}
		}
		node.setAssetRecord(asset);
		m_nodeDao.saveOrUpdate(node);
	}

	private static String fetchSnmpAssetString(SnmpAgentConfig agentConfig, MibObjs mibObjs, String formatString) throws MissingFormatArgumentException {

		List<String> aliases = new ArrayList<String>();
		List<SnmpObjId> objs = new ArrayList<SnmpObjId>();
		for (MibObj mibobj : mibObjs.getMibObj()) {
			aliases.add(mibobj.getAlias());
			objs.add(SnmpObjId.get(mibobj.getOid()));
		}
		// Fetch the values from the SNMP agent
		SnmpValue[] values = SnmpUtils.get(agentConfig, objs.toArray(new SnmpObjId[0]));
		if (values.length == aliases.size()) {
			Properties substitutions = new Properties();
			boolean foundAValue = false;
			for (int i = 0; i < values.length; i++) {
				if (SnmpValue.SNMP_NO_SUCH_OBJECT == values[i].getType()) {
					// No value for this OID
					continue;
				}
				foundAValue = true;
				substitutions.setProperty(aliases.get(i), values[i].toString());
			}

			if (!foundAValue) {
				if (log().isDebugEnabled()) {
					log().debug("fetchSnmpAssetString: Failed to fetch any SNMP values for system " + agentConfig.toString());
				}
				throw new MissingFormatArgumentException("fetchSnmpAssetString: Failed to fetch any SNMP values for system " + agentConfig.toString());
			} else {
				log().debug("fetchSnmpAssetString: Fetched asset properties from SNMP agent:\n" + formatPropertiesAsString(substitutions));
			}

			if (objs.size() != substitutions.size()) {
				String props = formatPropertiesAsString(substitutions);
				log().warn("fetchSnmpAssetString: Unexpected number of properties returned from SNMP GET:\n" + props);
			}

			return PropertiesUtils.substitute(formatString, substitutions);
		} else {
			log().warn("fetchSnmpAssetString: Invalid number of SNMP parameters returned: " + values.length + " != " + aliases.size());
			throw new MissingFormatArgumentException("fetchSnmpAssetString: Invalid number of SNMP parameters returned: " + values.length + " != " + aliases.size());
		}
	}

	protected static String formatPropertiesAsString(Properties props) {
		StringBuffer propertyValues = new StringBuffer();
		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			propertyValues.append("  ");
			propertyValues.append(entry.getKey().toString());
			propertyValues.append(" => ");
			propertyValues.append(entry.getValue().toString());
			propertyValues.append("\n");
		}
		return propertyValues.toString();
	}

	/**
	 * <p>doUpdate</p>
	 *
	 * @param nodeId a int.
	 * @param retry a boolean.
	 * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
	 */
	@Override
	public void doUpdateNode(int nodeId) throws ProvisioningAdapterException {
		log().debug("doUpdate: updating nodeid: " + nodeId);

		final OnmsNode node = m_nodeDao.get(nodeId);
		Assert.notNull(node, "doUpdate: failed to return node for given nodeId:"+nodeId);

		InetAddress ipaddress = m_template.execute(new TransactionCallback<InetAddress>() {
			public InetAddress doInTransaction(TransactionStatus arg0) {
				return getIpForNode(node);
			}
		});

		SnmpAgentConfig agentConfig = m_snmpConfigDao.getAgentConfig(ipaddress);

		OnmsAssetRecord asset = node.getAssetRecord();
		AssetField[] fields = m_config.getAssetFieldsForAddress(ipaddress, node.getSysObjectId());
		for (AssetField field : fields) {
			try {
				String value = fetchSnmpAssetString(agentConfig, field.getMibObjs(), field.getFormatString());
				if (log().isDebugEnabled()) {
					log().debug("doUpdate: Setting asset field \"" + field.getName() + "\" to value: " + value);
				}
				// Use Spring bean-accessor classes to set the field value
				BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(asset);
				try {
					wrapper.setPropertyValue(field.getName(), value);
				} catch (BeansException e) {
					log().warn("doUpdate: Could not set property \"" + field.getName() + "\" on asset object: " + e.getMessage(), e);
				}
			} catch (MissingFormatArgumentException e) {
				// This exception is thrown if the SNMP operation fails or an incorrect number of
				// parameters is returned by the agent or because of a misconfiguration.
				log().warn("doUpdate: Could not set value for asset field \"" + field.getName() + "\": " + e.getMessage(), e);
			}
		}
		node.setAssetRecord(asset);
		m_nodeDao.saveOrUpdate(node);
	}

	/**
	 * <p>doNodeConfigChanged</p>
	 *
	 * @param nodeId a int.
	 * @param retry a boolean.
	 * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
	 */
	@Override
	public void doNotifyConfigChange(int nodeId) throws ProvisioningAdapterException {
		log().debug("doNodeConfigChanged: nodeid: " + nodeId);
	}

	/**
	 * <p>getNodeDao</p>
	 *
	 * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
	 */
	public NodeDao getNodeDao() {
		return m_nodeDao;
	}
	/**
	 * <p>setNodeDao</p>
	 *
	 * @param dao a {@link org.opennms.netmgt.dao.NodeDao} object.
	 */
	public void setNodeDao(NodeDao dao) {
		m_nodeDao = dao;
	}

	/**
	 * <p>getEventForwarder</p>
	 *
	 * @return a {@link org.opennms.netmgt.model.events.EventForwarder} object.
	 */
	public EventForwarder getEventForwarder() {
		return m_eventForwarder;
	}

	/**
	 * <p>setEventForwarder</p>
	 *
	 * @param eventForwarder a {@link org.opennms.netmgt.model.events.EventForwarder} object.
	 */
	public void setEventForwarder(EventForwarder eventForwarder) {
		m_eventForwarder = eventForwarder;
	}

	/**
	 * @return the snmpConfigDao
	 */
	public SnmpAgentConfigFactory getSnmpPeerFactory() {
		return m_snmpConfigDao;
	}

	/**
	 * @param snmpConfigDao the snmpConfigDao to set
	 */
	public void setSnmpPeerFactory(SnmpAgentConfigFactory snmpConfigDao) {
		this.m_snmpConfigDao = snmpConfigDao;
	}

	/**
	 * @return the m_config
	 */
	public SnmpAssetAdapterConfig getSnmpAssetAdapterConfig() {
		return m_config;
	}

	/**
	 * @param mConfig the m_config to set
	 */
	public void setSnmpAssetAdapterConfig(SnmpAssetAdapterConfig mConfig) {
		m_config = mConfig;
	}

	private static ThreadCategory log() {
		return ThreadCategory.getInstance(SnmpAssetProvisioningAdapter.class);
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return NAME;
	}

	private InetAddress getIpForNode(OnmsNode node) {
		log().debug("getIpForNode: node: " + node.getNodeId() + " Foreign Source: " + node.getForeignSource());
		OnmsIpInterface primaryInterface = node.getPrimaryInterface();
		InetAddress ipaddr = null;
		try { 
			ipaddr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// Can this even happen?
			log().error("Could not fetch localhost address", e);
		}
		if (primaryInterface == null) {
			log().debug("getIpForNode: found null Snmp Primary Interface, getting interfaces");
			Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
			for (OnmsIpInterface onmsIpInterface : ipInterfaces) {
				log().debug("getIpForNode: trying Interface with id: " + onmsIpInterface.getId());
				if (onmsIpInterface.getIpAddress() != null) 
					ipaddr = onmsIpInterface.getInetAddress();
				else 
					log().debug("getIpForNode: found null ip address on Interface with id: " + onmsIpInterface.getId());

			}
		} else {        
			log().debug("getIpForNode: found Snmp Primary Interface");
			if (primaryInterface.getIpAddress() != null )
				ipaddr = primaryInterface.getInetAddress();
			else 
				log().debug("getIpForNode: found null ip address on Primary Interface");
		}
		return ipaddr;
	}

	/**
	 * <p>handleReloadConfigEvent</p>
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	@EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
	public void handleReloadConfigEvent(Event event) {
		if (isReloadConfigEventTarget(event)) {
			LogUtils.debugf(this, "Reloading the snmp asset adapter configuration");
			try {
				m_config.update();
			} catch (Throwable e) {
				LogUtils.infof(this, e, "Unable to reload snmp asset adapter configuration");
			}
		}
	}

	private boolean isReloadConfigEventTarget(Event event) {
		boolean isTarget = false;

		List<Parm> parmCollection = event.getParms().getParmCollection();

		for (Parm parm : parmCollection) {
			if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && ("Provisiond." + NAME).equalsIgnoreCase(parm.getValue().getContent())) {
				isTarget = true;
				break;
			}
		}

		log().debug("isReloadConfigEventTarget: Provisiond." + NAME + " was target of reload event: " + isTarget);
		return isTarget;
	}

	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
