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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import org.opennms.netmgt.model.OnmsCategory;

import java.util.Set;

/**
 * This interface is used to represent components that are refreshable by a surveillance view table.
 *
 * @author Christian Pape
 */
public interface SurveillanceViewDetail {
    /**
     * Refreshes the contents of this component. This is triggered by a mouse interaction or data
     * change in associated surveillance view.
     *
     * @param rowCategories the row categories
     * @param colCategories the column categories
     */
    public void refreshDetails(Set<OnmsCategory> rowCategories, Set<OnmsCategory> colCategories);
}
