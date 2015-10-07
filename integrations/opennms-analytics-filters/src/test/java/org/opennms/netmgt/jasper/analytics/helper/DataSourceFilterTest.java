/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.analytics.helper;


import java.util.Date;
import java.util.List;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.jasper.analytics.AnalyticsCommand;
import org.opennms.netmgt.jasper.analytics.AnalyticsFilterTest;

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
