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
package org.hawkular.agent.prometheus.walkers;

import java.util.Map;

import org.hawkular.agent.prometheus.types.Counter;
import org.hawkular.agent.prometheus.types.Gauge;
import org.hawkular.agent.prometheus.types.Histogram;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.types.Summary;

/**
 * Implementors iterate a collection of metric families and their metrics.
 */
public interface PrometheusMetricsWalker {

    /**
     * Called when a walk has been started.
     */
    void walkStart();

    /**
     * Called when a walk has traversed all the metrics.
     * @param familiesProcessed total number of families processed
     * @param metricsProcessed total number of metrics across all families processed
     */
    void walkFinish(int familiesProcessed, int metricsProcessed);

    /**
     * Called when a new metric family is about to be traversed.
     *
     * @param family information about the family being traversed such as the name, help description, etc.
     * @param index index of the family being processed, where 0 is the first one.
     */
    void walkMetricFamily(MetricFamily family, int index);

    /**
     * Called when a new counter metric is found.
     *
     * @param family information about the family being traversed such as the name, help description, etc.
     * @param counter the metric being processed
     * @param index index of the metric being processed, where 0 is the first one.
     */
    void walkCounterMetric(MetricFamily family, Counter counter, int index);

    /**
     * Called when a new gauge metric is found.
     *
     * @param family information about the family being traversed such as the name, help description, etc.
     * @param gauge the metric being processed
     * @param index index of the metric being processed, where 0 is the first one.
     */
    void walkGaugeMetric(MetricFamily family, Gauge gauge, int index);

    /**
     * Called when a new summary metric is found.
     *
     * @param family information about the family being traversed such as the name, help description, etc.
     * @param summary the metric being processed
     * @param index index of the metric being processed, where 0 is the first one.
     */
    void walkSummaryMetric(MetricFamily family, Summary summary, int index);

    /**
     * Called when a new histogram metric is found.
     *
     * @param family information about the family being traversed such as the name, help description, etc.
     * @param histogram the metric being processed
     * @param index index of the metric being processed, where 0 is the first one.
     */
    void walkHistogramMetric(MetricFamily family, Histogram histogram, int index);

    /**
     * Convienence method that takes the given label list and returns a string in the form of
     * "labelName1=labelValue1,labelName2=labelValue2,..."
     *
     * @param labels the label list
     * @param prefix if not null, these characters will prefix the label list
     * @param suffix if not null, these characters will suffix the label list
     * @return the string form of the labels, optionally prefixed and suffixed
     */
    default String buildLabelListString(Map<String, String> labels, String prefix, String suffix) {
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = "";
        }
        if (labels == null) {
            return String.format("%s%s", prefix, suffix);
        }

        StringBuilder str = new StringBuilder("");
        for (Map.Entry<String, String> pair : labels.entrySet()) {
            if (str.length() > 0) {
                str.append(",");
            }
            str.append(pair.getKey()).append("=").append(pair.getValue());
        }
        return String.format("%s%s%s", prefix, str, suffix);
    }

}
