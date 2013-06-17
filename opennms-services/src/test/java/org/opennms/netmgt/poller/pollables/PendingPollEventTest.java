package org.opennms.netmgt.poller.pollables;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;
import org.opennms.netmgt.model.events.EventBuilder;

public class PendingPollEventTest {
    @Test
    public void testPollEventTimeout() throws Exception {
        final Date d = new Date();
        final EventBuilder eb = new EventBuilder("foo", "bar", d);
        final PendingPollEvent ppe = new PendingPollEvent(eb.getEvent());
        ppe.setExpirationTimeInMillis(Long.MAX_VALUE);
        assertFalse("timedOut should be false: " + ppe.isTimedOut(), ppe.isTimedOut());
        assertTrue("pending should be true: " + ppe.isPending(), ppe.isPending());
        ppe.setExpirationTimeInMillis(Long.MIN_VALUE);
        assertTrue("timedOut should be true: " + ppe.isTimedOut(), ppe.isTimedOut());
        assertFalse("pending should be false: " + ppe.isPending(), ppe.isPending());
    }
}
