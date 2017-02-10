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

/************************************************************************
 * Change history
 *
 * 2013-04-18 Updated package names to match new XMP JAR (jeffg@opennms.org)
 *
 ************************************************************************/

/*
* OCA CONTRIBUTION ACKNOWLEDGEMENT - NOT PART OF LEGAL BOILERPLATE
* DO NOT DUPLICATE THIS COMMENT BLOCK WHEN CREATING NEW FILES!
*
* This file was contributed to the OpenNMS(R) project under the
* terms of the OpenNMS Contributor Agreement (OCA).  For details on
* the OCA, see http://www.opennms.org/index.php/Contributor_Agreement
*
* Contributed under the terms of the OCA by:
*
* Bobby Krupczak <rdk@krupczak.org>
* THE KRUPCZAK ORGANIZATION, LLC
* http://www.krupczak.org/
*/

/**
 *   OpenNMS XMP Collector class allows for the querying of XMP-enabled
 *   devices by the OpenNMS network management software suite.  XMP is
 *   the <b>X</b>ML <b>M</b>management <b>P</b>rotocol; One XmpCollector
 *   object is instantiated per OpenNMS instance and its responsible for
 *   querying all XMP-enabled systems.  Consequently, we need to be
 *   careful about leaving XMP sessions open for too long.
 *   @author <a href="mailto:rdk@krupczak.org">Bobby Krupczak</a>
 *   @version $Id: XmpCollector.java 38 2008-07-24 13:39:32Z rdk $
 */

package org.opennms.netmgt.protocols.xmp.collector;

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.krupczak.xmp.SocketOpts;
import org.krupczak.xmp.Xmp;
import org.krupczak.xmp.XmpMessage;
import org.krupczak.xmp.XmpSession;
import org.krupczak.xmp.XmpVar;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.AbstractLegacyServiceCollector;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;
import org.opennms.netmgt.config.xmpDataCollection.Group;
import org.opennms.netmgt.config.xmpDataCollection.MibObj;
import org.opennms.netmgt.config.xmpDataCollection.XmpCollection;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.protocols.xmp.config.XmpAgentConfig;
import org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory;
import org.opennms.netmgt.protocols.xmp.config.XmpPeerFactory;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class XmpCollector extends AbstractLegacyServiceCollector {
	
	private static final Logger LOG = LoggerFactory.getLogger(XmpCollector.class);


    /* class variables and methods *********************** */
    static final String SERVICE_NAME = "XMP";

    /* instance variables ******************************** */
    int xmpPort;
    int timeout;  /* millseconds */
    int retries;
    Set<CollectionAgent> setOfNodes;
    SocketOpts sockopts;
    String authenUser;

    /* constructors  ************************************* */
    /**
     * <p>Constructor for XmpCollector.</p>
     */
    public XmpCollector() 
    {
        LOG.debug("XmpCollector created");

        // initialize collections and containers for storing
        // list of systems to query 
        setOfNodes = new HashSet<CollectionAgent>();

        // defaults
        xmpPort = Xmp.XMP_PORT;
        sockopts = new SocketOpts();
        authenUser = new String("xmpUser"); 
        timeout = 3000; /* millseconds */

        return; 
    }

    /* private methods *********************************** */
   

    // handle scalar query and put in a single collection resource
    // devoted to scalars; check sysUptime if its present
    // and indicate if data should be persisted 

    private boolean handleScalarQuery(String groupName,
            XmpCollectionSet collectionSet,
            long oldUptime,
            XmpSession session, 
            XmpCollectionResource scalarResource, 
            XmpVar[] queryVars)
    {
        XmpMessage reply;
        AttributeGroupType agt;
        AttributeGroup ag;
        long newUptime;
        int i;
        XmpVar[] vars;
        XmpCollectionAttribute aVar;
        XmpCollectionAttributeType attribType;

        //log().debug("sending scalar query");
        reply = session.queryVars(queryVars);

        if (reply == null) {
            LOG.warn("collect: query to {} failed, {}", collectionSet.getCollectionAgent(), Xmp.errorStatusToString(session.getErrorStatus()));
            return false;
        }

        agt = new AttributeGroupType(groupName, AttributeGroupType.IF_TYPE_IGNORE);
        ag = new AttributeGroup(scalarResource,agt);

        // for each variable in reply, store it in collectionSet
        // hack alert: somewhere in some query, we asked for
        // sysUptime; find it save value for later

        // vars[i] should match up with mibObjects[i] !!!

        vars = reply.getMIBVars();

        newUptime = 0;

        for (i=0; i<vars.length; i++) {

            if (vars[i].getMibName().equals("core") &&
                    vars[i].getObjName().equals("sysUpTime")) {
                newUptime = vars[i].getValueLong();
            }

            // put in collectionSet via this attribute group

            attribType = new XmpCollectionAttributeType(vars[i],agt);
            aVar = new XmpCollectionAttribute(scalarResource,
                                              attribType,
                                              vars[i]);

            ag.addAttribute(aVar);
        }

        if (newUptime > oldUptime) { 
            collectionSet.ignorePersistFalse();
        }

        if (newUptime > 0) {
            // save the agent's sysUpTime in the CollectionAgent
            collectionSet.getCollectionAgent().setSavedSysUpTime(newUptime);
        }

        scalarResource.addAttributeGroup(ag);

        return true;

    } /* handleScalarQuery */

    // handle a tabular query, save each row in its own
    // collection resource
    private boolean handleTableQuery(String groupName, 
            String resourceType,
            XmpCollectionSet collectionSet,
            String[] tableInfo,
            XmpSession session, 
            XmpVar[] queryVars)
    {
        int numColumns,numRows;
        XmpMessage reply;
        int i,j;
        XmpVar[] vars;
        String targetInstance;

        numColumns = queryVars.length;

        // make sure we have an instance or * for all rows; preserve
        // passed in value as targetInstance so we know if we are
        // are going to use targetInstance for saving results or
        // use returned instance for saving values

        // if resourceType is present, we use it as a subDir in
        // our RRD dir

        targetInstance = tableInfo[2];
        if ((tableInfo[2] == null) || (tableInfo[2].length() == 0)) {
            tableInfo[2] = new String("*");
            targetInstance = null;
        }

        LOG.debug("sending table query {},{},{} target: {}", tableInfo[0], tableInfo[1], tableInfo[2], targetInstance);

        reply = session.queryTableVars(tableInfo,0,queryVars);

        if (reply == null) {
            LOG.warn("collect: query to {} failed, {}", collectionSet.getCollectionAgent(), Xmp.errorStatusToString(session.getErrorStatus()));
            return false;
        }

        vars = reply.getMIBVars();

        // we have to go through the reply and find out how 
        // many rows we have

        // for each row: create a CollectionResource of
        //               appropriate type, instance, etc.
        //               create AttributeGroup to put 
        //               the values in 

        numRows = vars.length / numColumns;

        LOG.info("query returned valid table data for {} numRows={} numColumns={}", groupName, numRows, numColumns);

        for (i=0; i<numRows; i++) {

            XmpCollectionResource rowResource;
            AttributeGroup ag;
            AttributeGroupType agt;
            String rowInstance;

            // determine instance for this row
            // we use either the rowInstance or targetInstance for
            // naming the instance for saving RRD file; if user
            // wanted all rows (blank instance), then we will use
            // the returned instance; if user specified an instance
            // we use that instance for specifying the RRD file
            // and collection resource

            rowInstance = vars[i*numColumns].getKey();

            // instead of using '*' for the nodeTypeName, use the
            // table name so that the proper rrd file is spec'd

            if (targetInstance != null)
                rowResource = new XmpCollectionResource(collectionSet.getCollectionAgent(),resourceType, tableInfo[1],targetInstance);
            else 
                rowResource = new XmpCollectionResource(collectionSet.getCollectionAgent(),resourceType, tableInfo[1],rowInstance);

            agt = new AttributeGroupType(groupName, AttributeGroupType.IF_TYPE_ALL);
            ag = new AttributeGroup(rowResource,agt);

            LOG.debug("queryTable instance={}", rowInstance);

            for (j=0; j<numColumns; j++) {

                XmpCollectionAttributeType attribType = new XmpCollectionAttributeType(vars[i*numColumns+j],agt);

                XmpCollectionAttribute aVar = 
                    new XmpCollectionAttribute(rowResource,
                                               attribType,
                                               vars[i*numColumns+j]);

                ag.addAttribute(aVar);

            } /* for each column */

            rowResource.addAttributeGroup(ag);
            collectionSet.addResource(rowResource);
            LOG.info("query table data adding row resource {}", rowResource);

        } /* for each row returned */

        return true;

    } /* handleTableQuery() */

    /* public methods ************************************ */

    /**
     * {@inheritDoc}
     *
     * initialize our XmpCollector with global parameters *
     */
    @Override
    public void initialize(Map<String, String> parameters)
    {
        // parameters come from collectd-configuration.xml 
        // and they are the service parameters specified in xml
        // with keyname and value
        // parameter key=collection value=default

        // initialize our data collection factory

        LOG.debug("initialize(params) called");

        try {
            XmpCollectionFactory.init();
        } catch (Throwable e) {
            LOG.error("initialize: XmpCollectionFactory failed to initialize");
            throw new UndeclaredThrowableException(e);
        }
        
        try {
            XmpPeerFactory.init();
        } catch (Throwable e) {
            LOG.error("initialize: XmpPeerFactory failed to initialize");
            throw new UndeclaredThrowableException(e);
        }

        // initialize authenUser, port, timeout, other parameters
        // want a xmp-config.xml for port, authenUser, timeout, etc.

        try {
            XmpConfigFactory.init();
        } catch (Throwable e) {
            LOG.error("initialize: config factory failed to initialize");
            throw new UndeclaredThrowableException(e);
        }

        // initialize an RRD repository with various parameters 
        // /opt/opennms/share/rrd/snmp/

        File f = new File(XmpCollectionFactory.getInstance().getRrdPath());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("Unable to create RRD file " + "repository.  Path doesn't already exist and could not make directory: " + 
                                           XmpCollectionFactory.getInstance().getRrdPath());
            }
        }

        // get our top-level object for our protocol config file,
        // xmp-config.xml, already parsed and ready to examine
        XmpConfig protoConfig = XmpConfigFactory.getInstance().getXmpConfig();

        if (protoConfig.hasPort())
            xmpPort = protoConfig.getPort();
        if (protoConfig.hasTimeout())
            timeout = protoConfig.getTimeout();

        // authenUser is optional; if it is present, it will
        // be non-null
        if (protoConfig.getAuthenUser() != null)
            authenUser = protoConfig.getAuthenUser();

        LOG.debug("initialize: authenUser '{}' port {}", authenUser, xmpPort);
        LOG.debug("initialize: keystore found? {}", sockopts.getKeystoreFound());

        return;

    } /* initialize() */

    /**
     * {@inheritDoc}
     *
     * initialize the querying of a particular agent/interface with
     * parameters specific to this agent/interface *
     */
    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters)
    {
        LOG.debug("initialize agent/params called for {}", agent);

        // add an agent to our set to query
        setOfNodes.add(agent);

        // we are using whichever CollectionAgent instantiation
        // is passed into us.

        // parameters include SERVICE/service-name 
        // superset of parameters passed in main initialize
        // ignore for now; other parameters like collection name


        return;
    }

    /**
     * Release/stop all querying of agents/interfaces and release
     *       state associated with them *
     */
    @Override
    public void release() 
    {
        LOG.info("release()");

        // orphan existing set thus making them available
        // for garbage collection 
        setOfNodes = new HashSet<CollectionAgent>();

        return;
    }

    /**
     * {@inheritDoc}
     *
     * Release/stop querying a particular agent *
     */
    @Override
    public void release(CollectionAgent agent)
    {
        LOG.info("release agent called for {}",agent);

        // remove agent from set; ignore return value
        setOfNodes.remove(agent);

        return;
    }

    /**
     * who am I and what am I ? *
     *
     * @return a {@link java.lang.String} object.
     */
    public String serviceName() { return SERVICE_NAME; }

    /**
     * {@inheritDoc}
     *
     * Collect data, via XMP, from a particular agent EventProxy is
     *       used to send opennms events into the system in case a
     *       collection fails or if a system is back working again after a
     *       failure (suceed event).  But otherwise, no events sent if
     *       collection succeeds.  Collect is called once per agent per
     *       collection cycle.  Parameters are a map of String Key/String
     *       Value passed in.  Keys come from collectd config
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, 
            Map<String, Object> parameters)
    {
        XmpCollectionSet collectionSet;
        XmpSession session;
        long oldUptime;
        int i;
        XmpCollection collection;
        XmpCollectionResource scalarResource;

        LOG.debug("collect agent {}",agent);

        oldUptime = 0;
        
        // First go to the peer factory
        XmpAgentConfig peerConfig = XmpPeerFactory.getInstance().getAgentConfig(agent.getAddress());
        authenUser = peerConfig.getAuthenUser();
        timeout = (int)peerConfig.getTimeout();
        retries = peerConfig.getRetry();
        xmpPort = peerConfig.getPort();

        if (parameters.get("authenUser") != null)
            authenUser = ParameterMap.getKeyedString(parameters, "authenUser", null);

        if (parameters.get("timeout") != null) {
            timeout = ParameterMap.getKeyedInteger(parameters, "timeout", 3000);
        }
        
        if (parameters.get("retry") != null) {
            retries = ParameterMap.getKeyedInteger(parameters, "retries", 0);
        }
        parameters.get("collection");

        if (parameters.get("port") != null) {
            xmpPort = Integer.valueOf((String)parameters.get("port"));
        }

        //log().debug("collect got parameters for "+agent);

        String collectionName = ParameterMap.getKeyedString(parameters, "collection", null);

        //log().debug("XMP collection name "+collectionName);

        // collectionName tells us what set of data to get 
        // this would/will come from xmp-datacollection.xml
        if (collectionName == null) {
            // log this!
            LOG.warn("collect found no collectionName for {}", agent);
            return null;
        }

        //log().debug("collect got collectionName for "+agent);
        LOG.debug("XmpCollector: collect {} from {}", collectionName, agent);

        // get/create our collections set
        collectionSet = new XmpCollectionSet(agent);
        collectionSet.setCollectionTimestamp(new Date());
        collectionSet.setStatusFailed(); // default
        collectionSet.ignorePersistTrue(); // default not to persist

        // default collection resource for putting scalars in
        scalarResource = new XmpCollectionResource(agent,null,"node",null);
        collectionSet.addResource(scalarResource);

        // get the collection, again, from the data config file factory
        // because it could have changed; its not necessarily re-parsed,
        // but we are getting another copy of it for each agent
        // that we are queried each time we are invoked

        collection = XmpCollectionFactory.getInstance().getXmpCollection(collectionName);
        if (collection == null) {
            LOG.warn("collect found no matching collection for {}", agent);
            return collectionSet;
        }

        oldUptime = agent.getSavedSysUpTime();

        // open/get a session with the target agent

        LOG.debug("collect: attempting to open XMP session with {}:{},{}", agent.getAddress(), xmpPort, authenUser);

        // Set the SO_TIMEOUT, why don't we...
        sockopts.setConnectTimeout(timeout);

        session = new XmpSession(sockopts, agent.getAddress(), xmpPort,authenUser);

        if (session.isClosed()) {
            LOG.warn("collect unable to open XMP session with {}", agent);
            return collectionSet;
        }

        LOG.debug("collect: successfully opened XMP session with{}", agent);

        // for each group within the collection (from data config)
        // query agent

        for (Group group : collection.getGroups().getGroup()) {

            // get name of group and MIB objects in group
            String groupName = group.getName();
            MibObj[] mibObjects = group.getMibObj();
            XmpVar[] vars = new XmpVar[mibObjects.length];

            LOG.debug("collecting XMP group {} with {} mib objects", groupName, mibObjects.length);

            // prepare the query vars
            for (i=0 ; i< mibObjects.length; i++) {

                vars[i] = new XmpVar(mibObjects[i].getMib(),
                                     mibObjects[i].getVar(), 
                                     mibObjects[i].getInstance(),
                                     "",
                                     Xmp.SYNTAX_NULLSYNTAX);

            } /* for each MIB object in a particular group */

            if ((mibObjects[0].getTable() != null) && 
                    (mibObjects[0].getTable().length() != 0)) {

                String[] tableInfo = new String[3];
                tableInfo[0] = mibObjects[0].getMib();
                tableInfo[1] = mibObjects[0].getTable();
                tableInfo[2] = mibObjects[0].getInstance();

                // tabular query               
                if (handleTableQuery(group.getName(),
                                     group.getResourceType(),
                                     collectionSet,
                                     tableInfo,
                                     session,
                                     vars) == false) {
                    session.closeSession();
                    return collectionSet;
                }
            }
            else {
                // scalar query
                if (handleScalarQuery(group.getName(),
                                      collectionSet,
                                      oldUptime,
                                      session,
                                      scalarResource,
                                      vars) == false) {
                    session.closeSession();
                    return collectionSet;
                }
            }

        } /* for each Group in collection Group list */

        // done talking to this agent; close session
        session.closeSession();

        // Did agent restart since last query?  If so, set
        // ignorePersist to true; our scalar
        // query will have handled this by searching returned
        // MIB objects for sysUpTime

        // WARNING, EACH COLLECTION SHOULD HAVE A SCALAR QUERY THAT
        // INCLUDES Core.sysUpTime 

        collectionSet.setStatus(CollectionStatus.SUCCEEDED);

        LOG.debug("XMP collect finished for {}, uptime for {} is {}", collectionName, agent, agent.getSavedSysUpTime());

        return collectionSet;
    }

    /** {@inheritDoc} */
    @Override
    public RrdRepository getRrdRepository(String collectionName)
    {
        LOG.debug("XMP getRrdRepository called for {}", collectionName);

        // return the Rrd that I initialized but
        // I don't have to put data in it; initialize
        // it with the defaults, as example, that SNMP uses
        // in datacollection-config.xml 

        return XmpCollectionFactory.getInstance().getRrdRepository(collectionName);
    }

} /* class XmpCollector */
