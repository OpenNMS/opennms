///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

/**
 * The Date an OnmsDatePicker in time-only mode holds for a clock time. Only the time of day means anything, so the
 * date is a fixed day, 10 January 2000, on which no time zone changed its clocks: any clock time exists on it, which
 * is not true of today on a daylight-saving day (02:30 on a spring-forward day would become 03:30). The picker puts
 * typed times on this day; consumers start their time-only values on it.
 */
export const toTimeOnlyDate = (hours: number, minutes: number, seconds = 0): Date =>
  new Date(2000, 0, 10, hours, minutes, seconds)
