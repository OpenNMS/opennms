package org.opennms.netmgt.poller.pollables;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.events.EventBuilder;

public class PendingPollEventTest {

    @Before
    public void setUp() {
        System.setProperty("org.opennms.netmgt.poller.pendingEventTimeout", "1000");
    }

    @Test
    public void testPollEventTimeout() {
        final Date d = new Date();
        final EventBuilder eb = new EventBuilder("foo", "bar", d);
        final PendingPollEvent ppe = new PendingPollEvent(eb.getEvent());
        assertFalse(ppe.isTimedOut());
        assertTrue(ppe.isPending());
    }
}
