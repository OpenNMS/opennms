/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.Source;

/**
 * Utility functions.
 *
 * @author jwhite
 */
public class Utils {

    /**
     * Attempts to cast an arbitrary object to a Double.
     *
     * @throws NullPointerException when o is null
     * @throws NumberFormatException when the cast fails
     */
    public static Double toDouble(Object o) {
        if (o instanceof Double) {
            return (Double)o;
        } else {
            // Simple way of casting integers, floats and longs
            return Double.valueOf(o.toString());
        }
    }

    /**
     * Converts constants stored in strings.properties to {@link org.opennms.netmgt.measurements.api.FetchResults}
     * constants.
     *
     * Keys are prefix with the source label in order to avoid collisions.
     * Values are converted to doubles when possible to allows the to be used by the {@link org.opennms.netmgt.measurements.api.ExpressionEngine}.
     *
     */
    public static void convertStringAttributesToConstants(String sourceLabel, Map<String, String> stringAttributes, Map<String, Object> fetchResultConstants) {
        for (final Map.Entry<String, String> propertyEntry : stringAttributes.entrySet()) {
            final String propertyName = propertyEntry.getKey();

            // Attempt to cast the value as a double, fall back to keeping it as a string
            Object propertyValue;
            try {
                propertyValue = toDouble(propertyEntry.getValue());
            } catch (Throwable t) {
                propertyValue = propertyEntry.getValue();
            }

            fetchResultConstants.put(String.format("%s.%s", sourceLabel, propertyName),
                    propertyValue);
        }
    }

    /**
     * Enrich the <code>fetchResults</code> with NaN values for all <code>sources</code> which do not have values in the <code>fetchResults</code>.
     *
     * @param fetchResults
     * @param sources
     */
    public static void fillMissingValues(FetchResults fetchResults, List<Source> sources) {
        Objects.requireNonNull(fetchResults);
        Objects.requireNonNull(sources);
        final int rowCount = fetchResults.getTimestamps().length;
        for (Source eachSource : sources) {
            if (!fetchResults.getColumns().containsKey(eachSource.getLabel())) {
                fetchResults.getColumns().put(eachSource.getLabel(), createNaNArray(rowCount));
            }
        }
    }

    private static double[] createNaNArray(int numberOfRows) {
        final double[] values = new double[numberOfRows];
        for (int i=0; i<numberOfRows; i++) {
            values[i] = Double.NaN;
        }
        return values;
    }

    public static FetchResults createEmtpyFetchResults(final long step, final Map<String, Object> constants) {
        final Map<String, double[]> columns = new HashMap<>();
        return new FetchResults(new long[0], columns, step, constants);
    }
}
