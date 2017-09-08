/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.config.SnmpAssetAdapterConfig;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmpAsset.adapter.AssetField;
import org.opennms.netmgt.config.snmpAsset.adapter.MibObj;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.snmp.SyntaxToEvent;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.util.Assert;

@EventListener(name="SnmpAssetProvisioningAdapter")
public class SnmpAssetProvisioningAdapter extends SimplerQueuedProvisioningAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpAssetProvisioningAdapter.class);

	private NodeDao m_nodeDao;
	private EventForwarder m_eventForwarder;
	private SnmpAssetAdapterConfig m_config;
	private SnmpAgentConfigFactory m_snmpConfigDao;
	private LocationAwareSnmpClient m_locationAwareSnmpClient;

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
		LOG.info("createScheduleForNode: Scheduling {} for nodeid {} with schedule: {}", aos, adapterOperationType, nodeId);
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
	public void doAddNode(final int nodeId) throws ProvisioningAdapterException {
		LOG.debug("doAdd: adding nodeid: {}", nodeId);

		final OnmsNode node = m_nodeDao.get(nodeId);
		Assert.notNull(node, "doAdd: failed to return node for given nodeId:"+nodeId);

		InetAddress ipaddress = m_template.execute(new TransactionCallback<InetAddress>() {
                        @Override
			public InetAddress doInTransaction(TransactionStatus arg0) {
				return getIpForNode(node);
			}
		});

		SnmpAgentConfig agentConfig = null;
        String locationName = node.getLocation() != null ? node.getLocation().getLocationName() : null;
        agentConfig = m_snmpConfigDao.getAgentConfig(ipaddress, locationName);

		final OnmsAssetRecord asset = node.getAssetRecord();
		m_config.getReadLock().lock();
		try {
		    for (final AssetField field : m_config.getAssetFieldsForAddress(ipaddress, node.getSysObjectId())) {
    			try {
				final String value = fetchSnmpAssetString(m_locationAwareSnmpClient, agentConfig, locationName, field.getMibObjs(), field.getFormatString());
				LOG.debug("doAdd: Setting asset field \" {} \" to value: {}", field.getName(), value);
    				// Use Spring bean-accessor classes to set the field value
    				final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(asset);
    				try {
    					wrapper.setPropertyValue(field.getName(), value);
    				} catch (final BeansException e) {
					LOG.warn("doAdd: Could not set property \" {} \" on asset object {}", field.getName(), e.getMessage(), e);
    				}
    			} catch (final Throwable t) {
    				// This exception is thrown if the SNMP operation fails or an incorrect number of
    				// parameters is returned by the agent or because of a misconfiguration.
				LOG.warn("doAdd: Could not set value for asset field \" {} \": {}", field.getName(), t.getMessage(), t);
    			}
    		}
		} finally {
		    m_config.getReadLock().unlock();
		}
        node.setAssetRecord(asset);
        m_nodeDao.saveOrUpdate(node);
		m_nodeDao.flush();
	}

	private static String fetchSnmpAssetString(final LocationAwareSnmpClient locationAwareSnmpClient, final SnmpAgentConfig agentConfig,
	        final String location, final List<MibObj> mibObjs, final String formatString) {

	    final List<String> aliases = new ArrayList<>();
		final List<SnmpObjId> objs = new ArrayList<>();
		for (final MibObj mibobj : mibObjs) {
			aliases.add(mibobj.getAlias());
			objs.add(SnmpObjId.get(mibobj.getOid()));
		}

		// Fetch the values from the SNMP agent
		final CompletableFuture<List<SnmpValue>> future = locationAwareSnmpClient.get(agentConfig, objs)
		        .withLocation(location)
		        .execute();

		List<SnmpValue> values;
		try {
			values = future.get();
		} catch (InterruptedException|ExecutionException e) {
			// Propagate
			throw new RuntimeException(e);
		}

		if (values.size() == aliases.size()) {
			final Properties substitutions = new Properties();
			boolean foundAValue = false;
			for (int i = 0; i < values.size(); i++) {
				// If the value is a NO_SUCH_OBJECT or NO_SUCH_INSTANCE error, then skip it
				if (values.get(i) == null || values.get(i).isError()) {
					// No value for this OID
					continue;
				}
				foundAValue = true;
				// Use trapd's SyntaxToEvent parser so that we format base64
				// and MAC address values appropriately
				Parm parm = SyntaxToEvent.processSyntax(aliases.get(i), values.get(i));
				substitutions.setProperty(
						aliases.get(i),
						parm.getValue().getContent()
				);
			}

			if (!foundAValue) {
				LOG.debug("fetchSnmpAssetString: Failed to fetch any SNMP values for system {}", agentConfig);
				throw new MissingFormatArgumentException("fetchSnmpAssetString: Failed to fetch any SNMP values for system " + agentConfig.toString());
			} else {
				LOG.debug("fetchSnmpAssetString: Fetched asset properties from SNMP agent:\n {}", formatPropertiesAsString(substitutions));
			}

			if (objs.size() != substitutions.size()) {
			    LOG.warn("fetchSnmpAssetString: Unexpected number of properties returned from SNMP GET:\n {}", formatPropertiesAsString(substitutions));
			}

			return PropertiesUtils.substitute(formatString, substitutions);
		} else {
			LOG.warn("fetchSnmpAssetString: Invalid number of SNMP parameters returned: {} != {}", aliases.size(), values.size());
			throw new MissingFormatArgumentException("fetchSnmpAssetString: Invalid number of SNMP parameters returned: " + values.size() + " != " + aliases.size());
		}
	}

	protected static String formatPropertiesAsString(final Properties props) {
		final StringBuilder propertyValues = new StringBuilder();
		for (final Map.Entry<Object, Object> entry : props.entrySet()) {
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
	public void doUpdateNode(final int nodeId) throws ProvisioningAdapterException {
	    LOG.debug("doUpdate: updating nodeid: {}", nodeId);

		final OnmsNode node = m_nodeDao.get(nodeId);
		Assert.notNull(node, "doUpdate: failed to return node for given nodeId:"+nodeId);

		final InetAddress ipaddress = m_template.execute(new TransactionCallback<InetAddress>() {
                        @Override
			public InetAddress doInTransaction(final TransactionStatus arg0) {
				return getIpForNode(node);
			}
		});


        final String locationName = node.getLocation() != null ? node.getLocation().getLocationName() : null;
        final SnmpAgentConfig agentConfig = m_snmpConfigDao.getAgentConfig(ipaddress, locationName);

		final OnmsAssetRecord asset = node.getAssetRecord();
		m_config.getReadLock().lock();
		try {
    		for (AssetField field : m_config.getAssetFieldsForAddress(ipaddress, node.getSysObjectId())) {
    			try {
    				String value = fetchSnmpAssetString(m_locationAwareSnmpClient, agentConfig, locationName, field.getMibObjs(), field.getFormatString());
    				LOG.debug("doUpdate: Setting asset field \" {} \" to value: {}", value, field.getName());
    				// Use Spring bean-accessor classes to set the field value
    				BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(asset);
    				try {
    					wrapper.setPropertyValue(field.getName(), value);
    				} catch (BeansException e) {
					LOG.warn("doUpdate: Could not set property \" {} \" on asset object: {}", field.getName(), e.getMessage(), e);
    				}
    			} catch (Throwable t) {
    				// This exception is thrown if the SNMP operation fails or an incorrect number of
    				// parameters is returned by the agent or because of a misconfiguration.
    			    LOG.warn("doUpdate: Could not set value for asset field \" {} \": {}", field.getName(), t.getMessage(), t);
    			}
    		}
		} finally {
		    m_config.getReadLock().unlock();
		}
		node.setAssetRecord(asset);
		m_nodeDao.saveOrUpdate(node);
		m_nodeDao.flush();
	}

	/**
	 * <p>doNodeConfigChanged</p>
	 *
	 * @param nodeId a int.
	 * @param retry a boolean.
	 * @throws org.opennms.netmgt.provision.ProvisioningAdapterException if any.
	 */
	@Override
	public void doNotifyConfigChange(final int nodeId) throws ProvisioningAdapterException {
		LOG.debug("doNodeConfigChanged: nodeid: {}", nodeId);
	}

	/**
	 * <p>getNodeDao</p>
	 *
	 * @return a {@link org.opennms.netmgt.dao.api.NodeDao} object.
	 */
	public NodeDao getNodeDao() {
		return m_nodeDao;
	}
	/**
	 * <p>setNodeDao</p>
	 *
	 * @param dao a {@link org.opennms.netmgt.dao.api.NodeDao} object.
	 */
	public void setNodeDao(final NodeDao dao) {
		m_nodeDao = dao;
	}

	/**
	 * <p>getEventForwarder</p>
	 *
	 * @return a {@link org.opennms.netmgt.events.api.EventForwarder} object.
	 */
	public EventForwarder getEventForwarder() {
		return m_eventForwarder;
	}

	/**
	 * <p>setEventForwarder</p>
	 *
	 * @param eventForwarder a {@link org.opennms.netmgt.events.api.EventForwarder} object.
	 */
	public void setEventForwarder(final EventForwarder eventForwarder) {
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
	public void setSnmpPeerFactory(final SnmpAgentConfigFactory snmpConfigDao) {
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
	public void setSnmpAssetAdapterConfig(final SnmpAssetAdapterConfig mConfig) {
		m_config = mConfig;
	}

	public void setLocationAwareSnmpClient(final LocationAwareSnmpClient locationAwareSnmpClient) {
		m_locationAwareSnmpClient = locationAwareSnmpClient;
	}

	public LocationAwareSnmpClient getLocationAwareSnmpClient() {
		return m_locationAwareSnmpClient;
	}

	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String getName() {
		return NAME;
	}

	private InetAddress getIpForNode(final OnmsNode node) {
		LOG.debug("getIpForNode: node: {} Foreign Source: {}", node.getNodeId(), node.getForeignSource());
		final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
		InetAddress ipaddr = InetAddressUtils.getLocalHostAddress();
		if (primaryInterface == null) {
			LOG.debug("getIpForNode: found null SNMP Primary Interface, getting interfaces");
			final Set<OnmsIpInterface> ipInterfaces = node.getIpInterfaces();
			for (final OnmsIpInterface onmsIpInterface : ipInterfaces) {
				LOG.debug("getIpForNode: trying Interface with id: {}", onmsIpInterface.getId());
				if (InetAddressUtils.str(onmsIpInterface.getIpAddress()) != null) 
					ipaddr = onmsIpInterface.getIpAddress();
				else 
					LOG.debug("getIpForNode: found null ip address on Interface with id: {}", onmsIpInterface.getId());

			}
		} else {        
			LOG.debug("getIpForNode: found SNMP Primary Interface");
			if (InetAddressUtils.str(primaryInterface.getIpAddress()) != null )
				ipaddr = primaryInterface.getIpAddress();
			else 
				LOG.debug("getIpForNode: found null ip address on Primary Interface");
		}
		return ipaddr;
	}

	/**
	 * <p>handleReloadConfigEvent</p>
	 *
	 * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	@EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
	public void handleReloadConfigEvent(final Event event) {
		if (isReloadConfigEventTarget(event)) {
			EventBuilder ebldr = null;
			LOG.debug("Reloading the SNMP asset adapter configuration");
			try {
				m_config.update();
		                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, "Provisiond." + NAME);
		                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond." + NAME);
			} catch (Throwable e) {
				LOG.info("Unable to reload SNMP asset adapter configuration", e);
				ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, "Provisiond." + NAME);
				ebldr.addParam(EventConstants.PARM_DAEMON_NAME, "Provisiond." + NAME);
				ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(1, 128));
			}
			if (ebldr != null) {
				getEventForwarder().sendNow(ebldr.getEvent());
			}
		}
	}

	private boolean isReloadConfigEventTarget(final Event event) {
		boolean isTarget = false;

		for (final Parm parm : event.getParmCollection()) {
			if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && ("Provisiond." + NAME).equalsIgnoreCase(parm.getValue().getContent())) {
				isTarget = true;
				break;
			}
		}

		LOG.debug("isReloadConfigEventTarget: Provisiond. {} was target of reload event: {}", isTarget, NAME);
		return isTarget;
	}
}
