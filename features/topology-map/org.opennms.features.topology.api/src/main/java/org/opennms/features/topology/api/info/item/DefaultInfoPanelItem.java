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
package org.opennms.features.topology.api.info.item;

import com.vaadin.ui.Component;

/**
 * Default Implementation of an {@link InfoPanelItem}.
 */
public class DefaultInfoPanelItem implements InfoPanelItem {

    private Component component;
    private String title;
    private int order;
    private String id;

    public DefaultInfoPanelItem withComponent(Component component) {
        this.component = component;
        return this;
    }

    public DefaultInfoPanelItem withOrder(int order) {
        this.order = order;
        return this;
    }

    public DefaultInfoPanelItem withTitle(String title) {
        this.title = title;
        return this;
    }

    public DefaultInfoPanelItem withId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Component getComponent() {
        return component;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getId() {
        return id;
    }
}
