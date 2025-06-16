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
 * Encapsulates the algorithm for forecasting one-step ahead estimate using Holt-Winters (Triple-Exponential Smoothing) method.
 *
 * @see <a href="https://otexts.org/fpp2/holt-winters.html">Holt-Winters' Seasonal Method</a>
 */
public class HoltWintersOnlineAlgorithm {

    /**
     * Get a single forecast value for the next tick given the previously observed values.  Updates values for the model's components based on this observation.
     *
     * @param y          Observed value for time "t"
     * @param params     Contains the parameters for model
     * @param components Contains the online values used to calculate level, base and seasonality components (based on most recent history from observation at t-1).
     */
    public void observeValueAndUpdateForecast(double y, HoltWintersPointForecasterParams params, HoltWintersOnlineComponents components) {
        // Retrieve the model's parameters as set by user (or defaults)
        double alpha = params.getAlpha();
        double beta = params.getBeta();
        double gamma = params.getGamma();
        boolean multiplicative = params.isMultiplicative();
        // Retrieve model's level and base component values from previous observation (t - 1)
        double prevLevel = components.getLevel();
        double prevBase = components.getBase();
        // Retrieve model's seasonal component that relates to the current season we're observing for time t (i.e. "frequency" seasons ago)
        int seasonalIdx = components.getCurrentSeasonalIndex();
        double season = components.getSeasonal(seasonalIdx);

        double newLevel, newBase, newSeason;

        // Calculate new components given y_t (current observed value) and generate new forecast for y_t+1
        if (multiplicative) {
            newLevel = alpha * (y / season) + (1 - alpha) * (prevLevel + prevBase);
            newBase = beta * (newLevel - prevLevel) + (1 - beta) * prevBase;
            newSeason = gamma * (y / (prevLevel + prevBase)) + (1 - gamma) * season;
        } else {
            newLevel = alpha * (y - season) + (1 - alpha) * (prevLevel + prevBase);
            newBase = beta * (newLevel - prevLevel) + (1 - beta) * prevBase;
            newSeason = gamma * (y - prevLevel - prevBase) + (1 - gamma) * season;
        }

        // Update the model's components
        updateComponents(components, y, newLevel, newBase, newSeason, seasonalIdx);

        // Record observation of y
        observeValue(components, y);

        double nextSeason = components.getSeasonal(components.getCurrentSeasonalIndex());
        // Forecast the value for next season
        updateForecast(components, getForecast(params.getSeasonalityType(), newLevel, newBase, nextSeason));
    }

    public double getForecast(HoltWintersSeasonalityType seasonalityType, double level, double base, double season) {
        return HoltWintersSeasonalityType.MULTIPLICATIVE.equals(seasonalityType)
                ? (level + base) * season
                : level + base + season;
    }

    private void updateComponents(HoltWintersOnlineComponents components, double y, double newLevel, double newBase, double newSeason, int seasonalIdx) {
        components.setLevel(newLevel);
        components.setBase(newBase);
        components.setSeasonal(seasonalIdx, newSeason, y);
    }

    private void updateForecast(HoltWintersOnlineComponents components, double newForecast) {
        components.setForecast(newForecast);
    }

    private void observeValue(HoltWintersOnlineComponents components, double y) {
        components.addValue(y);
    }

}
