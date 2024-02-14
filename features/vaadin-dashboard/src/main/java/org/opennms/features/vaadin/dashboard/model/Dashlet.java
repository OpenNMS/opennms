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
package org.opennms.features.vaadin.dashboard.model;

import com.vaadin.ui.UI;

/**
 * This interface defines the required methods for implementing a dashlet.
 *
 * @author Christian Pape
 */
public interface Dashlet {
    /**
     * This method returns the name of this dashlet.
     *
     * @return the dashlet's name
     */
    String getName();

    DashletSpec getDashletSpec();

    /**
     * Checks whether this dashlet is boosted.
     *
     * @return true, if boosted, false otherwise
     */
    boolean isBoosted();

    /**
     * Updates the dashlet contents and computes new boosted state
     * @param ui The {@link UI} which holds the component.
     */

    DashletComponent getWallboardComponent(final UI ui);

    DashletComponent getDashboardComponent(final UI ui);
}
