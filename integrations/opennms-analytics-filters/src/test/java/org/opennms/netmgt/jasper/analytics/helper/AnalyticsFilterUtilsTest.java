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

package org.opennms.netmgt.jasper.analytics.helper;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.jasper.analytics.AnalyticsCommand;
import org.opennms.netmgt.jasper.analytics.AnalyticsFilterTest;

public class AnalyticsFilterUtilsTest extends AnalyticsFilterTest {

    @Test
    public void canExtractAnalyticsCommandFromQueryString() {
        List<AnalyticsCommand> commands = AnalyticsFilterUtils.createFromQueryString("ANALYTICS:HoltWinters=HW:X:12:1:0.95");
        Assert.assertEquals(1, commands.size());
        Assert.assertEquals("HoltWinters", commands.get(0).getModule());
        Assert.assertEquals("HW", commands.get(0).getColumnNameOrPrefix());
        Assert.assertArrayEquals(new String[]{"X", "12", "1", "0.95"}, commands.get(0).getArguments());
    }

    @Test
    public void canExtractMultipleAnalyticCommandsFromQueryString() {
        final String expectedQs = "--start 0 --end 1 "
                + "DEF:xx=/path/to/my/file.jrb:test:AVERAGE"
                + "XPORT:xx:Values";
        final String qs = expectedQs + " "
                + "ANALYTICS:fn1=A:Values "
                + "ANALYTICS:fn2=B:Values "
                + "ANALYTICS:fn3=C:Values:1:2:3:4";
        List<AnalyticsCommand> commands = AnalyticsFilterUtils.createFromQueryString(qs);
        Assert.assertNotNull(commands);
        Assert.assertEquals(3, commands.size());

        AnalyticsCommand fn1 = commands.get(0);
        Assert.assertEquals("fn1", fn1.getModule());
        Assert.assertEquals("A", fn1.getColumnNameOrPrefix());
        Assert.assertEquals(1, fn1.getArguments().length);
        Assert.assertEquals("Values", fn1.getArguments()[0]);

        AnalyticsCommand fn3 = commands.get(2);
        Assert.assertEquals("fn3", fn3.getModule());
        Assert.assertEquals("C", fn3.getColumnNameOrPrefix());
        Assert.assertEquals(5, fn3.getArguments().length);
        Assert.assertEquals("Values", fn3.getArguments()[0]);
        Assert.assertEquals("4", fn3.getArguments()[4]);
    }

    @Test
    public void canInferFieldNamesFromQs() {
        String qs = "--start 0 --end 1 "
                + "DEF:xx=/path/to/my/file.jrb:test:AVERAGE"
                + "XPORT:xx:Values XPORT:xx:Values2";

        Assert.assertArrayEquals(new String[]{"Values", "Values2", "timestamp", "step", "start", "end"},
                AnalyticsFilterUtils.extratFieldNames(qs));
    }
}
