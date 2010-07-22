/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: December 16, 2008
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import org.opennms.netmgt.model.events.EventBuilder;
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
	// private AssetRecordDao m_assetRecordDao;
	private EventForwarder m_eventForwarder;
	private SnmpAssetAdapterConfig m_config;
	private SnmpAgentConfigFactory m_snmpConfigDao;

	private static final String MESSAGE_PREFIX = "SNMP asset provisioning failed: ";

	/** 
	 * Constant <code>NAME="SnmpAssetProvisioningAdapter"</code> 
	 */
	public static final String NAME = "SnmpAssetProvisioningAdapter";
	private final ConcurrentMap<Integer, InetAddress> m_onmsNodeIpMap = new ConcurrentHashMap<Integer, InetAddress>();

	public SnmpAssetProvisioningAdapter() {
		super(NAME);
	}

	@Override
	AdapterOperationSchedule createScheduleForNode(final int nodeId, AdapterOperationType adapterOperationType) {
		log().debug("Scheduling: " + adapterOperationType + " for nodeid: " + nodeId);
		if (adapterOperationType.equals(AdapterOperationType.CONFIG_CHANGE)) {
			if (log().isDebugEnabled()) {
				InetAddress ipaddress = m_onmsNodeIpMap.get(nodeId);
				log().debug("Found suitable IP address: " + ipaddress);
			}
			return new AdapterOperationSchedule(10, 5, 3, TimeUnit.SECONDS);
		} else {
			return new AdapterOperationSchedule(0, 5, 3, TimeUnit.SECONDS);
		}
	}

	/**
	 * <p>afterPropertiesSet</p>
	 *
	 * @throws java.lang.Exception if any.
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_config, "SNMP Asset Provisioning Adapter requires config property to be set.");
		Assert.notNull(m_nodeDao, "SNMP Asset Provisioning Adapter requires nodeDao property to be set.");
		Assert.notNull(m_eventForwarder, "SNMP Asset Provisioning Adapter requires eventForwarder property to be set.");
		Assert.notNull(m_template, "SNMP Asset Provisioning Adapter requires template property to be set.");
		m_template.execute(new TransactionCallback<Object>() {
			public Object doInTransaction(TransactionStatus arg0) {
				buildNodeIpMap();
				return null;
			}
		});
	}

	private void buildNodeIpMap() {
		List<OnmsNode> nodes = m_nodeDao.findAllProvisionedNodes();
		for (OnmsNode onmsNode : nodes) {
			InetAddress ipaddr = getIpForNode(onmsNode);
			if (ipaddr != null) {
				m_onmsNodeIpMap.putIfAbsent(onmsNode.getId(), ipaddr);
			}
		}
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

		try {
			m_onmsNodeIpMap.putIfAbsent(nodeId, ipaddress);
		} catch (ProvisioningAdapterException ae) {
			sendAndThrow(nodeId, ae);
		} catch (Throwable e) {
			sendAndThrow(nodeId, e);
		}

		SnmpAgentConfig agentConfig = null;
		agentConfig = m_snmpConfigDao.getAgentConfig(ipaddress);

		OnmsAssetRecord asset = node.getAssetRecord();
		AssetField[] fields = m_config.getAssetFieldsForAddress(ipaddress, node.getSysObjectId());
		for (AssetField field : fields) {
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
		}
		node.setAssetRecord(asset);
		m_nodeDao.saveOrUpdate(node);
	}

	private static String fetchSnmpAssetString(SnmpAgentConfig agentConfig, MibObjs mibObjs, String formatString) {

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
			for (int i = 0; i < values.length; i++) {
				substitutions.setProperty(aliases.get(i), values[i].toString());
			}
			if (log().isDebugEnabled()) {
				log().debug("fetchSnmpAssetString: Fetched asset properties from SNMP agent:\n" + formatPropertiesAsString(substitutions));
			}
			if (objs.size() != substitutions.size()) {
				String props = formatPropertiesAsString(substitutions);
				log().warn("fetchSnmpAssetString: Unexpected number of properties returned from SNMP GET:\n" + props);
			}

			try {
				return PropertiesUtils.substitute(formatString, substitutions);
			} catch (MissingFormatArgumentException e) {
				log().warn("fetchSnmpAssetString: Insufficient SNMP parameters returned to satisfy format string: " + formatString);
				return formatString;
			}
		} else {
			log().warn("fetchSnmpAssetString: Invalid number of SNMP parameters returned: " + values.length + " != " + aliases.size());
			return formatString;
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

		m_onmsNodeIpMap.put(nodeId, ipaddress);

		SnmpAgentConfig agentConfig = null;
		agentConfig = m_snmpConfigDao.getAgentConfig(ipaddress);

		OnmsAssetRecord asset = node.getAssetRecord();
		AssetField[] fields = m_config.getAssetFieldsForAddress(ipaddress, node.getSysObjectId());
		for (AssetField field : fields) {
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
		}
		node.setAssetRecord(asset);
		m_nodeDao.saveOrUpdate(node);
	}

	/**
	 * <p>doDelete</p>
	 *
	 * @param nodeId a int.
	 * @param retry a boolean.
	 * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
	 */
	@Override
	public void doDeleteNode(int nodeId) throws ProvisioningAdapterException {

		log().debug("doDelete: deleting nodeid: " + nodeId);

		/*
		 * The work to maintain the hashmap boils down to needing to do deletes, so
		 * here we go.
		 */
		try {
			m_onmsNodeIpMap.remove(Integer.valueOf(nodeId));
		} catch (Throwable e) {
			sendAndThrow(nodeId, e);
		}
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

	private void sendAndThrow(int nodeId, Throwable e) {
		log().debug("sendAndThrow: error working on Dao nodeid: " + nodeId);
		log().debug("sendAndThrow: Exception: " + e.getMessage());
		Event event = buildEvent(EventConstants.PROVISIONING_ADAPTER_FAILED, nodeId).addParam("reason", MESSAGE_PREFIX+e.getLocalizedMessage()).getEvent();
		m_eventForwarder.sendNow(event);
		throw new ProvisioningAdapterException(MESSAGE_PREFIX, e);
	}

	private EventBuilder buildEvent(String uei, int nodeId) {
		EventBuilder builder = new EventBuilder(uei, "Provisioner", new Date());
		builder.setNodeid(nodeId);
		return builder;
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
	 * @return the assetRecordDao
	 */
	/*
	public AssetRecordDao getAssetRecordDao() {
		return m_assetRecordDao;
	}
	 */

	/**
	 * @param assetRecordDao the assetRecordDao to set
	 */
	/*
	public void setAssetRecordDao(AssetRecordDao assetRecordDao) {
		this.m_assetRecordDao = assetRecordDao;
	}
	 */

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

	/** {@inheritDoc} */
	@Override
	public boolean isNodeReady(final AdapterOperation op) {
		boolean ready = true;
		log().debug("isNodeReady: " + ready + " For Operation " + op.getType() + " for node: " + op.getNodeId());
		return ready;
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
				m_template.execute(new TransactionCallback<Object>() {
					public Object doInTransaction(TransactionStatus arg0) {
						buildNodeIpMap();
						return null;
					}
				});
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
}
