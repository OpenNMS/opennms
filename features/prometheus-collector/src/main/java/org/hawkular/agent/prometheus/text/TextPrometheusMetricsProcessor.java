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

import java.io.InputStream;

import org.hawkular.agent.prometheus.PrometheusMetricsProcessor;
import org.hawkular.agent.prometheus.types.MetricFamily;
import org.hawkular.agent.prometheus.walkers.PrometheusMetricsWalker;

/**
 * This will iterate over a list of Prometheus metrics that are given as text data.
 */
public class TextPrometheusMetricsProcessor extends PrometheusMetricsProcessor<MetricFamily> {
    public TextPrometheusMetricsProcessor(InputStream inputStream, PrometheusMetricsWalker theWalker) {
        super(inputStream, theWalker);
    }

    @Override
    public TextPrometheusMetricDataParser createPrometheusMetricDataParser() {
        return new TextPrometheusMetricDataParser(getInputStream());
    }

    @Override
    protected MetricFamily convert(MetricFamily metricFamily) {
        return metricFamily; // no conversion necessary - our text parser already uses the common api
    }

}
