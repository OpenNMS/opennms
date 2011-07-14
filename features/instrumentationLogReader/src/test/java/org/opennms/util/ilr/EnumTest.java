package org.opennms.util.ilr;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.util.ilr.Collector.SortColumn;

public class EnumTest {
    @Test
    public void testSetSortColumn(){
        String param = "TOTALCOLLECTS";
        assertEquals(SortColumn.TOTALCOLLECTS, SortColumn.valueOf(param));
    }
}
