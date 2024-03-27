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
package org.opennms.features.vaadin.dashboard.ui.dashboard;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
import org.slf4j.LoggerFactory;

/**
 * This class is used to represent a dashboard view.
 *
 * @author Christian Pape
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class DashboardView extends DashboardBody implements View {

    /**
     * Default constructor.
     */
    public DashboardView() {
    }

    /**
     * Method invoked when the view is entered.
     *
     * @param event the view change event
     */
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        LoggerFactory.getLogger(DashboardView.class).warn("enter(): " + event.getParameters());
        if (event.getParameters() != null) {
            Wallboard wallboard = WallboardProvider.getInstance().getWallboard(event.getParameters());
            if (wallboard != null) {
                setDashletSpecs(wallboard.getDashletSpecs());
            }
        }
    }
}
