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
package org.opennms.features.vaadin.dashboard.dashlets;

import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.Label;

/**
 * This class represents a "undefined" {@link Dashlet} used for error conditions when the
 * required {@link Dashlet} cannot be found.
 *
 * @author Christian Pape
 */
public class UndefinedDashlet extends AbstractDashlet {

    /**
     * Constructor for instantiating this {@link Dashlet}
     *
     * @param dashletSpec the {@link DashletSpec} to use
     */
    public UndefinedDashlet(String name, DashletSpec dashletSpec) {
        super(name, dashletSpec);
    }

    @Override
    public DashletComponent getWallboardComponent(final UI ui) {
        DashletComponent dashletComponent = new AbstractDashletComponent() {
            @Override
            public void refresh() {
            }

            @Override
            public Component getComponent() {
                VerticalLayout verticalLayout = new VerticalLayout();
                Label label = new Label("The specified dashlet could not be found!");
                verticalLayout.addComponent(label);
                verticalLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

                return verticalLayout;
            }
        };
        return dashletComponent;
    }

    @Override
    public DashletComponent getDashboardComponent(final UI ui) {
        return getWallboardComponent(ui);
    }
}
