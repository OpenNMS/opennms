/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.protocols.nsclient.collector;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.collectd.AbstractCollectionResource;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.opennms.netmgt.config.nsclient.Attrib;
import org.opennms.netmgt.config.nsclient.NsclientCollection;
import org.opennms.netmgt.config.nsclient.Wpm;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.protocols.nsclient.NSClientAgentConfig;
import org.opennms.protocols.nsclient.NsclientCheckParams;
import org.opennms.protocols.nsclient.NsclientException;
import org.opennms.protocols.nsclient.NsclientManager;
import org.opennms.protocols.nsclient.NsclientPacket;
import org.opennms.protocols.nsclient.config.NSClientDataCollectionConfigFactory;
import org.opennms.protocols.nsclient.config.NSClientPeerFactory;

/**
 * <p>NSClientCollector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NSClientCollector implements ServiceCollector {

    // Don't make this static because each service will have its own
    // copy and the key won't require the service name as part of the key.
    private final HashMap<Integer, NSClientAgentState> m_scheduledNodes = new HashMap<Integer, NSClientAgentState>();

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    class NSClientCollectionAttributeType implements CollectionAttributeType {
        Attrib m_attribute;
        AttributeGroupType m_groupType;

        protected NSClientCollectionAttributeType(Attrib attribute, AttributeGroupType groupType) {
            m_groupType=groupType;
            m_attribute=attribute;
        }

        public AttributeGroupType getGroupType() {
            return m_groupType;
        }

        public void storeAttribute(CollectionAttribute attribute, Persister persister) {
            //Only numeric data comes back from NSClient in data collection
            persister.persistNumericAttribute(attribute);
        }

        public String getName() {
            return m_attribute.getAlias();
        }

        public String getType() {
            return m_attribute.getType();
        }
    }
    
    class NSClientCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {

        String m_alias;
        String m_value;
        NSClientCollectionResource m_resource;
        CollectionAttributeType m_attribType;
        
        NSClientCollectionAttribute(NSClientCollectionResource resource, CollectionAttributeType attribType, String alias, String value) {
            super();
            m_resource=resource;
            m_attribType=attribType;
            m_alias = alias;
            m_value = value;
        }

        public CollectionAttributeType getAttributeType() {
            return m_attribType;
        }

        public String getName() {
            return m_alias;
        }

        public String getNumericValue() {
            return m_value;
        }

        public CollectionResource getResource() {
            return m_resource;
        }

        public String getStringValue() {
            return m_value; //Should this be null instead?
        }

        public boolean shouldPersist(ServiceParameters params) {
            return true;
        }

        public String getType() {
            return m_attribType.getType();
        }
        
        public String toString() {
            return "NSClientCollectionAttribute " + m_alias+"=" + m_value;
        }
        
    }
    
    class NSClientCollectionResource extends AbstractCollectionResource {
         
		NSClientCollectionResource(CollectionAgent agent) { 
            super(agent);
        }
        
        public int getType() {
            return -1; //Is this right?
        }

        //A rescan is never needed for the NSClientCollector, at least on resources
        public boolean rescanNeeded() {
            return false;
        }

        public boolean shouldPersist(ServiceParameters params) {
            return true;
        }

        public void setAttributeValue(CollectionAttributeType type, String value) {
            NSClientCollectionAttribute attr = new NSClientCollectionAttribute(this, type, type.getName(), value);
            addAttribute(attr);
        }
        
        public String getResourceTypeName() {
            return "node"; //All node resources for NSClient; nothing of interface or "indexed resource" type
        }
        
        public String getInstance() {
            return null; //For node type resources, use the default instance
        }

        public String getParent() {
            return m_agent.getStorageDir().toString();
        }
    }
    
    class NSClientCollectionSet implements CollectionSet {
        private int m_status;
        private Date m_timestamp;
        private NSClientCollectionResource m_collectionResource;
        
        NSClientCollectionSet(CollectionAgent agent, Date timestamp) {
            m_status = ServiceCollector.COLLECTION_FAILED;
            m_collectionResource = new NSClientCollectionResource(agent);
        }
        
        public int getStatus() {
            return m_status;
        }
        
        void setStatus(int status) {
            m_status = status;
        }

        public void visit(CollectionSetVisitor visitor) {
            visitor.visitCollectionSet(this);
            m_collectionResource.visit(visitor);
            visitor.completeCollectionSet(this);
        }

        public NSClientCollectionResource getResource() {
            return m_collectionResource;
        }

		public boolean ignorePersist() {
			return false;
		}

		@Override
		public Date getCollectionTimestamp() {
			return m_timestamp;
		}        
    }
    
    /** {@inheritDoc} */
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) {
        int status = ServiceCollector.COLLECTION_FAILED;
        final ServiceParameters serviceParams = new ServiceParameters(parameters);
        String collectionName = serviceParams.getCollectionName();

        // Find attributes to collect - check groups in configuration. For each,
        // check scheduled nodes to see if that group should be collected
        NsclientCollection collection = NSClientDataCollectionConfigFactory.getInstance().getNSClientCollection(collectionName);
        NSClientAgentState agentState = m_scheduledNodes.get(agent.getNodeId());
        
        NSClientCollectionSet collectionSet=new NSClientCollectionSet(agent, new Date());
        NSClientCollectionResource collectionResource=collectionSet.getResource();
        
        for (Wpm wpm : collection.getWpms().getWpm()) {
            //All NSClient Perfmon counters are per node
            AttributeGroupType attribGroupType=new AttributeGroupType(wpm.getName(),"all");
            // A wpm consists of a list of attributes, identified by name
            if (agentState.shouldCheckAvailability(wpm.getName(), wpm.getRecheckInterval())) {
                log().debug("Checking availability of group " + wpm.getName());
                NsclientManager manager = null;
                try {
                    manager = agentState.getManager();
                    manager.init();
                    NsclientCheckParams params = new NsclientCheckParams(wpm.getKeyvalue());
                    NsclientPacket result = manager.processCheckCommand(NsclientManager.CHECK_COUNTER, params);
                    manager.close();
                    boolean isAvailable = (result.getResultCode() == NsclientPacket.RES_STATE_OK);
                    agentState.setGroupIsAvailable(wpm.getName(), isAvailable);
                    log().debug("Group "+wpm.getName()+" is "+(isAvailable?"":"not")+"available ");
                } catch (NsclientException e) {
                    log().error("Error checking group (" + wpm.getName() + ") availability", e);
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
                            log().info("unable to collect params for attribute '" + attrib.getName() + "'", e);
                        }

                        if (result != null) {
                            if (result.getResultCode() != NsclientPacket.RES_STATE_OK) {
                                log().info("not writing parameters for attribute '" + attrib.getName() + "', state is not 'OK'");
                            } else {
                                NSClientCollectionAttributeType attribType=new NSClientCollectionAttributeType(attrib, attribGroupType);
                                collectionResource.setAttributeValue(attribType, result.getResponse());
                                status = ServiceCollector.COLLECTION_SUCCEEDED;
                            }
                        }
                    }
                    manager.close(); // Only close once all the attribs have
                                        // been done (optimizing as much as
                                        // possible with NSClient)
                } catch (NsclientException e) {
                    log().error("Error collecting data", e);
                }
            }
        }
        collectionSet.setStatus(status);
        return collectionSet;
    }

    /** {@inheritDoc} */
    public void initialize(Map<String, String> parameters) {
        log().debug("initialize: Initializing NSClientCollector.");
        m_scheduledNodes.clear();
        initNSClientPeerFactory();
        initNSClientCollectionConfig();
        initDatabaseConnectionFactory();
        initializeRrdRepository();
    }

    private void initNSClientPeerFactory() {
        log().debug("initialize: Initializing NSClientPeerFactory");
        try {
            NSClientPeerFactory.init();
        } catch (MarshalException e) {
            log().fatal("initialize: Error marshalling configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initialize: Error validating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log().fatal("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initNSClientCollectionConfig() {
        log().debug("initialize: Initializing collector: " + getClass());
        try {
            NSClientDataCollectionConfigFactory.init();
        } catch (MarshalException e) {
            log().fatal("initialize: Error marshalling configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initialize: Error validating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (FileNotFoundException e) {
            log().fatal("initialize: Error locating configuration.", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log().fatal("initialize: Error reading configuration", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    private void initializeRrdRepository() {
        log().debug("initializeRrdRepository: Initializing RRD repo from NSClientCollector...");
        initializeRrdDirs();
    }

    private void initializeRrdDirs() {
        /*
         * If the RRD file repository directory does NOT already exist, create
         * it.
         */
        File f = new File(NSClientDataCollectionConfigFactory.getInstance().getRrdPath());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("Unable to create RRD file " + "repository.  Path doesn't already exist and could not make directory: " + DataCollectionConfigFactory.getInstance().getRrdPath());
            }
        }
    }

    private void initDatabaseConnectionFactory() {
        try {
            DataSourceFactory.init();
        } catch (IOException e) {
            log().fatal("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            log().fatal("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log().fatal("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            log().fatal("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            log().fatal("initDatabaseConnectionFactory: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /** {@inheritDoc} */
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) {
        log().debug("initialize: Initializing NSClient collection for agent: " + agent);
        Integer scheduledNodeKey = agent.getNodeId();
        NSClientAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);

        if (nodeState != null) {
            log().info("initialize: Not scheduling interface for NSClient collection: " + nodeState.getAddress());
            final StringBuffer sb = new StringBuffer();
            sb.append("initialize service: ");

            sb.append(" for address: ");
            sb.append(nodeState.getAddress());
            sb.append(" already scheduled for collection on node: ");
            sb.append(agent);
            log().debug(sb.toString());
            throw new IllegalStateException(sb.toString());
        } else {
            nodeState = new NSClientAgentState(agent.getInetAddress(), parameters);
            log().info("initialize: Scheduling interface for collection: " + nodeState.getAddress());
            m_scheduledNodes.put(scheduledNodeKey, nodeState);
        }
    }

    /**
     * <p>release</p>
     */
    public void release() {
        m_scheduledNodes.clear();
    }

    /** {@inheritDoc} */
    public void release(final CollectionAgent agent) {
        final Integer scheduledNodeKey = agent.getNodeId();
        NSClientAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);
        if (nodeState != null) {
            m_scheduledNodes.remove(scheduledNodeKey);
        }
    }

    private class NSClientAgentState {
        private NsclientManager m_manager;
        private NSClientAgentConfig m_agentConfig; // Do we need to keep this?
        private String m_address;
        private HashMap<String, NSClientGroupState> m_groupStates = new HashMap<String, NSClientGroupState>();

        public NSClientAgentState(InetAddress address, Map<String, Object> parameters) {
            m_address = InetAddressUtils.str(address);
            m_agentConfig = NSClientPeerFactory.getInstance().getAgentConfig(address);
            m_manager = new NsclientManager(m_address);
            m_manager.setPassword(m_agentConfig.getPassword());
            m_manager.setTimeout(m_agentConfig.getTimeout());
            m_manager.setPortNumber(m_agentConfig.getPort());
        }

        public String getAddress() {
            return m_address;
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
                log().warn("didCheckGroupAvailability called on a group without state - this is odd");
                return;
            }
            groupState.setLastChecked(new Date());
        }

    }

    private class NSClientGroupState {
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
    public RrdRepository getRrdRepository(String collectionName) {
        return NSClientDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }

}
