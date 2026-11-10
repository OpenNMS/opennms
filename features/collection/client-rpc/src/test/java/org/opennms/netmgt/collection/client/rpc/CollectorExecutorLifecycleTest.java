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
package org.opennms.netmgt.collection.client.rpc;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.junit.Test;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionException;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.api.ServiceCollectorRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

public class CollectorExecutorLifecycleTest {

    @Test(timeout = 5000)
    public void closingCollectdContextInterruptsActiveCollectorTask() throws Exception {
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource(
                "META-INF/opennms/applicationContext-rpc-collector.xml"));

        final ExecutorService executor = beanFactory.getBean("collectorExecutor", ExecutorService.class);
        final CountDownLatch taskStarted = new CountDownLatch(1);
        final CountDownLatch taskInterrupted = new CountDownLatch(1);
        final CountDownLatch blocker = new CountDownLatch(1);

        final ServiceCollector collector = mock(ServiceCollector.class);
        when(collector.collect(any(CollectionAgent.class), anyMap())).thenAnswer(invocation -> {
            taskStarted.countDown();
            try {
                blocker.await();
                return null;
            } catch (InterruptedException e) {
                taskInterrupted.countDown();
                Thread.currentThread().interrupt();
                throw new CollectionException("Interrupted", e);
            }
        });

        final String collectorClass = ServiceCollector.class.getName();
        final ServiceCollectorRegistry registry = mock(ServiceCollectorRegistry.class);
        when(registry.getCollectorFutureByClassName(collectorClass))
                .thenReturn(CompletableFuture.completedFuture(collector));

        final CollectorClientRpcModule rpcModule = new CollectorClientRpcModule();
        rpcModule.setServiceCollectorRegistry(registry);
        rpcModule.setExecutor(executor);

        final CollectorRequestDTO request = new CollectorRequestDTO();
        request.setAgent(mock(CollectionAgent.class));
        request.setClassName(collectorClass);
        final CompletableFuture<CollectorResponseDTO> response = rpcModule.execute(request);

        try {
            assertTrue("Collector task did not start", taskStarted.await(1, SECONDS));

            // This exercises Spring's destruction of the collectorExecutor bean from
            // the production context. It must interrupt the task rather than wait for it.
            beanFactory.destroySingletons();

            assertTrue("Collector task was not interrupted", taskInterrupted.await(1, SECONDS));
            assertTrue("Collector executor did not terminate", executor.awaitTermination(1, SECONDS));
            assertTrue("Collection future did not fail after interruption", response.isCompletedExceptionally());
        } finally {
            blocker.countDown();
            executor.shutdownNow();
        }
    }
}
