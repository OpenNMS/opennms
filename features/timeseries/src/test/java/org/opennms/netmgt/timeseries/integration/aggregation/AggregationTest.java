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


import static java.lang.Double.NaN;
import static org.opennms.netmgt.timeseries.integration.aggregation.Utils.assertRowsEqual;
import static org.opennms.newts.api.query.StandardAggregationFunctions.AVERAGE;
import static org.opennms.newts.api.query.StandardAggregationFunctions.MAX;
import static org.opennms.newts.api.query.StandardAggregationFunctions.MIN;

import java.util.Iterator;

import org.junit.Test;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;

/** Copied from Newts project. */
public class AggregationTest {

    @Test
    public void test() {

        Iterator<Row<Measurement>> testData = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
                .row(   1).element("m0", 1)
                .row( 300).element("m0", 1)
                .row( 600).element("m0", 1)
                .row( 900).element("m0", 1)
                .row(1200).element("m0", 1)
                .row(1500).element("m0", 1)
                .row(1800).element("m0", 3)
                .row(2100).element("m0", 3)
                .row(2400).element("m0", 3)
                .row(2700).element("m0", 3)
                .row(3000).element("m0", 3)
                .row(3300).element("m0", 3)
                .build();

        ResultDescriptor rDescriptor = new ResultDescriptor(Duration.seconds(300))
                .datasource("m0-avg", "m0", Duration.seconds(600), AVERAGE)
                .datasource("m0-min", "m0", Duration.seconds(600), MIN)
                .datasource("m0-max", "m0", Duration.seconds(600), MAX);

        Iterator<Row<Measurement>> expected = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
                .row(   0).element("m0-avg", NaN).element("m0-min", NaN).element("m0-max", NaN)
                .row(3600).element("m0-avg",   2).element("m0-min",   1).element("m0-max",   3)
                .build();

        Aggregation aggregation = new Aggregation(
                new Resource("localhost"),
                Timestamp.fromEpochSeconds(   1),
                Timestamp.fromEpochSeconds(3300),
                rDescriptor,
                Duration.minutes(60),
                testData);

        assertRowsEqual(expected, aggregation);

    }

}
