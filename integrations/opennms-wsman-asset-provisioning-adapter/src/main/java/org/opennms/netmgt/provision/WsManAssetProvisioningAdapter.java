/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManConstants;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.netmgt.config.WsManAssetAdapterConfig;
import org.opennms.netmgt.config.wsman.WsmanAgentConfig;
import org.opennms.netmgt.config.wsmanAsset.adapter.AssetField;
import org.opennms.netmgt.config.wsmanAsset.adapter.WqlObj;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.w3c.dom.Node;

import com.google.common.collect.Lists;

@EventListener(name = "WsManAssetProvisioningAdapter")
public class WsManAssetProvisioningAdapter extends SimplerQueuedProvisioningAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(WsManAssetProvisioningAdapter.class);

    private NodeDao m_nodeDao;
    private WsManAssetAdapterConfig m_config;
    private WSManClientFactory m_factory = new CXFWSManClientFactory();
    private WSManConfigDao m_wsManConfigDao;

    private static final String NAME = "WsManAssetProvisioningAdapter";

    public WsManAssetProvisioningAdapter() {
        super(NAME);

        // Set the default time delay to 30 seconds
        setDelay(30);
        setTimeUnit(TimeUnit.SECONDS);
    }

    /**
     * Creating a custom schedule for this adapter.  We need to make sure that the node has a system object ID set
     * and that it has had enough time for that to have happened.
     *
     * @param nodeId
     * @param adapterOperationType
     */
    @Override
    AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
        AdapterOperationSchedule aos = new AdapterOperationSchedule(m_delay, 60, 3, m_timeUnit);
        LOG.info("createScheduleForNode: Scheduling {} for nodeid {} with schedule: {}", adapterOperationType, nodeId, aos);
        return aos;
    }

    @Override
    public boolean isNodeReady(AdapterOperation op) {
        return true;
    }

    @Override
    public void doAddNode(final int nodeId) throws ProvisioningAdapterException {
        m_template.execute((TransactionCallback<Void>) status -> {
            LOG.debug("doAdd: adding nodeid: {}", nodeId);

            final OnmsNode node = m_nodeDao.get(nodeId);
            Objects.requireNonNull(node, "doAdd: failed to return node for given nodeId: " + nodeId);

            final InetAddress ipaddress = getIpForNode(node);

            LOG.debug("doAdd: Fetching vendor asset string");
            final String vendor = node.getAssetRecord().getVendor();
            LOG.debug("doAdd: Fetched asset string: {}", vendor);

            if (m_wsManConfigDao == null) {
                m_wsManConfigDao = BeanUtils.getBean("daoContext", "wsManConfigDao", WSManConfigDao.class);
            }
            final WsmanAgentConfig config = m_wsManConfigDao.getAgentConfig(ipaddress);
            final WSManEndpoint endpoint = WSManConfigDao.getEndpoint(config, ipaddress);
            final WSManClient client = m_factory.getClient(endpoint);
            LOG.debug("doAdd: m_config: {} ", m_config);

            final OnmsAssetRecord asset = node.getAssetRecord();
            m_config.getReadLock().lock();
            try {
                for (final AssetField field : m_config.getAssetFieldsForAddress(ipaddress, vendor)) {
                    try {
                        final String value = fetchWsManAssetString(client, endpoint, field.getWqlObjs(), field.getFormatString());
                        LOG.debug("doAdd: Setting asset field \"{}\" to value: {}", field.getName(), value);
                        // Use Spring bean-accessor classes to set the field value
                        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(asset);
                        wrapper.setPropertyValue(field.getName(), value);
                    } catch (final BeansException e) {
                        LOG.warn("doAdd: Could not set property \"{}\" on asset object {}", field.getName(),
                                e.getMessage(), e);
                    } catch (final Throwable t) {
                        // This exception is thrown if the WSMAN ENUM fails or an incorrect number of
                        // parameters is returned by the agent or because of a misconfiguration.
                        LOG.warn("doAdd: Could not set value for asset field \"{}\": {}", field.getName(), t.getMessage(), t);
                    }
                }
            } finally {
                m_config.getReadLock().unlock();
            }

            node.setAssetRecord(asset);
            m_nodeDao.saveOrUpdate(node);
            m_nodeDao.flush();
            return null;
        });
    }

    private static String fetchWsManAssetString(final WSManClient client, final WSManEndpoint endpoint, final List<WqlObj> wqlObjs, final String formatString) {

        final List<String> aliases = new ArrayList<>();
        final List<String> wqls = new ArrayList<>();
        final List<String> resourceUris = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        for (final WqlObj wqlobj : wqlObjs) {
            List<Node> nodes = Lists.newLinkedList();
            aliases.add(wqlobj.getAlias());
            wqls.add(wqlobj.getWql());
            resourceUris.add(wqlobj.getResourceUri());
            final StringBuilder combinedResult = new StringBuilder();
            client.enumerateAndPullUsingFilter(wqlobj.getResourceUri(), WSManConstants.XML_NS_WQL_DIALECT, wqlobj.getWql(), nodes, true);
            if (!nodes.isEmpty()) {
                for (int num = 0; num < nodes.size(); num++) {
                    if (num > 0) {
                        combinedResult.append("\n");
                    }
                    combinedResult.append(nodes.get(num).getTextContent());
                }
                values.add(combinedResult.toString());
            } else {
                values.add(null);
            }
        }
        if (values.size() == aliases.size() && values.size() == resourceUris.size() && values.size() == wqls.size()) {
            final Properties substitutions = new Properties();
            boolean foundAValue = false;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) == null) {
                    // No value for this WQL
                    continue;
                }
                foundAValue = true;
                substitutions.setProperty(
                        aliases.get(i),
                        values.get(i)
                );
            }

            if (!foundAValue) {
                LOG.debug("fetchWsManAssetString: Failed to fetch any WsMan values for endpoint {}", endpoint.getUrl());
                throw new MissingFormatArgumentException("fetchWsManAssetString: Failed to fetch any WsMan values for endpoint " + endpoint.getUrl());
            } else {
                LOG.debug("fetchWsManAssetString: Fetched asset properties from WsMan agent:\n {}", formatPropertiesAsString(substitutions));
            }

            if (values.size() != substitutions.size()) {
                LOG.warn("fetchWsManAssetString: Unexpected number of properties returned from WsMan WQL:\n {}", formatPropertiesAsString(substitutions));
            }

            return PropertiesUtils.substitute(formatString, substitutions);
        } else {
            LOG.warn("fetchWsManAssetString: Invalid number of parameters returned: {} != {}", aliases.size(), values.size());
            throw new MissingFormatArgumentException("fetchWsManAssetString: Invalid number of parameters returned: " + values.size() + " != " + aliases.size());
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

    @Override
    public void doUpdateNode(final int nodeId) throws ProvisioningAdapterException {
        m_template.execute((TransactionCallback<Void>) status -> {
            LOG.debug("doUpdate: updating nodeid: {}", nodeId);

            final OnmsNode node = m_nodeDao.get(nodeId);
            Objects.requireNonNull(node, "doAdd: failed to return node for given nodeId: " + nodeId);
            LOG.debug("doUpdate: Fetching vendor asset string");
            final InetAddress ipaddress = getIpForNode(node);
            String vendor = node.getAssetRecord().getVendor();
            LOG.debug("doUpdate: Fetched asset string: \"{}\"", vendor);

            if (m_wsManConfigDao == null) {
                m_wsManConfigDao = BeanUtils.getBean("daoContext", "wsManConfigDao", WSManConfigDao.class);
            }
            final WsmanAgentConfig config = m_wsManConfigDao.getAgentConfig(ipaddress);
            final WSManEndpoint endpoint = WSManConfigDao.getEndpoint(config, ipaddress);
            final WSManClient client = m_factory.getClient(endpoint);
            LOG.debug("doUpdate: m_config: \"{}\"", m_config);

            final OnmsAssetRecord asset = node.getAssetRecord();
            m_config.getReadLock().lock();
            try {
                for (final AssetField field : m_config.getAssetFieldsForAddress(ipaddress, vendor)) {
                    try {
                        final String value = fetchWsManAssetString(client, endpoint, field.getWqlObjs(), field.getFormatString());
                        LOG.debug("doUpdate: Setting asset field \" {} \" to value: {}", field.getName(), value);
                        // Use Spring bean-accessor classes to set the field value
                        final BeanWrapper wrapper = PropertyAccessorFactory.forBeanPropertyAccess(asset);
                        wrapper.setPropertyValue(field.getName(), value);
                    } catch (final BeansException e) {
                        LOG.warn("doUpdate: Could not set property \" {} \" on asset object {}", field.getName(), e.getMessage(), e);
                    } catch (final Throwable t) {
                        // This exception is thrown if the WSMAN ENUM fails or an incorrect number of
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
            return null;
        });
    }

    @Override
    public void doNotifyConfigChange(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doNodeConfigChanged: nodeid: {}", nodeId);
    }

    public void setNodeDao(final NodeDao dao) {
        m_nodeDao = dao;
    }

    public void setWsmanAssetAdapterConfig(final WsManAssetAdapterConfig mConfig) {
        m_config = mConfig;
    }

    public void setWsmanClientFactory(WSManClientFactory factory) {
        m_factory = Objects.requireNonNull(factory);
    }

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
            if (InetAddressUtils.str(primaryInterface.getIpAddress()) != null)
                ipaddr = primaryInterface.getIpAddress();
            else
                LOG.debug("getIpForNode: found null ip address on Primary Interface");
        }
        return ipaddr;
    }

    private void handleConfigurationChanged() {
        try {
            m_config.update();
        } catch (Throwable e) {
            LOG.info("Unable to reload WS-Man asset adapter configuration", e);
        }
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadEvent(Event e) {
        DaemonTools.handleReloadEvent(e, WsManAssetProvisioningAdapter.NAME, (event) -> handleConfigurationChanged());
    }

}
