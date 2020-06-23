/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collectd.jdbc.JdbcAgentState;
import org.opennms.netmgt.collection.api.AbstractRemoteServiceCollector;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.collection.support.builder.Resource;
import org.opennms.netmgt.config.jdbc.JdbcColumn;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcQuery;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcCollector extends AbstractRemoteServiceCollector {
    private static final Logger LOG = LoggerFactory.getLogger(JdbcCollector.class);

    private static final String JDBC_COLLECTION_KEY = "jdbcCollection";

    private static final Map<String, Class<?>> TYPE_MAP = Collections.unmodifiableMap(Stream.of(
            new SimpleEntry<>(JDBC_COLLECTION_KEY, JdbcDataCollection.class))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    private JdbcDataCollectionConfigDao m_jdbcCollectionDao;

    public JdbcCollector() {
        super(TYPE_MAP);
    }

    @Override
    public void initialize() {
        LOG.debug("initialize: Initializing JdbcCollector.");
        if (m_jdbcCollectionDao == null) {
            // Retrieve the DAO for our configuration file.
            m_jdbcCollectionDao = BeanUtils.getBean("daoContext", "jdbcDataCollectionConfigDao", JdbcDataCollectionConfigDao.class);
        }
    }

    private static void initDatabaseConnectionFactory(String dataSourceName) {
        DataSourceFactory.init(dataSourceName);
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        final Map<String, Object> runtimeAttributes = new HashMap<>();
        final String collectionName = ParameterMap.getKeyedString(parameters, "collection", ParameterMap.getKeyedString(parameters, "jdbc-collection", null));
        final JdbcDataCollection collection = m_jdbcCollectionDao.getDataCollectionByName(collectionName);
        if (collection == null) {
            throw new IllegalArgumentException(String.format("JdbcCollector: No collection found with name '%s'.",  collectionName));
        }
        runtimeAttributes.put(JDBC_COLLECTION_KEY, collection);
        return runtimeAttributes;
    }

    protected JdbcAgentState createAgentState(InetAddress address, Map<String, Object> parameters) {
        return new JdbcAgentState(address, parameters);
    }

    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) throws CollectionException {
        final JdbcDataCollection collection = (JdbcDataCollection)parameters.get(JDBC_COLLECTION_KEY);
        
        JdbcAgentState agentState = null;
        Connection con = null;
        ResultSet results = null;
        Statement stmt = null;
        try {
            agentState = createAgentState(agent.getAddress(), parameters);
            agentState.setupDatabaseConnections(parameters);

            // Create a new collection set.
            CollectionSetBuilder builder = new CollectionSetBuilder(agent);

            // Creating a single resource object, because all node-level metric must belong to the exact same resource.
            final NodeLevelResource nodeResource = new NodeLevelResource(agent.getNodeId());

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
                            Resource resource = null;

                            // Create the appropriate resource container.
                            if(singleInstance) {
                                resource = nodeResource;
                            } else {
                                // Retrieve the name of the column to use as the instance key for multi-row queries.
                                String instance = results.getString(query.getInstanceColumn());
                                resource = new DeferredGenericTypeResource(nodeResource, query.getResourceType(), instance);
                            }

                            for(JdbcColumn curColumn : query.getJdbcColumns()) {
                                final AttributeType type = curColumn.getDataType();

                                String columnName = null;
                                if(curColumn.getDataSourceName() != null && curColumn.getDataSourceName().length() != 0) {
                                    columnName = curColumn.getDataSourceName();
                                } else {
                                    columnName = curColumn.getColumnName();
                                }

                                String columnValue = results.getString(columnName);
                                if (columnValue == null) {
                                    LOG.debug("Skipping column named '{}' with null value.", curColumn.getColumnName());
                                    continue;
                                }

                                if (type.isNumeric()) {
                                    Double numericValue = Double.NaN;
                                    try {
                                        numericValue = Double.parseDouble(columnValue);
                                    } catch (NumberFormatException e) {
                                        LOG.warn("Value '{}' for column named '{}' cannot be converted to a number. Skipping.", columnValue, curColumn.getColumnName());
                                        continue;
                                    }
                                    builder.withNumericAttribute(resource, query.getQueryName(), curColumn.getAlias(), numericValue, type);
                                } else {
                                    builder.withStringAttribute(resource, query.getQueryName(), curColumn.getAlias(), columnValue);
                                }
                            }
                        }
                    }
                } catch(SQLException e) {
                    // Close the statement but retain the connection, log the exception and continue to the next query.
                    LOG.warn("There was a problem executing query '{}' Please review the query or configuration. Reason: {}", query.getQueryName(), e.getMessage());
                    continue;
                } finally {
                    agentState.closeResultSet(results);
                    agentState.closeStmt(stmt);
                    agentState.closeConnection(con);
                }
            }
            builder.withStatus(CollectionStatus.SUCCEEDED);
            return builder.build();
        } finally {
            if(agentState != null) {
                // Make sure that when we're done we close all results, statements and connections.
                agentState.closeResultSet(results);
                agentState.closeStmt(stmt);
                agentState.closeConnection(con);
                //agentState.closeAgentConnection();
            }
        }
    }

    // Simply check the database the query is supposed to connect to to see if it is available.
    private static boolean isGroupAvailable(JdbcAgentState agentState, JdbcQuery query) {
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

    public JdbcDataCollectionConfigDao getJdbcCollectionDao() {
        return m_jdbcCollectionDao;
    }

    public void setJdbcCollectionDao(JdbcDataCollectionConfigDao jdbcCollectionDao) {
        m_jdbcCollectionDao = jdbcCollectionDao;
    }

}
