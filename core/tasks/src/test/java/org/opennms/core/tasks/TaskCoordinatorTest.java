/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
