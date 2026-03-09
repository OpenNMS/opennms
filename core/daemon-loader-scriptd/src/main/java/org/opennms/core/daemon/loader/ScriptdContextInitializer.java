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
package org.opennms.core.daemon.loader;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Initializes static singletons that Scriptd depends on:
 * <ol>
 *   <li>{@link BeanUtils#setStaticApplicationContext} — so Scriptd's
 *       {@code BeanUtils.getBeanFactory("daoContext")} resolves beans from
 *       the daemon-loader's own ApplicationContext (which has nodeDao,
 *       sessionUtils, etc.)</li>
 *   <li>{@link EventIpcManagerFactory#setIpcManager} — so Scriptd's
 *       {@code BroadcastEventProcessor} can subscribe to events via the
 *       Kafka-backed EventIpcManager</li>
 * </ol>
 */
public class ScriptdContextInitializer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(ScriptdContextInitializer.class);

    private ApplicationContext applicationContext;
    private EventIpcManager eventIpcManager;

    public void setEventIpcManager(EventIpcManager eventIpcManager) {
        this.eventIpcManager = eventIpcManager;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        LOG.info("Initializing BeanUtils.setStaticApplicationContext for Scriptd");
        BeanUtils.setStaticApplicationContext(applicationContext);

        LOG.info("Initializing EventIpcManagerFactory.setIpcManager for Scriptd");
        EventIpcManagerFactory.setIpcManager(eventIpcManager);
    }
}
