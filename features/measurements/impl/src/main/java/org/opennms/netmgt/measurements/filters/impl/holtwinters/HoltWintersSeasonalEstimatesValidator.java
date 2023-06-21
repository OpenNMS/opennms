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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Comparator.comparing;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * The heuristics for validating the estimates for the initial seasonal component are taken from https://otexts.org/fpp2/holt-winters.html:
 * <ul>
 * <li>
 * <b>MULTIPLICATIVE</b>: "With the multiplicative method, the seasonal component is expressed in relative terms (percentages).
 * Within each year, the seasonal component will sum up to approximately m (frequency)."
 * </li>
 * <li>
 * <b>ADDITIVE</b>: "With the additive method, the seasonal component is expressed in absolute terms in the scale of the observed series.
 * Within each year, the seasonal component will add up to approximately zero."
 * </li>
 * </ul>
 */
public class HoltWintersSeasonalEstimatesValidator {

    /**
     * Validate the given estimates.
     *
     * @param initSeasonalEstimates Array containing the provided estimates for initial seasonal component values.  Must be same length as frequency
     * @param frequency             The amount of observations per cycle.
     * @param seasonalityType       Are the estimates MULTIPLICATIVE or ADDITIVE?
     */
    public void validate(double[] initSeasonalEstimates, int frequency, HoltWintersSeasonalityType seasonalityType) {
        checkNotNull(initSeasonalEstimates);
        if (initSeasonalEstimates.length <= 0) return;
        checkLength(initSeasonalEstimates, frequency);
        checkSumOfEstimates(initSeasonalEstimates, frequency, seasonalityType);
    }

    private void checkNotNull(double[] initSeasonalEstimates) {
        notNull(initSeasonalEstimates, "Required: initSeasonalEstimates must be either an empty array or an array of n=frequency initial " +
                "estimates for seasonal components.");
    }

    private void checkLength(double[] initSeasonalEstimates, int frequency) {
        isTrue(initSeasonalEstimates.length == frequency,
                format("Invalid: initSeasonalEstimates size (%d) must equal frequency (%d)", initSeasonalEstimates.length, frequency));
    }

    private void checkSumOfEstimates(double[] initSeasonalEstimates, int frequency, HoltWintersSeasonalityType seasonalityType) {
        BigDecimal seasonalSum = sum(initSeasonalEstimates, frequency);
        BigDecimal maxAbs = maxOfAbsolutes(distancesFromIdentity(initSeasonalEstimates, seasonalityType, frequency), frequency);
        BigDecimal tolerance = onePercent(maxAbs);
        if (seasonalityType.equals(HoltWintersSeasonalityType.MULTIPLICATIVE)) {
            isBetween(seasonalSum.doubleValue(), frequency - tolerance.doubleValue(), frequency + tolerance.doubleValue(),
                    additiveErrorMessage(frequency, seasonalSum, tolerance));
        } else {
            isBetween(seasonalSum.doubleValue(), -(tolerance.doubleValue()), 1 + tolerance.doubleValue(),
                    multiplicativeErrorMessage(seasonalSum, tolerance));
        }
    }

    private static BigDecimal sum(double[] values, int frequency) {
        return Arrays.stream(values, 0, frequency).mapToObj(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static double[] distancesFromIdentity(double[] values, HoltWintersSeasonalityType seasonalityType, int frequency) {
        return seasonalityType.equals(HoltWintersSeasonalityType.MULTIPLICATIVE) ? eachMinusOne(values, frequency) : values;
    }

    private static double[] eachMinusOne(double[] values, int frequency) {
        // Use BigDecimal to save loss of accuracy during subtraction
        Stream<BigDecimal> stream = Arrays.stream(values, 0, frequency).mapToObj(BigDecimal::valueOf);
        return stream.map(y -> y.subtract(BigDecimal.ONE)).mapToDouble(BigDecimal::doubleValue).toArray();
    }

    private static BigDecimal maxOfAbsolutes(double[] values, int frequency) {
        Stream<BigDecimal> stream = Arrays.stream(values, 0, frequency).mapToObj(BigDecimal::valueOf);
        return stream.max(comparing(BigDecimal::abs)).get().abs();
    }

    private static BigDecimal onePercent(BigDecimal val) {
        return val.divide(new BigDecimal(100));
    }

    private static String fmt(BigDecimal val) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(val.scale());
        return df.format(val);
    }

    private String multiplicativeErrorMessage(BigDecimal seasonalSum, BigDecimal tolerance) {
        return format("Invalid: Sum of initSeasonalEstimates (%s) was outside accepted tolerance. " +
                "Sum should be 0 with a tolerance within 1%% of largest seasonal estimate distance from 0 (±%s), " +
                "for ADDITIVE seasonality type.", fmt(seasonalSum), fmt(tolerance));
    }

    private String additiveErrorMessage(int frequency, BigDecimal seasonalSum, BigDecimal tolerance) {
        return format("Invalid: Sum of initSeasonalEstimates (%s) was outside accepted tolerance. " +
                "Sum should equal 'frequency' with a tolerance within 1%% of largest seasonal estimate distance from 1 (%d ± %s), " +
                "for MULTIPLICATIVE seasonality type.", fmt(seasonalSum), frequency, fmt(tolerance));
    }

    public static void isBetween(double value, double lowerBd, double upperBd, String message) {
        isTrue(lowerBd <= value && value <= upperBd, message);
    }
}
