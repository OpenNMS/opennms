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
package org.hawkular.agent.prometheus.text;

import org.hawkular.agent.prometheus.types.Metric;
import org.hawkular.agent.prometheus.types.MetricVisitor;

/**
 * This represents a sample as found in the text data. This may or may not represent
 * a full metric. In the case of a counter or gauge, it will represent the full metric.
 * In the case of a summary or histogram, this only represents one quantile or one bucket
 * in a full summary or histogram metric. For those two cases, additional processing needs to
 * be made to combine multiple TextMetric objects into a single SummaryMetric or HistogramMetric.
 */
public class TextSample extends Metric {

    public static class Builder extends Metric.Builder<Builder> {
        private String value;
        private String line;

        @SuppressWarnings("unchecked")
        public TextSample build() {
            return new TextSample(this);
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setLine(String line) {
            this.line = line;
            return this;
        }
    }

    private final String value;
    private final String line;

    public TextSample(Builder builder) {
        super(builder);
        this.value = builder.value;
        this.line = builder.line;
    }

    public String getValue() {
        return value;
    }

    /**
     * This is the line of text in the text data where this sample came from.
     * This can be used for debugging purposes so you know what the sample
     * looked like before being parsed.
     *
     * @return the sample text line
     */
    public String getLine() {
        return line;
    }

    @Override
    public void visit(MetricVisitor visitor) {
        visitor.visitTextSample(this);
    }
}
