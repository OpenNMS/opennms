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

package org.hawkular.agent.prometheus;

import java.io.IOException;
import java.io.InputStream;

/**
 * An object that can parse Prometheus found in a specific data format in an input stream.
 * The type <T> is the metric family object for the specific data format.
 * It is the job of the associated {@link PrometheusMetricsProcessor} to process
 * the parsed data.
 *
 * @param <T> the metric family object type that the parser produces
 */
public abstract class PrometheusMetricDataParser<T> {
    private InputStream inputStream;

    /**
     * Provides the input stream where the parser will look for metric data.
     * NOTE: this object will not own this stream - it should never attempt to close it.
     *
     * @param inputStream the stream where the metric data can be found
     */
    public PrometheusMetricDataParser(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Stream must not be null");
        }
        this.inputStream = inputStream;
    }

    protected InputStream getInputStream() {
        return this.inputStream;
    }

    /**
     * Reads a single metric family from the Prometheus metric data stream and returns it.
     * Returns null when no more data is in the stream.
     *
     * This method is designed to be called several times, each time it returns the next metric family
     * found in the input stream.
     *
     * @return the metric family data found in the stream, or null
     * @throws IOException if failed to read the data from the stream
     */
    public abstract T parse() throws IOException;
}
