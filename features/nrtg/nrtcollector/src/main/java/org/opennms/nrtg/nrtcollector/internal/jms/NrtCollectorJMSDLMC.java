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
package org.opennms.nrtg.nrtcollector.internal.jms;

import org.opennms.nrtg.nrtcollector.api.NrtCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

/**
 * A JMS based NrtCollector listening to CollectionJobs using a
 * {@link org.springframework.jms.listener.AbstractMessageListenerContainer}. Received Jobs will be send to a
 * {@link CollectionJobListener} configured via spring.
 *
 * @author Simon Walter
 */
public class NrtCollectorJMSDLMC implements NrtCollector {

    private static final Logger logger = LoggerFactory
            .getLogger(NrtCollectorJMSDLMC.class);

    private AbstractMessageListenerContainer listenerContainer;

    public void setListenerContainer(
            AbstractMessageListenerContainer listenerContainer) {
        this.listenerContainer = listenerContainer;
    }

    @Override
    public void start() {
        logger.info("Starting instance: " + this.hashCode()
                + " with destination: ["
                + listenerContainer.getDestinationName() + "] msgListener: ["
                + listenerContainer.getMessageListener() + "]");

        listenerContainer.start();
    }

    @Override
    public boolean terminated() {
        return !listenerContainer.isRunning();
    }

    @Override
    public void stop() {
        logger.info("Stopping instance: " + this.hashCode());
        listenerContainer.stop();
        listenerContainer.destroy();
    }
}
