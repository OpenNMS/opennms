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
import WeekdayOptions from '../../../main/assets/js/lib/onms-schedule-editor/scripts/WeekdayOptions';
import Weekdays from '../../../main/assets/js/lib/onms-schedule-editor/scripts/Weekdays';

test('Verify construct empty', () => {
    const options = new WeekdayOptions();
    const expected = {
        Sunday: false,
        Monday: false,
        Tuesday: false,
        Wednesday: false,
        Thursday: false,
        Friday: false,
        Saturday: false
    };
    expect(options).toEqual(expected);
});

test('Verify creating from array', () => {
    let options = new WeekdayOptions([ Weekdays.Tuesday, Weekdays.Friday ]);
    const expected = {
        Sunday: false,
        Monday: false,
        Tuesday: true,
        Wednesday: false,
        Thursday: false,
        Friday: true,
        Saturday: false
    };
    expect(options).toEqual(expected);

    const options2 = new WeekdayOptions([Weekdays.Tuesday.label, Weekdays.Friday.label ]);
    expect(options2).toEqual(expected);
});

test('Verify createFrom(String) with unsupported expression', () => {
    let options = WeekdayOptions.createFrom('');
    const expected = {
        Sunday: false,
        Monday: false,
        Tuesday: false,
        Wednesday: false,
        Thursday: false,
        Friday: false,
        Saturday: false
    };
    expect(options).toEqual(expected);

    options = WeekdayOptions.createFrom('*');
    expect(options).toEqual(expected);

    options = WeekdayOptions.createFrom('MON-FRI');
    expect(options).toEqual(expected);

    options = WeekdayOptions.createFrom('?');
    expect(options).toEqual(expected);
});

test('Verify createFrom(String) with supported expression', () => {
    const expected = {
        Sunday: true,
        Monday: true,
        Tuesday: true,
        Wednesday: true,
        Thursday: true,
        Friday: true,
        Saturday: true
    };
    let options = WeekdayOptions.createFrom('SUN,MON,TUE,WED,THU,FRI,SAT');
    expect(options).toEqual(expected);

    // Now verify partial parsing
    expected.Sunday = false;
    expected.Wednesday = false;
    expected.Saturday = false;

    options = WeekdayOptions.createFrom('MON,TUE,THU,FRI');
    expect(options).toEqual(expected);

    // Now try random order
    options = WeekdayOptions.createFrom('FRI,THU,MON,TUE');
    expect(options).toEqual(expected);
});

test('Verify getSelectedWeekdays', () => {
    expect(new WeekdayOptions().getSelectedWeekdays()).toEqual([]);
    expect(new WeekdayOptions([Weekdays.Monday, Weekdays.Sunday]).getSelectedWeekdays()).toEqual([ Weekdays.Sunday, Weekdays.Monday ]);
    expect(new WeekdayOptions([Weekdays.Monday.label, Weekdays.Sunday.label]).getSelectedWeekdays()).toEqual([ Weekdays.Sunday, Weekdays.Monday ]);
});
