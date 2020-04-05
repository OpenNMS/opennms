/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import static com.jayway.awaitility.Awaitility.await;
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
