/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.jdbc;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.CollectionAgent;
import org.opennms.netmgt.collectd.CollectionException;
import org.opennms.netmgt.collectd.ServiceCollector;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.netmgt.config.jdbc.JdbcColumn;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcQuery;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.model.events.EventProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcCollector implements ServiceCollector {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcCollector.class);

    private JdbcDataCollectionConfigDao m_jdbcCollectionDao;
    private final HashMap<Integer, JdbcAgentState> m_scheduledNodes = new HashMap<Integer, JdbcAgentState>();
    private HashMap<String, AttributeGroupType> m_groupTypeList = new HashMap<String, AttributeGroupType>();
    private HashMap<String, JdbcCollectionAttributeType> m_attribTypeList = new HashMap<String, JdbcCollectionAttributeType>();
    
    public JdbcDataCollectionConfigDao getJdbcCollectionDao() {
        return m_jdbcCollectionDao;
    }

    public void setJdbcCollectionDao(JdbcDataCollectionConfigDao jdbcCollectionDao) {
        m_jdbcCollectionDao = jdbcCollectionDao;
    }
    
    private void loadAttributeGroupList(JdbcDataCollection collection) {
        for (JdbcQuery query : collection.getQueries()) {
            AttributeGroupType attribGroupType1 = new AttributeGroupType(query.getQueryName(), query.getIfType());
            m_groupTypeList.put(query.getQueryName(), attribGroupType1);
        }
    }

    private void loadAttributeTypeList(JdbcDataCollection collection) {
        for (JdbcQuery query : collection.getQueries()) {
            for (JdbcColumn column : query.getJdbcColumns()) {
                AttributeGroupType attribGroupType = m_groupTypeList.get(query.getQueryName());
                JdbcCollectionAttributeType attribType = new JdbcCollectionAttributeType(column, attribGroupType);
                m_attribTypeList.put(column.getColumnName(), attribType);
            }
        }
    }

    @Override
    public void initialize(Map<String, String> parameters) {
        LOG.debug("initialize: Initializing JdbcCollector.");
        // Retrieve the DAO for our configuration file.
        m_jdbcCollectionDao = BeanUtils.getBean("daoContext", "jdbcDataCollectionConfigDao", JdbcDataCollectionConfigDao.class);
        
        // Clear out the node list.
        m_scheduledNodes.clear();
        
        initializeRrdDirs();
        initDatabaseConnectionFactory();
    }
    
    private void initializeRrdDirs() {
        /*
         * If the RRD file repository directory does NOT already exist, create
         * it.
         */
        LOG.debug("initializeRrdRepository: Initializing RRD repo from JdbcCollector...");
        File f = new File(m_jdbcCollectionDao.getConfig().getRrdRepository());
        if (!f.isDirectory()) {
            if (!f.mkdirs()) {
                throw new RuntimeException("Unable to create RRD file " + "repository.  Path doesn't already exist and could not make directory: " + m_jdbcCollectionDao.getConfig().getRrdRepository());
            }
        }
    }

    private void initDatabaseConnectionFactory() {
        try {            
            DataSourceFactory.init();
        } catch (IOException e) {
            LOG.error("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            LOG.error("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            LOG.error("initDatabaseConnectionFactory: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        }
    }
    
    private void initDatabaseConnectionFactory(String dataSourceName) {
        try {            
            DataSourceFactory.init(dataSourceName);
        } catch (IOException e) {
            LOG.error("initDatabaseConnectionFactory: IOException getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (MarshalException e) {
            LOG.error("initDatabaseConnectionFactory: Marshall Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            LOG.error("initDatabaseConnectionFactory: Validation Exception getting database connection", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            LOG.error("initDatabaseConnectionFactory: Failed getting connection to the database.", e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            LOG.error("initDatabaseConnectionFactory: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    @Override
    public void release() {
        m_scheduledNodes.clear();
    }

    @Override
    public void initialize(CollectionAgent agent, Map<String, Object> parameters) {        
        LOG.debug("initialize: Initializing JDBC collection for agent: {}", agent);
        
        Integer scheduledNodeKey = new Integer(agent.getNodeId());
        JdbcAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);

        if (nodeState != null) {
            LOG.info("initialize: Not scheduling interface for JDBC collection: {}", nodeState.getAddress());
            final StringBuffer sb = new StringBuffer();
            sb.append("initialize service: ");

            sb.append(" for address: ");
            sb.append(nodeState.getAddress());
            sb.append(" already scheduled for collection on node: ");
            sb.append(agent);
            LOG.debug(sb.toString());
            throw new IllegalStateException(sb.toString());
        } else {
            nodeState = new JdbcAgentState(agent.getInetAddress(), parameters);
            LOG.info("initialize: Scheduling interface for collection: {}", nodeState.getAddress());
            m_scheduledNodes.put(scheduledNodeKey, nodeState);
        }
    }

    @Override
    public void release(CollectionAgent agent) {
        Integer scheduledNodeKey = new Integer(agent.getNodeId());
        JdbcAgentState nodeState = m_scheduledNodes.get(scheduledNodeKey);
        if (nodeState != null) {
            m_scheduledNodes.remove(scheduledNodeKey);
        }
    }

    @Override
    public CollectionSet collect(CollectionAgent agent, EventProxy eproxy, Map<String, Object> parameters) throws CollectionException {
        JdbcAgentState agentState = null;
        if(parameters == null) {
            LOG.error("Null parameters is now allowed in JdbcCollector!!");
        }
        
        Connection con = null;
        ResultSet results = null;
        Statement stmt = null;
        
        try {
            String collectionName = ParameterMap.getKeyedString(parameters, "collection", null);
            if (collectionName == null) {
                //Look for the old configuration style:
                collectionName = ParameterMap.getKeyedString(parameters, "jdbc-collection", null);
            }
        
            JdbcDataCollection collection = m_jdbcCollectionDao.getDataCollectionByName(collectionName);
        
            agentState = m_scheduledNodes.get(agent.getNodeId());
            agentState.setupDatabaseConnections(parameters);
        
            // Load the attribute group types.
            loadAttributeGroupList(collection);

            // Load the attribute types.
            loadAttributeTypeList(collection);
        
            // Create a new collection set.
            JdbcCollectionSet collectionSet = new JdbcCollectionSet(agent);
            collectionSet.setCollectionTimestamp(new Date());
        
            // Cycle through all of the queries for this collection
            for(JdbcQuery query : collection.getQueries()) {
                // Verify if we should check for availability of a query.
                if (agentState.shouldCheckAvailability(query.getQueryName(), query.getRecheckInterval())) {
                    // Check to see if the query is available.
                    if (!isGroupAvailable(agentState, query)) {
                        LOG.warn("Group is not available.");
                        continue;
                    }
                }
                
                try {
                    // If the query is available, lets collect it.
                    if (agentState.groupIsAvailable(query.getQueryName())) {
                        if(agentState.getUseDataSourceName()) {
                            initDatabaseConnectionFactory(agentState.getDataSourceName());
                            con = DataSourceFactory.getInstance(agentState.getDataSourceName()).getConnection();
                        } else {
                            con = agentState.getJdbcConnection();
                        }
                        stmt = agentState.createStatement(con);
                        results = agentState.executeJdbcQuery(stmt, query);
                    
                        // Determine if there were any results for this query to                    
                        if (results.isBeforeFirst() && results.isAfterLast()) {
                            LOG.warn("Query '{}' returned no results.", query.getQueryName());
                            // Close the statement, but retain the connection.
                            agentState.closeResultSet(results);
                            agentState.closeStmt(stmt);
                            continue;
                        }
                        
                        // Determine if there are results and how many.
                        results.last();
                        boolean singleInstance = (results.getRow()==1)?true:false;
                        results.beforeFirst();
                        
                        
                        // Iterate through each row.
                        while(results.next() ) {
                            JdbcCollectionResource resource = null;
                            
                            // Create the appropriate resource container.
                            if(singleInstance) {
                                resource = new JdbcSingleInstanceCollectionResource(agent);
                            } else {
                                // Retrieve the name of the column to use as the instance key for multi-row queries.
                                String instance = results.getString(query.getInstanceColumn());
                                resource = new JdbcMultiInstanceCollectionResource(agent,instance, query.getResourceType());
                            }
                            
                            for(JdbcColumn curColumn : query.getJdbcColumns()) {
                                String columnName = null;
                                if(curColumn.getDataSourceName() != null && curColumn.getDataSourceName().length() != 0) {
                                    columnName = curColumn.getDataSourceName();
                                } else {
                                    columnName = curColumn.getColumnName();
                                }
                                
                                JdbcCollectionAttributeType attribType = m_attribTypeList.get(curColumn.getColumnName());
                                resource.setAttributeValue(attribType, results.getString(columnName));
                            }

                            collectionSet.getCollectionResources().add(resource);
                        }
                    }
                } catch(SQLException e) {
                    // Close the statement but retain the connection, log the exception and continue to the next query.
                    LOG.warn("There was a problem executing query '{}' Please review the query or configuration. Reason: {}", query.getQueryName(), e.getMessage());
                    agentState.closeResultSet(results);
                    agentState.closeStmt(stmt);
                    agentState.closeConnection(con);
                    continue;
                }
            }
            collectionSet.setStatus(ServiceCollector.COLLECTION_SUCCEEDED);
            return collectionSet;
        } finally {
            // Make sure that when we're done we close all results, statements and connections.
            agentState.closeResultSet(results);
            agentState.closeStmt(stmt);
            agentState.closeConnection(con);
            
            if(agentState != null) {
                //agentState.closeAgentConnection();
            }
        }
    }
    
    
    // Simply check the database the query is supposed to connect to to see if it is available.
    private boolean isGroupAvailable(JdbcAgentState agentState, JdbcQuery query) {
        LOG.debug("Checking availability of group {}", query.getQueryName());
        boolean status = false;
        ResultSet resultset = null;
        Connection con = null;
        
        try {
            if(agentState.getUseDataSourceName()) {
                initDatabaseConnectionFactory(agentState.getDataSourceName());
                con = DataSourceFactory.getInstance(agentState.getDataSourceName()).getConnection();
            } else {
                con = agentState.getJdbcConnection();
            }
            
            DatabaseMetaData metadata = con.getMetaData();
            resultset = metadata.getCatalogs();
            while (resultset.next()) {
                resultset.getString(1);
            }

            // The query worked, assume than the server is ok
            if (resultset != null) {
                status = true;
            }
        } catch(SQLException sqlEx) {
            LOG.warn("Error checking group ({}) availability", query.getQueryName(), sqlEx);
            agentState.setGroupIsAvailable(query.getQueryName(), status);
            status=false;
        } finally {
            agentState.closeResultSet(resultset);
            agentState.closeConnection(con);
        }
        LOG.debug("Group {} is {} available", query.getQueryName(), (status ? "" : "not"));
        agentState.setGroupIsAvailable(query.getQueryName(), status);
        return status;
    }

    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        return m_jdbcCollectionDao.getConfig().buildRrdRepository(collectionName);
    }
    
}
