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
package org.opennms.netmgt.provision.persist.rpc;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.junit.Test;
import org.opennms.netmgt.provision.persist.RequisitionProvider;
import org.opennms.netmgt.provision.persist.RequisitionProviderRegistry;
import org.opennms.netmgt.provision.persist.RequisitionRequest;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

public class RequisitionExecutorLifecycleTest {

    @Test(timeout = 5000)
    public void closingDaoContextInterruptsActiveRequisitionRequest() throws Exception {
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource("META-INF/opennms/component-dao.xml"));

        final ExecutorService executor = beanFactory.getBean("requisitionRequestExecutor", ExecutorService.class);
        final CountDownLatch taskStarted = new CountDownLatch(1);
        final CountDownLatch taskInterrupted = new CountDownLatch(1);
        final CountDownLatch blocker = new CountDownLatch(1);

        final RequisitionProvider provider = mock(RequisitionProvider.class);
        when(provider.getRequisition(any(RequisitionRequest.class))).thenAnswer(invocation -> {
            taskStarted.countDown();
            try {
                blocker.await();
                return null;
            } catch (InterruptedException e) {
                taskInterrupted.countDown();
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted", e);
            }
        });

        final RequisitionProviderRegistry registry = mock(RequisitionProviderRegistry.class);
        when(registry.getProviderByType("test")).thenReturn(provider);

        final RequisitionRpcModule rpcModule = new RequisitionRpcModule();
        rpcModule.setRegistry(registry);
        rpcModule.setExecutor(executor);

        final RequisitionRequestDTO request = new RequisitionRequestDTO();
        request.setType("test");
        request.setProviderRequest(mock(RequisitionRequest.class));
        final CompletableFuture<RequisitionResponseDTO> response = rpcModule.execute(request);

        try {
            assertTrue("Requisition request did not start", taskStarted.await(1, SECONDS));

            beanFactory.destroySingletons();

            assertTrue("Requisition request was not interrupted", taskInterrupted.await(1, SECONDS));
            assertTrue("Requisition executor did not terminate", executor.awaitTermination(1, SECONDS));
            assertTrue("Requisition response did not complete", response.isDone());
        } finally {
            blocker.countDown();
            executor.shutdownNow();
        }
    }
}
