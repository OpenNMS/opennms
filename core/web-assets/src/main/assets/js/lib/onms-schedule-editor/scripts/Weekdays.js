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
class Weekdays {
    constructor(id, label) {
        this.id = id;
        this.label = label;
    }

    get shortcut() {
        return this.label.substr(0, 3).toUpperCase();
    }
}

const Sunday = new Weekdays(1, 'Sunday');
const Monday = new Weekdays(2, 'Monday');
const Tuesday = new Weekdays(3, 'Tuesday');
const Wednesday = new Weekdays(4, 'Wednesday');
const Thursday = new Weekdays(5, 'Thursday');
const Friday = new Weekdays(6, 'Friday');
const Saturday = new Weekdays(7, 'Saturday');

const all = [
    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
];

export default {
    all,
    Weekdays,
    Sunday,
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday
};
