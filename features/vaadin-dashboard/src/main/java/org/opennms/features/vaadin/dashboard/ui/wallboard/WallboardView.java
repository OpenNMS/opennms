/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.dashboard.ui.wallboard;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.VerticalLayout;
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
