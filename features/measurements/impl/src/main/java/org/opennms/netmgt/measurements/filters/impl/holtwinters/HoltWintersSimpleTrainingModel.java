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

import java.util.Arrays;

import static java.util.stream.DoubleStream.concat;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

/**
 * Implements an online model to train the HoltWintersComponents values based on the first two cycles of observations.
 * <p>
 * See the "simple" value for the "initial" argument to <a href="https://www.rdocumentation.org/packages/forecast/versions/8.5/topics/ses#l_arguments">ses</a> in the R "forecast" package.
 * <br>
 * R source code: <a href="https://github.com/robjhyndman/forecast/blob/master/R/HoltWintersNew.R#L61-L67">https://github.com/robjhyndman/forecast/blob/master/R/HoltWintersNew.R#L61-L67</a>
 */
public class HoltWintersSimpleTrainingModel {
    private int n = 0;
    private final double[] firstCycle;
    private final double[] secondCycle;

    public HoltWintersSimpleTrainingModel(HoltWintersPointForecasterParams params) {
        this.firstCycle = new double[params.getFrequency()];
        this.secondCycle = new double[params.getFrequency()];
    }

    /**
     * SIMPLE training method requires 2 complete cycles of observations to finish training the initial level, base and seasonal components (l, b, s).
     * l and s can be calculated after the first cycle, b can only be determined after the 2nd cycle. This object stores those 2 cycles as the
     * firstCycle and secondCycle array fields.
     * <p>
     * E.g. if frequency=4, then on the 8th observation, the model will complete its training of l, b, s.
     * Furthermore, on the 8th observation this method will then fit the model to the 8 initial observations, by running through those 8 stored data
     * points (in firstCycle and secondCycle) one at a time, to retrospectively apply the smoothing parameters (alpha, beta, gamma) to l, b, s.
     * <p>
     * After the 8th observation, the components.getForecast() returns the correct forecast for the 9th observation which is the first non-training
     * observation that can be used to detect anomalies.
     *
     * @param y          data series
     * @param params     model parameters
     * @param components model components
     */
    public void observeAndTrain(double y, HoltWintersPointForecasterParams params, HoltWintersOnlineComponents components) {
        checkNulls(params, components);
        checkTrainingMethod(params);
        checkStillInInitialTraining(params);
        int frequency = params.getFrequency();

        // Capture data points
        if (isBetween(n, 0, frequency - 1)) {
            firstCycle[n] = y;
        } else {
            secondCycle[n - frequency] = y;
        }
        // Train
        if (n == params.calculateInitTrainingPeriod() - 1) {
            setLevel(components);
            setSeasonals(y, params, components);
            setBase(params, components);
            updateComponentsAndForecast(params, components);
        }
        n++;
    }

    public boolean isTrainingComplete(HoltWintersPointForecasterParams params) {
        return n >= (params.calculateInitTrainingPeriod());
    }

    /**
     * Update the level, base and seasonal components by running the main algorithm over each of the observations to this point.
     */
    private void updateComponentsAndForecast(HoltWintersPointForecasterParams params, HoltWintersOnlineComponents components) {
        HoltWintersOnlineAlgorithm algorithm = new HoltWintersOnlineAlgorithm();
        concat(Arrays.stream(firstCycle), Arrays.stream(secondCycle)).forEach(y -> algorithm.observeValueAndUpdateForecast(y, params, components));
    }

    private void setLevel(HoltWintersOnlineComponents components) {
        components.setLevel(mean(firstCycle));
    }

    private void setBase(HoltWintersPointForecasterParams params, HoltWintersOnlineComponents components) {
        double base = (mean(secondCycle) - components.getLevel()) / params.getFrequency();
        components.setBase(base);
    }

    private void setSeasonals(double y, HoltWintersPointForecasterParams params, HoltWintersOnlineComponents components) {
        for (int i = 0; i < params.getFrequency(); i++) {
            double s = params.isMultiplicative()
                    ? firstCycle[i] / components.getLevel()
                    : firstCycle[i] - components.getLevel();
            components.setSeasonal(i, s, y);
        }
    }

    private void checkNulls(HoltWintersPointForecasterParams params, HoltWintersOnlineComponents components) {
        notNull(params, "params can't be null");
        notNull(components, "components can't be null");
    }

    private void checkTrainingMethod(HoltWintersPointForecasterParams params) {
        isTrue(HoltWintersTrainingMethod.SIMPLE.equals(params.getInitTrainingMethod()),
                String.format("Expected training method to be %s but was %s", HoltWintersTrainingMethod.SIMPLE, params.getInitTrainingMethod()));
    }

    private void checkStillInInitialTraining(HoltWintersPointForecasterParams params) {
        isTrue(!isTrainingComplete(params),
                String.format("Training invoked %d times which is greater than the training window of frequency * 2 (%d * 2 = %d) observations.",
                        n + 1, params.getFrequency(), params.calculateInitTrainingPeriod()));
    }

    // TODO HW: Potential reuse opportunity
    private boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x <= upper;
    }

    // TODO HW: Potential reuse opportunity
    private double mean(double[] values) {
        return Arrays.stream(values).average().getAsDouble();
    }

}
