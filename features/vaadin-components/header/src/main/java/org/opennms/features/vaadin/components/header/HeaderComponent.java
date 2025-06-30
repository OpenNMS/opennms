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
package org.opennms.features.vaadin.components.header;

import com.vaadin.annotations.JavaScript;
import com.vaadin.ui.AbstractJavaScriptComponent;

/**
 * Header component for Vaadin Topology and other UI pages.
 * Note that the 'header-component_connector.vaadin.js' file loads 'bootstrap.jsp'.
 * The query string param 'superQuiet' disables the old menu and some other things.
 * The query string param 'fromVaadin' will direct 'bootstrap.jsp' to include the code injecting the
 * Vue top/side menus into the vaadin-based page.
 */
@JavaScript("theme://../opennms/assets/header-component_connector.vaadin.js")
public class HeaderComponent extends AbstractJavaScriptComponent {

    public HeaderComponent() {
        setId("onmsheader");
        setWidth(100, Unit.PERCENTAGE);
    }
}
