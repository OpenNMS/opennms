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
package org.opennms.features.topology.api;

import com.vaadin.ui.UI;

/**
 * Context object to allow access to the underlying UI and GraphContainer.
 *
 * @author Donald Desloge
 * @author Markus von Rüden
 *
 */
public interface OperationContext {
	
	enum DisplayLocation { MENUBAR, CONTEXTMENU, SEARCH }

    /**
     * Returns the current UI, the operation was triggered from
     *
     * @return the current UI, the operation was triggered from
     */
    UI getMainWindow();

    /**
     * Returns the current {@link GraphContainer}
     *
     * @return The current {@link GraphContainer}
     */
    GraphContainer getGraphContainer();

    /**
     * Returns the {@link DisplayLocation} the operation was triggered from
     *
     * @return the {@link DisplayLocation} the operation was triggered from
     */
    DisplayLocation getDisplayLocation();
}
