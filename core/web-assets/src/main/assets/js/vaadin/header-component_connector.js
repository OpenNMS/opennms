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
require('vendor/jquery-js');

if (!window.org_opennms_features_vaadin_components_header_HeaderComponent) {
    window.org_opennms_features_vaadin_components_header_HeaderComponent = function HeaderComponent() {
        console.log('headercomponent: registering state change');
        this.onStateChange = function onStateChange() {
            console.log('headercomponent: state change triggered', this.getState());

            $("#onmsheader").empty();
            var div = $("<div></div>").load("/opennms/includes/bootstrap.jsp?nobreadcrumbs=true&superQuiet=true");
            $(div).appendTo("#onmsheader");
        };
    };
    console.log('init: headercomponent');
}

module.exports = window.org_opennms_features_vaadin_components_header_HeaderComponent;