/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;

public class PersistStateIT extends CorrelationRulesTestCase {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() {
        // Engine states are written to the JVM's temp dir - overwrite this so we don't
        // inadvertently share state between runs.
        System.setProperty("java.io.tmpdir", temporaryFolder.getRoot().getAbsolutePath());
    }

    @Test(timeout=30000)
    public void canPersistState() throws Exception {
        DroolsCorrelationEngine engine = findEngineByName("persistStateTest");
        assertThat(engine, notNullValue());

        engine.correlate(createNodeLostServiceEvent(1, "SSH"));
        assertThat(engine.getKieSessionObjects(), hasSize(3));
        engine.tearDown();

        // Re-initialize and verify
        engine.initialize();
        assertThat(engine.getKieSessionObjects(), hasSize(3));
        engine.tearDown();
    }

    @Test(timeout=30000)
    public void canPersistStateWithStreaming() throws Exception {
        DroolsCorrelationEngine engine = findEngineByName("persistStateStreamingTest");
        assertThat(engine, notNullValue());

        engine.correlate(createNodeLostServiceEvent(1, "SSH"));
        await().atMost(1, TimeUnit.MINUTES).until(engine::getKieSessionObjects, hasSize(3));
        engine.tearDown();

        // Re-initialize and verify
        engine.initialize();
        assertThat(engine.getKieSessionObjects(), hasSize(3));
        engine.tearDown();
    }

    @Test
    public void persistedDroolsFactsAccessibleInNewSession() throws Exception {
        String factClass = "org.opennms.netmgt.correlation.drools.PersistedTestFact";
        DroolsCorrelationEngine engine = findEngineByName("persistStateTest");
        assertThat(engine, notNullValue());
        engine.initialize();
        // Make sure working memory is clean
        engine.getKieSession().getFactHandles().forEach(fh -> engine.getKieSession().delete(fh));

        // Insert a test fact to be persisted
        engine.correlate(createFactPersistenceTestEvent("insertPersistenceTestFact", "PersistStateIT-42"));
        assertEquals(inventoryWorkingMemory(engine), 1, engine.getKieSessionObjects().stream().filter(fh ->
                fh.getClass().getCanonicalName().equals(factClass)).count());
        assertThat(inventoryWorkingMemory(engine), engine.getKieSessionObjects(), hasSize(1));

        // Re-initialize and verify
        engine.reloadConfig(true);
        assertThat(inventoryWorkingMemory(engine), engine.getKieSessionObjects(), hasSize(1));

        // Insert another test fact to ensure the query finds facts from before and after reload
        engine.correlate(createFactPersistenceTestEvent("insertPersistenceTestFact", "PersistStateIT-42"));

        // Activate the query-and-delete rule
        engine.correlate(createFactPersistenceTestEvent("deletePersistenceTestFact", "PersistStateIT-42"));
        assertEquals("There should be no objects in working memory\n" + inventoryWorkingMemory(engine), 0,
            engine.getKieSessionObjects().size());
    }

    private Event createNodeLostServiceEvent(int nodeid, String serviceName) {
        return new EventBuilder(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, serviceName)
                .setNodeid(nodeid)
                .getEvent();
    }

    private Event createFactPersistenceTestEvent(String ueiSuffix, String source) {
        return new EventBuilder("uei.opennms.org/junit/" + ueiSuffix, source).getEvent();
    }

    private String inventoryWorkingMemory(DroolsCorrelationEngine engine) {
        StringBuilder wmInventory = new StringBuilder("Working Memory Inventory:");
        for (Object o : engine.getKieSessionObjects()) {
            wmInventory.append("\n\t" + o.toString());
        }
        return wmInventory.toString();
    }
}
