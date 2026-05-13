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
'use strict';

// Inspect a /rest/measurements response to detect forecast failure modes the
// user should be told about. Returns a human-readable warning string when one
// of the conditions below applies, or null when the forecast looks healthy.
function checkForecastWarning(results) {
    if (!results || !results.columnNameToIndex) {
        return null;
    }
    var idx = results.columnNameToIndex;
    if (!('HWFit' in idx)) {
        return 'Forecast could not be produced. The most common cause is that the selected training window does not have enough historical data.';
    }
    var fit = results.columns[idx.HWFit];
    var hasValidFit = false;
    if (fit) {
        for (var k = 0; k < fit.length; k++) {
            if (!isNaN(fit[k])) { hasValidFit = true; break; }
        }
    }
    if (!hasValidFit) {
        return 'Forecast produced no valid values. This typically means gaps or outliers in the training window left too few usable samples after filtering.';
    }
    if ('HWLwr' in idx && 'HWUpr' in idx) {
        var lwr = results.columns[idx.HWLwr];
        var upr = results.columns[idx.HWUpr];
        var boundsVisible = false;
        for (var i = 0; i < lwr.length; i++) {
            if (!isNaN(lwr[i]) && !isNaN(upr[i]) && Math.abs(upr[i] - lwr[i]) > 1e-12) {
                boundsVisible = true;
                break;
            }
        }
        if (!boundsVisible) {
            return 'Confidence bounds have zero width (training residuals had no variance); upper and lower bounds coincide with the fit line.';
        }
    }
    return null;
}

module.exports = checkForecastWarning;
