/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.timeseries.samplewrite;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.integration.api.v1.timeseries.Aggregation;
import org.opennms.integration.api.v1.timeseries.DataPoint;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.TimeSeriesData;
import org.opennms.integration.api.v1.timeseries.TimeSeriesFetchRequest;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTimeSeriesFetchRequest;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Used to verify that numeric attributes in CollectionSets are persisted
 * in Cassandra as Samples when using the NewtsPersister.
 *
 * @author jwhite
 */

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-timeseries-test.xml",
        "classpath:/META-INF/opennms/applicationContext-jceks-scv.xml"

})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.timeseries.strategy=integration"
})
@JUnitTemporaryDatabase(dirtiesContext=true)
public class TimeseriesPersisterIT {

    @Autowired
    private TimeseriesPersisterFactory persisterFactory;

    @Autowired
    private TimeseriesStorageManager storage;

    @Test
    public void canPersist() throws InterruptedException, StorageException {
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repo = new RrdRepository();
        // Only the last element of the path matters here
        repo.setRrdBaseDir(Paths.get("a","path","that","ends","with","snmp").toFile());
        Persister persister = persisterFactory.createPersister(params, repo);

        int nodeId = 1;
        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get(Integer.toString(nodeId)));
        NodeLevelResource nodeLevelResource = new NodeLevelResource(nodeId);

        // Build a collection set with a single sample
        Instant now = Instant.now();
        CollectionSet collectionSet = new CollectionSetBuilder(agent)
                .withNumericAttribute(nodeLevelResource, "metrics", "metric", 900, AttributeType.GAUGE)
                .withTimestamp(Date.from(now))
                .build();

        // Persist
        collectionSet.visit(persister);

        // Wait for the sample(s) to be flushed
        Thread.sleep(5 * 1000);

        // Fetch the (persisted) sample
        String resourceId = "snmp/1/metrics";
        Instant end = Instant.now();

        ImmutableMetric metric = ImmutableMetric.builder()
                .intrinsicTag(IntrinsicTagNames.resourceId, resourceId)
                .intrinsicTag(IntrinsicTagNames.name, "metric")
                .build();
        TimeSeriesFetchRequest request = ImmutableTimeSeriesFetchRequest.builder()
                .start(now)
                .end(end)
                .metric(metric)
                .aggregation(Aggregation.NONE)
                .step(Duration.ofMillis(1))
                .build();
        TimeSeriesData data = this.storage.get().getTimeSeriesData(request);

        assertEquals(1, data.getDataPoints().size());
        DataPoint row = data.getDataPoints().get(0);
        assertEquals(900, row.getValue(), 0.00001);
    }
}
