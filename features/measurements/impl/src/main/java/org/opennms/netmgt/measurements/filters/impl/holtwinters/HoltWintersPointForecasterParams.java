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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.lang.String.format;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public final class HoltWintersPointForecasterParams {
    private static final Logger LOG = LoggerFactory.getLogger(HoltWintersPointForecasterParams.class);

    /**
     * SeasonalityType parameter used to determine which Seasonality method (Multiplicative or Additive) to use.
     */
    private HoltWintersSeasonalityType seasonalityType = HoltWintersSeasonalityType.MULTIPLICATIVE;

    /**
     * Frequency parameter representing periodicity of the data.
     * E.g. 24 = data is provided in hourly samples and seasons are represented as single days.
     * E.g.  7 = data is provided in daily samples and seasons are represented as single weeks.
     * E.g. 12 = data is provided in monthly samples and seasons are represented as single years.
     * E.g.  4 = data is provided in quarterly samples and seasons are represented as single years.
     */
    private int frequency = 0;

    /**
     * Alpha smoothing parameter used for "level" calculation.
     * A double between 0-1 inclusive.
     */
    private double alpha = 0.15;

    /**
     * Beta smoothing parameter used for "base" or "trend" calculation.
     * A double between 0-1 inclusive.
     */
    private double beta = 0.15;

    /**
     * Gamma smoothing parameter used for "seasonality" calculation.
     * A double between 0-1 inclusive.
     */
    private double gamma = 0.15;

    /**
     * Minimum number of data points required before the anomaly detector is ready for use.
     * A value of 0 means the detector could begin emitting anomalies immediately on first observation.
     * A minimum equivalent to "frequency" is suggested, with 2 * frequency being ideal for a lot of scenarios.
     * If no initial Base/Level/Seasonal estimate parameters are supplied, then warmUpPeriod = (2 * frequency) is an ideal minimum
     * - it allows the detector to "warm up" the seasonal components with at least 2 observations each, providing the ability
     * to calculate a standard deviation.
     */
    private int warmUpPeriod = 0;

    /**
     * Initial estimate for Level component.
     * Only applies if initTrainingMethod = HoltWintersTrainingMethod.NONE.
     * If not set, then 1.0 will be used for MULTIPLICATIVE seasonality and 0.0 for ADDITIVE seasonality.
     */
    private double initLevelEstimate = Double.NaN;

    /**
     * Initial estimate for Base component.
     * Only applies if initTrainingMethod = HoltWintersTrainingMethod.NONE.
     * If not set, then 1.0 will be used for MULTIPLICATIVE seasonality and 0.0 for ADDITIVE seasonality.
     */
    private double initBaseEstimate = Double.NaN;

    /**
     * Initial estimates for Seasonal components.
     * Only applies if initTrainingMethod = HoltWintersTrainingMethod.NONE.
     * Either 0 or n=frequency values must be provided.
     */
    private double[] initSeasonalEstimates = {};

    /**
     * Initial training method to use. See {@link HoltWintersTrainingMethod} for details.
     */
    private HoltWintersTrainingMethod initTrainingMethod = HoltWintersTrainingMethod.NONE;

    private final HoltWintersSeasonalEstimatesValidator seasonalEstimatesValidator = new HoltWintersSeasonalEstimatesValidator();

    public boolean isMultiplicative() {
        return seasonalityType.equals(HoltWintersSeasonalityType.MULTIPLICATIVE);
    }

    /**
     * Calculates the initial training period (if applicable) based on initTrainingMethod and frequency.
     * Used to determine whether to perform training or forecasting on an observation.
     *
     * @return Length of initial training period in number of observations.
     */
    public int calculateInitTrainingPeriod() {
        return (initTrainingMethod == HoltWintersTrainingMethod.SIMPLE) ? (frequency * 2) : 0;
    }

    public void validate() {
        notNull(seasonalityType, "Required: seasonalityType one of " + Arrays.toString(HoltWintersSeasonalityType.values()));
        notNull(initTrainingMethod, "Required: initTrainingMethod one of " + Arrays.toString(HoltWintersTrainingMethod.values()));
        isTrue(0 < frequency, "Required: frequency value greater than 0");
        isTrue(0.0 <= alpha && alpha <= 1.0, "Required: alpha in the range [0, 1]");
        isTrue(0.0 <= beta && beta <= 1.0, "Required: beta in the range [0, 1]");
        isTrue(0.0 <= gamma && gamma <= 1.0, "Required: gamma in the range [0, 1]");
        validateInitTrainingMethod();
        validateInitSeasonalEstimates();
    }

    private void validateInitTrainingMethod() {
        if (initTrainingMethod == HoltWintersTrainingMethod.SIMPLE) {
            int minWarmUpPeriod = calculateInitTrainingPeriod();
            if (warmUpPeriod < minWarmUpPeriod) {
                LOG.warn(format("warmUpPeriod (%d) should be greater than or equal to (frequency * 2) (%d), " +
                                "as the detector will not emit anomalies during training. Setting warmUpPeriod to %d.",
                        warmUpPeriod, minWarmUpPeriod, minWarmUpPeriod));
                warmUpPeriod = minWarmUpPeriod;
            }
        }
    }

    private void validateInitSeasonalEstimates() {
        seasonalEstimatesValidator.validate(initSeasonalEstimates, frequency, seasonalityType);
    }

    public HoltWintersSeasonalityType getSeasonalityType() {
        return seasonalityType;
    }

    public HoltWintersPointForecasterParams setSeasonalityType(HoltWintersSeasonalityType seasonalityType) {
        this.seasonalityType = seasonalityType;
        return this;
    }

    public int getFrequency() {
        return frequency;
    }

    public HoltWintersPointForecasterParams setFrequency(int frequency) {
        this.frequency = frequency;
        return this;
    }

    public double getAlpha() {
        return alpha;
    }

    public HoltWintersPointForecasterParams setAlpha(double alpha) {
        this.alpha = alpha;
        return this;
    }

    public double getBeta() {
        return beta;
    }

    public HoltWintersPointForecasterParams setBeta(double beta) {
        this.beta = beta;
        return this;
    }

    public double getGamma() {
        return gamma;
    }

    public HoltWintersPointForecasterParams setGamma(double gamma) {
        this.gamma = gamma;
        return this;
    }

    public int getWarmUpPeriod() {
        return warmUpPeriod;
    }

    public HoltWintersPointForecasterParams setWarmUpPeriod(int warmUpPeriod) {
        this.warmUpPeriod = warmUpPeriod;
        return this;
    }

    public double getInitLevelEstimate() {
        return initLevelEstimate;
    }

    public void setInitLevelEstimate(double initLevelEstimate) {
        this.initLevelEstimate = initLevelEstimate;
    }

    public double getInitBaseEstimate() {
        return initBaseEstimate;
    }

    public void setInitBaseEstimate(double initBaseEstimate) {
        this.initBaseEstimate = initBaseEstimate;
    }

    public double[] getInitSeasonalEstimates() {
        return initSeasonalEstimates;
    }

    public void setInitSeasonalEstimates(double[] initSeasonalEstimates) {
        this.initSeasonalEstimates = initSeasonalEstimates;
    }

    public HoltWintersTrainingMethod getInitTrainingMethod() {
        return initTrainingMethod;
    }

    public HoltWintersPointForecasterParams setInitTrainingMethod(HoltWintersTrainingMethod initTrainingMethod) {
        this.initTrainingMethod = initTrainingMethod;
        return this;
    }
}
