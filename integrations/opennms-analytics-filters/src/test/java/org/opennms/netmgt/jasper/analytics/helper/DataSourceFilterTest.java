package org.opennms.netmgt.jasper.analytics.helper;


import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.jasper.analytics.AnalyticsCommand;
import org.opennms.netmgt.jasper.analytics.AnalyticsFilterTest;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class DataSourceFilterTest extends AnalyticsFilterTest {

    @Test
    public void failsWithUnknownModule() {
        final String qs = "ANALYTICS:shouldNotExist=NA:Series";
        List<AnalyticsCommand> cmd = AnalyticsFilterUtils.createFromQueryString(qs);
        try {
            getDataSourceFilter().filter(cmd, TreeBasedTable.<Integer, String, Double>create());
            Assert.fail("Exception expected, but was not thrown");
        } catch (Exception ex) {
            Assert.assertEquals("No analytics module found for shouldNotExist", ex.getMessage());
        }
    }

    @Test
    public void canFilter() throws Exception {
        final RowSortedTable<Integer, String, Double> table = TreeBasedTable.create();
        table.put(0, "Timestamp", (double) new Date().getTime());
        table.put(0, "Y", 0d);

        getDataSourceFilter().filter(
                AnalyticsFilterUtils.createFromQueryString("ANALYTICS:NOOP=NA:Y"),
                table);
    }
}
