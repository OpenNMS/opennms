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

// jquery-ui base
require('jquery-ui/ui/core');
require('jquery-ui/ui/widget');
require('jquery-ui/ui/widgets/mouse');
require('jquery-ui/ui/widgets/draggable');
require('jquery-ui/ui/widgets/droppable');
require('jquery-ui/ui/widgets/resizable');
require('jquery-ui/ui/widgets/selectable');
require('jquery-ui/ui/widgets/sortable');
require('jquery-ui/ui/effect');

// additional core plugins
require('jquery-ui/ui/data');
require('jquery-ui/ui/disable-selection');
require('jquery-ui/ui/focusable');
require('jquery-ui/ui/form-reset-mixin');
require('jquery-ui/ui/form');
require('jquery-ui/ui/ie');
require('jquery-ui/ui/keycode');
require('jquery-ui/ui/labels');
require('jquery-ui/ui/plugin');
require('jquery-ui/ui/position');
require('jquery-ui/ui/safe-active-element');
require('jquery-ui/ui/safe-blur');
require('jquery-ui/ui/scroll-parent');
require('jquery-ui/ui/tabbable');
require('jquery-ui/ui/unique-id');
require('jquery-ui/ui/version');

// additional widgets
require('jquery-ui/ui/widgets/accordion');
require('jquery-ui/ui/widgets/autocomplete');
require('jquery-ui/ui/widgets/button');
require('jquery-ui/ui/widgets/checkboxradio');
require('jquery-ui/ui/widgets/controlgroup');
require('jquery-ui/ui/widgets/datepicker');
require('jquery-ui/ui/widgets/dialog');
require('jquery-ui/ui/widgets/menu');
require('jquery-ui/ui/widgets/progressbar');
require('jquery-ui/ui/widgets/selectmenu');
require('jquery-ui/ui/widgets/slider');
require('jquery-ui/ui/widgets/spinner');
require('jquery-ui/ui/widgets/tabs');
require('jquery-ui/ui/widgets/tooltip');

// 3rd-party jquery-ui plugins
require('jquery-ui-treemap');
require('jquery-sparkline/dist/jquery.sparkline');

console.log('init: jquery-ui-js'); // eslint-disable-line no-console

module.exports = jQuery;
