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
import static org.opennms.newts.api.Duration.seconds;
import static org.opennms.newts.api.query.StandardAggregationFunctions.AVERAGE;

import java.util.Iterator;

import org.junit.Test;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.query.ResultDescriptor;
import org.opennms.newts.api.query.ResultDescriptor.BinaryFunction;

/** Copied from Newts project. */
public class ComputeTest {

    private static final BinaryFunction PLUS;
    private static final BinaryFunction DIVIDE;

    static {
        PLUS = new BinaryFunction() {
            private static final long serialVersionUID = 0L;

            @Override
            public double apply(double a, double b) {
                return a + b;
            }
        };
        
        DIVIDE = new BinaryFunction() {
            private static final long serialVersionUID = 0L;

            @Override
            public double apply(double a, double b) {
                return a / b;
            }
        };
    }

    @Test
    public void test() {

        Iterator<Row<Measurement>> testData = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
                .row(300).element("in", 2).element("out", 2)
                .row(600).element("in", 6).element("out", 4)
                .build();

        ResultDescriptor rDescriptor = new ResultDescriptor()
                .datasource("in",  AVERAGE)
                .datasource("out", AVERAGE)
                .calculate("total", PLUS, "in", "out");

        Iterator<Row<Measurement>> expected = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
                .row(300).element("in", 2).element("out", 2).element("total", 4)
                .row(600).element("in", 6).element("out", 4).element("total", 10)
                .build();

        Compute compute = new Compute(rDescriptor, testData);

        assertRowsEqual(expected, compute);

    }

    @Test
    public void testCalcOfCalc() {
        Iterator<Row<Measurement>> testData = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
            .row(300).element("in", 20).element("out", 20)
            .row(600).element("in", 60).element("out", 40)
            .build();

        ResultDescriptor rDescriptor = new ResultDescriptor()
            .datasource("in", AVERAGE)
            .datasource("out", AVERAGE)
            .calculate("sum", PLUS, "in", "out")
            .calculate("tens", DIVIDE, "sum", "10")
            .export("tens")
        ;

        Iterator<Row<Measurement>> expected = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
            .row(300).element("in", 20).element("out", 20).element("sum", 40).element("tens", 4)
            .row(600).element("in", 60).element("out", 40).element("sum", 100).element("tens", 10)
            .build();

        Compute compute = new Compute(rDescriptor, testData);

        assertRowsEqual(expected, compute);

    }

    @Test
    public void testExpressions() {
        Iterator<Row<Measurement>> testData = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
            .row(300).element("in", 20).element("out", 20)
            .row(600).element("in", 60).element("out", 40)
            .build();

        ResultDescriptor rDescriptor = new ResultDescriptor()
            .datasource("in", "ifInOctets", seconds(600), AVERAGE)
            .datasource("out", "ifOutOctets", seconds(600), AVERAGE)
            .expression("sum", "in + out")
            .expression("diff", "in - out")
            .expression("ratio", "diff/sum")
            .export("ratio");

        Iterator<Row<Measurement>> expected = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
            .row(300).element("in", 20).element("out", 20).element("sum", 40).element("diff", 0).element("ratio", 0)
            .row(600).element("in", 60).element("out", 40).element("sum", 100).element("diff", 20).element("ratio", 0.2)
            .build();

        Compute compute = new Compute(rDescriptor, testData);

        assertRowsEqual(expected, compute);

    }
}
