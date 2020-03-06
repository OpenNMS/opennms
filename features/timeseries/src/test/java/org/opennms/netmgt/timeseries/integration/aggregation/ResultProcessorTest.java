/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration.aggregation;

import static org.opennms.netmgt.timeseries.integration.aggregation.Utils.assertRowsEqual;
import static org.opennms.newts.api.query.StandardAggregationFunctions.AVERAGE;

import java.util.Iterator;

import org.junit.Test;
import org.opennms.netmgt.timeseries.integration.aggregation.Utils.MeasurementRowsBuilder;
import org.opennms.netmgt.timeseries.integration.aggregation.Utils.SampleRowsBuilder;
import org.opennms.newts.aggregate.ResultProcessor;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;
import org.opennms.newts.api.query.ResultDescriptor.BinaryFunction;

/** Copied from Newts project. */
public class ResultProcessorTest {

    @Test
    public void testCalculated() {

        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.COUNTER)
                .row(900000000).element("m0",  3000).element("m1",  3000)      // Thu Jul  9 11:00:00 CDT 1998
                .row(900000300).element("m0",  6000).element("m1",  6000)
                .row(900000600).element("m0",  9000).element("m1",  9000)
                .row(900000900).element("m0", 12000).element("m1", 12000)
                .row(900001200).element("m0", 15000).element("m1", 15000)
                .row(900001500).element("m0", 18000).element("m1", 18000)
                .row(900001800).element("m0", 21000).element("m1", 21000)
                .row(900002100).element("m0", 24000).element("m1", 24000)
                .row(900002400).element("m0", 27000).element("m1", 27000)
                .row(900002700).element("m0", 30000).element("m1", 30000)
                .row(900003000).element("m0", 33000).element("m1", 33000)
                .row(900003300).element("m0", 36000).element("m1", 36000)
                .row(900003600).element("m0", 39000).element("m1", 39000)
                .row(900003900).element("m0", 42000).element("m1", 42000)
                .row(900004200).element("m0", 45000).element("m1", 45000)
                .row(900004500).element("m0", 48000).element("m1", 48000)
                .row(900004800).element("m0", 51000).element("m1", 51000)
                .row(900005100).element("m0", 54000).element("m1", 54000)
                .row(900005400).element("m0", 57000).element("m1", 57000)
                .row(900005700).element("m0", 60000).element("m1", 60000)
                .row(900006000).element("m0", 63000).element("m1", 63000)
                .row(900006300).element("m0", 66000).element("m1", 66000)
                .row(900006600).element("m0", 69000).element("m1", 69000)
                .row(900006900).element("m0", 72000).element("m1", 72000)
                .row(900007200).element("m0", 75000).element("m1", 75000)      // Thu Jul  9 13:00:00 CDT 1998
                .build();

        // Function to add two values
        BinaryFunction sum = new BinaryFunction() {
            private static final long serialVersionUID = 0L;

            @Override
            public double apply(double a, double b) {
                return a + b;
            }
        };

        ResultDescriptor rDescriptor = new ResultDescriptor(Duration.seconds(300))
                .datasource("m0", AVERAGE)
                .datasource("m1", AVERAGE)
                .calculate("total", sum, "m0", "m1")
                .export("total");

        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row(900003600).element("total", 20)
                .row(900007200).element("total", 20)
                .build();

        ResultProcessor processor = new ResultProcessor(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(900003600),
                Timestamp.fromEpochSeconds(900007200),
                rDescriptor,
                Duration.minutes(60));

        assertRowsEqual(expected, processor.process(testData).iterator());

    }

    @Test
    public void testCounterRate() {

        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.COUNTER)
                .row(900000000).element("m0",  3000)        // Thu Jul  9 11:00:00 CDT 1998
                .row(900000300).element("m0",  6000)
                .row(900000600).element("m0",  9000)
                .row(900000900).element("m0", 12000)
                .row(900001200).element("m0", 15000)
                .row(900001500).element("m0", 18000)
                .row(900001800).element("m0", 21000)
                .row(900002100).element("m0", 24000)
                .row(900002400).element("m0", 27000)
                .row(900002700).element("m0", 30000)
                .row(900003000).element("m0", 33000)
                .row(900003300).element("m0", 36000)
                .row(900003600).element("m0", 39000)
                .row(900003900).element("m0", 42000)
                .row(900004200).element("m0", 45000)
                .row(900004500).element("m0", 48000)
                .row(900004800).element("m0", 51000)
                .row(900005100).element("m0", 54000)
                .row(900005400).element("m0", 57000)
                .row(900005700).element("m0", 60000)
                .row(900006000).element("m0", 63000)
                .row(900006300).element("m0", 66000)
                .row(900006600).element("m0", 69000)
                .row(900006900).element("m0", 72000)
                .row(900007200).element("m0", 75000)        // Thu Jul  9 13:00:00 CDT 1998
                .build();

        ResultDescriptor rDescriptor = new ResultDescriptor(Duration.seconds(300)).datasource("m0", AVERAGE).export("m0");

        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row(900003600).element("m0", 10.0)
                .row(900007200).element("m0", 10.0)
                .build();

        ResultProcessor processor = new ResultProcessor(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(900003600),
                Timestamp.fromEpochSeconds(900007200),
                rDescriptor,
                Duration.minutes(60));

        assertRowsEqual(expected, processor.process(testData).iterator());

    }

    @Test
    public void test() {

        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(900000000).element("m0", 1)        // Thu Jul  9 11:00:00 CDT 1998
                .row(900000300).element("m0", 1)
                .row(900000600).element("m0", 1)
                .row(900000900).element("m0", 1)
                .row(900001200).element("m0", 1)
                .row(900001500).element("m0", 1)
                .row(900001800).element("m0", 1)
                .row(900002100).element("m0", 3)
                .row(900002400).element("m0", 3)
                .row(900002700).element("m0", 3)
                .row(900003000).element("m0", 3)
                .row(900003300).element("m0", 3)
                .row(900003600).element("m0", 3)
                .row(900003900).element("m0", 1)
                .row(900004200).element("m0", 1)
                .row(900004500).element("m0", 1)
                .row(900004800).element("m0", 1)
                .row(900005100).element("m0", 1)
                .row(900005400).element("m0", 1)
                .row(900005700).element("m0", 3)
                .row(900006000).element("m0", 3)
                .row(900006300).element("m0", 3)
                .row(900006600).element("m0", 3)
                .row(900006900).element("m0", 3)
                .row(900007200).element("m0", 3)        // Thu Jul  9 13:00:00 CDT 1998
                .build();

        ResultDescriptor rDescriptor = new ResultDescriptor(Duration.seconds(300))
                .datasource("m0-avg", "m0", Duration.seconds(600), AVERAGE).export("m0-avg");

        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row(900003600).element("m0-avg", 2.0)
                .row(900007200).element("m0-avg", 2.0)
                .build();

        ResultProcessor processor = new ResultProcessor(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(900003600),
                Timestamp.fromEpochSeconds(900007200),
                rDescriptor,
                Duration.minutes(60));

        assertRowsEqual(expected, processor.process(testData).iterator());

    }

}
