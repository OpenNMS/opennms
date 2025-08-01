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
 * A general item to show up in the info panel.
 */
public interface InfoPanelItem extends Comparable<InfoPanelItem> {
    /**
     * The vaadin component displayed to the user.
     *
     * @return a component
     */
    Component getComponent();

    /**
     * The title displayed to the user.
     *
     * @return a short title string
     */
    String getTitle();

    /**
     * The order of the item in which it should occur.
     *
     * @return a number used to sort the contribution
     */
    int getOrder();

    /**
     * The id of the component.
     *
     * @return the id of the component.
     */
    default String getId() {
        return null;
    }

    default int compareTo(final InfoPanelItem that) {
        return Integer.compare(this.getOrder(), that.getOrder());
    }
}
