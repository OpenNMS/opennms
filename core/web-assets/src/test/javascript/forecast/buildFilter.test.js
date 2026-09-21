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
const buildFilter = require('forecast/buildFilter');

test('returns a filter descriptor with the given name', () => {
    const result = buildFilter('HoltWinters', { inputColumn: 'rt' });
    expect(result.type).toBe('filter');
    expect(result.name).toBe('HoltWinters');
});

test('coerces numeric values to strings', () => {
    const result = buildFilter('Trend', {
        polynomialOrder: 3,
        secondsAhead: 86400,
        outlierThreshold: 0.975
    });
    const byKey = {};
    result.parameter.forEach(p => { byKey[p.key] = p; });
    expect(byKey.polynomialOrder.value).toBe('3');
    expect(byKey.secondsAhead.value).toBe('86400');
    expect(byKey.outlierThreshold.value).toBe('0.975');
    result.parameter.forEach(p => {
        expect(typeof p.value).toBe('string');
    });
});

test('coerces boolean values to strings', () => {
    const result = buildFilter('Chomp', { stripNaNs: true });
    expect(result.parameter[0]).toEqual({ key: 'stripNaNs', value: 'true' });
});

test('passes through string values unchanged', () => {
    const result = buildFilter('Outlier', {
        inputColumn: 'response_time'
    });
    expect(result.parameter[0]).toEqual({ key: 'inputColumn', value: 'response_time' });
});

test('preserves key order', () => {
    const result = buildFilter('Foo', { a: 1, b: 2, c: 3 });
    expect(result.parameter.map(p => p.key)).toEqual(['a', 'b', 'c']);
});
