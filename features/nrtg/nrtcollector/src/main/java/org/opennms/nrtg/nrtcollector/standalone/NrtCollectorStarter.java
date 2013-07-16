/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.nrtg.nrtcollector.standalone;

import org.opennms.nrtg.nrtcollector.api.NrtCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Starts a CollectionSatallite the listening for CollectionJobs. The technology
 * to listen for is provided by spring.
 *
 * @author Markus Neumann
 */
@Component
public class NrtCollectorStarter {

    private static final Logger logger = LoggerFactory.getLogger(NrtCollectorStarter.class);

    private static AbstractApplicationContext context;

    public static void main(String args[]) {
        context = new AnnotationConfigApplicationContext(org.opennms.nrtg.nrtcollector.standalone.config.AppConfig.class);
        context.registerShutdownHook();
        NrtCollector nrtCollector = (NrtCollector) context.getBean("nrtCollector");
        nrtCollector.start();

        while (!nrtCollector.terminated()) {
            try {
                Thread.sleep(1000l);
            } catch (InterruptedException e) {
                logger.error("'{}'", e.getMessage());
            }
        }
        context.close();
    }

    public static AbstractApplicationContext getContext() {
        return context;
    }
}