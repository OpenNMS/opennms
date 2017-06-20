/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.wmi.WmiAgentState;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiDataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.wmi.Attrib;
import org.opennms.netmgt.config.wmi.WmiAgentConfig;
import org.opennms.netmgt.config.wmi.WmiCollection;
import org.opennms.netmgt.config.wmi.Wpm;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.wmi.WmiClient;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class is designed to be used by the performance collection daemon to
 * collect various  WMI performance metrics from a remote server.
 * </P>
 *
 * @author <a href="mailto:matt.raykowski@gmail.com">Matt Raykowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class WmiCollector extends AbstractRemoteServiceCollector {

	private static final Logger LOG = LoggerFactory.getLogger(WmiCollector.class);

	private static final String WMI_COLLECTION_KEY = "wmiCollection";

	private static final String WMI_AGENT_CONFIG_KEY = "wmiAgentConfig";

	private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(WMI_COLLECTION_KEY, WmiCollection.class),
            new SimpleEntry<>(WMI_AGENT_CONFIG_KEY, WmiAgentConfig.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    public WmiCollector() {
        super(TYPE_MAP);
    }

    @Override
    public void initialize() {
        LOG.debug("initialize: Initializing WmiCollector.");
        initWMIPeerFactory();
        initWMICollectionConfig();
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();
        final String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "wmi-collection", null));
        final WmiCollection collection = WmiDataCollectionConfigFactory.getInstance().getWmiCollection(collectionName);
        runtimeAttributes.put(WMI_COLLECTION_KEY, collection);
        final WmiAgentConfig agentConfig = WmiPeerFactory.getInstance().getAgentConfig(agent.getAddress());
        runtimeAttributes.put(WMI_AGENT_CONFIG_KEY, agentConfig);
        return runtimeAttributes;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionSet collect(final CollectionAgent agent, final Map<String, Object> parameters) {
        // Find attributes to collect - check groups in configuration. For each,
        // check scheduled nodes to see if that group should be collected
        final WmiCollection collection = (WmiCollection)parameters.get(WMI_COLLECTION_KEY);
        final WmiAgentConfig agentConfig = (WmiAgentConfig)parameters.get(WMI_AGENT_CONFIG_KEY);
        final WmiAgentState agentState = new WmiAgentState(agent.getAddress(), agentConfig, parameters);

        // Create a new collection set.
        CollectionSetBuilder builder = new CollectionSetBuilder(agent)
                .withStatus(CollectionStatus.FAILED);

        if (collection.getWpms().size() < 1) {
            LOG.info("No groups to collect.");
            return builder.withStatus(CollectionStatus.SUCCEEDED).build();
        }

        final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());

        // Iterate through the WMI collection groups.
        for (final Wpm wpm : collection.getWpms()) {
            // A wpm consists of a list of attributes, identified by name
            if (agentState.shouldCheckAvailability(wpm.getName(), wpm.getRecheckInterval())) {
                if (!isGroupAvailable(agentState, wpm)) {
                    continue;
                }
            }

            if (agentState.groupIsAvailable(wpm.getName())) {
                WmiClient client = null;
                // Collect the data
                try {
                    // Tell the agent to connect
                    agentState.connect(wpm.getWmiNamespace());

                    // And retrieve the client object for working.
                    client = (WmiClient) agentState.getWmiClient();

                    // Retrieve the WbemObjectSet from the class defined on the group.
                    final OnmsWbemObjectSet wOS = client.performInstanceOf(wpm.getWmiClass());

                    // If we received a WbemObjectSet result, lets go through it and collect it.
                    if (wOS != null) {
                        //  Go through each object (class instance) in the object set.
                        for (int i = 0; i < wOS.count(); i++) {
                            // Create a new collection resource.
                            Resource resource = null;

                            // Fetch our WBEM Object
                            final OnmsWbemObject obj = wOS.get(i);

                            // If this is multi-instance, fetch the instance name and store it.
                            if(wOS.count()>1) {
                                // Fetch the value of the key value. e.g. Name.
                                final OnmsWbemProperty prop = obj.getWmiProperties().getByName(wpm.getKeyvalue());
                                final Object propVal = prop.getWmiValue();
                                String instance = null;
                                if(propVal instanceof String) {
                                    instance = (String)propVal;
                                } else {
                                    instance = propVal.toString();
                                }
                                resource = getWmiResource(agent, wpm.getResourceType(), nodeResource, instance);
                            } else {
                                resource = nodeResource;
                            }

                            for (final Attrib attrib : wpm.getAttribs()) {
                                final OnmsWbemProperty prop = obj.getWmiProperties().getByName(attrib.getWmiObject());
                                final AttributeType type = attrib.getType();
                                final String stringValue = prop.getWmiValue().toString();
                                if (type.isNumeric()) {
                                    Double numericValue = Double.NaN;
                                    try {
                                        numericValue = Double.parseDouble(stringValue);
                                    } catch (NumberFormatException e) {
                                        LOG.warn("Value '{}' for attribute named '{}' cannot be converted to a number. Skipping.",
                                                prop.getWmiValue(), attrib.getName());
                                        continue;
                                    }
                                    builder.withNumericAttribute(resource, wpm.getName(), attrib.getAlias(), numericValue, type);
                                } else {
                                    builder.withStringAttribute(resource, wpm.getName(), attrib.getAlias(), stringValue);
                                }
                            }
                        }
                    }
                    builder.withStatus(CollectionStatus.SUCCEEDED);
                } catch (final WmiException e) {
                    LOG.info("unable to collect params for wpm '{}'", wpm.getName(), e);
                } finally {
                    if (client != null) {
                        try {
                            client.disconnect();
                        } catch (final WmiException e) {
                            LOG.warn("An error occurred disconnecting while collecting from WMI.", e);
                        }
                    }
                }
            }
        }
        return builder.build();
    }

    private boolean isGroupAvailable(final WmiAgentState agentState, final Wpm wpm) {
        LOG.debug("Checking availability of group {} via object {} of class {} in namespace {}", wpm.getName(), wpm.getKeyvalue(), wpm.getWmiClass(), wpm.getWmiNamespace());
        WmiManager manager = null;

        /*
         * We provide a bogus comparison value and use an operator of "NOOP"
         * to ensure that, regardless of results, we receive a result and perform
         * no logic. We're only validating that the agent is reachable and gathering
         * the result objects.
         */
        try {
            // Get and initialize the WmiManager
            manager = agentState.getManager();
            manager.setNamespace(wpm.getWmiNamespace());
            manager.init();

            final WmiParams params = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF, "not-applicable", "NOOP", wpm.getWmiClass(), wpm.getKeyvalue());
            final WmiResult result = manager.performOp(params);

            final boolean isAvailable = (result.getResultCode() == WmiResult.RES_STATE_OK);

            agentState.setGroupIsAvailable(wpm.getName(), isAvailable);
            LOG.debug("Group {} is {}{}.", wpm.getName(), (isAvailable ? "" : "not "), "available");
        } catch (final WmiException e) {
            // Log a warning signifying that this group is unavailable.
            LOG.warn("Error checking group ({}) availability.", wpm.getName(), e);
            // Set the group as unavailable.
            agentState.setGroupIsAvailable(wpm.getName(), false);
            
            // And then continue on to check the next wpm entry.
            return false;
        } finally {
            if (manager != null) {
                try {
                    manager.close();
                } catch (WmiException e) {
                    LOG.warn("An error occurred closing the WMI Manager", e);
                }
            }
        }
        return true;
    }

    private Resource getWmiResource(CollectionAgent agent, String resourceType, NodeLevelResource nodeResource, String instance) {
        ResourceType rt = DataCollectionConfigFactory.getInstance().getConfiguredResourceTypes().get(resourceType);
        if (rt == null) {
            LOG.debug("getWmiResourceType: using default WMI resource type strategy - index / all");
            rt = new ResourceType();
            rt.setName(resourceType);
            rt.setStorageStrategy(new StorageStrategy());
            rt.getStorageStrategy().setClazz(IndexStorageStrategy.class.getName());
            rt.setPersistenceSelectorStrategy(new PersistenceSelectorStrategy());
            rt.getPersistenceSelectorStrategy().setClazz(PersistAllSelectorStrategy.class.getName());
        }
        return new GenericTypeResource(nodeResource, rt, instance);
    }

    private void initWMIPeerFactory() {
        LOG.debug("initialize: Initializing WmiPeerFactory");
        try {
            WmiPeerFactory.init();
        } catch (final IOException e) {
            LOG.error("initialize: Error reading configuration.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initWMICollectionConfig() {
        LOG.debug("initialize: Initializing collector: {}", getClass());
        try {
            WmiDataCollectionConfigFactory.init();
        } catch (FileNotFoundException e) {
            LOG.error("initialize: Error locating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            LOG.error("initialize: Error reading configuration.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        return WmiDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

}
