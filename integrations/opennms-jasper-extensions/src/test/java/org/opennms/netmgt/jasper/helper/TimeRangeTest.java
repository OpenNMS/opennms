package org.opennms.netmgt.jasper.helper;

import org.junit.Assert;
import org.junit.Test;

public class TimeRangeTest {

    @Test
    public void testTimeRange() {
        for (TimeRange eachTimeRange : TimeRange.values()) {
            Assert.assertEquals(eachTimeRange.getEndDate(), TimeRange.getEndDate(eachTimeRange.name()));
            Assert.assertEquals(eachTimeRange.getStartDate(), TimeRange.getStartDate(eachTimeRange.name()));
        }
    }
}
