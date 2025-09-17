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
import Weekdays from './Weekdays';

export default class WeekdayOptions {
    constructor(input) {
        let options = {};

        // in case of an array, convert it to an object
        if (Array.isArray(input)) {
            let newOptions = {};
            input.forEach((item) => {
                if (item instanceof Weekdays.Weekdays) {
                    newOptions[item.label] = true;
                } else {
                    newOptions[item] = true;
                }
            });
            options = newOptions;
        } else if (typeof input !== 'undefined') {
            options = input;
        }

        // Initialize
        Object.values(Weekdays.all).forEach((weekday) => {
            if (options.hasOwnProperty(weekday.label) === false) {
                this[weekday.label] = false;
            } else {
                this[weekday.label] = options[weekday.label] && true; // enforce boolean
            }
        }, this);
    }

    getSelectedWeekdays() {
        const selectedWeekdays = [];
        Object.values(Weekdays.all).forEach((weekday) => {
            if (this[weekday.label] === true) {
                selectedWeekdays.push(weekday);
            }
        });
        return selectedWeekdays;
    }

    static createFrom(daysOfWeekExpression) {
        const days = daysOfWeekExpression.split(',');
        const weekdays = Object.values(Weekdays.all);
        const selectedWeekdays = [];
        days.forEach((eachDay) => {
            for (let i = 0; i < weekdays.length; i++) {
                if (eachDay === weekdays[i].shortcut) {
                    selectedWeekdays.push(weekdays[i])
                }
            }
        });
        const options = new WeekdayOptions(selectedWeekdays);
        return options;
    }
}
