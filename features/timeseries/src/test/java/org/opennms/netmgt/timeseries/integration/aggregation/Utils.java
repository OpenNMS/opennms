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


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.opennms.newts.api.Element;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/** Copied from Newts project. */
class Utils {

    static abstract class AbstractRowsBuilder<T extends Element<?>> {

        private final List<Row<T>> m_results = Lists.newArrayList();
        private final Resource m_resource;

        private Row<T> m_current;

        AbstractRowsBuilder(Resource resource) {
            m_resource = checkNotNull(resource, "resource argument");
        }

        AbstractRowsBuilder<T> row(Timestamp timestamp) {
            if (m_current != null && (!timestamp.gt(m_current.getTimestamp()))) {
                throw new IllegalArgumentException("rows must be added in sort order");
            }
            m_current = new Row<>(timestamp, m_resource);
            m_results.add(m_current);
            return this;
        }

        AbstractRowsBuilder<T> row(int epochSeconds) {
            return row(Timestamp.fromEpochSeconds(epochSeconds));
        }

        protected Resource getResource() {
            return m_resource;
        }

        protected Timestamp getCurrentTimestamp() {
            return m_current.getTimestamp();
        }

        protected void addElement(T element) {
            m_current.addElement(element);
        }

        abstract AbstractRowsBuilder<T> element(String name, double value);
        abstract AbstractRowsBuilder<T> element(String name, double value, Map<String, String> attrs);

        Iterator<Row<T>> build() {
            return m_results.iterator();
        }

    }

    static class MeasurementRowsBuilder extends AbstractRowsBuilder<Measurement> {

        MeasurementRowsBuilder(Resource resource) {
            super(resource);
        }

        @Override
        MeasurementRowsBuilder element(String name, double value) {
            addElement(new Measurement(getCurrentTimestamp(), getResource(), name, value));
            return this;
        }

        @Override
        AbstractRowsBuilder<Measurement> element(String name, double value, Map<String, String> attrs) {
            addElement(new Measurement(getCurrentTimestamp(), getResource(), name, value, attrs));
            return this;
        }

    }

    static class SampleRowsBuilder extends AbstractRowsBuilder<Sample> {

        private final MetricType m_type;

        SampleRowsBuilder(Resource resource, MetricType type) {
            super(resource);

            m_type = checkNotNull(type, "type argument");
        }

        @Override
        SampleRowsBuilder element(String name, double value) {
            addElement(new Sample(getCurrentTimestamp(), getResource(), name, m_type, ValueType.compose(value, m_type)));
            return this;
        }

        @Override
        AbstractRowsBuilder<Sample> element(String name, double value, Map<String, String> attrs) {
            addElement(new Sample(getCurrentTimestamp(), getResource(), name, m_type, ValueType.compose(value, m_type), attrs));
            return this;
        }

    }

    /**
     * Assert that two sets of {@link Row} results are equal.
     * 
     * @param expectedRows
     *            expected value
     * @param actualRows
     *            actual value
     */
    static void assertRowsEqual(Iterator<Row<Measurement>> expectedRows, Iterator<Row<Measurement>> actualRows) {

        while (actualRows.hasNext()) {
            Row<Measurement> actual = actualRows.next();

            assertTrue("Extraneous result row(s)", expectedRows.hasNext());

            Row<Measurement> expected = expectedRows.next();

            assertEquals("Unexpected row resource", expected.getResource(), actual.getResource());
            assertEquals("Unexpected row timestamp", expected.getTimestamp(), actual.getTimestamp());
            assertEquals("Measurement count mismatch", expected.getElements().size(), actual.getElements().size());

            for (Measurement m : actual.getElements()) {
                assertNotNull("Extraneous result measurement(s)", expected.getElement(m.getName()));
                assertSamplesEqual(expected.getElement(m.getName()), m);
            }

        }

        assertFalse("Missing result rows(s)", expectedRows.hasNext());

    }

    /**
     * Assert that two {@link Measurements}s are equal.
     * 
     * @param expected
     *            expected value
     * @param actual
     *            actual value
     */
    static void assertSamplesEqual(Measurement expected, Measurement actual) {
        checkNotNull(expected, "expected");
        checkNotNull(actual, "actual");
        assertEquals("Unexpected measurement name", expected.getName(), actual.getName());
        assertEquals("Unexpected measurement resource", expected.getResource(), actual.getResource());
        assertEquals("Unexpected measurement timestamp", expected.getTimestamp(), actual.getTimestamp());
        assertEquals("Incorrect value", expected.getValue().doubleValue(), actual.getValue().doubleValue(), 0.00000001d);
    }


    /**
     * Return a {@link Map} given an even numbered sequence of {@link String}s.
     *
     * @param attributes
     *            sequence of strings, (k0, v0, k1, v1, ...)
     * @return map representation of supplied strings
     */
    static Map<String, String> mapFor(String... attributes) {
        checkArgument((attributes.length % 2) == 0, "not an even sequence of k/v pairs");

        Map<String, String> r = Maps.newHashMap();

        for (int i = 0; i < attributes.length; i += 2) {
            r.put(attributes[i], attributes[i + 1]);
        }

        return r;
    }

    /**
     * Assert that the attributes of a {@link Measurement} match the expected {@link Map}.
     * 
     * @param measurement
     *            measurement to test
     * @param expected
     *            map that the measurements attributes should match
     */
    static void assertAttributes(Measurement measurement, Map<String, String> expected) {
        assertThat("Missing measurement", measurement, notNullValue());
        assertThat("Missing measurement attributes", measurement.getAttributes(), notNullValue());
        assertThat(measurement.getAttributes(), equalTo(expected));
    }

}
