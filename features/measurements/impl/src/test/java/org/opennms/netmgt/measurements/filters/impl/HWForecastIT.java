/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.measurements.filters.impl;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.measurements.api.Filter;
import org.opennms.netmgt.measurements.model.FilterDef;

import com.google.common.collect.RowSortedTable;
import com.google.common.collect.TreeBasedTable;

public class HWForecastIT extends AnalyticsFilterTest {

    @Test
    public void canCheckForecastSupport() {
        // Verify that this function doesn't throw any exceptions under normal circumstances
        HWForecast.checkForecastSupport();
    }

    @Test
    public void canForecastValues() throws Exception {
        FilterDef filterDef = new FilterDef("HoltWinters",
                "outputPrefix", "HW",
                "inputColumn", "X",
                "numPeriodsToForecast", "12",
                "periodInSeconds", "1",
                "confidenceLevel", "0.95");

        // Use constant values for the Y column
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 100; i++) {
            table.put(i, Filter.TIMESTAMP_COLUMN_NAME, (double)(i * 1000));
            table.put(i, "X", 1.0d);
        }

        // Make the forecasts
        getFilterEngine().filter(filterDef, table);

        // Original size + 12 forecasts
        Assert.assertEquals(112, table.rowKeySet().size());

        // The timestamps should be continuous
        for (long i = 0; i < 112; i++) {
            Assert.assertEquals((double) (i * 1000), table.get(i, Filter.TIMESTAMP_COLUMN_NAME), 0.0001);
        }

        // The forecasted value should be constant
        for (long i = 100; i < 112; i++) {
            Assert.assertEquals(1.0d, table.get(i, "HWFit"), 0.0001);
        }

        // Constant input yields zero residuals post-warmup, so the bounds should
        // coincide with the fit.
        for (long i = 100; i < 112; i++) {
            Assert.assertEquals(1.0d, table.get(i, "HWLwr"), 0.0001);
            Assert.assertEquals(1.0d, table.get(i, "HWUpr"), 0.0001);
        }
    }

    @Test
    public void boundsWidenOnNoisyInput() throws Exception {
        FilterDef filterDef = new FilterDef("HoltWinters",
                "outputPrefix", "HW",
                "inputColumn", "X",
                "numPeriodsToForecast", "2",
                "periodInSeconds", "10",
                "confidenceLevel", "0.95");

        // Deterministic Gaussian noise around a constant mean. numSamplesPerPeriod = 10
        // gives 20 training samples (10 warmup + 10 scored residuals), enough for a
        // meaningful standard deviation.
        java.util.Random rng = new java.util.Random(42);
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 200; i++) {
            table.put(i, Filter.TIMESTAMP_COLUMN_NAME, (double)(i * 1000));
            table.put(i, "X", 10.0d + rng.nextGaussian());
        }

        getFilterEngine().filter(filterDef, table);

        // numForecasts = numSamplesPerPeriod * numPeriodsToForecast = 10 * 2 = 20.
        // idxForecast runs 1..20 over rows 200..219; the horizon is capped at
        // numSamplesPerPeriod=10, so rows 200..209 should grow and rows 210..219
        // should be flat at the capped width.
        double prevWidth = 0.0;
        for (long i = 200; i < 210; i++) {
            double fit = table.get(i, "HWFit");
            double lwr = table.get(i, "HWLwr");
            double upr = table.get(i, "HWUpr");

            Assert.assertTrue("lwr should be <= fit at i=" + i, lwr <= fit);
            Assert.assertTrue("upr should be >= fit at i=" + i, upr >= fit);

            double width = upr - lwr;
            Assert.assertTrue("interval width should grow with horizon at i=" + i,
                    width >= prevWidth);
            prevWidth = width;
        }

        Assert.assertTrue("interval should have non-zero width with noisy input",
                prevWidth > 0.0);

        // Past the cap, widths are constant.
        double cappedWidth = prevWidth;
        for (long i = 210; i < 220; i++) {
            double width = table.get(i, "HWUpr") - table.get(i, "HWLwr");
            Assert.assertEquals("width should be capped past one period at i=" + i,
                    cappedWidth, width, 1e-9);
        }
    }

    @Test
    public void confidenceLevelZeroSuppressesBounds() throws Exception {
        FilterDef filterDef = new FilterDef("HoltWinters",
                "outputPrefix", "HW",
                "inputColumn", "X",
                "numPeriodsToForecast", "2",
                "periodInSeconds", "10",
                "confidenceLevel", "0");

        java.util.Random rng = new java.util.Random(42);
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        for (long i = 0; i < 200; i++) {
            table.put(i, Filter.TIMESTAMP_COLUMN_NAME, (double)(i * 1000));
            table.put(i, "X", 10.0d + rng.nextGaussian());
        }

        getFilterEngine().filter(filterDef, table);

        // Fit must still be produced.
        Assert.assertTrue("HWFit should still be produced when confidenceLevel=0",
                table.containsColumn("HWFit"));

        // Bounds must not be emitted (or at least never carry a value distinct from the fit).
        for (long i = 200; i < 220; i++) {
            Double lwr = table.get(i, "HWLwr");
            Double upr = table.get(i, "HWUpr");
            // Either the columns weren't emitted at all, or the bound rows are absent.
            Assert.assertNull("HWLwr should not be emitted with confidenceLevel=0 at i=" + i, lwr);
            Assert.assertNull("HWUpr should not be emitted with confidenceLevel=0 at i=" + i, upr);
        }
    }

    @Test
    public void insufficientSamplesEmitsNoFit() throws Exception {
        FilterDef filterDef = new FilterDef("HoltWinters",
                "outputPrefix", "HW",
                "inputColumn", "X",
                "numPeriodsToForecast", "2",
                "periodInSeconds", "10",
                "confidenceLevel", "0.95");

        // Only one row with a value; the filter should bail early because there
        // aren't enough samples to fit anything.
        RowSortedTable<Long, String, Double> table = TreeBasedTable.create();
        table.put(0L, Filter.TIMESTAMP_COLUMN_NAME, 0.0d);
        table.put(0L, "X", 1.0d);

        getFilterEngine().filter(filterDef, table);

        Assert.assertFalse("HWFit must not be emitted when there is no usable history",
                table.containsColumn("HWFit"));
        Assert.assertFalse("HWLwr must not be emitted when there is no usable history",
                table.containsColumn("HWLwr"));
        Assert.assertFalse("HWUpr must not be emitted when there is no usable history",
                table.containsColumn("HWUpr"));
    }
}
