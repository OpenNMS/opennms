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
package org.opennms.netmgt.eventd.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;

/**
 * Unit tests used to verify the number of transactions openened.
 *
 * @author jesse
 */
public class HibernateEventWriterTest {

    private HibernateEventWriter eventWriter;
    private TransactionOperations transactionManager;

    @Before
    public void setUp() {
        eventWriter = new HibernateEventWriter(new MetricRegistry());
        transactionManager = mock(TransactionOperations.class);
        eventWriter.setTransactionManager(transactionManager);
    }

    /**
     * Verifies that no transaction is opened when none of the events
     * in the event log need to be persisted.
     */
    @Test
    public void testNoTransactionOpened() throws EventProcessorException {
        // A null log
        eventWriter.process(null);
        verify(transactionManager, never()).execute(any());

        // An empty log
        eventWriter.process(new Log());
        verify(transactionManager, never()).execute(any());

        // A log with a single event that marked as 'donopersist'
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest(HibernateEventWriter.LOG_MSG_DEST_DO_NOT_PERSIST);
        eventWriter.process(bldr.getLog());
        verify(transactionManager, never()).execute(any());
    }


    /**
     * Verifies that a transaction is opened when at least of
     * of the events in the log need to be persisted.
     */
    @Test
    public void testSingleTransactionCreated() throws EventProcessorException {
        // A log with a single event that marked as 'logndisplay'
        EventBuilder bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);
        eventWriter.process(bldr.getLog());
        verify(transactionManager, times(1)).execute(any());
        reset(transactionManager);

        // A log with a multiple events that marked as 'logndisplay'
        bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);
        Event e1 = bldr.getEvent();

        bldr = new EventBuilder("testUei", "testSource");
        bldr.setLogDest(HibernateEventWriter.LOG_MSG_DEST_LOG_AND_DISPLAY);
        Event e2 = bldr.getEvent();

        Events events = new Events();
        events.setEvent(new Event[]{e1, e2});

        Log log = new Log();
        log.setEvents(events);

        eventWriter.process(log);
        verify(transactionManager, times(1)).execute(any());
    }
}
