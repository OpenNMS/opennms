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
package org.opennms.features.vaadin.dashboard.ui.wallboard;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.v7.ui.VerticalLayout;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.Wallboard;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class WallboardView extends VerticalLayout implements View {

    private final WallboardBody dashletBoardBody;

    public WallboardView() {
        setSizeFull();
        dashletBoardBody = new WallboardBody();
        addComponents(dashletBoardBody);
        setExpandRatio(dashletBoardBody, 1.0f);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        if (event.getParameters() != null) {
            Wallboard wallboard = WallboardProvider.getInstance().getWallboard(event.getParameters());
            if (wallboard != null) {
                dashletBoardBody.setDashletSpecs(wallboard.getDashletSpecs());
            }
        }
    }

    public boolean isPaused() {
        return dashletBoardBody.isPaused();
    }

    public boolean isPausable() {
        return dashletBoardBody.isPausable();
    }

    public void pause() {
        dashletBoardBody.pause();
    }

    public void resume() {
        dashletBoardBody.resume();
    }
}
