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
const checkForecastWarning = require('forecast/checkForecastWarning');

// Build a measurement response in the same shape Backshift hands to drawChart.
// columns is a map of name -> values array; the helper builds the numeric
// columnNameToIndex and columns-by-index that the function inspects.
function buildResults(columnsByName) {
    const names = Object.keys(columnsByName);
    const columnNameToIndex = {};
    const columns = [];
    names.forEach((name, idx) => {
        columnNameToIndex[name] = idx;
        columns.push(columnsByName[name]);
    });
    return { columnNameToIndex, columns };
}

test('returns null when results are null or undefined', () => {
    expect(checkForecastWarning(null)).toBeNull();
    expect(checkForecastWarning(undefined)).toBeNull();
    expect(checkForecastWarning({})).toBeNull();
});

test('warns when the HWFit column is missing entirely', () => {
    const results = buildResults({
        timestamp: [1, 2, 3],
        Trend: [10, 11, 12]
        // no HWFit
    });
    expect(checkForecastWarning(results)).toMatch(/could not be produced/i);
});

test('warns when HWFit is present but every sample is NaN', () => {
    const results = buildResults({
        HWFit: [NaN, NaN, NaN, NaN],
        HWLwr: [NaN, NaN, NaN, NaN],
        HWUpr: [NaN, NaN, NaN, NaN]
    });
    expect(checkForecastWarning(results)).toMatch(/no valid values/i);
});

test('warns when HWLwr and HWUpr coincide with HWFit (zero-width bounds)', () => {
    const results = buildResults({
        HWFit: [10, 11, 12, 13],
        HWLwr: [10, 11, 12, 13],
        HWUpr: [10, 11, 12, 13]
    });
    expect(checkForecastWarning(results)).toMatch(/zero width/i);
});

test('warns on zero-width bounds even when some values are NaN', () => {
    const results = buildResults({
        HWFit: [10, 11, NaN, 13],
        HWLwr: [10, 11, NaN, 13],
        HWUpr: [10, 11, NaN, 13]
    });
    expect(checkForecastWarning(results)).toMatch(/zero width/i);
});

test('returns null on a healthy forecast with non-zero bounds', () => {
    const results = buildResults({
        HWFit: [10, 11, 12, 13],
        HWLwr: [9.5, 10.2, 11.1, 12.0],
        HWUpr: [10.5, 11.8, 12.9, 14.0]
    });
    expect(checkForecastWarning(results)).toBeNull();
});

test('returns null when HWFit has values but HWLwr/HWUpr are absent', () => {
    // confidenceLevel=0 path: server emits HWFit but no bounds. Should not warn.
    const results = buildResults({
        HWFit: [10, 11, 12]
    });
    expect(checkForecastWarning(results)).toBeNull();
});

test('returns null when only the first sample is non-NaN', () => {
    // Even one valid value qualifies as a usable forecast.
    const results = buildResults({
        HWFit: [10, NaN, NaN, NaN],
        HWLwr: [9.5, NaN, NaN, NaN],
        HWUpr: [10.5, NaN, NaN, NaN]
    });
    expect(checkForecastWarning(results)).toBeNull();
});
