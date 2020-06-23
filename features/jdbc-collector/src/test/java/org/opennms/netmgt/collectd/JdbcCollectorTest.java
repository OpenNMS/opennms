/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.collection.test.CollectionSetUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collectd.jdbc.JdbcAgentState;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.support.IndexStorageStrategy;
import org.opennms.netmgt.collection.support.PersistAllSelectorStrategy;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.netmgt.config.jdbc.JdbcColumn;
import org.opennms.netmgt.config.jdbc.JdbcDataCollection;
import org.opennms.netmgt.config.jdbc.JdbcDataCollectionConfig;
import org.opennms.netmgt.config.jdbc.JdbcQuery;
import org.opennms.netmgt.dao.JdbcDataCollectionConfigDao;
import org.opennms.netmgt.model.ResourcePath;

public class JdbcCollectorTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void canCollectEmptyCollection() throws Exception {
        JdbcDataCollection collection = new JdbcDataCollection();
        CollectionSet collectionSet = collect(collection);
        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        assertEquals(0, CollectionSetUtils.getAttributesByName(collectionSet).size());
    }

    @Test
    public void canCollectNodeLevelResource() throws Exception {
        // Build the query
        JdbcQuery query = new JdbcQuery();
        query.setIfType("ignore");
        query.setQueryName("someJdbcQuery"); // This is the group name

        JdbcColumn column = new JdbcColumn();
        column.setColumnName("someColumnName");
        column.setAlias("someAlias");
        column.setDataType(AttributeType.GAUGE);
        query.addJdbcColumn(column);

        JdbcDataCollection collection = new JdbcDataCollection();
        collection.addQuery(query);

        // Mock the result set
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getRow()).thenReturn(1);
        when(resultSet.getString("someColumnName")).thenReturn("99");
        when(resultSet.next()).thenReturn(true).thenReturn(false);

        // Collect and verify
        CollectionSet collectionSet = collect(collection, resultSet);
        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        List<String> collectionSetKeys = CollectionSetUtils.flatten(collectionSet);
        assertEquals(Arrays.asList("snmp/1/someJdbcQuery/someAlias[null,99.0]"),
                collectionSetKeys);
    }

    @Test
    public void canCollectGenericResource() throws Exception {
        // Build the query
        JdbcQuery query = new JdbcQuery();
        query.setQueryName("pg_tablespace_size");
        query.setIfType("all");
        query.setResourceType("pgTableSpace");
        query.setInstanceColumn("spcname");

        JdbcColumn spcnameColumn = new JdbcColumn();
        spcnameColumn.setColumnName("spcname");
        spcnameColumn.setAlias("spcname");
        spcnameColumn.setDataType(AttributeType.STRING);
        query.addJdbcColumn(spcnameColumn);

        JdbcColumn tssizeColumn = new JdbcColumn();
        tssizeColumn.setColumnName("ts_size");
        tssizeColumn.setAlias("ts_size");
        tssizeColumn.setDataType(AttributeType.GAUGE);
        query.addJdbcColumn(tssizeColumn);

        JdbcDataCollection collection = new JdbcDataCollection();
        collection.addQuery(query);

        // Mock the result set
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getRow()).thenReturn(2);
        when(resultSet.getString("spcname")).thenReturn("some: name");
        when(resultSet.getString("ts_size")).thenReturn("41").thenReturn("52");
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);

        // Define the resource type
        ResourceType resourceType = new ResourceType();
        resourceType.setName("pgTableSpace");
        resourceType.setLabel("PostgreSQL Tablespace");
        resourceType.setResourceLabel("${spcname}");
        StorageStrategy storageStrategy = new StorageStrategy();
        storageStrategy.setClazz(IndexStorageStrategy.class.getCanonicalName());
        resourceType.setStorageStrategy(storageStrategy);
        PersistenceSelectorStrategy persistenceSelectorStrategy = new PersistenceSelectorStrategy();
        persistenceSelectorStrategy.setClazz(PersistAllSelectorStrategy.class.getCanonicalName());
        resourceType.setPersistenceSelectorStrategy(persistenceSelectorStrategy);

        // Collect and verify
        CollectionSet collectionSet = collect(collection, resultSet, resourceType);
        assertEquals(CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        List<String> collectionSetKeys = CollectionSetUtils.flatten(collectionSet);

        assertEquals(Arrays.asList("snmp/1/pgTableSpace/some__name/pg_tablespace_size/spcname[some: name,null]",
                "snmp/1/pgTableSpace/some__name/pg_tablespace_size/ts_size[null,41.0]",
                "snmp/1/pgTableSpace/some__name/pg_tablespace_size/spcname[some: name,null]",
                "snmp/1/pgTableSpace/some__name/pg_tablespace_size/ts_size[null,52.0]"),
                    collectionSetKeys);
    }

    public CollectionSet collect(JdbcDataCollection collection) throws Exception {
        return collect(collection, null);
    }

    public CollectionSet collect(JdbcDataCollection collection, ResultSet resultSet, ResourceType...resourceTypes) throws Exception {
        final int nodeId = 1;

        JdbcDataCollectionConfig config = new JdbcDataCollectionConfig();
        config.setRrdRepository(tempFolder.getRoot().getAbsolutePath());

        JdbcDataCollectionConfigDao jdbcCollectionDao = mock(JdbcDataCollectionConfigDao.class);
        when(jdbcCollectionDao.getConfig()).thenReturn(config);
        when(jdbcCollectionDao.getDataCollectionByName(null)).thenReturn(collection);

        ResourceTypeMapper.getInstance().setResourceTypeMapper((name) -> {
            for (ResourceType resourceType : resourceTypes) {
                if (resourceType.getName().equals(name)) {
                    return resourceType;
                }
            }
            return null;
        });

        MyJdbcCollector jdbcCollector = new MyJdbcCollector();
        jdbcCollector.setJdbcCollectionDao(jdbcCollectionDao);
        jdbcCollector.initialize();

        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getNodeId()).thenReturn(nodeId);
        when(agent.getAddress()).thenReturn(InetAddressUtils.ONE_TWENTY_SEVEN);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get("snmp", Integer.toString(nodeId)));

        JdbcAgentState jdbcAgentState = mock(JdbcAgentState.class);
        when(jdbcAgentState.groupIsAvailable(any(String.class))).thenReturn(true);
        when(jdbcAgentState.executeJdbcQuery(anyObject(), anyObject())).thenReturn(resultSet);
        jdbcCollector.setJdbcAgentState(jdbcAgentState);

        Map<String, Object> params = new HashMap<>();
        params.putAll(jdbcCollector.getRuntimeAttributes(agent, params));
        CollectionSet collectionSet = jdbcCollector.collect(agent, params);

        return collectionSet;
    }

    public static class MyJdbcCollector extends JdbcCollector {

        private JdbcAgentState s_agentState;

        public void setJdbcAgentState(JdbcAgentState agentState) {
            s_agentState = agentState;
        }

        @Override
        protected JdbcAgentState createAgentState(InetAddress address, Map<String, Object> parameters) {
            if (s_agentState != null) return s_agentState;
            return super.createAgentState(address, parameters);
        }
    }

}
