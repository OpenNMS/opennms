package org.opennms.netmgt;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class EventConstantsTest {

    @Test
    public void testEventDateParse() throws Exception {
        final String sampleTimeText = "Thursday, 10 March 2011 22:40:37 o'clock GMT";
        final long sampleTimeEpoch = 1299796837 * 1000L;
        final Date date = EventConstants.parseToDate(sampleTimeText);
        assertEquals(sampleTimeEpoch, date.getTime());
    }

}
