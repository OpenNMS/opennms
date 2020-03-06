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

import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.query.ResultDescriptor;
import org.opennms.newts.api.query.Calculation;

import com.google.common.base.Optional;

/** Copied from Newts project. */
class Compute implements Iterator<Row<Measurement>>, Iterable<Row<Measurement>> {

    private final ResultDescriptor m_resultDescriptor;
    private final Iterator<Row<Measurement>> m_input;

    Compute(ResultDescriptor resultDescriptor, Iterator<Row<Measurement>> input) {
        m_resultDescriptor = checkNotNull(resultDescriptor, "result descriptor argument");
        m_input = checkNotNull(input, "input argument");
    }

    @Override
    public boolean hasNext() {
        return m_input.hasNext();
    }

    @Override
    public Row<Measurement> next() {

        if (!hasNext()) throw new NoSuchElementException();

        Row<Measurement> row = m_input.next();

        for (Calculation calc : m_resultDescriptor.getCalculations().values()) {
            double v = calc.getCalculationFunction().apply(getValues(row, calc.getArgs()));
            row.addElement(new Measurement(row.getTimestamp(), row.getResource(), calc.getLabel(), v));
        }

        return row;
    }

    private double[] getValues(Row<Measurement> row, String[] names) {
        double[] values = new double[names.length];

        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            Optional<Double> d = parseDouble(name);
            values[i] = d.isPresent() ? d.get() : checkNotNull(row.getElement(name), "Missing measurement; Upstream iterator is bugged").getValue();
        }

        return values;
    }
    
    Optional<Double> parseDouble(String maybeNum) {
        try {
            return Optional.of(Double.parseDouble(maybeNum));
        } catch (NumberFormatException e) {
            return Optional.absent();
        }
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
