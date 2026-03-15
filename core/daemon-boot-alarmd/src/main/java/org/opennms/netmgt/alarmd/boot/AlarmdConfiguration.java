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
package org.opennms.netmgt.alarmd.boot;

import org.opennms.core.daemon.common.DaemonSmartLifecycle;
import org.opennms.netmgt.alarmd.Alarmd;
import org.opennms.netmgt.alarmd.AlarmLifecycleListenerManager;
import org.opennms.netmgt.alarmd.AlarmPersister;
import org.opennms.netmgt.alarmd.AlarmPersisterImpl;
import org.opennms.netmgt.alarmd.NorthbounderManager;
import org.opennms.netmgt.events.api.AnnotationBasedEventListenerAdapter;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot @Configuration that wires all Alarmd beans.
 *
 * <p>This replaces the Karaf-era {@code applicationContext-daemon-loader-alarmd.xml}.
 * Beans that depend on DAOs and other infrastructure services (EventUtil,
 * AlarmEntityNotifier, SessionUtils, EventProxy, etc.) receive those dependencies
 * via @Autowired field injection in the existing classes. The actual DAO beans
 * are expected to be provided by a separate configuration (e.g., JPA auto-config
 * or a dedicated DAO configuration class).</p>
 *
 * <p>The {@link AnnotationBasedEventListenerAdapter} bridges Alarmd's
 * {@code @EventHandler}-annotated methods to the {@link EventSubscriptionService},
 * registering Alarmd as an event listener during {@code afterPropertiesSet()}.</p>
 */
@Configuration
public class AlarmdConfiguration {

    @Bean
    public AlarmPersisterImpl alarmPersister() {
        return new AlarmPersisterImpl();
    }

    @Bean
    public AlarmLifecycleListenerManager alarmLifecycleListenerManager() {
        return new AlarmLifecycleListenerManager();
    }

    @Bean
    public NorthbounderManager northbounderManager() {
        return new NorthbounderManager();
    }

    @Bean
    public Alarmd alarmd(AlarmPersister alarmPersister) {
        var alarmd = new Alarmd();
        alarmd.setPersister(alarmPersister);
        return alarmd;
    }

    @Bean
    public AnnotationBasedEventListenerAdapter alarmdEventListenerAdapter(
            Alarmd alarmd,
            EventSubscriptionService eventSubscriptionService) {
        var adapter = new AnnotationBasedEventListenerAdapter();
        adapter.setAnnotatedListener(alarmd);
        adapter.setEventSubscriptionService(eventSubscriptionService);
        return adapter;
    }

    @Bean
    public SmartLifecycle alarmdLifecycle(Alarmd alarmd) {
        return new DaemonSmartLifecycle(alarmd);
    }
}
