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
import CronDefinition from '../../../main/assets/js/lib/onms-schedule-editor/scripts/CronDefintion';

test('Verify default creation', () => {
    const cron = new CronDefinition();
    expect(cron.seconds).toEqual({});
    expect(cron.minutes).toEqual({});
    expect(cron.hours).toEqual({});
    expect(cron.month).toEqual({});
    expect(cron.dayOfMonth).toEqual({});
    expect(cron.dayOfWeek).toEqual({});
});

test('Verify custom creation', () => {
    const cron = new CronDefinition({
        seconds: 0,
        minutes: 0,
        hours: 0,
        month: '*',
        dayOfMonth: '*',
        dayOfWeek: '?',
    });
    expect(cron.seconds).toBe(0);
    expect(cron.minutes).toBe(0);
    expect(cron.hours).toBe(0);
    expect(cron.month).toBe('*');
    expect(cron.dayOfMonth).toBe('*');
    expect(cron.dayOfWeek).toBe('?');
});

test('Verify asCronExpression', () => {
    const cron = new CronDefinition({seconds: 1, minutes: 2, hours: 3, month: '?', dayOfMonth: 4, dayOfWeek: '6L'});
    expect(cron.asCronExpression()).toBe('1 2 3 4 ? 6L');
});

test('Verify minutes concrete,interval and range methods', () => {
    const cron = new CronDefinition();
    cron.minutes = 2;
    expect(cron.isConcreteMinutes()).toBe(true);
    expect(cron.isMinutesInterval()).toBe(false);
    expect(cron.isMinutesRange()).toBe(false);

    cron.minutes = '*';
    expect(cron.isConcreteMinutes()).toBe(false);
    expect(cron.isMinutesInterval()).toBe(false);
    expect(cron.isMinutesRange()).toBe(false);

    cron.minutes = '?';
    expect(cron.isConcreteMinutes()).toBe(false);
    expect(cron.isMinutesInterval()).toBe(false);
    expect(cron.isMinutesRange()).toBe(false);

    cron.minutes = '0/30';
    expect(cron.isConcreteMinutes()).toBe(false);
    expect(cron.isMinutesInterval()).toBe(true);
    expect(cron.isMinutesRange()).toBe(false);

    cron.minutes = '5-10';
    expect(cron.isConcreteMinutes()).toBe(false);
    expect(cron.isMinutesInterval()).toBe(false);
    expect(cron.isMinutesRange()).toBe(true);

    cron.minutes = '5,6';
    expect(cron.isConcreteMinutes()).toBe(false);
    expect(cron.isMinutesInterval()).toBe(false);
    expect(cron.isMinutesRange()).toBe(false);
});

test('Verify hours concrete,interval and range methods', () => {
    const cron = new CronDefinition();
    cron.hours = 2;
    expect(cron.isConcreteHours()).toBe(true);
    expect(cron.isHoursInterval()).toBe(false);
    expect(cron.isHoursRange()).toBe(false);

    cron.hours = '*';
    expect(cron.isConcreteHours()).toBe(false);
    expect(cron.isHoursInterval()).toBe(false);
    expect(cron.isHoursRange()).toBe(false);

    cron.hours = '?';
    expect(cron.isConcreteHours()).toBe(false);
    expect(cron.isHoursInterval()).toBe(false);
    expect(cron.isHoursRange()).toBe(false);

    cron.hours = '0/30';
    expect(cron.isConcreteHours()).toBe(false);
    expect(cron.isHoursInterval()).toBe(true);
    expect(cron.isHoursRange()).toBe(false);

    cron.hours = '5-10';
    expect(cron.isConcreteHours()).toBe(false);
    expect(cron.isHoursInterval()).toBe(false);
    expect(cron.isHoursRange()).toBe(true);

    cron.hours = '5,6';
    expect(cron.isConcreteHours()).toBe(false);
    expect(cron.isHoursInterval()).toBe(false);
    expect(cron.isHoursRange()).toBe(false);
});

test('Verify get interval', () => {
    const cron = new CronDefinition({hours: '10', minutes: '15'});
    expect(cron.interval).toBe('0');

    cron.minutes = '0/5';
    expect(cron.interval).toBe('5');
    cron.minutes = '0/10';
    expect(cron.interval).toBe('10');
    cron.minutes = '0/15';
    expect(cron.interval).toBe('15');
    cron.minutes = '0/30';
    expect(cron.interval).toBe('30');

    cron.minutes = '0';
    cron.hours = '4-4/2';
    expect(cron.interval).toBe('120');
    cron.hours = '4-8/3';
    expect(cron.interval).toBe('180');
    cron.hours = '2-3';
    expect(cron.interval).toBe('60');
});
