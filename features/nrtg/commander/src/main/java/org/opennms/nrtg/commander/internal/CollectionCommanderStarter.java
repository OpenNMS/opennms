/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
