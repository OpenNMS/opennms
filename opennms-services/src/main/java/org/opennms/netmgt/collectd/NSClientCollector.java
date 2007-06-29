//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:

package org.opennms.netmgt.collectd;

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

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.NSClientDataCollectionConfigFactory;
import org.opennms.netmgt.config.NSClientPeerFactory;
import org.opennms.netmgt.config.nsclient.Attrib;
import org.opennms.netmgt.config.nsclient.NsclientCollection;
import org.opennms.netmgt.config.nsclient.Wpm;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.poller.nsclient.NSClientAgentConfig;
import org.opennms.netmgt.poller.nsclient.NsclientCheckParams;
import org.opennms.netmgt.poller.nsclient.NsclientException;
import org.opennms.netmgt.poller.nsclient.NsclientManager;
import org.opennms.netmgt.poller.nsclient.NsclientPacket;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.utils.EventProxy;

public class NSClientCollector implements ServiceCollector {
    
    //Don't make this static because each service will have its own
    //copy and the key won't require the service name as  part of the key.
    private final HashMap<Integer, NSClientAgentState> m_scheduledNodes = new HashMap<Integer, NSClientAgentState>();

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    @SuppressWarnings("unchecked")
    public int collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) {
        String collectionName=parameters.get("nsclient-collection");
        final CollectionAgent theAgent=agent; //For ResourceIdentifier anonymous class to access the var
        
        //Find out what attribs to collect for this node, then collect and store them
        
        //Find attribs to collect - check groups in configuration.  For each, check scheduled nodes to see if that group should be collected
        NsclientCollection collection=NSClientDataCollectionConfigFactory.getInstance().getNSClientCollection(collectionName);
        NSClientAgentState agentState=m_scheduledNodes.get(agent.getNodeId());
 
        for(Wpm wpm : collection.getWpms().getWpm()) {
            //A wpm consists of a list of attributes, identified by name
            if(agentState.shouldCheckAvailability(wpm.getName(), wpm.getRecheckInterval())) {
                log().debug("Checking availability of group "+wpm.getName());
                try {
                    NsclientManager manager = agentState.getManager();
                    manager.init();
                    NsclientCheckParams params = new NsclientCheckParams(wpm.getKeyvalue());
                    NsclientPacket result = manager.processCheckCommand(
                                                                        NsclientManager.CHECK_COUNTER,
                                                                        params);
                    manager.close();
                    boolean isAvailable=(result.getResultCode()==NsclientPacket.RES_STATE_OK);
                    agentState.setGroupIsAvailable(wpm.getName(), isAvailable);
                } catch (NsclientException e) {
                    throw new NSClientCollectorException(
                    "Error checking group ("+wpm.getName()+") availability", e);
                }
          }
            
            if(agentState.groupIsAvailable(wpm.getName())) {
                //Collect the data
                RrdRepository rrdRepository = NSClientDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
                ResourceIdentifier resource = new ResourceIdentifier() {
                    public String getOwnerName() {
                        return theAgent.getHostAddress();
                    }
                    public File getResourceDir(RrdRepository repository) {
                        return new File(repository.getRrdBaseDir(), Integer.toString(theAgent.getNodeId()));
                    }
                };
                try {
                    NsclientManager manager = agentState.getManager();
                    manager.init(); //Open the connection, then do each attribute

                    for (Attrib attrib : wpm.getAttrib()) {
                        NsclientCheckParams params = new NsclientCheckParams(
                                                                             attrib.getName());
                        NsclientPacket result = manager.processCheckCommand(
                                                             NsclientManager.CHECK_COUNTER,
                                                             params);

                        NSClientCollectionAttribute attribute = new NSClientCollectionAttribute(
                                                                                                attrib.getAlias(),
                                                                                                attrib.getType(),
                                                                                                result.getResponse());
                        PersistOperationBuilder builder = new PersistOperationBuilder(
                                                                                      rrdRepository,
                                                                                      resource,
                                                                                      attribute.getName());
                        builder.declareAttribute(attribute);
                        log().debug(
                                    "doCollection: setting attribute: "
                                            + attribute);
                        builder.setAttributeValue(attribute,
                                                  attribute.getValue());
                        try {
                            builder.commit();
                        } catch (RrdException e) {
                            throw new NSClientCollectorException(
                                                                 "Error writing RRD", e);
                        }
                    }
                    manager.close(); //Only close once all the attribs have been done (optimizing as much as possible with NSClient)
                } catch (NsclientException e) {
                    throw new NSClientCollectorException(
                                                         "Error collecting data", e);
                }
            }
        }
         

        
        return ServiceCollector.COLLECTION_SUCCEEDED;
    }
    
    class NSClientCollectionAttribute implements AttributeDefinition {
        String m_alias;
        String m_type;
        String m_value;
        
        NSClientCollectionAttribute(String alias, String type, String value) {
            m_alias = alias;
            m_type= type;
            m_value = value;
        }

        public String getName() {
            return m_alias;
        }

        public String getType() {
            return m_type;
        }
        
        public String getValue() {
            return m_value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NSClientCollectionAttribute) {
                NSClientCollectionAttribute other = (NSClientCollectionAttribute)obj;
                return getName().equals(other.getName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }

        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("NSClientCollectionAttribute: ");
            buffer.append(getName());
            buffer.append(":");
            buffer.append(getType());
            buffer.append(":");
            buffer.append(getValue());
            return buffer.toString();
        }
        
    }
 
    public class NSClientCollectorException extends RuntimeException {
        public static final long serialVersionUID = 1L;

        NSClientCollectorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public void initialize(Map parameters) {
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
        initializeRrdInterface();
    }

    private void initializeRrdDirs() {
    /*
     * If the RRD file repository directory does NOT already exist, create it.
     */
        File f = new File(NSClientDataCollectionConfigFactory.getInstance().getRrdPath());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException(
                                           "Unable to create RRD file "
                                                   + "repository.  Path doesn't already exist and could not make directory: "
                                                   + DataCollectionConfigFactory.getInstance().getRrdPath());
            }
        }
    }


    private void initializeRrdInterface() {
        try {
            RrdUtils.initialize();
        } catch (RrdException e) {
            log().error("initializeRrdInterface: Unable to initialize RrdUtils", e);
            throw new RuntimeException("Unable to initialize RrdUtils", e);
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
    
    public void initialize(CollectionAgent agent, Map parameters) {
        log().debug("initialize: Initializing NSClient collection for agent: "+agent);
        Integer scheduledNodeKey = new Integer(agent.getNodeId());
        NSClientAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);
        
        if (nodeState != null) {
            log().info(
                       "initialize: Not scheduling interface for NSClient collection: " + nodeState.getAddress());
            final StringBuffer sb = new StringBuffer();
            sb.append("initialize service: ");
            
            sb.append(" for address: ");
            sb.append(nodeState.getAddress());
            sb.append(" already scheduled for collection on node: ");
            sb.append(agent);
            log().debug(sb.toString());
            throw new IllegalStateException(sb.toString());
        } else {
            nodeState=new NSClientAgentState(agent.getInetAddress(), parameters);
            log().info("initialize: Scheduling interface for collection: "+nodeState.getAddress());
            m_scheduledNodes.put(scheduledNodeKey, nodeState);
        }
    }

    public void release() {
        m_scheduledNodes.clear();
    }

    public void release(CollectionAgent agent) {
        Integer scheduledNodeKey = new Integer(agent.getNodeId());
        NSClientAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);
        if(nodeState!=null) {
            m_scheduledNodes.remove(scheduledNodeKey);
        }
    }

    private class NSClientAgentState {
        private NsclientManager m_manager;
        private NSClientAgentConfig m_agentConfig; //Do we need to keep this?
        private String m_address;
        private HashMap<String, NSClientGroupState> m_groupStates=new HashMap<String, NSClientGroupState>();
        
        public NSClientAgentState(InetAddress address, Map parameters) {
            m_address=address.getHostAddress();
            m_agentConfig=NSClientPeerFactory.getInstance().getAgentConfig(address);
            m_manager=new NsclientManager(m_address);
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
           NSClientGroupState groupState=m_groupStates.get(groupName);
           if(groupState==null) {
               return false; //If the group availability hasn't been set yet, it's not available.
           }
           return groupState.isAvailable();
        }
        
        public void setGroupIsAvailable(String groupName, boolean available) {
            NSClientGroupState groupState=m_groupStates.get(groupName);
            if(groupState==null) {
                groupState=new NSClientGroupState(available);
            }
            groupState.setAvailable(available);
            m_groupStates.put(groupName, groupState);
        }
        
        public boolean shouldCheckAvailability(String groupName, int recheckInterval) {
            NSClientGroupState groupState=m_groupStates.get(groupName);
            if(groupState==null) {
                //If the group hasn't got a status yet, then it should be checked regardless (and setGroupIsAvailable will
                // be called soon to create the status object)
                return true; 
            }
            Date lastchecked=groupState.getLastChecked();
            Date now=new Date();
            return (now.getTime()-lastchecked.getTime()>recheckInterval);
        }
        
        public void didCheckGroupAvailability(String groupName) {
            NSClientGroupState groupState=m_groupStates.get(groupName);
            if(groupState==null) {
                //Probably an error - log it as a warning, and give up
                log().warn("didCheckGroupAvailability called on a group without state - this is odd");
                return;
            }
            groupState.setLastChecked(new Date());
        }
       
    }
    
    private class NSClientGroupState {
        private boolean available=false;
        private Date lastChecked;
        
        public NSClientGroupState(boolean isAvailable) {
           this(isAvailable, new Date());
        }
        
        public NSClientGroupState(boolean isAvailable, Date lastChecked) {
            this.available=isAvailable;
            this.lastChecked=lastChecked;
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
}
