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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import java.util.Date;
import java.util.List;

import net.sf.jasperreports.data.cache.ColumnValues;
import net.sf.jasperreports.data.cache.ColumnValuesDataSource;
import net.sf.jasperreports.data.cache.DoubleArrayValues;
import net.sf.jasperreports.data.cache.ObjectArrayValues;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRRewindableDataSource;

import org.junit.Test;

public class RrdDataSourceFilterTest {
    @Test
    public void canInferFieldNamesFromQs() {
        String qs = "--start 0 --end 1 "
                + "DEF:xx=/path/to/my/file.jrb:test:AVERAGE"
                + "XPORT:xx:Values XPORT:xx:Values2";
        RrdDataSourceFilter dse = new RrdDataSourceFilter(qs);
        assertArrayEquals(new String[]{"Values", "Values2", "Timestamp"},
                dse.getFieldNames());
    }

    @Test
    public void canParseQueryString() {
        final String expectedQs = "--start 0 --end 1 "
                + "DEF:xx=/path/to/my/file.jrb:test:AVERAGE"
                + "XPORT:xx:Values";   
        final String qs = expectedQs + " "
                + "ANALYTICS:fn1=A:Values "
                + "ANALYTICS:fn2=B:Values "
                + "ANALYTICS:fn3=C:Values:1:2:3:4";

        RrdDataSourceFilter dse = new RrdDataSourceFilter(qs);
        
        // All of the analytics commands should be removed from the qs
        assertEquals(expectedQs, dse.getRrdQueryString());

        // Verify the resulting commands
        List<AnalyticsCommand> commands = dse.getAnalyticsCommands();

        AnalyticsCommand fn1 = commands.get(0);
        assertEquals("fn1", fn1.getModule());
        assertEquals("A", fn1.getColumnNameOrPrefix());
        assertEquals(1, fn1.getArguments().length);
        assertEquals("Values", fn1.getArguments()[0]);

        AnalyticsCommand fn3 = commands.get(2);
        assertEquals("fn3", fn3.getModule());
        assertEquals("C", fn3.getColumnNameOrPrefix());
        assertEquals(5, fn3.getArguments().length);
        assertEquals("Values", fn3.getArguments()[0]);
        assertEquals("4", fn3.getArguments()[4]);
    }

    @Test(expected = JRException.class)
    public void failsWithUnknownModule() throws JRException {
        final String qs = "ANALYTICS:shouldNotExist=NA:Series";
        RrdDataSourceFilter dse = new RrdDataSourceFilter(qs);
        dse.filter(new JREmptyDataSource());
    }

    @Test
    public void canFilter() throws JRException {
        final String qs = "ANALYTICS:NOOP=NA:Y";
        RrdDataSourceFilter dse = new RrdDataSourceFilter(qs);

        // Build a data source
        ColumnValues timestamp = new ObjectArrayValues(
                new Object[] {new Date()}
        );

        ColumnValues y = new DoubleArrayValues(
                new double[] {0}
        );

        JRRewindableDataSource dsToFilter = new ColumnValuesDataSource(
                new String[] {"Timestamp", "Y"},
                1,
                new ColumnValues[] {timestamp, y}
        );

        // Filter the ds
        dse.filter(dsToFilter);
    }
}
