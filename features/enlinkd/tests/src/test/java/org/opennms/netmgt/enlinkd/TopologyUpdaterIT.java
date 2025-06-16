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
package org.opennms.netmgt.enlinkd;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.base.Throwables;
import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class TopologyUpdaterIT {

    @Autowired
    private NodeTopologyService nodeTopologyService;

    @Autowired
    private OnmsTopologyDao topologyDao;

    // See NMS-12443 for more details
    @Test
    public void verifyGetTopologyAccessWhileDiscoveryInProgressDoesNotBlock() throws ExecutionException, InterruptedException, TimeoutException {
        final TopologyService service = Mockito.mock(TopologyService.class);
        final TopologyUpdater updater = new TopologyUpdater(service, topologyDao, nodeTopologyService) {

            @Override
            public OnmsTopologyProtocol getProtocol() {
                return OnmsTopologyProtocol.create("BRIDGE");
            }

            @Override
            public String getName() {
                return "Test Topology Updater";
            }

            @Override
            public OnmsTopology buildTopology() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw Throwables.propagate(e);
                }
                final OnmsTopology onmsTopology = new OnmsTopology();
                final Set<OnmsTopologyVertex> vertexSet = Sets.newHashSet();
                vertexSet.add(OnmsTopologyVertex.create("v1", "Vertex 1","127.0.0.1", "defaultKey"));
                vertexSet.add(OnmsTopologyVertex.create("v2", "Vertex 2", "127.0.0.1", "defaultKey"));
                onmsTopology.setVertices(vertexSet);
                return onmsTopology;
            }
        };

        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        final Runnable discoveryRunnable = () -> {
            long start = System.currentTimeMillis();
            LoggerFactory.getLogger("DISCOVERY").info("Start: {}", new Date());
            updater.runSchedulable();
            LoggerFactory.getLogger("DISCOVERY").info("Took {} ms", (System.currentTimeMillis() - start));
        };
        Future<?> future1 = executorService.submit(discoveryRunnable);

        // Verify access to getTopology works, even if it is still discovering
        Future<OnmsTopology> future2 = executorService.submit(updater::getTopology);
        OnmsTopology topology = future2.get(1, TimeUnit.SECONDS); // should not block and return immediately
        Assert.assertThat(topology, Matchers.is(Matchers.not(Matchers.nullValue())));
        Assert.assertThat(topology.getVertices(), Matchers.hasSize(0));
        Assert.assertThat(topology.getEdges(), Matchers.hasSize(0));

        // Wait until discovery is done
        future1.get();

        // Verify updated value is now available
        future2 = executorService.submit(updater::getTopology);
        topology = future2.get(1, TimeUnit.SECONDS); // should not block and return immediately
        Assert.assertThat(topology, Matchers.is(Matchers.not(Matchers.nullValue())));
        Assert.assertThat(topology.getVertices(), Matchers.hasSize(2));
        Assert.assertThat(topology.getEdges(), Matchers.hasSize(0));

        // Shutdown thread pool
        executorService.shutdown();
    }
}