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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.timeseries.TimeseriesPersisterFactory;
import org.opennms.netmgt.collection.persistence.timeseries.TimeseriesPersisterIT;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.support.NodeSnmpResourceType;
import org.opennms.netmgt.measurements.impl.TimeseriesFetchStrategy;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Optional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-timeseries-test.xml",
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.timeseries.strategy=timescale",
        //   "mock.db.adminPassword=password" // TODO Patrick: remove
})
@JUnitTemporaryDatabase(dirtiesContext=true)
public class TimeseriesRoundtripIT {

    @Autowired
    private TimeseriesPersisterFactory persisterFactory;

    @Autowired
    private ResourceDao resourceDao;

//    @Autowired
//    private TimeseriesFetchStrategy fetchStrategy;


    @Test
    public void canPersist() throws InterruptedException {
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repo = new RrdRepository();
        // Only the last element of the path matters here
        repo.setRrdBaseDir(Paths.get("a","path","that","ends","with","snmp").toFile());
        Persister persister = persisterFactory.createPersister(params, repo);

        int nodeId = 1;
        CollectionAgent agent = mock(CollectionAgent.class);
        ResourcePath path = ResourcePath.get(Integer.toString(nodeId));
        when(agent.getStorageResourcePath()).thenReturn(path);
        NodeLevelResource nodeLevelResource = new NodeLevelResource(nodeId);

        // Build a collection set with a single sample
        Timestamp now = Timestamp.now();
        CollectionSet collectionSet = new CollectionSetBuilder(agent)
                .withNumericAttribute(nodeLevelResource, "metrics", "metric", 900, AttributeType.GAUGE)
                .withTimestamp(now.asDate())
                .build();

        // Persist
        collectionSet.visit(persister);

        // Wait for the sample(s) to be flushed
        Thread.sleep(5 * 1000);

        // TODO: Patrick get results again and compare with original Collection

        // Fetch the (persisted) sample
        Resource resource = new Resource("snmp:1:metrics");
        Timestamp end = Timestamp.now();

        ResourceId parentResourceId = ResourceId.get("node", "1");
        ResourceId resourceId = ResourceId.get(parentResourceId, "nodeSnmp", "");
        final OnmsResource resourceFromStorage = resourceDao.getResourceById(resourceId);
        // assertEquals(resource.getId(), resourceFromStorage.getId().toString());

        // Fetch the (persisted) sample
//        Results<Sample> samples = m_sampleRepository.select(Context.DEFAULT_CONTEXT, resource, Optional.of(now), Optional.of(end));
//
//        assertEquals(1, samples.getRows().size());
//        Row<Sample> row = samples.getRows().iterator().next();
//        assertEquals(900, row.getElement("metric").getValue().doubleValue(), 0.00001);
    }

}
