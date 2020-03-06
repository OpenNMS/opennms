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

import java.util.Iterator;

import org.junit.Test;
import org.opennms.netmgt.timeseries.integration.aggregation.Utils.*;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;

/** Copied from Newts project. */
public class PrimaryDataTest {

    @Test
    public void testLeadingSamplesMiss() {

        // Missing a couple leading samples
        Iterator<Row<Sample>> testData = new Utils.SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(900000300).element("m0", 0)
                .row(900000600).element("m0", 1)
                .row(900000900).element("m0", 2)
                .row(900001200).element("m0", 3)
                .build();

        ResultDescriptor rDescriptor = new ResultDescriptor(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(600), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
                .row(900000000).element("m0", Double.NaN)
                .row(900000300).element("m0", Double.NaN)
                .row(900000600).element("m0", 1)
                .row(900000900).element("m0", 2)
                .row(900001200).element("m0", 3)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(900000000),
                Timestamp.fromEpochSeconds(900001200),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testShortSamples() {

        // Samples occur prior to the nearest step interval boundary.
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(000).element("m0", 0).element("m1", 1)
                .row(250).element("m0", 1).element("m1", 2)
                .row(550).element("m0", 2).element("m1", 3)
                .row(850).element("m0", 3).element("m1", 4)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(600), null).datasource("m1", "m1", Duration.seconds(600), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row(300).element("m0", 1.16666667).element("m1", 2.16666667)
                .row(600).element("m0", 2.16666667).element("m1", 3.16666667)
                .row(900).element("m0",        3.0).element("m1",        4.0)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(900),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testVeryShortSamples() {

        // Samples occur prior to the nearest step interval boundary.
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(000).element("m0", 0).element("m1", 1)
                .row(250).element("m0", 1).element("m1", 2)
                .row(550).element("m0", 2).element("m1", 3)
                .row(850).element("m0", 3).element("m1", 4)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(600), null).datasource("m1", "m1", Duration.seconds(600), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row(300).element("m0", 1.16666667).element("m1", 2.16666667)
                .row(600).element("m0", 2.16666667).element("m1", 3.16666667)
                .row(900).element("m0", 3.0).element("m1", 4.0)
                .row(1200).element("m0", Double.NaN).element("m1", Double.NaN)
                .row(1500).element("m0", Double.NaN).element("m1", Double.NaN)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(1500),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }


    @Test
    public void testSkippedSample() {

        // Sample m0 is missing at timestamp 550, (but interval does not exceed heartbeat).
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(  0).element("m0", 0).element("m1", 1)
                .row(250).element("m0", 1).element("m1", 2)
                .row(550).element("m1", 3)
                .row(840).element("m0", 3).element("m1", 4)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(600), null).datasource("m1", "m1", Duration.seconds(600), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row(300).element("m0", 1.33333333).element("m1", 2.16666667)
                .row(600).element("m0", 3.00000000).element("m1", 3.16666667)
                .row(900).element("m0", 3.00000000).element("m1", 4.00000000)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(900),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testManyToOneSamples() {

        // Element interval is less than step size.
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(   0).element("m0", 0).element("m1", 1)
                .row( 300).element("m0", 1).element("m1", 2)
                .row( 600).element("m0", 2).element("m1", 3)
                .row( 900).element("m0", 3).element("m1", 4)
                .row(1200).element("m0", 4).element("m1", 5)
                .row(1500).element("m0", 5).element("m1", 6)
                .row(1800).element("m0", 6).element("m1", 7)
                .row(2100).element("m0", 7).element("m1", 8)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(900))
                .datasource("m0", "m0", Duration.seconds(1800), null).datasource("m1", "m1", Duration.seconds(1800), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row( 900).element("m0", 2).element("m1", 3)
                .row(1800).element("m0", 5).element("m1", 6)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds( 900),
                Timestamp.fromEpochSeconds(1800),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testOneToOneSamples() {

        // Samples perfectly correlate to step interval boundaries.
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(   0).element("m0", 0).element("m1", 1)
                .row( 300).element("m0", 1).element("m1", 2)
                .row( 600).element("m0", 2).element("m1", 3)
                .row( 900).element("m0", 3).element("m1", 4)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(600), null).datasource("m1", "m1", Duration.seconds(600), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row(300).element("m0", 1).element("m1", 2)
                .row(600).element("m0", 2).element("m1", 3)
                .row(900).element("m0", 3).element("m1", 4)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(900),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testOneToManySamples() {

        // Actual sample interval is smaller than step size; One sample is mapped to many measurements
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(   0).element("m0", 0).element("m1", 1)
                .row( 900).element("m0", 1).element("m1", 2)
                .row(1800).element("m0", 2).element("m1", 3)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(1000), null).datasource("m1", "m1", Duration.seconds(1000), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row( 300).element("m0", 1).element("m1", 2)
                .row( 600).element("m0", 1).element("m1", 2)
                .row( 900).element("m0", 1).element("m1", 2)
                .row(1200).element("m0", 2).element("m1", 3)
                .row(1500).element("m0", 2).element("m1", 3)
                .row(1800).element("m0", 2).element("m1", 3)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds( 300),
                Timestamp.fromEpochSeconds(1800),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testLongSamples() {

        // Samples occur later-than (after) the step interval.
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(  0).element("m0", 0).element("m1", 1)
                .row(350).element("m0", 1).element("m1", 2)
                .row(650).element("m0", 2).element("m1", 3)
                .row(950).element("m0", 3).element("m1", 4)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(600), null).datasource("m1", "m1", Duration.seconds(600), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row( 300).element("m0",        1.0).element("m1",        2.0)
                .row( 600).element("m0", 1.83333333).element("m1", 2.83333333)
                .row( 900).element("m0", 2.83333333).element("m1", 3.83333333)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(900),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testHeartbeatOneSample() {

        // Sample interval of 600 seconds (m1) exceeds heartbeat of 601
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(  0).element("m1", 1)
                .row(300).element("m1", 2)
                .row(900).element("m1", 4)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m1", "m1", Duration.seconds(601), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row( 300)
                    .element("m1", 2)
                .row( 600)
                    .element("m1", 4)
                .row( 900)
                    .element("m1", 4)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(900),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testHeartbeatTwoSamples() {

        // Sample interval of 600 seconds (m1) exceeds heartbeat of 601
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(  0)
                    .element("m0", 0)
                    .element("m1", 1)
                .row(300)
                    .element("m0", 1)
                    .element("m1", 2)
                .row(600)
                    .element("m0", 2)
                    // missing entry for m1
                .row(900)
                    .element("m0", 3)
                    .element("m1", 4)
                .build();

        // Minimal result descriptor
        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m0", "m0", Duration.seconds(601), null)
                .datasource("m1", "m1", Duration.seconds(601), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row( 300)
                    .element("m0", 1)
                    .element("m1", 2)
                .row( 600)
                    .element("m0", 2)
                    .element("m1", 4)
                .row( 900)
                    .element("m0", 3)
                    .element("m1", 4)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(900),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }

    @Test
    public void testHeartbeatNaNs() {

        // Test for NEWTS-70
        Iterator<Row<Sample>> testData = new SampleRowsBuilder(new Resource("localhost"), MetricType.GAUGE)
                .row(   0).element("m1", 1)
                .row( 300).element("m1", 2)
                .row(1800).element("m1", 8)
                .build();

        ResultDescriptor rDescriptor = new ResultDescriptor().step(Duration.seconds(300))
                .datasource("m1", "m1", Duration.seconds(600), null);

        // Expected results
        Iterator<Row<Measurement>> expected = new MeasurementRowsBuilder(new Resource("localhost"))
                .row( 300)
                    .element("m1", 2)
                .row( 600)
                    .element("m1", Double.NaN)
                .row( 900)
                    .element("m1", Double.NaN)
                .row(1200)
                    .element("m1", Double.NaN)
                .row(1500)
                    .element("m1", Double.NaN)
                .row(1800)
                    .element("m1", Double.NaN)
                .build();

        PrimaryData primaryData = new PrimaryData(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(300),
                Timestamp.fromEpochSeconds(1800),
                rDescriptor,
                testData);

        assertRowsEqual(expected, primaryData);

    }
}
