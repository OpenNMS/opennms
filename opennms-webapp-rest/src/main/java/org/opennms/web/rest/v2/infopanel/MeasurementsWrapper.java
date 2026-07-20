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
package org.opennms.web.rest.v2.infopanel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opennms.netmgt.measurements.api.MeasurementsService;
import org.opennms.netmgt.measurements.api.exceptions.MeasurementException;
import org.opennms.netmgt.measurements.model.QueryRequest;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

import com.google.common.primitives.Doubles;

/**
 * Wrapper around {@link MeasurementsService} placed into the Jinjava template
 * context as {@code measurements}, so templates can embed last-values,
 * arbitrary queries, and interface utilization.
 *
 * <p>Mirrors the legacy topology map's wrapper of the same name so existing
 * {@code etc/infopanel/} templates render unchanged.
 */
public class MeasurementsWrapper {

    /** Default look-back window for last-value and utilization queries: 15 minutes. */
    private static final long DEFAULT_WINDOW_MS = 15 * 60 * 1000;

    /** Default query step (resolution): 5 minutes. */
    private static final long DEFAULT_STEP_MS = 5 * 60 * 1000;

    private final MeasurementsService measurementsService;

    public MeasurementsWrapper(final MeasurementsService measurementsService) {
        this.measurementsService = measurementsService;
    }

    /** Last value for a resource/attribute over the default 15-minute window (AVERAGE). */
    public double getLastValue(final String resource, final String attribute) throws MeasurementException {
        return getLastValue(resource, attribute, "AVERAGE");
    }

    /** Last value for a resource/attribute with the given aggregation. */
    public double getLastValue(final String resource, final String attribute, final String aggregation) throws MeasurementException {
        return getLastValue(resource, attribute, aggregation, true);
    }

    public double getLastValue(final String resource, final String attribute, final String aggregation, final boolean relaxed) throws MeasurementException {
        final long end = System.currentTimeMillis();
        final long start = end - DEFAULT_WINDOW_MS;

        final QueryResponse.WrappedPrimitive[] columns = queryInt(resource, attribute, start, end, DEFAULT_STEP_MS, aggregation, relaxed).getColumns();

        if (columns.length > 0) {
            final double[] values = columns[0].getList();
            for (int i = values.length - 1; i >= 0; i--) {
                if (!Double.isNaN(values[i])) {
                    return values[i];
                }
            }
        }
        return Double.NaN;
    }

    /** Query a resource/attribute over an explicit window, returning the series. */
    public List<Double> query(final String resource, final String attribute, final long start, final long end, final long step, final String aggregation, final boolean relaxed) throws MeasurementException {
        final QueryResponse.WrappedPrimitive[] columns = queryInt(resource, attribute, start, end, step, aggregation, relaxed).getColumns();
        if (columns.length > 0) {
            return Doubles.asList(columns[0].getList());
        }
        return Collections.emptyList();
    }

    /**
     * Compute the in/out percentage utilization of a node interface (by ifName),
     * using HC octet attributes with non-HC fallbacks.
     */
    public List<Double> computeUtilization(final OnmsNode node, final String ifName) throws MeasurementException {
        final long end = System.currentTimeMillis();
        final long start = end - DEFAULT_WINDOW_MS;

        for (final OnmsSnmpInterface snmpInterface : node.getSnmpInterfaces()) {
            if (ifName.equals(snmpInterface.getIfName())) {
                final String resourceId = "node[" + node.getId() + "].interfaceSnmp[" + snmpInterface.computeLabelForRRD() + "]";
                return computeUtilization(resourceId, start, end, DEFAULT_STEP_MS, "AVERAGE");
            }
        }
        return Arrays.asList(Double.NaN, Double.NaN);
    }

    /**
     * Compute the in/out percentage utilization of an interface resource over a
     * window. The rates are queried as plain columns and the percentages are
     * computed here from the interface's {@code ifHighSpeed} constant (exposed
     * in the query response, prefixed by the source label) -- the legacy JEXL
     * expressions referenced misspelled variables and relied on engine quirks.
     */
    public List<Double> computeUtilization(final String resource, final long start, final long end, final long step, final String aggregation) throws MeasurementException {
        final QueryRequest request = new QueryRequest();
        request.setRelaxed(true);
        request.setStart(start);
        request.setEnd(end);
        request.setStep(step);

        final Source sourceIn = new Source();
        sourceIn.setAggregation(aggregation);
        sourceIn.setTransient(false);
        sourceIn.setAttribute("ifHCInOctets");
        sourceIn.setFallbackAttribute("ifInOctets");
        sourceIn.setResourceId(resource);
        sourceIn.setLabel("ifInOctets");

        final Source sourceOut = new Source();
        sourceOut.setAggregation(aggregation);
        sourceOut.setTransient(false);
        sourceOut.setAttribute("ifHCOutOctets");
        sourceOut.setFallbackAttribute("ifOutOctets");
        sourceOut.setResourceId(resource);
        sourceOut.setLabel("ifOutOctets");

        request.setSources(Arrays.asList(sourceIn, sourceOut));

        final QueryResponse response = measurementsService.query(request);
        final double speedMbps = constantAsDouble(response, "ifInOctets.ifHighSpeed", "ifOutOctets.ifHighSpeed");
        if (Double.isNaN(speedMbps) || speedMbps <= 0) {
            return Arrays.asList(Double.NaN, Double.NaN);
        }

        final int inIndex = indexOfLabel(response, "ifInOctets");
        final int outIndex = indexOfLabel(response, "ifOutOctets");
        if (inIndex < 0 || outIndex < 0) {
            return Arrays.asList(Double.NaN, Double.NaN);
        }
        final double[] valuesIn = response.getColumns()[inIndex].getList();
        final double[] valuesOut = response.getColumns()[outIndex].getList();

        for (int i = Math.min(valuesIn.length, valuesOut.length) - 1; i >= 0; i--) {
            if (!Double.isNaN(valuesIn[i]) && !Double.isNaN(valuesOut[i])) {
                // octets/s -> Mbit/s, as a percentage of the interface speed (Mbit/s)
                return Arrays.asList(
                        (8 * valuesIn[i] / 1_000_000d) / speedMbps * 100d,
                        (8 * valuesOut[i] / 1_000_000d) / speedMbps * 100d);
            }
        }
        return Arrays.asList(Double.NaN, Double.NaN);
    }

    /** First parseable value among the named response constants, else NaN. */
    private static double constantAsDouble(final QueryResponse response, final String... keys) {
        if (response.getConstants() == null) {
            return Double.NaN;
        }
        for (final String key : keys) {
            for (final QueryResponse.QueryConstant constant : response.getConstants()) {
                if (key.equals(constant.getKey()) && constant.getValue() != null) {
                    try {
                        return Double.parseDouble(constant.getValue());
                    } catch (final NumberFormatException ignored) {
                        // fall through to the next candidate
                    }
                }
            }
        }
        return Double.NaN;
    }

    private static int indexOfLabel(final QueryResponse response, final String label) {
        final String[] labels = response.getLabels();
        for (int i = 0; labels != null && i < labels.length; i++) {
            if (label.equals(labels[i])) {
                return i;
            }
        }
        return -1;
    }

    /** Direct pass-through to the measurements query API. */
    public QueryResponse query(final QueryRequest request) throws MeasurementException {
        return measurementsService.query(request);
    }

    private QueryResponse queryInt(final String resource, final String attribute, final long start, final long end, final long step, final String aggregation, final boolean relaxed) throws MeasurementException {
        final QueryRequest request = new QueryRequest();
        request.setRelaxed(relaxed);
        request.setStart(start);
        request.setEnd(end);
        request.setStep(step);

        final Source source = new Source();
        source.setAggregation(aggregation);
        source.setTransient(false);
        source.setAttribute(attribute);
        source.setResourceId(resource);
        source.setLabel(attribute);

        request.setSources(Collections.singletonList(source));
        return measurementsService.query(request);
    }
}
