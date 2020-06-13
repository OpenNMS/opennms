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
import static org.opennms.netmgt.timeseries.integration.aggregation.NewtsConverterUtils.toTimeseriesSample;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.newts.aggregate.ResultProcessor;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.ResultDescriptor;

/** Aggregates the 'Newts' way. */
public class NewtsLikeSampleAggregator {

    private final Resource resource;
    private final Timestamp start;
    private final Timestamp end;
    private final ResultDescriptor resultDescriptor;
    private final Duration resolution;
    private final Metric metric;

    private NewtsLikeSampleAggregator(Resource resource, Timestamp start, Timestamp end, ResultDescriptor descriptor, Duration resolution, Metric metric) {
        this.resource = checkNotNull(resource, "resource argument");
        this.start = checkNotNull(start, "start argument");
        this.end = checkNotNull(end, "end argument");
        resultDescriptor = checkNotNull(descriptor, "result descriptor argument");
        this.resolution = checkNotNull(resolution, "resolution argument");
        this.metric = checkNotNull(metric, "metric argument");
    }

    public static NewtsLikeSampleAggregatorBuilder builder() {
        return new NewtsLikeSampleAggregatorBuilder();
    }

    public List<org.opennms.integration.api.v1.timeseries.Sample> process(Iterator<Results.Row<Sample>> samples) {
        checkNotNull(samples, "samples argument");

        Results<Measurement> measurements = new ResultProcessor(resource, start, end, resultDescriptor, resolution).process(samples);
        List<org.opennms.integration.api.v1.timeseries.Sample> aggregatedSamples = new ArrayList<>();

        for (Results.Row<Measurement> row : measurements) {
            aggregatedSamples.add(toTimeseriesSample(row, metric));
        }

        return aggregatedSamples;
    }

    public static class NewtsLikeSampleAggregatorBuilder {
        private Resource resource;
        private Timestamp start;
        private Timestamp end;
        private ResultDescriptor resultDescriptor;
        private Duration resolution;
        private Metric metric;

        NewtsLikeSampleAggregatorBuilder() {
        }

        public NewtsLikeSampleAggregatorBuilder resource(Resource resource) {
            this.resource = resource;
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder start(Timestamp start) {
            this.start = start;
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder end(Timestamp end) {
            this.end = end;
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder resultDescriptor(ResultDescriptor resultDescriptor) {
            this.resultDescriptor = resultDescriptor;
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder resolution(Duration resolution) {
            this.resolution = resolution;
            return this;
        }

        public NewtsLikeSampleAggregatorBuilder metric(Metric metric) {
            this.metric = metric;
            return this;
        }

        public NewtsLikeSampleAggregator build() {
            return new NewtsLikeSampleAggregator(resource, start, end, resultDescriptor, resolution, metric);
        }

        public String toString() {
            return "NewtsLikeSampleAggregator.NewtsLikeSampleAggregatorBuilder(resource=" + this.resource + ", start=" + this.start + ", end=" + this.end + ", resultDescriptor=" + this.resultDescriptor + ", resolution=" + this.resolution + ", metric=" + this.metric + ")";
        }
    }
}
