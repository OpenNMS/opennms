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

package org.opennms.features.dashboard.client.portlet;

import org.opennms.features.dashboard.client.dnd.ResizeDragController;
import org.opennms.features.dashboard.client.resource.WidgetResource;

import com.allen_sauer.gwt.dnd.client.DragController;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * author: Tharindu Munasinghe (tharindumunasinghe@gmail.com)
 * org.opennms.features.dashboard
 */
public abstract class BasicPortlet extends FocusPanel implements
        IBasicPortlet {
    protected WidgetResource imageResource = WidgetResource.WIDGETRESOURCE;

    // protected WidgetCssResource cssResource =
    // imageResource.widgetsCssResource();

    public static final int DIRECTION_EAST = 0x0001;

    public static final int DIRECTION_NORTH = 0x0002;

    public static final int DIRECTION_SOUTH = 0x0004;

    public static final int DIRECTION_WEST = 0x0008;

    public static final DirectionConstant EAST = new DirectionConstant(
                                                                       DIRECTION_EAST,
                                                                       "e");

    public static final DirectionConstant NORTH = new DirectionConstant(
                                                                        DIRECTION_NORTH,
                                                                        "n");

    public static final DirectionConstant NORTH_EAST = new DirectionConstant(
                                                                             DIRECTION_NORTH
                                                                                     | DIRECTION_EAST,
                                                                             "ne");

    public static final DirectionConstant NORTH_WEST = new DirectionConstant(
                                                                             DIRECTION_NORTH
                                                                                     | DIRECTION_WEST,
                                                                             "nw");

    public static final DirectionConstant SOUTH = new DirectionConstant(
                                                                        DIRECTION_SOUTH,
                                                                        "s");

    public static final DirectionConstant SOUTH_EAST = new DirectionConstant(
                                                                             DIRECTION_SOUTH
                                                                                     | DIRECTION_EAST,
                                                                             "se");

    public static final DirectionConstant SOUTH_WEST = new DirectionConstant(
                                                                             DIRECTION_SOUTH
                                                                                     | DIRECTION_WEST,
                                                                             "sw");

    public static final DirectionConstant WEST = new DirectionConstant(
                                                                       DIRECTION_WEST,
                                                                       "w");

    private static final int BORDER_THICKNESS = 5;

    protected static final int TITLE_HEIGHT = 22;

    protected static final int BUTTON_PANEL_HEIGHT = 23;

    private static BasicPortlet focusedPortlet;
    
    /**
     * Main layout panel of the portlet
     */
    private Grid mainPanel;

    /**
     * Panel which contains the title & the body panel
     */

    private Widget eastWidget;

    private boolean initialLoad = false;

    private Widget southWidget;

    private Panel cornerWidget;

    private Grid containerPanel;

    protected FocusPanel titlePanel;

    protected SimplePanel contentpanel;

    protected ClickHandler closeHandler;

    private DragController moveDragController;

    private ResizeDragController resizeDragController;

    private int contentHeight;

    private int contentWidth;

    public BasicPortlet() {
        mainPanel = new Grid(3, 3);
        mainPanel.getCellFormatter().setWidth(0, 0, BORDER_THICKNESS + "px");
        mainPanel.getCellFormatter().setHeight(0, 0, BORDER_THICKNESS + "px");
        mainPanel.getCellFormatter().setWidth(2, 2, BORDER_THICKNESS + "px");
        mainPanel.getCellFormatter().setHeight(2, 2, BORDER_THICKNESS + "px");
        mainPanel.setCellSpacing(0);
        mainPanel.setCellPadding(0);
        this.add(mainPanel);

        this.addStyleName("popup-WindowPanel");
        mainPanel.addStyleName("borderPanel");

        DOM.setStyleAttribute(this.getElement(), "zIndex", "20000");
        DOM.setStyleAttribute(this.getElement(), "borderColor", "#C3D9FF");
        DOM.setStyleAttribute(this.getElement(), "border", "none");
        DOM.setStyleAttribute(this.getElement(), "background", "none");
        DOM.setStyleAttribute(this.getElement(), "padding", "0px");
        DOM.setStyleAttribute(this.getElement(), "outline", "0 none");

        containerPanel = new Grid(2, 1);
        containerPanel.setSize("100%", "100%");
        containerPanel.setCellSpacing(0);
        containerPanel.setCellPadding(0);
        mainPanel.setWidget(1, 1, containerPanel);

        titlePanel = new FocusPanel();
        containerPanel.setWidget(0, 0, titlePanel);
        CellFormatter cellFormatter = containerPanel.getCellFormatter();
        cellFormatter.setHeight(0, 0, TITLE_HEIGHT + "px");
        titlePanel.setStyleName("popup-WindowPanel-title");
        titlePanel.setSize("100%", TITLE_HEIGHT + "px");

        contentpanel = new SimplePanel();
        contentpanel.setSize("100%", "100%");
        containerPanel.setWidget(1, 0, contentpanel);

        setupCell(0, 0, NORTH_WEST);
        setupCell(0, 1, NORTH);
        setupCell(0, 2, NORTH_EAST);

        setupCell(1, 0, WEST);
        eastWidget = setupCell(1, 2, EAST);
        eastWidget.setSize(BORDER_THICKNESS + "px", "100%");

        setupCell(2, 0, SOUTH_WEST);
        southWidget = setupCell(2, 1, SOUTH);
        southWidget.setSize("100%", BORDER_THICKNESS + "px");

        cornerWidget = setupCell(2, 2, SOUTH_EAST);
        DOM.setStyleAttribute(cornerWidget.getElement(), "position", "absolute");

        Image corner = new Image(imageResource.resizeCorner());
        cornerWidget.add(corner);
        DOM.setStyleAttribute(corner.getElement(), "position", "absolute");
        DOM.setStyleAttribute(corner.getElement(), "right", "2px");
        DOM.setStyleAttribute(corner.getElement(), "bottom", "4px");

        closeHandler = new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                BasicPortlet.this.setVisible(false);
                BasicPortlet.this.removeFromParent();
            }
        };
        
        titlePanel.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent arg0) {
                selectItem();
            }
        });
    }

    private Panel setupCell(int row, int col, DirectionConstant direction) {
        final FocusPanel widget = new FocusPanel();
        widget.setPixelSize(BORDER_THICKNESS, BORDER_THICKNESS);
        mainPanel.setWidget(row, col, widget);
        return widget;
    }

    @Override
    public void makeResizable(ResizeDragController resizeDragController) {
        if (this.resizeDragController == null) {
            this.resizeDragController = resizeDragController;
        }
        makeNotResizable();
        addResizeHandle(eastWidget, EAST);
        addResizeHandle(southWidget, SOUTH);
        addResizeHandle(cornerWidget, SOUTH_EAST);
    }

    @Override
    public void makeDraggable(DragController moveDragController) {
        if (this.moveDragController == null) {
            this.moveDragController = moveDragController;
        }
        makeNotDraggable();
        moveDragController.makeDraggable(this, titlePanel);
    }

    @Override
    public void makeNotResizable() {
        removeResizeHandle(eastWidget, EAST);
        removeResizeHandle(southWidget, SOUTH);
        removeResizeHandle(cornerWidget, SOUTH_EAST);
    }

    private void addResizeHandle(Widget w, DirectionConstant direction) {
        resizeDragController.makeDraggable(w, direction);
        w.addStyleName("Resize-" + direction.directionLetters);
    }

    private void removeResizeHandle(Widget w, DirectionConstant direction) {
        if (resizeDragController != null) {
            try {
                resizeDragController.makeNotDraggable(w);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        w.removeStyleName("Resize-" + direction.directionLetters);
    }

    @Override
    public void makeNotDraggable() {
        if (moveDragController != null) {
            try {
                moveDragController.makeNotDraggable(this);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        if (!initialLoad && mainPanel.getOffsetHeight() != 0) {
            initialLoad = true;
            setContentSize(mainPanel.getOffsetWidth(),
                           mainPanel.getOffsetHeight());
        }
    }

    @Override
    public void setContentSize(int width, int height) {
        if (width != contentWidth) {
            contentWidth = width;
        }
        if (height != contentHeight) {
            contentHeight = height;
        }
        mainPanel.setPixelSize(contentWidth, contentHeight);
    }
    
    public void selectItem() {
        if (focusedPortlet != this) {
            if (focusedPortlet != null) {
                focusedPortlet.removeStyleName("demo-gridview-widget-focus");
                focusedPortlet.setStyleName("demo-gridview-widget");
            }
            focusedPortlet = this;
            focusedPortlet.removeStyleName("demo-gridview-widget");
            focusedPortlet.setStyleName("demo-gridview-widget-focus");
        }
    }   

    @Override
    public void setHeight(String height) {
        mainPanel.setHeight(height);
    }

    @Override
    public void setWidth(String width) {
        mainPanel.setWidth(width);
    }

    @Override
    public void setPixelSize(int width, int height) {
        mainPanel.setPixelSize(width, height);
    }

    @Override
    public void setSize(String width, String height) {
        mainPanel.setSize(width, height);
    }

    @Override
    public void clearPortletContent() {
        contentpanel.clear();
    }
    
    @Override
    public void addPortletContent(Widget w) {
       contentpanel.add(w);
    }
    
    @Override
    public Widget asWidget() {
        return this;
    }
    
    @Override
    public void removeFromParent()
    {
        super.removeFromParent();
    }
    
    @Override
    public int getContentHeight() {
        return contentHeight;
    }

    @Override
    public int getContentWidth() {
        return contentWidth;
    }
    
    @Override
    public void selectPortlet()
    {}
}
