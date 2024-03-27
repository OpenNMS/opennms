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
package org.opennms.netmgt.timeseries.sampleread.aggregation;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.opennms.netmgt.timeseries.sampleread.aggregation.NewtsConverterUtils.toTimeSeriesDataPoint;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

import org.opennms.integration.api.v1.timeseries.DataPoint;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.TimeSeriesData;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableTimeSeriesData;
import org.opennms.netmgt.measurements.model.Source;
import org.opennms.netmgt.timeseries.sampleread.LateAggregationParams;
import org.opennms.newts.aggregate.ResultProcessor;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.AggregationFunction;
import org.opennms.newts.api.query.ResultDescriptor;
import org.opennms.newts.api.query.StandardAggregationFunctions;

/** Aggregates the 'Newts' way. */
public class NewtsLikeSampleAggregator {

    private final Resource resource;
    private final Timestamp start;
    private final Timestamp end;
    private final ResultDescriptor resultDescriptor;
    private final List<Source> currentSources;
    private final Duration resolution;
    private final Metric metric;

    private NewtsLikeSampleAggregator(Resource resource, Timestamp start, Timestamp end, List<Source> currentSources,
                                      final LateAggregationParams lag, Metric metric) {
        this.resource = checkNotNull(resource, "resource argument");
        this.start = checkNotNull(start, "start argument");
        this.end = checkNotNull(end, "end argument");
        this.currentSources = checkNotNull(currentSources, "currentSources argument");
        checkNotNull(lag, "lag argument");
        this.resultDescriptor = createResultDescriptor(this.currentSources, lag);
        this.resolution = Duration.millis(lag.getStep());
        this.metric = checkNotNull(metric, "metric argument");
    }

    private ResultDescriptor createResultDescriptor(final List<Source> listOfSources, final LateAggregationParams lag) {
        ResultDescriptor resultDescriptor = new ResultDescriptor(lag.getInterval());
        for (Source source : listOfSources) {
            // Use the datasource as the metric name if set, otherwise use the name of the attribute
            final String metricName = source.getDataSource() != null ? source.getDataSource() : source.getAttribute();
            final String name = source.getLabel();
            final AggregationFunction fn = toAggregationFunction(source.getAggregation());

            resultDescriptor.datasource(name, metricName, lag.getHeartbeat(), fn);
            resultDescriptor.export(name);
        }
        return resultDescriptor;
    }

    // Newts
    private static AggregationFunction toAggregationFunction(String fn) {
        if ("average".equalsIgnoreCase(fn) || "avg".equalsIgnoreCase(fn)) {
            return org.opennms.newts.api.query.StandardAggregationFunctions.AVERAGE;
        } else if ("max".equalsIgnoreCase(fn)) {
            return org.opennms.newts.api.query.StandardAggregationFunctions.MAX;
        } else if ("min".equalsIgnoreCase(fn)) {
            return StandardAggregationFunctions.MIN;
        } else {
            throw new IllegalArgumentException("Unsupported aggregation function: " + fn);
        }
    }

    public static NewtsLikeSampleAggregatorBuilder builder() {
        return new NewtsLikeSampleAggregatorBuilder();
    }

    public TimeSeriesData process(Iterator<Results.Row<Sample>> samples) {
        checkNotNull(samples, "samples argument");

        Results<Measurement> measurements = new ResultProcessor(resource, start, end, resultDescriptor, resolution).process(samples);
        List<DataPoint> aggregatedDataPoints = new ArrayList<>();

        for (Results.Row<Measurement> row : measurements) {
            aggregatedDataPoints.add(toTimeSeriesDataPoint(row));
        }

        return ImmutableTimeSeriesData.builder()
                .metric(metric)
                .dataPoints(aggregatedDataPoints).build();
    }

    public static class NewtsLikeSampleAggregatorBuilder {
        private Resource resource;
        private Timestamp start;
        private Timestamp end;
        private List<Source> currentSources;
        private LateAggregationParams lateAggregationParams;
        private Metric metric;

        NewtsLikeSampleAggregatorBuilder() {
        }

        public NewtsLikeSampleAggregatorBuilder resource(String resourceId) {
            this.resource = new Resource(resourceId);
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder start(Instant start) {
            this.start = Timestamp.fromEpochMillis(start.toEpochMilli());
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder end(Instant end) {
            this.end = Timestamp.fromEpochMillis(end.toEpochMilli());
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder currentSources(List<Source> currentSources) {
            this.currentSources = currentSources;
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder lag(final LateAggregationParams lateAggregationParams) {
            this.lateAggregationParams = lateAggregationParams;
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder metric(Metric metric) {
            this.metric = metric;
            return this;
        }

        public NewtsLikeSampleAggregator build() {
            return new NewtsLikeSampleAggregator(resource, start, end, currentSources, lateAggregationParams, metric);
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", NewtsLikeSampleAggregatorBuilder.class.getSimpleName() + "[", "]")
                    .add("resource=" + resource)
                    .add("start=" + start)
                    .add("end=" + end)
                    .add("currentSources=" + currentSources)
                    .add("lateAggregationParams=" + lateAggregationParams)
                    .add("metric=" + metric)
                    .toString();
        }
    }
}
