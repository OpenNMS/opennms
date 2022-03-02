/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmpmetadata.Config;
import org.opennms.netmgt.config.snmpmetadata.Container;
import org.opennms.netmgt.config.snmpmetadata.Entry;
import org.opennms.netmgt.config.snmpmetadata.SnmpMetadataConfigDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMetaData;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.google.common.base.Strings;

@EventListener(name = SnmpMetadataProvisioningAdapter.NAME)
public class SnmpMetadataProvisioningAdapter extends SimplerQueuedProvisioningAdapter implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpMetadataProvisioningAdapter.class);
    public static final String PREFIX = "Provisiond.";
    public static final String NAME = "SnmpMetadataProvisioningAdapter";
    public static final String CONTEXT = "snmp";
    private NodeDao nodeDao;
    private SnmpAgentConfigFactory snmpConfigDao;
    private LocationAwareSnmpClient locationAwareSnmpClient;
    private EventForwarder eventForwarder;
    private SnmpMetadataConfigDao snmpMetadataAdapterConfigDao;

    public SnmpMetadataProvisioningAdapter() {
        super(NAME);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(nodeDao, "Node DAO must not be null");
        Assert.notNull(snmpConfigDao, "SNMP Configuration DAO must not be null");
        Assert.notNull(eventForwarder, "Event Forwarder must not be null");
        Assert.notNull(locationAwareSnmpClient, "Location-Aware SNMP client must not be null");
        Assert.notNull(snmpMetadataAdapterConfigDao, "SNMP Metadata Configuration DAO must not be null");
    }

    @Override
    public void doAddNode(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doAddNode: adding nodeId: {}", nodeId);
        queryNode(nodeId);
    }

    @Override
    public void doUpdateNode(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doUpdateNode: updating nodeId: {}", nodeId);
        queryNode(nodeId);
    }

    public void queryNode(final int nodeId) {
        // retrieve the node
        final OnmsNode node = nodeDao.get(nodeId);
        if (node == null) {
            LOG.debug("Failed to return node for given nodeId: {}" + nodeId);
            return;
        }

        // retrieve primary interface
        final OnmsIpInterface primaryInterface = node.getPrimaryInterface();
        if (primaryInterface == null) {
            throw new ProvisioningAdapterException("Can't find primary interface for nodeId: " + nodeId);
        }

        final InetAddress ipAddress = primaryInterface.getIpAddress();

        EventBuilder ebldr = null;

        try {
            // now get the sysObjectId
            if (node.getSysObjectId() == null) {
                LOG.debug("Node {} does not support SNMP. Skipping...", nodeId);
                return;
            }

            // get all configs that apply to the node's sysObjectId
            final List<Config> configs = snmpMetadataAdapterConfigDao.getContainer().getObject().getConfigs().stream()
                    .filter(c -> {
                        if (Strings.isNullOrEmpty(c.getSysObjectId())) {
                            return false;
                        } else {
                            if (c.getSysObjectId().startsWith("~")) {
                                // regex
                                final String regExp = c.getSysObjectId().substring(1);
                                return node.getSysObjectId().matches(regExp);
                            } else {
                                // non-regex
                                return node.getSysObjectId().equals(c.getSysObjectId());
                            }
                        }
                    })
                    .collect(Collectors.toList());

            final OnmsMonitoringLocation location = node.getLocation();
            final String locationName = (location == null) ? null : location.getLocationName();
            final SnmpAgentConfig agentConfig = snmpConfigDao.getAgentConfig(ipAddress, locationName);

            final List<OnmsMetaData> results = new ArrayList<>();

            for (final Config config : configs) {
                final SnmpObjId rootOId = SnmpObjId.get(config.getTree());

                final CompletableFuture<List<SnmpResult>> resultFuture = locationAwareSnmpClient.walk(agentConfig, rootOId)
                        .withDescription("walk" + "_" + config.getName() + "_" + node.getLabel())
                        .withLocation(locationName)
                        .execute();

                try {
                    for (final Entry entry : config.getEntries()) {
                        results.addAll(processEntry(CONTEXT, rootOId.append(entry.getTree()), config.getName(), entry, resultFuture.get(), new ArrayList<>()));
                    }
                } catch (ExecutionException e) {
                    LOG.error("Aborting SNMP walk for " + agentConfig, e);
                    throw new SnmpMetadataException("Agent failed for OId " + config.getTree() + ": " + e.getMessage());
                } catch (final InterruptedException e) {
                    throw new SnmpMetadataException("SNMP walk interrupted, exiting");
                }
            }

            results.addAll(node.getMetaData().stream()
                    .filter(m -> !m.getContext().equals(CONTEXT))
                    .collect(Collectors.toList()));

            node.setMetaData(results);
            nodeDao.saveOrUpdate(node);

            ebldr = new EventBuilder(EventConstants.HARDWARE_INVENTORY_SUCCESSFUL_UEI, PREFIX + NAME);
            ebldr.addParam(EventConstants.PARM_METHOD, NAME);
            ebldr.setNodeid(nodeId);
            ebldr.setInterface(ipAddress);
            getEventForwarder().sendNow(ebldr.getEvent());
        } catch (Throwable e) {
            ebldr = new EventBuilder(EventConstants.HARDWARE_INVENTORY_FAILED_UEI, PREFIX + NAME);
            ebldr.addParam(EventConstants.PARM_METHOD, NAME);
            ebldr.setNodeid(nodeId);
            ebldr.setInterface(ipAddress);
            ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
            getEventForwarder().sendNow(ebldr.getEvent());
        }
    }

    private List<OnmsMetaData> processEntry(final String context, final SnmpObjId baseOId, final String parentName, final Container entry, final List<SnmpResult> walk, final List<SnmpObjId> indices) {
        final List<OnmsMetaData> results = new ArrayList<>();

        if (indices != null && indices.size() > 0) {
            // leaf inside table, return output for each index

            for (final SnmpObjId index : indices) {
                final Optional<SnmpResult> result = walk.stream()
                        .filter(s -> s.getAbsoluteInstance().equals(baseOId.append(index)))
                        .findFirst();

                if (result.isPresent()) {
                    results.add(new OnmsMetaData(context, parentName + "[" + index + "]" + "." + entry.getName(), result.get().getValue().toDisplayString()));
                }
            }
        } else {
            // leaf outside table, output single value

            final Optional<SnmpResult> result = walk.stream()
                    .filter(s -> s.getAbsoluteInstance().equals(entry.isExact() ? baseOId : baseOId.append(".0")))
                    .findFirst();

            if (result.isPresent()) {
                results.add(new OnmsMetaData(context, parentName + "." + entry.getName(), result.get().getValue().toDisplayString()));
            }
        }

        if (entry.getEntries().size() > 0) {
            // get OIds for all index sub-entries
            final List<SnmpObjId> matchingIndexOIds = entry.getEntries().stream()
                    .filter(e -> e.isIndex())
                    .map(e -> baseOId.append(SnmpObjId.get(e.getTree())))
                    .collect(Collectors.toList());

            final List<SnmpObjId> newIndices;

            // pick the first one
            final Optional<SnmpObjId> firstIndex = matchingIndexOIds.stream().findFirst();

            if (firstIndex.isPresent()) {
                // extract all known indices out of the data
                newIndices = walk.stream()
                        .map(s -> s.getAbsoluteInstance())
                        .filter(s -> firstIndex.get().isPrefixOf(s))
                        .map(s -> stripPrefix(s, firstIndex.get()))
                        .collect(Collectors.toList());
            } else {
                newIndices = new ArrayList<>();
            }

            for (Entry next : entry.getEntries()) {
                results.addAll(processEntry(context, baseOId.append(next.getTree()), (parentName == null ? "" : parentName + ".") + entry.getName(), next, walk, newIndices));
            }
        }

        return results;
    }

    private SnmpObjId stripPrefix(final SnmpObjId snmpObjId, final SnmpObjId prefix) {
        if (prefix.isPrefixOf(snmpObjId)) {
            int ids[] = snmpObjId.getIds();
            int pfx[] = prefix.getIds();
            SnmpObjId newOid = SnmpObjId.get(Arrays.copyOfRange(ids, pfx.length, ids.length));
            return newOid;
        } else {
            return null;
        }
    }

    @Override
    public void doNotifyConfigChange(final int nodeId) throws ProvisioningAdapterException {
        LOG.debug("doNotifyConfigChange: nodeid: {}", nodeId);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadConfigEvent(final IEvent event) {
        if (isReloadConfigEventTarget(event)) {
            EventBuilder ebldr = null;
            LOG.debug("Reloading the Hardware Inventory adapter configuration");
            try {
                snmpMetadataAdapterConfigDao.getContainer().reload();
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, PREFIX + NAME);
            } catch (Throwable e) {
                LOG.warn("Unable to reload Hardware Inventory adapter configuration", e);
                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_DAEMON_NAME, PREFIX + NAME);
                ebldr.addParam(EventConstants.PARM_REASON, e.getMessage());
            }
            if (ebldr != null) {
                ebldr.addParam(EventConstants.PARM_METHOD, NAME);
                getEventForwarder().sendNow(ebldr.getEvent());
            }
        }
    }

    private boolean isReloadConfigEventTarget(final IEvent event) {
        boolean isTarget = false;
        for (final IParm parm : event.getParmCollection()) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && (PREFIX + NAME).equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }
        LOG.debug("isReloadConfigEventTarget: Provisiond. {} was target of reload event: {}", NAME, isTarget);
        return isTarget;
    }

    public NodeDao getNodeDao() {
        return nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        this.nodeDao = nodeDao;
    }

    public SnmpAgentConfigFactory getSnmpConfigDao() {
        return snmpConfigDao;
    }

    public void setSnmpConfigDao(SnmpAgentConfigFactory snmpConfigDao) {
        this.snmpConfigDao = snmpConfigDao;
    }

    public LocationAwareSnmpClient getLocationAwareSnmpClient() {
        return locationAwareSnmpClient;
    }

    public void setLocationAwareSnmpClient(LocationAwareSnmpClient locationAwareSnmpClient) {
        this.locationAwareSnmpClient = locationAwareSnmpClient;
    }

    public EventForwarder getEventForwarder() {
        return eventForwarder;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    public SnmpMetadataConfigDao getSnmpMetadataAdapterConfigDao() {
        return snmpMetadataAdapterConfigDao;
    }

    public void setSnmpMetadataAdapterConfigDao(SnmpMetadataConfigDao snmpMetadataAdapterConfigDao) {
        this.snmpMetadataAdapterConfigDao = snmpMetadataAdapterConfigDao;
    }
}
