/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.dashboard.client.layout;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.dashboard.client.dnd.GridViewDragHandler;
import org.opennms.features.dashboard.client.dnd.ResizeDragController;
import org.opennms.features.dashboard.client.portlet.IBasicPortlet;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.HorizontalPanelDropController;
import com.allen_sauer.gwt.dnd.client.drop.VerticalPanelDropController;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public class VerticalDBLayout extends BasicDBLayout {
    private static final String CSS_GRID_PANEL = "demo-gridview";

    private static final String CSS_GRID_PANEL_COLUMN_COMPOSITE = "demo-gridview-column-composite";

    private static final String CSS_GRID_PANEL_CONTAINER = "demo-gridview-container";

    private static final String CSS_GRID_PANEL_HEADING = "demo-gridview-heading";

    private static final String CSS_GRID_PANEL_WIDGET = "demo-gridview-widget";

    private static final int COLUMNS = 3;

    private static final int SPACING = 1;

    private GridViewDragHandler dragHandler;

    private PickupDragController columnDragController;

    private HorizontalPanel bodyPanel;

    private HorizontalPanelDropController columnDropController;

    private List<VerticalPanel> columnList;

    public VerticalDBLayout() {

        // initialize horizontal panel to hold our columns       
        bodyPanel.setStyleName(CSS_GRID_PANEL_CONTAINER);
        bodyPanel.setSpacing(SPACING);
        this.add(bodyPanel);        
    }

    @Override
    public void init() {
        dragHandler = new GridViewDragHandler();
        bodyPanel = new HorizontalPanel();
        columnList = new ArrayList<VerticalPanel>();
        
        resizeDragController = new ResizeDragController(RootPanel.get());
        resizeDragController.setBehaviorConstrainedToBoundaryPanel(true);
        resizeDragController.setBehaviorMultipleSelection(false);

        // initialize our widget drag controller
        moveDragController = new PickupDragController(this, false);
        moveDragController.setBehaviorMultipleSelection(false);
        moveDragController.addDragHandler(dragHandler);

        // initialize our column drag controller
        columnDragController = new PickupDragController(this, false);
        columnDragController.setBehaviorMultipleSelection(false);
        columnDragController.addDragHandler(dragHandler);

        // initialize our column drop controller
        columnDropController = new HorizontalPanelDropController(bodyPanel);
        columnDragController.registerDropController(columnDropController);

        for (int col = 0; col < COLUMNS; col++) {
            addNewColumn();
        }
    }

    public VerticalPanel addNewColumn() {
        // initialize a vertical panel to hold the heading and a second
        // vertical
        // panel
        VerticalPanel columnCompositePanel = new VerticalPanel();
        columnCompositePanel.setStyleName(CSS_GRID_PANEL_COLUMN_COMPOSITE);
        bodyPanel.add(columnCompositePanel);

        VerticalPanel verticalPanel = new VerticalPanelWithSpacer();
        verticalPanel.setStyleName(CSS_GRID_PANEL_CONTAINER);
        verticalPanel.setSpacing(SPACING);
        columnList.add(verticalPanel);

        // initialize a widget drop controller for the current column
        VerticalPanelDropController widgetDropController = new VerticalPanelDropController(
                                                                                           verticalPanel);
        moveDragController.registerDropController(widgetDropController);

        // Put together the column pieces
        Label heading = new Label();
        heading.setStyleName(CSS_GRID_PANEL_HEADING);
        columnCompositePanel.add(heading);
        columnCompositePanel.add(verticalPanel);

        // make the column draggable by its heading
        columnDragController.makeDraggable(columnCompositePanel, heading);

        return verticalPanel;
    }

    @Override
    public int addNewPortlet(IBasicPortlet portlet) {
        if (!columnList.isEmpty()) {
            int index = portletList.size() % columnList.size();
            return addNewPortlet(index, -1, portlet);
        } else {
            addNewColumn();
            return addNewPortlet(columnList.size() - 1, -1, portlet);
        }
    }

    @Override
    public int addNewPortlet(int column, int y, IBasicPortlet portlet) {
        if (columnList.size() > column) {
            VerticalPanel verticalPanel = columnList.get(column);

            verticalPanel.add(portlet.asWidget());
            portletList.add(portlet);
            // make the portlet draggable
            portlet.makeResizable(resizeDragController);
            portlet.makeDraggable(moveDragController);

            return portletList.size() - 1;
        }
        return -1;
    }
}
