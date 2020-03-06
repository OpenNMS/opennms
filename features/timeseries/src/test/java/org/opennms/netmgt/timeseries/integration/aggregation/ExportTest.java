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
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.query.ResultDescriptor;

/** Copied from Newts project. */
public class ExportTest {

    @Test
    public void test() {

        // Rows with measurements for "m0", "m1", and "m2".
        Iterator<Row<Measurement>> testData = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
                .row(  1).element("m0", 1).element("m1", 2).element("m2", 3)
                .row(300).element("m0", 1).element("m1", 2).element("m2", 3)
                .row(600).element("m0", 1).element("m1", 2).element("m2", 3)
                .build();

        // ResultDescriptor that exports only "m1".
        ResultDescriptor rDescriptor = new ResultDescriptor().datasource("m1", null).export("m1");

        // Expected results.
        Iterator<Row<Measurement>> expected = new Utils.MeasurementRowsBuilder(new Resource("localhost"))
                .row(  1).element("m1", 2)
                .row(300).element("m1", 2)
                .row(600).element("m1", 2)
                .build();

        assertRowsEqual(expected, new Export(rDescriptor.getExports(), testData));

    }

}
