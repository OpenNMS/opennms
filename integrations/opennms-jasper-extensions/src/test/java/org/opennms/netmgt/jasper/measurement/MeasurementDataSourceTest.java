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

package org.opennms.netmgt.jasper.measurement;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.measurements.model.QueryResponse;

import net.sf.jasperreports.engine.JRField;

/**
 * Verifies that the {@link MeasurementDataSource} is working correctly.
 */
public class MeasurementDataSourceTest {

    @Test
    public void testUnmarshal() {
        // ensure that unmarshalling works, basically that QueryResponse.class is available
        MeasurementDataSource ds = new MeasurementDataSource(getClass().getResourceAsStream("/responses/response1.xml"));
        Assert.assertNotNull(ds.response);
    }

    // if there is no data it should work
    @Test
    public void testEmptyData() throws Exception {
        MeasurementDataSource ds = new MeasurementDataSource((InputStream) null);
        Assert.assertFalse(ds.next());

        ds = new MeasurementDataSource((QueryResponse) null);
        Assert.assertFalse(ds.next());

        // Accessing fields should NOT fail
        Assert.assertNull(ds.getFieldValue("step", ds.getCurrentRow()));
        Assert.assertNull(ds.getFieldValue("start", ds.getCurrentRow()));
        Assert.assertNull(ds.getFieldValue("end", ds.getCurrentRow()));
        Assert.assertNull(ds.getFieldValue("timestamp", ds.getCurrentRow()));
        Assert.assertNull(ds.getFieldValue("IfHCInOctets", ds.getCurrentRow()));
        Assert.assertNull(ds.getFieldValue("IfHCOutOctets", ds.getCurrentRow()));
    }

    @Test
    public void testMeasurementDataSource() {
        MeasurementDataSource ds = new MeasurementDataSource(getClass().getResourceAsStream("/responses/response1.xml"));
        List<JRField> fields = ds.getFields();
        Assert.assertEquals(6, fields.size());
        Assert.assertEquals(-1, ds.getCurrentRow());
        Assert.assertEquals(6, ds.getRowCount());

        final Double[][] expectedValues = new Double[][] {
                {Double.NaN, Double.NaN},
                {Double.NaN, Double.NaN},
                {1.3, 2.3},
                {1.4, 2.4},
                {1.5, 2.5},
                {1.6, 2.6},
        };

        final Long[] expectedTimestamps = new Long[] {
                1439544100000L, 1439544200000L, 1439544300000L,
                1439544400000L, 1439544500000L, 1439544600000L
        };

        // to verify that moveFirst() is correctly implemented, we execute the test twice
        int runCount = 2;
        do {
            while (ds.next()) {
                // verify constant fields
                verifyField(ds, "step", Long.class, 1234567L);
                verifyField(ds, "start", Long.class, 1439539163433L);
                verifyField(ds, "end", Long.class, 1439557163433L);
                verifyField(ds, "timestamp", Date.class, new Date(expectedTimestamps[ds.getCurrentRow()]));

                // verify constant fields
                verifyField(ds, "IfHCInOctets", Double.class, expectedValues[ds.getCurrentRow()][0]);
                verifyField(ds, "IfHCOutOctets", Double.class, expectedValues[ds.getCurrentRow()][1]);
            }
            Assert.assertEquals(ds.getRowCount(), ds.getCurrentRow());
            ds.moveFirst();
        } while (--runCount > 0);
    }

    // verifies that both getFieldValue-methods work correctly
    private <T> void verifyField(MeasurementDataSource ds, String fieldName, Class<T> clazz, T value) {
        Assert.assertEquals(value, ds.getFieldValue(fieldName, ds.getCurrentRow()));
        Assert.assertEquals(value, ds.getFieldValue(MeasurementDataSource.createField(fieldName, clazz)));
    }
}
