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
package org.opennms.core.tasks;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.opennms.core.tasks.DefaultTaskCoordinator.SerialRunnable;

/**
 * @author Seth
 */
public class TaskCoordinatorTest {

    /**
     * Make sure that task executions on the {@link RunnableActor} thread are
     * being performed serially. Simulate the default provisiond configuration:
     * 
     * <bean id="taskCoordinator" class="org.opennms.core.tasks.DefaultTaskCoordinator">
     *   <constructor-arg value="Provisiond" />
     *   <property name="defaultExecutor" value="scan" />
     *   <property name="executors">
     *       <map>
     *           <entry key="import" value-ref="importExecutor" />
     *           <entry key="scan" value-ref="scanExecutor" />
     *           <entry key="write" value-ref="writeExecutor" />
     *       </map>
     *   </property>
     * </bean>
     * 
     * @throws Exception
     */
    @Test
    public void testDefaultProvisiondConfiguration() throws Exception {

        final int numberOfTasks = 10;

        final CountDownLatch latch = new CountDownLatch(numberOfTasks);

        DefaultTaskCoordinator coordinator = new DefaultTaskCoordinator("Provisiond");
        coordinator.setDefaultExecutor("scan");

        Map<String, Executor> executors = new HashMap<>();

        executors.put("import", Executors.newScheduledThreadPool(10));
        executors.put("scan", Executors.newScheduledThreadPool(10));
        executors.put("write", Executors.newScheduledThreadPool(10));

        coordinator.setExecutors(executors);

        final StringBuilder result = new StringBuilder();

        for (int i = 0; i < numberOfTasks; i++) {
            final int count = i;
            coordinator.onProcessorThread(new SerialRunnable() {
                @Override
                public void run() {
                    // Introduct some jitter into the threads
                    try {
                        Thread.sleep(300 * (count % 2));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(System.currentTimeMillis() + " " + count);
                    result.append(count);
                    latch.countDown();
                }
            });
        }
        latch.await();
        assertEquals("0123456789", result.toString());
    }
}
