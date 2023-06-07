/*
 * Copyright 2018-2019 Expedia Group, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.netmgt.measurements.filters.impl.holtwinters;

/**
 * The training method to use for determining the initial level, base and seasonal components for Holt-Winters Anomaly Detector.
 */
public enum HoltWintersTrainingMethod {
    /**
     * Use either the user-provided init*Estimate params, or the default values if none provided for initial l, b, and s components
     */
    NONE("none"),
    /**
     * Use the first 2 seasons to calculate the implied level, base and seasonal components for the observation immediately preceding the first
     * observation.
     * Implements Hyndman's "simple" method for selecting initial state values.
     * Ensure that warmUpPeriod gte (frequency * 2) to ensure no anomalies are emitted during training period.
     *
     * @see <a href="https://github.com/robjhyndman/forecast/blob/master/R/HoltWintersNew.R#L61-L67">HoltWintersNew.R</a>
     */
    SIMPLE("simple");

    private final String name;

    HoltWintersTrainingMethod(String name) {
        this.name = name;
    }

}
