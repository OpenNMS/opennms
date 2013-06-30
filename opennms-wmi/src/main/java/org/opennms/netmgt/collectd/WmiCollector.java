/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.wmi.WmiAgentState;
import org.opennms.netmgt.collectd.wmi.WmiCollectionAttributeType;
import org.opennms.netmgt.collectd.wmi.WmiCollectionResource;
import org.opennms.netmgt.collectd.wmi.WmiCollectionSet;
import org.opennms.netmgt.collectd.wmi.WmiMultiInstanceCollectionResource;
import org.opennms.netmgt.collectd.wmi.WmiSingleInstanceCollectionResource;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiDataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.wmi.Attrib;
import org.opennms.netmgt.config.wmi.WmiCollection;
import org.opennms.netmgt.config.wmi.Wpm;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
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
public class WmiCollector implements ServiceCollector {
	
	private static final Logger LOG = LoggerFactory.getLogger(WmiCollector.class);


    // Don't make this static because each service will have its own
    // copy and the key won't require the service name as part of the key.
    private final HashMap<Integer, WmiAgentState> m_scheduledNodes = new HashMap<Integer, WmiAgentState>();
    private HashMap<String, AttributeGroupType> m_groupTypeList = new HashMap<String, AttributeGroupType>();
    private HashMap<String, WmiCollectionAttributeType> m_attribTypeList = new HashMap<String, WmiCollectionAttributeType>();

    /** {@inheritDoc} */
    @Override
    public CollectionSet collect(final CollectionAgent agent, final EventProxy eproxy, final Map<String, Object> parameters) {

        String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "wmi-collection", null));
        // Find attributes to collect - check groups in configuration. For each,
        // check scheduled nodes to see if that group should be collected
        final WmiCollection collection = WmiDataCollectionConfigFactory.getInstance().getWmiCollection(collectionName);
        final WmiAgentState agentState = m_scheduledNodes.get(agent.getNodeId());

        // Load the attribute group types.
        loadAttributeGroupList(collection);

        // Load the attribute types.
        loadAttributeTypeList(collection);

        // Create a new collection set.
        final WmiCollectionSet collectionSet = new WmiCollectionSet(agent);        
        collectionSet.setCollectionTimestamp(new Date());

        // Iterate through the WMI collection groups.
        for (final Wpm wpm : collection.getWpms().getWpm()) {
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
                    agentState.connect();

                    // And retrieve the client object for working.
                    client = (WmiClient) agentState.getWmiClient();

                    // Retrieve the WbemObjectSet from the class defined on the group.
                    final OnmsWbemObjectSet wOS = client.performInstanceOf(wpm.getWmiClass());

                    // If we received a WbemObjectSet result, lets go through it and collect it.
                    if (wOS != null) {
                        //  Go through each object (class instance) in the object set.
                        for (int i = 0; i < wOS.count(); i++) {
                            // Create a new collection resource.
                            WmiCollectionResource resource = null;

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
                                resource = new WmiMultiInstanceCollectionResource(agent,instance,wpm.getResourceType());
                            } else {
                                resource = new WmiSingleInstanceCollectionResource(agent);
                            }


                            for (final Attrib attrib : wpm.getAttrib()) {
                                final OnmsWbemProperty prop = obj.getWmiProperties().getByName(attrib.getWmiObject());                                
                                final WmiCollectionAttributeType attribType = m_attribTypeList.get(attrib.getName());
                                resource.setAttributeValue(attribType, prop.getWmiValue().toString());
                            }
                            collectionSet.getResources().add(resource);
                        }
                    }
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
        collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
        return collectionSet;
    }

    private void loadAttributeGroupList(final WmiCollection collection) {
        for (final Wpm wpm : collection.getWpms().getWpm()) {
            final AttributeGroupType attribGroupType1 = new AttributeGroupType(wpm.getName(), wpm.getIfType());
            m_groupTypeList.put(wpm.getName(), attribGroupType1);
        }
    }

    private void loadAttributeTypeList(final WmiCollection collection) {
        for (final Wpm wpm : collection.getWpms().getWpm()) {
            for (final Attrib attrib : wpm.getAttrib()) {
                final AttributeGroupType attribGroupType = m_groupTypeList.get(wpm.getName());
                final WmiCollectionAttributeType attribType = new WmiCollectionAttributeType(attrib, attribGroupType);
                m_attribTypeList.put(attrib.getName(), attribType);
            }
        }
    }

    private boolean isGroupAvailable(final WmiAgentState agentState, final Wpm wpm) {
        LOG.debug("Checking availability of group {}", wpm.getName());
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

    /** {@inheritDoc} */
    @Override
    public void initialize(final Map<String, String> parameters) {
        LOG.debug("initialize: Initializing WmiCollector.");
        m_scheduledNodes.clear();
        initWMIPeerFactory();
        initWMICollectionConfig();
        initDatabaseConnectionFactory();
        initializeRrdRepository();
    }

    private void initWMIPeerFactory() {
        LOG.debug("initialize: Initializing WmiPeerFactory");
        try {
            WmiPeerFactory.init();
        } catch (final MarshalException e) {
            LOG.error("initialize: Error marshalling configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (final ValidationException e) {
            LOG.error("initialize: Error validating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (final IOException e) {
            LOG.error("initialize: Error reading configuration.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initWMICollectionConfig() {
        LOG.debug("initialize: Initializing collector: {}", getClass());
        try {
            WmiDataCollectionConfigFactory.init();
        } catch (final MarshalException e) {
            LOG.error("initialize: Error marshalling configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("initialize: Error validating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (FileNotFoundException e) {
            LOG.error("initialize: Error locating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            LOG.error("initialize: Error reading configuration.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initializeRrdRepository() {
        LOG.debug("initializeRrdRepository: Initializing RRD repo from WmiCollector...");
        initializeRrdDirs();
    }

    private void initializeRrdDirs() {
        /*
         * If the RRD file repository directory does NOT already exist, create
         * it.
         */
        final File f = new File(WmiDataCollectionConfigFactory.getInstance().getRrdPath());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("Unable to create RRD file repository.  Path doesn't already exist and could not make directory: " + DataCollectionConfigFactory.getInstance().getRrdPath());
            }
        }
    }

    private void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (final Exception e) {
            LOG.error("initDatabaseConnectionFactory: Error initializing DataSourceFactory.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final CollectionAgent agent, final Map<String, Object> parameters) {
        LOG.debug("initialize: Initializing WMI collection for agent: {}", agent);
        final Integer scheduledNodeKey = new Integer(agent.getNodeId());
        WmiAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);

        if (nodeState != null) {
            LOG.info("initialize: Not scheduling interface for WMI collection: {}", nodeState.getAddress());
            final StringBuffer sb = new StringBuffer();
            sb.append("initialize service: ");
            sb.append(" for address: ");
            sb.append(nodeState.getAddress());
            sb.append(" already scheduled for collection on node: ");
            sb.append(agent);
            LOG.debug(sb.toString());
            throw new IllegalStateException(sb.toString());
        } else {
            nodeState = new WmiAgentState(agent.getInetAddress(), parameters);
            LOG.info("initialize: Scheduling interface for collection: {}", nodeState.getAddress());
            m_scheduledNodes.put(scheduledNodeKey, nodeState);
        }
    }

    /**
     * <p>release</p>
     */
    @Override
    public void release() {
        m_scheduledNodes.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void release(final CollectionAgent agent) {
        final WmiAgentState nodeState = m_scheduledNodes.get((Integer) agent.getNodeId());
        if (nodeState != null) {
            m_scheduledNodes.remove((Integer) agent.getNodeId());
        }
    }

    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        return WmiDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }



}
