/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import static org.mockito.Matchers.any;
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
