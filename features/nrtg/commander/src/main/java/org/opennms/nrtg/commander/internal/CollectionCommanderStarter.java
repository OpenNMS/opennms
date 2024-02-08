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
package org.opennms.nrtg.commander.internal;

import org.opennms.nrtg.api.model.CollectionTask;
import org.opennms.nrtg.commander.internal.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author Markus Neumann
 */
@Component
public class CollectionCommanderStarter {

    private static final Logger logger = LoggerFactory.getLogger(CollectionCommanderStarter.class);

    private static AbstractApplicationContext context;

    public static void main(String[] args) {
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        context.registerShutdownHook();
        new CollectionCommanderStarter().start();
        context.close();
    }

    public void start() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.initialize();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setQueueCapacity(8);
        taskExecutor.initialize();

        Long startTime = System.currentTimeMillis();
        Integer i = 0;
        while (i < 100) {
            CollectionTask ct = new CollectionTask(1200, "SNMP_All_Metrics");
            PooledJobPublisher jobPublisher = new PooledJobPublisher(ct);
            taskExecutor.execute(jobPublisher);
            i++;
        }
        logger.info("All started '{}'ms", System.currentTimeMillis() - startTime);
        Boolean done = false;
        while (!done) {
            if (taskExecutor.getActiveCount() == 0) {
                logger.info("Tasks active '{}'", taskExecutor.getActiveCount());
                logger.info("All done '{}'ms", System.currentTimeMillis() - startTime);
                taskExecutor.shutdown();
                done = true;
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error("'{}'", e.getMessage());
                }
            }
        }
    }

    public static AbstractApplicationContext getContext() {
        return context;
    }
}
