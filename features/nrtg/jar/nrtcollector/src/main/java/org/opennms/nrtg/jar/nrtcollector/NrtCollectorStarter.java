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
package org.opennms.nrtg.jar.nrtcollector;

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
public abstract class NrtCollectorStarter {

    private static final Logger logger = LoggerFactory.getLogger(NrtCollectorStarter.class);

    private static AbstractApplicationContext context;

    public static void main(String[] args) {
        context = new AnnotationConfigApplicationContext(org.opennms.nrtg.jar.nrtcollector.AppConfig.class);
        context.registerShutdownHook();
        NrtCollector nrtCollector = (NrtCollector) context.getBean("nrtCollector");
        nrtCollector.start();

        while (!nrtCollector.terminated()) {
            try {
                Thread.sleep(1000L);
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
