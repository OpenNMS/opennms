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

import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.Label;

public class AbstractDashlet implements Dashlet {
    private String m_name;
    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;

    public AbstractDashlet(String name, DashletSpec dashletSpec) {
        m_name = name;
        m_dashletSpec = dashletSpec;
    }

    @Override
    public final String getName() {
        return m_name;
    }

    public final void setName(String name) {
        m_name = name;
    }

    @Override
    public final DashletSpec getDashletSpec() {
        return m_dashletSpec;
    }

    public final void setDashletSpec(DashletSpec dashletSpec) {
        m_dashletSpec = dashletSpec;
    }

    private final void updateWallboard() {
    }

    private final void updateDashboard() {
    }

    @Override
    public boolean isBoosted() {
        return false;
    }

    @Override
    public DashletComponent getWallboardComponent(final UI ui) {
        return new AbstractDashletComponent() {
            private Label label = new Label(m_name + " wallboard view");

            public void refresh() {
            }

            public Component getComponent() {
                return label;
            }
        };
    }

    @Override
    public DashletComponent getDashboardComponent(final UI ui) {
        return new AbstractDashletComponent() {
            private Label label = new Label(m_name + " dashboard view");

            public void refresh() {
            }

            public Component getComponent() {
                return label;
            }
        };
    }
}
