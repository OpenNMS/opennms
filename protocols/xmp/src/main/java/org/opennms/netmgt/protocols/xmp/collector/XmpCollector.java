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

import java.lang.reflect.UndeclaredThrowableException;
import java.util.Map;

import org.krupczak.xmp.SocketOpts;
import org.krupczak.xmp.Xmp;
import org.krupczak.xmp.XmpMessage;
import org.krupczak.xmp.XmpSession;
import org.krupczak.xmp.XmpVar;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.AbstractServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.api.ResourceTypesDao;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.xmpConfig.XmpConfig;
import org.opennms.netmgt.config.xmpDataCollection.Group;
import org.opennms.netmgt.config.xmpDataCollection.MibObj;
import org.opennms.netmgt.config.xmpDataCollection.XmpCollection;
import org.opennms.netmgt.protocols.xmp.config.XmpAgentConfig;
import org.opennms.netmgt.protocols.xmp.config.XmpConfigFactory;
import org.opennms.netmgt.protocols.xmp.config.XmpPeerFactory;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmpCollector extends AbstractServiceCollector {
	
	private static final Logger LOG = LoggerFactory.getLogger(XmpCollector.class);


    /* class variables and methods *********************** */
    static final String SERVICE_NAME = "XMP";

    /* instance variables ******************************** */
    int xmpPort;
    int timeout;  /* millseconds */
    int retries;
    SocketOpts sockopts;
    String authenUser;

    private ResourceTypesDao m_resourceTypesDao;

    /* constructors  ************************************* */
    /**
     * <p>Constructor for XmpCollector.</p>
     */
    public XmpCollector() 
    {
        LOG.debug("XmpCollector created");

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
            CollectionAgent agent,
            CollectionSetBuilder collectionSetBuilder,
            long oldUptime,
            XmpSession session, 
            NodeLevelResource nodeLevelResource,
            XmpVar[] queryVars)
    {
        XmpMessage reply;
        long newUptime;
        int i;
        XmpVar[] vars;

        //log().debug("sending scalar query");
        reply = session.queryVars(queryVars);

        if (reply == null) {
            LOG.warn("collect: query to {} failed, {}", agent, Xmp.errorStatusToString(session.getErrorStatus()));
            return false;
        }

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

            final XmpVar xmpVar = vars[i];
            collectionSetBuilder.withAttribute(nodeLevelResource, groupName, xmpVar.getObjName(), xmpVar.getValue(), getType(xmpVar));
        }

        if (newUptime > oldUptime) { 
            collectionSetBuilder.disableCounterPersistence(false);
        }

        if (newUptime > 0) {
            // save the agent's sysUpTime in the CollectionAgent
            agent.setSavedSysUpTime(newUptime);
        }

        return true;

    } /* handleScalarQuery */

    // handle a tabular query, save each row in its own
    // collection resource
    private boolean handleTableQuery(String groupName, 
            String resourceType,
            CollectionAgent agent,
            CollectionSetBuilder collectionSetBuilder,
            String[] tableInfo,
            XmpSession session,
            NodeLevelResource nodeLevelResource,
            XmpVar[] queryVars) throws CollectionException
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
            LOG.warn("collect: query to {} failed, {}", agent, Xmp.errorStatusToString(session.getErrorStatus()));
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

            final String instanceName;
            if (targetInstance != null) {
                instanceName = targetInstance;
            } else {
                instanceName = rowInstance;
            }

            // node type can be "node" for scalars or
            // "if" for network interface resources and
            // "*" for all other resource types
            final String nodeTypeName = tableInfo[1];

            final Resource resource = getResource(nodeLevelResource, nodeTypeName, resourceType, instanceName);

            LOG.debug("queryTable instance={}", rowInstance);

            for (j=0; j<numColumns; j++) {
                final XmpVar var = vars[i*numColumns+j];
                collectionSetBuilder.withAttribute(resource, groupName, var.getObjName(), var.getValue(), getType(var));
            } /* for each column */
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
    public void initialize()
    {
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

        if (m_resourceTypesDao == null) {
            m_resourceTypesDao = BeanUtils.getBean("daoContext", "resourceTypesDao", ResourceTypesDao.class);
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
     * @throws CollectionException
     */
    @Override
    public CollectionSet collect(CollectionAgent agent, 
            Map<String, Object> parameters) throws CollectionException
    {
        CollectionSetBuilder collectionSetBuilder;
        XmpSession session;
        long oldUptime;
        int i;
        XmpCollection collection;

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
        collectionSetBuilder = new CollectionSetBuilder(agent)
                .withStatus(CollectionStatus.FAILED) // default to failed
                .disableCounterPersistence(true); // don't persist counters by default

        // default collection resource for putting scalars in
        final NodeLevelResource nodeLevelResource = new NodeLevelResource(agent.getNodeId());

        // get the collection, again, from the data config file factory
        // because it could have changed; its not necessarily re-parsed,
        // but we are getting another copy of it for each agent
        // that we are queried each time we are invoked

        collection = XmpCollectionFactory.getInstance().getXmpCollection(collectionName);
        if (collection == null) {
            LOG.warn("collect found no matching collection for {}", agent);
            return collectionSetBuilder.build();
        }

        if (collection.getGroups().getGroup().length < 1) {
            LOG.info("No groups to collect.");
            return collectionSetBuilder.withStatus(CollectionStatus.SUCCEEDED).build();
        }

        oldUptime = agent.getSavedSysUpTime();

        // open/get a session with the target agent

        LOG.debug("collect: attempting to open XMP session with {}:{},{}", agent.getAddress(), xmpPort, authenUser);

        // Set the SO_TIMEOUT, why don't we...
        sockopts.setConnectTimeout(timeout);

        session = new XmpSession(sockopts, agent.getAddress(), xmpPort,authenUser);

        if (session.isClosed()) {
            LOG.warn("collect unable to open XMP session with {}", agent);
            return collectionSetBuilder.build();
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
                                     agent,
                                     collectionSetBuilder,
                                     tableInfo,
                                     session,
                                     nodeLevelResource,
                                     vars) == false) {
                    session.closeSession();
                    return collectionSetBuilder.build();
                }
            }
            else {
                // scalar query
                if (handleScalarQuery(group.getName(),
                                      agent,
                                      collectionSetBuilder,
                                      oldUptime,
                                      session,
                                      nodeLevelResource,
                                      vars) == false) {
                    session.closeSession();
                    return collectionSetBuilder.build();
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

        collectionSetBuilder.withStatus(CollectionStatus.SUCCEEDED);

        LOG.debug("XMP collect finished for {}, uptime for {} is {}", collectionName, agent, agent.getSavedSysUpTime());

        return collectionSetBuilder.build();
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

    private static AttributeType getType(XmpVar var) {
        switch (var.getSyntax()) {
        case Xmp.SYNTAX_COUNTER:
            return AttributeType.COUNTER;
        case Xmp.SYNTAX_GAUGE:
        case Xmp.SYNTAX_INTEGER:
        case Xmp.SYNTAX_UNSIGNEDINTEGER:
        case Xmp.SYNTAX_FLOATINGPOINT:
            return AttributeType.GAUGE;
        case Xmp.SYNTAX_IPV4ADDRESS:
        case Xmp.SYNTAX_IPV6ADDRESS:
        case Xmp.SYNTAX_DATETIME:
        case Xmp.SYNTAX_BOOLEAN:
        case Xmp.SYNTAX_MACADDRESS:
        case Xmp.SYNTAX_PHYSADDRESS:
        case Xmp.SYNTAX_DISPLAYSTRING:
        case Xmp.SYNTAX_BINARYSTRING:
        case Xmp.SYNTAX_EXTENDEDBOOLEAN:
        case Xmp.SYNTAX_UNSUPPORTEDVAR:
            return AttributeType.STRING;
            // should not ever see these
        case Xmp.SYNTAX_NULLSYNTAX:
        case Xmp.SYNTAX_TABLE:
        default:
            return AttributeType.STRING;
        } /* Xmp syntax/type */
    }

    protected static String sanitizeInstance(String instance) {
        // filter the instance so it does not have slashes (/) nor colons
        // in it as they can munge our rrd file layout

        // filter so there are not spaces either just so that
        // it makes directory structures less annoying to deal with
        // rdk - 9/11/2009

        String instanceValue = instance.replace('/','_');
        instanceValue = instanceValue.replace('\\','_');
        instanceValue = instanceValue.replace(':','_');
        instanceValue = instanceValue.replace(' ','_');
        return instanceValue;
    }

    protected Resource getResource(NodeLevelResource nodeLevelResource, String nodeTypeName, String resourceType, String instance) throws CollectionException {
        if (CollectionResource.RESOURCE_TYPE_NODE.equalsIgnoreCase(nodeTypeName)) {
            return nodeLevelResource;
        }

        final String effectiveResourceType;
        if ((resourceType == null) || (resourceType.length() == 0)) {
            effectiveResourceType = null;
        } else {
            effectiveResourceType = resourceType;
        }

        final String effectiveInstance;
        if (instance != null) {
            effectiveInstance = XmpCollector.sanitizeInstance(instance);
        } else {
            effectiveInstance = null;
        }

        if (effectiveResourceType != null) {
            final ResourceType resourceTypeDef = m_resourceTypesDao.getResourceTypeByName(effectiveResourceType);
            if (resourceType == null) {
                throw new CollectionException("No resource type found with name '" + effectiveResourceType + "'.");
            }
            return new GenericTypeResource(nodeLevelResource, resourceTypeDef, effectiveInstance);
        } else {
            return new InterfaceLevelResource(nodeLevelResource, effectiveInstance);
        }
    }

    public void setResourceTypesDao(ResourceTypesDao resourceTypesDao) {
        m_resourceTypesDao = resourceTypesDao;
    }
} /* class XmpCollector */
