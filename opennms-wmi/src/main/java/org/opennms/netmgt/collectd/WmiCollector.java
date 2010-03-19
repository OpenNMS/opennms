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
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.collectd;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.collectd.wmi.WmiAgentState;
import org.opennms.netmgt.collectd.wmi.WmiCollectionAttributeType;
import org.opennms.netmgt.collectd.wmi.WmiCollectionResource;
import org.opennms.netmgt.collectd.wmi.WmiCollectionSet;
import org.opennms.netmgt.collectd.wmi.WmiMultiInstanceCollectionResource;
import org.opennms.netmgt.collectd.wmi.WmiSingleInstanceCollectionResource;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.WmiDataCollectionConfigFactory;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.wmi.Attrib;
import org.opennms.netmgt.config.wmi.WmiCollection;
import org.opennms.netmgt.config.wmi.Wpm;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.rrd.RrdException;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.protocols.wmi.WmiClient;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiParams;
import org.opennms.protocols.wmi.WmiResult;
import org.opennms.protocols.wmi.wbem.OnmsWbemObject;
import org.opennms.protocols.wmi.wbem.OnmsWbemObjectSet;
import org.opennms.protocols.wmi.wbem.OnmsWbemProperty;

/**
 * <P>
 * This class is designed to be used by the performance collection daemon to
 * collect various  WMI performance metrics from a remote server.
 * </P>
 *
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 */
public class WmiCollector implements ServiceCollector {

    // Don't make this static because each service will have its own
    // copy and the key won't require the service name as part of the key.
    private final HashMap<Integer, WmiAgentState> m_scheduledNodes = new HashMap<Integer, WmiAgentState>();
    private HashMap<String, AttributeGroupType> m_groupTypeList = new HashMap<String, AttributeGroupType>();
    private HashMap<String, WmiCollectionAttributeType> m_attribTypeList = new HashMap<String, WmiCollectionAttributeType>();

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, String> parameters) {

        String collectionName = parameters.get("collection");
        if (collectionName == null) {
            //Look for the old configuration style:
            collectionName = parameters.get("wmi-collection");
        }
        // Find attributes to collect - check groups in configuration. For each,
        // check scheduled nodes to see if that group should be collected
        WmiCollection collection = WmiDataCollectionConfigFactory.getInstance().getWmiCollection(collectionName);
        WmiAgentState agentState = m_scheduledNodes.get(agent.getNodeId());

        // Load the attribute group types.
        loadAttributeGroupList(collection);

        // Load the attribute types.
        loadAttributeTypeList(collection);

        // Create a new collection set.
        WmiCollectionSet collectionSet = new WmiCollectionSet(agent);        

        // Iterate through the WMI collection groups.
        for (Wpm wpm : collection.getWpms().getWpm()) {
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

                    OnmsWbemObjectSet wOS;

                    // Retrieve the WbemObjectSet from the class defined on the group.
                    wOS = client.performInstanceOf(wpm.getWmiClass());

                    // If we received a WbemObjectSet result, lets go through it and collect it.
                    if (wOS != null) {
                        //  Go through each object (class instance) in the object set.
                        for (int i = 0; i < wOS.count(); i++) {
                            // Create a new collection resource.
                            WmiCollectionResource resource = null;

                            // Fetch our WBEM Object
                            OnmsWbemObject obj = wOS.get(i);

                            // If this is multi-instance, fetch the instance name and store it.
                            if(wOS.count()>1) {
                                // Fetch the value of the key value. e.g. Name.
                                OnmsWbemProperty prop = obj.getWmiProperties().getByName(wpm.getKeyvalue());
                                Object propVal = prop.getWmiValue();
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


                            for (Attrib attrib : wpm.getAttrib()) {
                                OnmsWbemProperty prop = obj.getWmiProperties().getByName(attrib.getWmiObject());                                
                                WmiCollectionAttributeType attribType = m_attribTypeList.get(attrib.getName());
                                resource.setAttributeValue(attribType, prop.getWmiValue().toString());
                            }
                            collectionSet.getResources().add(resource);
                        }
                    }
                } catch (WmiException e) {
                    log().info("unable to collect params for wpm '" + wpm.getName() + "'", e);
                } finally {
                    if (client != null) {
                        try {
                            client.disconnect();
                        } catch (WmiException e) {
                            log().warn("An error occurred disconnecting while collecting from WMI", e);
                        }
                    }
                }
            }
        }
        collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
        return collectionSet;
    }

    private void loadAttributeGroupList(WmiCollection collection) {
        for (Wpm wpm : collection.getWpms().getWpm()) {
            AttributeGroupType attribGroupType1 = new AttributeGroupType(wpm.getName(), wpm.getIfType());
            m_groupTypeList.put(wpm.getName(), attribGroupType1);
        }
    }

    private void loadAttributeTypeList(WmiCollection collection) {
        for (Wpm wpm : collection.getWpms().getWpm()) {
            for (Attrib attrib : wpm.getAttrib()) {
                AttributeGroupType attribGroupType = m_groupTypeList.get(wpm.getName());
                WmiCollectionAttributeType attribType = new WmiCollectionAttributeType(attrib, attribGroupType);
                m_attribTypeList.put(attrib.getName(), attribType);
            }
        }
    }

    private boolean isGroupAvailable(WmiAgentState agentState, Wpm wpm) {
        log().debug("Checking availability of group " + wpm.getName());
        WmiManager manager;

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

            WmiParams params = new WmiParams(WmiParams.WMI_OPERATION_INSTANCEOF, "not-applicable", "NOOP",
                    wpm.getWmiClass(), wpm.getKeyvalue());

            WmiResult result = manager.performOp(params);
            manager.close();
            boolean isAvailable = (result.getResultCode() == WmiResult.RES_STATE_OK);
            agentState.setGroupIsAvailable(wpm.getName(), isAvailable);
            log().debug("Group " + wpm.getName() + " is " + (isAvailable ? "" : "not") + "available ");
        } catch (WmiException e) {
            //throw new WmiCollectorException("Error checking group (" + wpm.getName() + ") availability", e);
            // Log a warning signifying that this group is unavailable.
            log().warn("Error checking group (" + wpm.getName() + ") availability", e);
            // Set the group as unavailable.
            agentState.setGroupIsAvailable(wpm.getName(), false);
            // And then continue on to check the next wpm entry.
            return false;
        }
        return true;
    }

    public void initialize(Map parameters) {
        log().debug("initialize: Initializing WmiCollector.");
        m_scheduledNodes.clear();
        initWMIPeerFactory();
        initWMICollectionConfig();
        initDatabaseConnectionFactory();
        initializeRrdRepository();
    }

    private void initWMIPeerFactory() {
        log().debug("initialize: Initializing WmiPeerFactory");
        try {
            WmiPeerFactory.init();
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

    private void initWMICollectionConfig() {
        log().debug("initialize: Initializing collector: " + getClass());
        try {
            WmiDataCollectionConfigFactory.init();
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
        log().debug("initializeRrdRepository: Initializing RRD repo from WmiCollector...");
        initializeRrdDirs();
    }

    private void initializeRrdDirs() {
        /*
         * If the RRD file repository directory does NOT already exist, create
         * it.
         */
        File f = new File(WmiDataCollectionConfigFactory.getInstance().getRrdPath());
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

    public void initialize(CollectionAgent agent, Map parameters) {
        log().debug("initialize: Initializing WMI collection for agent: " + agent);
        Integer scheduledNodeKey = new Integer(agent.getNodeId());
        WmiAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);

        if (nodeState != null) {
            log().info("initialize: Not scheduling interface for WMI collection: " + nodeState.getAddress());
            final StringBuffer sb = new StringBuffer();
            sb.append("initialize service: ");

            sb.append(" for address: ");
            sb.append(nodeState.getAddress());
            sb.append(" already scheduled for collection on node: ");
            sb.append(agent);
            log().debug(sb.toString());
            throw new IllegalStateException(sb.toString());
        } else {
            nodeState = new WmiAgentState(agent.getInetAddress(), parameters);
            log().info("initialize: Scheduling interface for collection: " + nodeState.getAddress());
            m_scheduledNodes.put(scheduledNodeKey, nodeState);
        }
    }

    public void release() {
        m_scheduledNodes.clear();
    }

    public void release(CollectionAgent agent) {
        Integer scheduledNodeKey = new Integer(agent.getNodeId());
        WmiAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);
        if (nodeState != null) {
            m_scheduledNodes.remove(scheduledNodeKey);
        }
    }

    public RrdRepository getRrdRepository(String collectionName) {
        return WmiDataCollectionConfigFactory.getInstance().getRrdRepository(collectionName);
    }



}
