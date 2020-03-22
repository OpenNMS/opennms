/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.timeseries.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.integration.api.v1.timeseries.Aggregation;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.TimeSeriesFetchRequest;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTimeSeriesFetchRequest;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.dto.CollectionSetDTO;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.DeferredGenericTypeResource;
import org.opennms.netmgt.collection.support.builder.GenericTypeResource;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.timeseries.impl.TimeseriesStorageManager;
import org.opennms.netmgt.timeseries.integration.persistence.TimeseriesPersisterFactory;
import org.opennms.netmgt.timeseries.meta.TimeSeriesMetaDataDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-timeseries-test.xml",
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.timeseries.strategy=integration"
})
@JUnitTemporaryDatabase(dirtiesContext=true)
public class TimeseriesRoundtripIT {

    @Autowired
    private TimeseriesPersisterFactory persisterFactory;

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private ResourceDao resourceDao;


    @Autowired
    private TimeseriesStorageManager timeseriesStorageManager;

    @Autowired
    private TimeSeriesMetaDataDao metaDataDao;


    @Test
    public void canPersist() throws InterruptedException, StorageException {
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repo = new RrdRepository();
        // Only the last element of the path matters here
        repo.setRrdBaseDir(Paths.get("a","path","that","ends","with","snmp").toFile());
        Persister persister = persisterFactory.createPersister(params, repo);

        CollectionAgent agent = mock(CollectionAgent.class);
        int nodeId = 1;
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get(Integer.toString(nodeId)));
        NodeLevelResource nodeLevelResource = new NodeLevelResource(nodeId);
        InterfaceLevelResource ifLevelResource = new InterfaceLevelResource(nodeLevelResource, "if-x");

       //  DeferredGenericTypeResource dereferredGenericResource = new DeferredGenericTypeResource(nodeLevelResource, "sometype", "someinstance");

        ResourceType rt = mock(ResourceType.class, RETURNS_DEEP_STUBS);
        when(rt.getName()).thenReturn("Charles");
        when(rt.getStorageStrategy().getClazz()).thenReturn(MockStorageStrategy.class.getCanonicalName());
        when(rt.getPersistenceSelectorStrategy().getClazz()).thenReturn(MockPersistenceSelectorStrategy.class.getCanonicalName());

        DeferredGenericTypeResource deferredGenericTypeResource = new DeferredGenericTypeResource(nodeLevelResource, "Charles", "id");

        GenericTypeResource genericTypeResource = new GenericTypeResource(nodeLevelResource, rt, "idx");
        genericTypeResource.setTimestamp(new Date(0));
        ResourceTypeMapper.getInstance().setResourceTypeMapper((name) -> rt);

        Date now = new Date();
        CollectionSetDTO collectionSet = new CollectionSetBuilder(agent)
                // Node level
                .withNumericAttribute(nodeLevelResource, "metrics", "m1", 900, AttributeType.GAUGE)
                .withIdentifiedNumericAttribute(nodeLevelResource, "metrics", "m2", 1000, AttributeType.COUNTER, "idx-m2")
                .withStringAttribute(nodeLevelResource, "metrics", "sysname", "host1")
                // Interface level
                .withNumericAttribute(ifLevelResource, "if-metrics", "m3", 44, AttributeType.GAUGE)
                .withIdentifiedNumericAttribute(ifLevelResource, "if-metrics", "m4", 55, AttributeType.COUNTER, "idx-m4")
                .withStringAttribute(ifLevelResource, "if-metrics", "ifname", "eth0")
                // Generic
                .withNumericAttribute(deferredGenericTypeResource, "gen-metrics", "m5", 66, AttributeType.GAUGE)
                .withIdentifiedNumericAttribute(deferredGenericTypeResource, "gen-metrics", "m6", 77, AttributeType.COUNTER, "idx-m6")
                .withIdentifiedStringAttribute(deferredGenericTypeResource, "gen-metrics", "genname", "bgp", "oops")
                .withTimestamp(now)
                .build();

        // Persist
        collectionSet.visit(persister);

        // Wait for the sample(s) to be flushed
        Thread.sleep(5 * 1000);

        testForNumericAttribute("snmp:1:metrics", "m1", 900d);
        testForNumericAttribute("snmp:1:metrics", "m2", 1000d);
        testForStringAttribute("snmp/1/metrics", "idx-m2", "m2"); // Identified
        testForStringAttribute("snmp/1", "sysname", "host1");

        testForNumericAttribute("snmp:1:if-x:if-metrics", "m3", 44d);
        testForNumericAttribute("snmp:1:if-x:if-metrics", "m4", 55d);
        testForStringAttribute("snmp/1/if-x/if-metrics", "idx-m4", "m4"); // Identified
        testForStringAttribute("snmp/1/if-x", "ifname", "eth0");

        testForNumericAttribute("snmp:1:gen-metrics:gen-metrics", "m5", 66d);
        testForNumericAttribute("snmp:1:gen-metrics:gen-metrics", "m6", 77d);
        testForStringAttribute("snmp/1/gen-metrics/gen-metrics", "idx-m6", "m6"); // Identified
        testForStringAttribute("snmp/1/gen-metrics", "genname", "bgp");
    }

    private void testForNumericAttribute(String resourceId, String name, Double expectedValue) throws StorageException {

        List<Metric> metrics = timeseriesStorageManager.get().getMetrics(Arrays.asList(
                new ImmutableTag(CommonTagNames.resourceId, resourceId),
                new ImmutableTag(CommonTagNames.name, name)));
        assertEquals(1, metrics.size());

        TimeSeriesFetchRequest request = ImmutableTimeSeriesFetchRequest.builder()
                .aggregation(Aggregation.NONE)
                .start(Instant.ofEpochMilli(0))
                .end(Instant.now())
                .step(Duration.ofSeconds(1))
                .metric(metrics.get(0)).build();

        List<Sample> sample = timeseriesStorageManager.get().getTimeseries(request);
        assertEquals(1, sample.size());
        assertEquals(expectedValue, sample.get(0).getValue());

    }

    private void testForStringAttribute(String resourcePath, String name, String expectedValue) throws StorageException {
        Map<String, String> stringAttributes = metaDataDao.getForResourcePath(ResourcePath.fromString(resourcePath));
        assertEquals(expectedValue, stringAttributes.get(name));
    }

}
