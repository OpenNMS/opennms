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
package org.opennms.netmgt.provision.service;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

public class ProvisiondExecutorLifecycleTest {

    @Test(timeout = 5000)
    public void closingProvisiondContextInterruptsActiveNodeScan() throws Exception {
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions(new ClassPathResource(
                "META-INF/opennms/applicationContext-provisiond.xml"));
        beanFactory.registerSingleton("rescanThreads", 1);

        final ExecutorService executor = beanFactory.getBean("scheduledExecutor", ExecutorService.class);
        final CountDownLatch taskStarted = new CountDownLatch(1);
        final CountDownLatch taskInterrupted = new CountDownLatch(1);
        final CountDownLatch blocker = new CountDownLatch(1);

        executor.execute(() -> {
            taskStarted.countDown();
            try {
                blocker.await();
            } catch (InterruptedException e) {
                taskInterrupted.countDown();
                Thread.currentThread().interrupt();
            }
        });

        try {
            assertTrue("Provisiond task did not start", taskStarted.await(1, SECONDS));

            beanFactory.destroySingletons();

            assertTrue("Provisiond task was not interrupted", taskInterrupted.await(1, SECONDS));
            assertTrue("Provisiond executor did not terminate", executor.awaitTermination(1, SECONDS));
        } finally {
            blocker.countDown();
            executor.shutdownNow();
        }
    }
}
