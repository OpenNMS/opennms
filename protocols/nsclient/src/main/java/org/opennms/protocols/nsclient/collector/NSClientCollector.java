/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.protocols.nsclient.collector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.NumericAttributeUtils;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.datacollction.nsclient.Attrib;
import org.opennms.netmgt.config.datacollction.nsclient.NsclientCollection;
import org.opennms.netmgt.config.datacollction.nsclient.Wpm;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.protocols.nsclient.NSClientAgentConfig;
import org.opennms.protocols.nsclient.NsclientCheckParams;
import org.opennms.protocols.nsclient.NsclientException;
import org.opennms.protocols.nsclient.NsclientManager;
import org.opennms.protocols.nsclient.NsclientPacket;
import org.opennms.protocols.nsclient.config.NSClientDataCollectionConfigFactory;
import org.opennms.protocols.nsclient.config.NSClientPeerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>NSClientCollector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NSClientCollector extends AbstractRemoteServiceCollector {

	private static final Logger LOG = LoggerFactory.getLogger(NSClientCollector.class);

	private static final String NSCLIENT_COLLECTION_KEY = "nsClientCollection";

	private static final String NSCLIENT_AGENT_CONFIG_KEY = "nsClientAgentConfig";

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(NSCLIENT_COLLECTION_KEY, NsclientCollection.class),
            new SimpleEntry<>(NSCLIENT_AGENT_CONFIG_KEY, NSClientAgentConfig.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    public NSClientCollector() {
        super(TYPE_MAP);
    }

    @Override
    public void initialize() {
        LOG.debug("initialize: Initializing NSClientCollector.");
        initNSClientPeerFactory();
        initNSClientCollectionConfig();
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();
        final ServiceParameters serviceParams = new ServiceParameters(parameters);
        final String collectionName = serviceParams.getCollectionName();
        final NsclientCollection collection = NSClientDataCollectionConfigFactory.getInstance().getNSClientCollection(collectionName);
        if (collection == null) {
            throw new IllegalArgumentException(String.format("NSClientCollector: No collection found with name '%s'.",  collectionName));
        }
        runtimeAttributes.put(NSCLIENT_COLLECTION_KEY, collection);
        final NSClientAgentConfig agentConfig = NSClientPeerFactory.getInstance().getAgentConfig(agent.getAddress());
        runtimeAttributes.put(NSCLIENT_AGENT_CONFIG_KEY, agentConfig);
        return runtimeAttributes;
    }

    /** {@inheritDoc} */
    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) {
        CollectionSetBuilder builder = new CollectionSetBuilder(agent);
        builder.withStatus(CollectionStatus.FAILED);

        // Find attributes to collect - check groups in configuration. For each,
        // check scheduled nodes to see if that group should be collected
        NsclientCollection collection = (NsclientCollection)parameters.get(NSCLIENT_COLLECTION_KEY);
        NSClientAgentConfig agentConfig = (NSClientAgentConfig)parameters.get(NSCLIENT_AGENT_CONFIG_KEY);
        NSClientAgentState agentState = new NSClientAgentState(agent.getAddress(), parameters, agentConfig);

        if (collection.getWpms().getWpm().size() < 1) {
            LOG.info("No groups to collect.");
            builder.withStatus(CollectionStatus.SUCCEEDED);
            return builder.build();
        }

        // All node resources for NSClient; nothing of interface or "indexed resource" type
        NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());
        for (Wpm wpm : collection.getWpms().getWpm()) {
            // A wpm consists of a list of attributes, identified by name
            if (agentState.shouldCheckAvailability(wpm.getName(), wpm.getRecheckInterval())) {
                LOG.debug("Checking availability of group {}", wpm.getName());
                NsclientManager manager = null;
                try {
                    manager = agentState.getManager();
                    manager.init();
                    NsclientCheckParams params = new NsclientCheckParams(wpm.getKeyvalue());
                    NsclientPacket result = manager.processCheckCommand(NsclientManager.CHECK_COUNTER, params);
                    manager.close();
                    boolean isAvailable = (result.getResultCode() == NsclientPacket.RES_STATE_OK);
                    agentState.setGroupIsAvailable(wpm.getName(), isAvailable);
                    LOG.debug("Group {} is {}available ", wpm.getName(), (isAvailable?"":"not"));
                } catch (NsclientException e) {
                    LOG.error("Error checking group ({}) availability", wpm.getName(), e);
                    agentState.setGroupIsAvailable(wpm.getName(), false);
                } finally {
                    if (manager != null) {
                        manager.close();
                    }
                }
            }

            if (agentState.groupIsAvailable(wpm.getName())) {
                // Collect the data
                try {
                    NsclientManager manager = agentState.getManager();
                    manager.init(); // Open the connection, then do each
                                    // attribute

                    for (Attrib attrib : wpm.getAttrib()) {
                        NsclientPacket result = null;

                        try {
                            NsclientCheckParams params = new NsclientCheckParams(attrib.getName());
                            result = manager.processCheckCommand(NsclientManager.CHECK_COUNTER, params);
                        } catch (NsclientException e) {
                            LOG.info("unable to collect params for attribute '{}'", attrib.getName(), e);
                        }

                        if (result != null) {
                            if (result.getResultCode() != NsclientPacket.RES_STATE_OK) {
                                LOG.info("not writing parameters for attribute '{}', state is not 'OK'", attrib.getName());
                            } else {
                                // Only numeric data comes back from NSClient in data collection
                                builder.withNumericAttribute(nodeResource, wpm.getName(), attrib.getAlias(), NumericAttributeUtils.parseNumericValue(result.getResponse()), attrib.getType());
                            }
                        }
                    }
                    builder.withStatus(CollectionStatus.SUCCEEDED);
                    manager.close(); // Only close once all the attribs have
                                        // been done (optimizing as much as
                                        // possible with NSClient)
                } catch (NsclientException e) {
                    LOG.error("Error collecting data", e);
                }
            }
        }
        return builder.build();
    }

    private static void initNSClientPeerFactory() {
        LOG.debug("initialize: Initializing NSClientPeerFactory");
        try {
            NSClientPeerFactory.init();
        } catch (IOException e) {
            LOG.error("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private static void initNSClientCollectionConfig() {
        LOG.debug("initialize: Initializing collector: {}", NSClientCollector.class);
        try {
            NSClientDataCollectionConfigFactory.init();
        } catch (FileNotFoundException e) {
            LOG.error("initialize: Error locating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            LOG.error("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private static class NSClientAgentState {
        private final NsclientManager m_manager;
        private final String m_address;
        private final Map<String, NSClientGroupState> m_groupStates = new HashMap<String, NSClientGroupState>();

        public NSClientAgentState(InetAddress address, Map<String, Object> parameters, NSClientAgentConfig agentConfig) {
            m_address = InetAddressUtils.str(address);
            m_manager = new NsclientManager(m_address);
            m_manager.setPassword(agentConfig.getPassword());
            m_manager.setTimeout(agentConfig.getTimeout());
            m_manager.setPortNumber(agentConfig.getPort());
        }

        public NsclientManager getManager() {
            return m_manager;
        }

        public boolean groupIsAvailable(String groupName) {
            NSClientGroupState groupState = m_groupStates.get(groupName);
            if (groupState == null) {
                return false; // If the group availability hasn't been set
                                // yet, it's not available.
            }
            return groupState.isAvailable();
        }

        public void setGroupIsAvailable(String groupName, boolean available) {
            NSClientGroupState groupState = m_groupStates.get(groupName);
            if (groupState == null) {
                groupState = new NSClientGroupState(available);
            }
            groupState.setAvailable(available);
            m_groupStates.put(groupName, groupState);
        }

        public boolean shouldCheckAvailability(String groupName, int recheckInterval) {
            NSClientGroupState groupState = m_groupStates.get(groupName);
            if (groupState == null) {
                // If the group hasn't got a status yet, then it should be
                // checked regardless (and setGroupIsAvailable will
                // be called soon to create the status object)
                return true;
            }
            Date lastchecked = groupState.getLastChecked();
            Date now = new Date();
            return (now.getTime() - lastchecked.getTime() > recheckInterval);
        }

        @SuppressWarnings("unused")
        public void didCheckGroupAvailability(String groupName) {
            NSClientGroupState groupState = m_groupStates.get(groupName);
            if (groupState == null) {
                // Probably an error - log it as a warning, and give up
                LOG.warn("didCheckGroupAvailability called on a group without state - this is odd");
                return;
            }
            groupState.setLastChecked(new Date());
        }

    }

    private static class NSClientGroupState {
        private boolean available = false;
        private Date lastChecked;

        public NSClientGroupState(boolean isAvailable) {
            this(isAvailable, new Date());
        }

        public NSClientGroupState(boolean isAvailable, Date lastChecked) {
            this.available = isAvailable;
            this.lastChecked = lastChecked;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public Date getLastChecked() {
            return lastChecked;
        }

        public void setLastChecked(Date lastChecked) {
            this.lastChecked = lastChecked;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return NSClientDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

}
