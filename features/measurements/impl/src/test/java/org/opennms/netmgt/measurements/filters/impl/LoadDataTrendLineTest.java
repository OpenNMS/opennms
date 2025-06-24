/* This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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
 */

package org.opennms.netmgt.measurements.filters.impl;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.integrations.R.RScriptException;

import java.io.IOException;
import java.util.Map;

public class LoadDataTrendLineTest extends LoadData {


    @Test
    public void testFirst() throws RScriptException, IOException {
        Entry[] entries = getFirst();
        Assert.assertEquals(74,entries.length);
        TrendLine trendLine = new TrendLine("Trend","Load",0L,1);
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        Long i = Long.valueOf("0");
        for (Entry entry : entries) {
            Assert.assertNotNull(entry);
            Assert.assertNotNull(entry.load);
            Assert.assertNotNull(entry.timestamp);
            if (Double.isNaN(entry.load)) {
                System.out.println("testFirst: " + i + "->" + entry.load);
            }
            table.put(i,"Load", entry.load);
            table.put(i,"timestamp", entry.timestamp);
            i++;
        }
        Assert.assertEquals(74, table.column("Load").size());
        Assert.assertEquals(74, table.column("timestamp").size());
        Assert.assertEquals(0, table.column("Trend").size());
        Assert.assertEquals(148, table.size());
        Utils.TableLimits limits = Utils.getRowsWithValues(table, "Load");
        Assert.assertEquals(1L, limits.firstRowWithValues);
        Assert.assertEquals(73L, limits.lastRowWithValues);
        trendLine.filter(table);
        Map<Long,Double> load = table.column("Load");
        Assert.assertEquals(74, load.size());
        Map<Long,Double> ts = table.column("timestamp");
        Assert.assertEquals(75, ts.size());
        Map<Long,Double> trend = table.column("Trend");
        Assert.assertEquals(74, trend.size());
        Assert.assertEquals(223, table.size());
    }

    @Test
    public void testSecond() throws RScriptException, IOException {
        Entry[] entries = getSecond();
        Assert.assertEquals(74,entries.length);
        TrendLine trendLine = new TrendLine("Trend","Load",0L,1);
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        long i =0L;
        for (Entry entry : entries) {
            Assert.assertNotNull(entry);
            Assert.assertNotNull(entry.load);
            if (Double.isNaN(entry.load)) {
                System.out.println("testSecond: " + i + "->" + entry.load);
            }
            Assert.assertNotNull(entry.timestamp);
            table.put(i,"Load", entry.load);
            table.put(i,"timestamp", entry.timestamp);
            i++;
        }
        Utils.TableLimits limits = Utils.getRowsWithValues(table, "Load");
        Assert.assertEquals(0L, limits.firstRowWithValues);
        Assert.assertEquals(72L, limits.lastRowWithValues);
        Assert.assertEquals(74, table.column("Load").size());
        Assert.assertEquals(74, table.column("timestamp").size());
        Assert.assertEquals(0, table.column("Trend").size());
        Assert.assertEquals(148, table.size());
        trendLine.filter(table);
        Map<Long,Double> load = table.column("Load");
        Assert.assertEquals(74, load.size());
        Map<Long,Double> ts = table.column("timestamp");
        Assert.assertEquals(74, ts.size());
        Map<Long,Double> trend = table.column("Trend");
        Assert.assertEquals(74, trend.size());
        Assert.assertEquals(222, table.size());
    }

    @Test
    public void testThird() throws RScriptException, IOException {
        Entry[] entries = getThird();
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        long i = 0L;
        double before= Double.NaN;
        Assert.assertEquals(1442, entries.length);
        for (Entry entry : entries) {
            Assert.assertNotNull(entry);
            Assert.assertNotNull(entry.load);
            Assert.assertNotNull(entry.timestamp);
            Assert.assertFalse(entry.timestamp.isNaN());
            if (entry.load.isNaN()) {
                System.out.println(i+":load->"+entry.load);
            }
            double after = entry.timestamp;
            if (i > 0) {
                Assert.assertTrue(Double.compare(after, before) > 0);
                double duration = after - before;
                Assert.assertEquals(1800000L, Double.valueOf(duration).longValue());
            }
            before=after;
            table.put(i,"Load", entry.load);
            table.put(i,"timestamp", entry.timestamp);
            i++;
        }
        Utils.TableLimits limits = Utils.getRowsWithValues(table, "Load");
        Assert.assertEquals(0L,limits.firstRowWithValues);
        Assert.assertEquals(1441L,limits.lastRowWithValues);
        Assert.assertEquals(1442, table.column("Load").size());
        Assert.assertEquals(1442, table.column("timestamp").size());
        Assert.assertEquals(0, table.column("Trend").size());
        TrendLine trendLine = new TrendLine("Trend","Load",0L,1);
        trendLine.filter(table);
        Map<Long,Double> load = table.column("Load");
        Assert.assertEquals(1442, load.size());
        Map<Long,Double> ts = table.column("timestamp");
        Assert.assertEquals(1443, ts.size());
        Map<Long,Double> trend = table.column("Trend");
        Assert.assertEquals(1443, trend.size());
        Assert.assertEquals(4328, table.size());
    }

}
