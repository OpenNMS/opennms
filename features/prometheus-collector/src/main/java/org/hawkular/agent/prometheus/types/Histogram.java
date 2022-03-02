/*
 * Copyright 2015-2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.prometheus.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawkular.agent.prometheus.Util;

public class Histogram extends Metric {

    public static class Builder extends Metric.Builder<Builder> {
        private long sampleCount = 0;
        private double sampleSum = Double.NaN;
        private List<Bucket> buckets;

        @SuppressWarnings("unchecked")
        public Histogram build() {
            return new Histogram(this);
        }

        public Builder setSampleCount(long sampleCount) {
            this.sampleCount = sampleCount;
            return this;
        }

        public Builder setSampleSum(double sampleSum) {
            this.sampleSum = sampleSum;
            return this;
        }

        public Builder addBucket(double upperBound, long cumulativeCount) {
            if (buckets == null) {
                buckets = new ArrayList<>();
            }
            buckets.add(new Bucket(upperBound, cumulativeCount));
            return this;
        }

        public Builder addBuckets(List<Bucket> buckets) {
            if (this.buckets == null) {
                this.buckets = new ArrayList<>();
            }
            this.buckets.addAll(buckets);
            return this;
        }
    }

    public static class Bucket {
        private final double upperBound;
        private final long cumulativeCount;

        public Bucket(double upperBound, long cumulativeCount) {
            this.upperBound = upperBound;
            this.cumulativeCount = cumulativeCount;
        }

        public double getUpperBound() {
            return upperBound;
        }

        public long getCumulativeCount() {
            return cumulativeCount;
        }

        @Override
        public String toString() {
            return String.format("%s:%d", Util.convertDoubleToString(upperBound), cumulativeCount);
        }
    }

    private final long sampleCount;
    private final double sampleSum;
    private final List<Bucket> buckets;

    private Histogram(Builder builder) {
        super(builder);
        getLabels().remove("le");
        this.sampleCount = builder.sampleCount;
        this.sampleSum = builder.sampleSum;
        this.buckets = builder.buckets;
    }

    public long getSampleCount() {
        return sampleCount;
    }

    public double getSampleSum() {
        return sampleSum;
    }

    public List<Bucket> getBuckets() {
        if (buckets == null) {
            return Collections.emptyList();
        }
        return buckets;
    }

    @Override
    public void visit(MetricVisitor visitor) {
        visitor.visitHistogram(this);
    }
}
