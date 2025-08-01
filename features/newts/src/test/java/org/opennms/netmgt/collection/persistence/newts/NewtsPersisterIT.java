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
package org.opennms.netmgt.collection.persistence.newts;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.cassandra.NewtsInstance;
import org.opennms.newts.persistence.cassandra.CassandraSampleRepository;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Optional;

/**
 * Used to verify that numeric attributes in CollectionSets are persisted
 * in Cassandra as Samples when using the NewtsPersister.
 *
 * @author jwhite
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-timeseries-newts.xml"
})
@JUnitConfigurationEnvironment(systemProperties={
        "org.opennms.timeseries.strategy=newts"
})
public class NewtsPersisterIT {

    @ClassRule
    public static NewtsInstance s_newtsInstance = new NewtsInstance();

    @BeforeClass
    public static void setUpClass() {
        System.setProperty("org.opennms.newts.config.hostname", s_newtsInstance.getHost());
        System.setProperty("org.opennms.newts.config.port", Integer.toString(s_newtsInstance.getPort()));
        System.setProperty("org.opennms.newts.config.keyspace", s_newtsInstance.getKeyspace());
    }

    @Autowired
    private NewtsPersisterFactory m_persisterFactory;

    @Autowired
    private CassandraSampleRepository m_sampleRepository;

    @Test
    public void canPersist() throws InterruptedException {
        ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        RrdRepository repo = new RrdRepository();
        // Only the last element of the path matters here
        repo.setRrdBaseDir(Paths.get("a","path","that","ends","with","snmp").toFile());
        Persister persister = m_persisterFactory.createPersister(params, repo);

        int nodeId = 1;
        CollectionAgent agent = mock(CollectionAgent.class);
        when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get(Integer.toString(nodeId)));
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

        // Fetch the (persisted) sample
        Resource resource = new Resource("snmp:1:metrics");
        Timestamp end = Timestamp.now();
        Results<Sample> samples = m_sampleRepository.select(Context.DEFAULT_CONTEXT, resource, Optional.of(now), Optional.of(end));

        assertEquals(1, samples.getRows().size());
        Row<Sample> row = samples.getRows().iterator().next();
        assertEquals(900, row.getElement("metric").getValue().doubleValue(), 0.00001);
    }
}
