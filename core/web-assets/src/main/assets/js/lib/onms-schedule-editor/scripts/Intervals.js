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
const EVERY_3_HOURS = '180';
const EVERY_2_HOURS = '120';
const EVERY_HOUR = '60';
const EVERY_30_MINUTES = '30';
const EVERY_15_MINUTES = '15';
const EVERY_10_MINUTES = '10';
const EVERY_5_MINUTES = '5';

const Minutes = [
    EVERY_30_MINUTES,
    EVERY_15_MINUTES,
    EVERY_10_MINUTES,
    EVERY_5_MINUTES
];

const Hours = [
    EVERY_3_HOURS,
    EVERY_2_HOURS,
    EVERY_HOUR
];

const all = [
    EVERY_3_HOURS,
    EVERY_2_HOURS,
    EVERY_HOUR,
    EVERY_30_MINUTES,
    EVERY_15_MINUTES,
    EVERY_10_MINUTES,
    EVERY_5_MINUTES,
];

export default {
    all,
    EVERY_3_HOURS,
    EVERY_2_HOURS,
    EVERY_HOUR,
    EVERY_30_MINUTES,
    EVERY_15_MINUTES,
    EVERY_10_MINUTES,
    EVERY_5_MINUTES,
    Hours,
    Minutes,
};