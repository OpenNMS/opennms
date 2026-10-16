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
package org.hawkular.agent.prometheus.text;

import java.util.Map;

import org.hawkular.agent.prometheus.Util;
import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.Metric;
import org.hawkular.agent.prometheus.types.MetricType;
import org.hawkular.agent.prometheus.types.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  
 * Strategy for processing OpenMetrics v1 text data.
 * <p>
 * The strategy is very similar to the Prometheus one but counters have a mandatory _total suffix and there is supported for the "created" data sample.
 * </p>
 *
 */
enum OpenMetricsFormatStrategy implements TextFormatStrategy {
    INSTANCE;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenMetricsFormatStrategy.class);

    @Override
    public TextSampleProcessor getSampleProcessor(MetricType type) {

        switch (type) {
            case COUNTER:
                return OpenMetricsFormatStrategy::handleCounterSample;
            case GAUGE:
            //treat UNKNOWN as gauge
            case UNKNOWN:
                return OpenMetricsFormatStrategy::handleGaugeSample;            
            case SUMMARY:
                return OpenMetricsFormatStrategy::handleSummarySample;
            case HISTOGRAM:
                return OpenMetricsFormatStrategy::handleHistogramSample;
            default:
                throw new IllegalArgumentException("Unknown metric type: " + type);
        }
    }
    
    @Override
    public SampleNameValidator getNameValidator(MetricType type, String familyName) {

        switch (type) {
            case COUNTER:
                return SampleNameValidator.matchingNameWithSuffixes(familyName, "_total", "_created");  
            case GAUGE:
            case UNKNOWN:
                return SampleNameValidator.matchingNameWithSuffixes(familyName, "", "_created");            
            case SUMMARY:
                return SampleNameValidator.matchingNameWithSuffixes(familyName, "", "_count", "_sum", "_created");
            case HISTOGRAM:
                return SampleNameValidator.matchingNameWithSuffixes(familyName, "_count", "_sum", "_bucket", "_created");
            default:
                throw new IllegalArgumentException("Unknown metric type: " + type);
        }
    }

    
    private static void handleCounterSample(Map<Map<String, String>, Metric.Builder<?>> builders, String familyName, TextSample textSample) {
        Counter.Builder cBuilder = (Counter.Builder) builders.get(textSample.getLabels());
        if (cBuilder == null) {
            cBuilder = Counter.builder()
                    .setName(familyName)
                    .addLabels(textSample.getLabels());
            builders.put(textSample.getLabels(), cBuilder);
        }
        if (textSample.getName().endsWith("_created")) {
            cBuilder.setCreated(Util.tryConvertTimestamp(textSample.getValue()));
        }
        else {
            cBuilder
                .setValue(Util.convertStringToDouble(textSample.getValue()))
                .setTimestamp(textSample.getTimestamp());
        }
    }
    
    private static void handleGaugeSample(Map<Map<String, String>, Metric.Builder<?>> builders, String familyName, TextSample textSample) {
        Gauge.Builder gBuilder = (Gauge.Builder) builders.get(textSample.getLabels());
        if (gBuilder == null) {
            gBuilder = Gauge.builder()
                    .setName(familyName)
                    .addLabels(textSample.getLabels());
            builders.put(textSample.getLabels(), gBuilder);
        }
        if (textSample.getName().endsWith("_created")) {
            gBuilder.setCreated(Util.tryConvertTimestamp(textSample.getValue()));
        }
        else {
            gBuilder.setValue(Util.convertStringToDouble(textSample.getValue()))
                .setTimestamp(textSample.getTimestamp());
        }
    }
    
    private static void handleSummarySample(Map<Map<String, String>, Metric.Builder<?>> builders, String familyName, TextSample textSample) {
     // First we need to remove any existing quantile label since it isn't a "real" label.
        // This is to ensure our lookup uses all but only "real" labels.
        String quantileValue = textSample.getLabels().remove("quantile"); // may be null

        Summary.Builder sBuilder = (Summary.Builder) builders.get(textSample.getLabels());
        if (sBuilder == null) {
            sBuilder = new Summary.Builder();
            sBuilder.setName(familyName);
            sBuilder.addLabels(textSample.getLabels());
            builders.put(textSample.getLabels(), sBuilder);
        }
        if (textSample.getName().endsWith("_count")) {
            sBuilder.setSampleCount((long)Util.convertStringToDouble(textSample.getValue()));
        } else if (textSample.getName().endsWith("_sum")) {
            sBuilder.setSampleSum(Util.convertStringToDouble(textSample.getValue()));
        }
        else if (textSample.getName().endsWith("_created")) {
            sBuilder.setCreated(Util.tryConvertTimestamp(textSample.getValue()));
        }
        else {
            // This must be a quantile sample
            if (quantileValue == null) {
                LOGGER.debug("Summary quantile sample is missing the 'quantile' label: {}",
                        textSample.getLine());
            }
            sBuilder.addQuantile(Util.convertStringToDouble(quantileValue),
                    Util.convertStringToDouble(textSample.getValue()));
        }
    }
    
    private static void handleHistogramSample(Map<Map<String, String>, Metric.Builder<?>> builders, String familyName, TextSample textSample) {
        // Get the builder that we are using to build up the current metric. Remember we need to
        // get the builder for this specific metric identified with a unique set of labels.

        // First we need to remove any existing le label since it isn't a "real" label.
        // This is to ensure our lookup uses all but only "real" labels.
        String bucket = textSample.getLabels().remove("le"); // may be null

        Histogram.Builder hBuilder = (Histogram.Builder) builders.get(textSample.getLabels());
        if (hBuilder == null) {
            hBuilder = new Histogram.Builder();
            hBuilder.setName(familyName);
            hBuilder.addLabels(textSample.getLabels());
            builders.put(textSample.getLabels(), hBuilder);
        }
        if (textSample.getName().endsWith("_count")) {
            hBuilder.setSampleCount((long)Util.convertStringToDouble(textSample.getValue()));
        } else if (textSample.getName().endsWith("_sum")) {
            hBuilder.setSampleSum(Util.convertStringToDouble(textSample.getValue()));
        }
        else if (textSample.getName().endsWith("_created")) {
            hBuilder.setCreated(Util.tryConvertTimestamp(textSample.getValue()));
        }    
        else {
            // This must be a bucket sample
            if (bucket == null) {
                throw new IllegalArgumentException("Histogram bucket sample is missing the 'le' label");
            }
            hBuilder.addBucket(Util.convertStringToDouble(bucket),
                    (long)Util.convertStringToDouble(textSample.getValue()));
        }
    }

}
