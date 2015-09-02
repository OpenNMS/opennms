/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.analytics;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.jasper.analytics.helper.AnalyticsFilterUtils;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class TrendLineIT extends AnalyticsFilterTest {
    @Test
    public void canTrend() throws Exception {
        final String qs = "ANALYTICS:TrendLine=Z:Y:1:3";
        List<AnalyticsCommand> cmds = AnalyticsFilterUtils.createFromQueryString(qs);
    
        // Use constant values for the Y column
        RowSortedTable<Integer, String, Double> table = TreeBasedTable.create();
        for (int i = 0; i < 100; i++) {
            table.put(i, Filter.TIMESTAMP_COLUMN_NAME, (double)(i* 1000));
            table.put(i, "Y", 1.0d);
        }

        // Apply the filter
        getDataSourceFilter().filter(cmds, table);

        // The Z column should be constant
        for (int i = 1; i <= 100; i++) {
            Assert.assertEquals((double) i * 1000, table.get(i, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
            Assert.assertEquals(1.0d, table.get(i, "Z"), 0.0001);
        }
    }
}
