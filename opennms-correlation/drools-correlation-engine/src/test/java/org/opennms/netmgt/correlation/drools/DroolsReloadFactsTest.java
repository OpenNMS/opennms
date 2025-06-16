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
package org.opennms.netmgt.correlation.drools;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.codahale.metrics.MetricRegistry;

public class DroolsReloadFactsTest {

    static File DROOLS_SRC = new File("src/test/opennms-home/etc/drools-engine.d/droolsFusion/DroolsFusion.drl");

    @Test
    public void verifySaveFacts() throws Exception {

        DroolsCorrelationEngine droolsCorrelationEngine = new DroolsCorrelationEngine("droolsFusion", new MetricRegistry(), new FileSystemResource(DROOLS_SRC), null);
        List<Resource> resources = new ArrayList<>();
        resources.add(new FileSystemResource(DROOLS_SRC));
        droolsCorrelationEngine.setRulesResources(resources);
        droolsCorrelationEngine.setAssertBehaviour("identity");
        droolsCorrelationEngine.setEventProcessingMode("stream");
        MockEventIpcManager eventIpcManager = new MockEventIpcManager();
        droolsCorrelationEngine.setEventIpcManager(eventIpcManager);
        droolsCorrelationEngine.initialize();
        // Correlate with node lost event.
        Event event = new EventBuilder(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, "ICMP")
                .setNodeid(1)
                .getEvent();
        droolsCorrelationEngine.correlate(event);
        // Expect node up event.
        await().atMost(15, TimeUnit.SECONDS).until(() -> eventIpcManager.getEventAnticipator().getUnanticipatedEvents().size(), Matchers.greaterThanOrEqualTo(1));
        // save facts.
        droolsCorrelationEngine.saveFacts();
        // Verify that it should have atleast 4 objects from the rule
        Map<byte[], Class<?>> factObjects = droolsCorrelationEngine.getFactObjects();
        assertThat(factObjects.size(), Matchers.greaterThanOrEqualTo(4));
        // Now initialize again.
        droolsCorrelationEngine.initialize();
        // Now that facts are loaded and there shouldn't be any facts in factObjects.
        factObjects = droolsCorrelationEngine.getFactObjects();
        assertThat(factObjects.size(), Matchers.is(0));
        // Save facts from engine and Verify that all saved facts are loaded properly.
        droolsCorrelationEngine.saveFacts();
        factObjects = droolsCorrelationEngine.getFactObjects();
        assertThat(factObjects.size(), Matchers.greaterThanOrEqualTo(4));

    }
}
