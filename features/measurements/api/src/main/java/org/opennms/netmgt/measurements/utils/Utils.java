/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.measurements.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.QueryMetadata;
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
        return new FetchResults(new long[0], columns, step, constants, new QueryMetadata());
    }
}
