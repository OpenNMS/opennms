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
const jQuery = require('vendor/jquery-js');
require('vendor/d3-js');

require('flot');
require('flot/jquery.flot');
require('flot/jquery.flot.time');
require('flot/jquery.flot.canvas');
require('flot-legend/jquery.flot.legend');
require('flot.axislabels');
require('jquery.flot.tooltip/js/jquery.flot.tooltip');
require('flot.saveas');
// this is our patched version, rather than the upstream jquery.flot.navigate
require('flot.navigate');
require('flot-datatable/jquery.flot.datatable.js');

console.log('init: flot-js'); // eslint-disable-line no-console

module.exports = jQuery;