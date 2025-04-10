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
package org.opennms.web.event;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/daoWebRepositoryTestContext.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class EventFactoryIT {

    @Autowired
    private DatabasePopulator dbPopulator;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private OnmsNode node1;

    @Before
    public void setUp() {
        node1 = transactionTemplate.execute((a) -> {
            dbPopulator.populateDatabase();
            return dbPopulator.getNode1();
        });
    }

    /**
     * Verifies that we can retrieve the list of sorted events for a given node.
     *
     * (This was written to validate that the result set is the same when adding additional
     * columns to the ORDER BY clause in scope of NMS-10506)
     *
     * @throws SQLException on error
     */
    @Test
    public void canEventSortedEventsForNode() throws SQLException {
        int throttle = 5;
        int offset = 0;
        AcknowledgeType ackType = AcknowledgeType.UNACKNOWLEDGED;

        // Initial verification
        Event[] events = EventFactory.getEventsForNode(node1.getId(), SortStyle.ID, ackType, throttle, offset, null);
        assertEquals(events.length, 1);
        assertThat(events[0].uei, equalTo("uei.opennms.org/test"));

        // Save some events
        // Use a separate transaction for these to ensure they are visible by the JDBC calls used in the EventFactory
        Date now = new Date();
        transactionTemplate.execute((a) -> {
            OnmsEvent event = new OnmsEvent();
            event.setDistPoller(dbPopulator.getDistPollerDao().whoami());
            event.setEventUei("uei.opennms.org/test2");
            event.setEventTime(new Date());
            event.setEventSource("test");
            event.setEventCreateTime(now);
            event.setEventSeverity(OnmsSeverity.CLEARED.getId());
            event.setEventLog("Y");
            event.setEventDisplay("Y");
            event.setNode(node1);
            dbPopulator.getEventDao().save(event);
            dbPopulator.getEventDao().flush();

            OnmsEvent event2 = new OnmsEvent();
            event2.setDistPoller(dbPopulator.getDistPollerDao().whoami());
            event2.setEventUei("uei.opennms.org/test3");
            event2.setEventTime(new Date());
            event2.setEventSource("test");
            event2.setEventCreateTime(new Date(now.getTime()+1));
            event2.setEventSeverity(OnmsSeverity.CLEARED.getId());
            event2.setEventLog("Y");
            event2.setEventDisplay("Y");
            event2.setNode(node1);
            dbPopulator.getEventDao().save(event2);
            dbPopulator.getEventDao().flush();

            return null;
        });

        // Query again, we should see 3 events now
        events = EventFactory.getEventsForNode(node1.getId(), SortStyle.ID, ackType, throttle, offset, null);
        assertEquals(events.length, 3);

        assertThat(events[0].uei, equalTo("uei.opennms.org/test3"));
        assertThat(events[1].uei, equalTo("uei.opennms.org/test2"));
        assertThat(events[2].uei, equalTo("uei.opennms.org/test"));

        // Reverse order
        events = EventFactory.getEventsForNode(node1.getId(), SortStyle.REVERSE_ID, ackType, throttle, offset, null);
        assertEquals(events.length, 3);

        assertThat(events[0].uei, equalTo("uei.opennms.org/test"));
        assertThat(events[1].uei, equalTo("uei.opennms.org/test2"));
        assertThat(events[2].uei, equalTo("uei.opennms.org/test3"));
    }
}
