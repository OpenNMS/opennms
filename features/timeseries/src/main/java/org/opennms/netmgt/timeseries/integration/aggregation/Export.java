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


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Results.Row;


/**
 * Filter results to a specified set of exports.
 *
 * Copied from Newts project.
 *
 */
class Export implements Iterable<Row<Measurement>>, Iterator<Row<Measurement>> {

    private final Set<String> m_exports;
    private final Iterator<Row<Measurement>> m_input;

    private Row<Measurement> m_current;

    Export(Set<String> exports, Iterator<Row<Measurement>> input) {
        m_exports = checkNotNull(exports, "exports argument");
        m_input = checkNotNull(input, "input argument");

        m_current = m_input.hasNext() ? m_input.next() : null;

    }

    @Override
    public boolean hasNext() {
        return m_current != null;
    }

    @Override
    public Row<Measurement> next() {

        if (!hasNext()) throw new NoSuchElementException();

        Row<Measurement> result = new Row<>(m_current.getTimestamp(), m_current.getResource());

        for (String export : m_exports) {
            result.addElement(getMeasurement(export));
        }

        try {
            return result;
        }
        finally {
            m_current = m_input.hasNext() ? m_input.next() : null;
        }
    }

    private Measurement getMeasurement(String name) {
        Measurement measurement = m_current.getElement(name);
        return (measurement != null) ? measurement : getNan(name);
    }

    private Measurement getNan(String name) {
        return new Measurement(m_current.getTimestamp(), m_current.getResource(), name, Double.NaN);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Row<Measurement>> iterator() {
        return this;
    }

}
