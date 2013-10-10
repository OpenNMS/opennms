/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import java.util.Calendar;

/**
 * This class implements a {@link Dashlet} for displaying a Rrd graph.
 *
 * @author Christian Pape
 */
public class RrdDashlet extends VerticalLayout implements Dashlet {
    /**
     * the dashlet's name
     */
    private String m_name;

    /**
     * The {@link DashletSpec} for this instance
     */
    private DashletSpec m_dashletSpec;

    /**
     * The Rrd helper instance
     */
    RrdGraphHelper m_rrdGraphHelper;

    /**
     *
     */
    private GridLayout m_gridLayout;

    /**
     * Constructor for instantiating new objects.
     *
     * @param name           the name of the dashlet
     * @param dashletSpec    the {@link DashletSpec} to be used
     * @param rrdGraphHelper the rrd graph helper instance
     */
    public RrdDashlet(String name, DashletSpec dashletSpec, RrdGraphHelper rrdGraphHelper) {
        /**
         * Setting the member fields
         */
        m_name = name;
        m_dashletSpec = dashletSpec;
        m_rrdGraphHelper = rrdGraphHelper;

        /**
         * Setting up the layout
         */
        setCaption(getName());
        setSizeFull();

        /**
         * creating the grid layout
         */
        m_gridLayout = new GridLayout();
        m_gridLayout.setSizeFull();
        m_gridLayout.setColumns(1);
        m_gridLayout.setRows(1);

        addComponent(m_gridLayout);

        /**
         * initial update call
         */
        update();
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public boolean isBoosted() {
        return false;
    }

    /**
     * Updates the dashlet contents and computes new boosted state
     */
    @Override
    public void update() {
        /**
         * removing old components
         */
        m_gridLayout.removeAllComponents();

        /**
         * iniatizing the parameters
         */
        int columns = 0;
        int rows = 0;
        int width = 0;
        int height = 0;

        try {
            columns = Integer.parseInt(m_dashletSpec.getParameters().get("columns"));
        } catch (NumberFormatException numberFormatException) {
            columns = 1;
        }

        try {
            rows = Integer.parseInt(m_dashletSpec.getParameters().get("rows"));
        } catch (NumberFormatException numberFormatException) {
            rows = 1;
        }

        try {
            width = Integer.parseInt(m_dashletSpec.getParameters().get("width"));
        } catch (NumberFormatException numberFormatException) {
            width = 400;
        }

        try {
            height = Integer.parseInt(m_dashletSpec.getParameters().get("height"));
        } catch (NumberFormatException numberFormatException) {
            height = 100;
        }

        /**
         * getting the timeframe values
         */
        int timeFrameValue;
        int timeFrameType;

        try {
            timeFrameValue = Integer.parseInt(m_dashletSpec.getParameters().get("timeFrameValue"));
        } catch (NumberFormatException numberFormatException) {
            timeFrameValue = 1;
        }

        try {
            timeFrameType = Integer.parseInt(m_dashletSpec.getParameters().get("timeFrameType"));
        } catch (NumberFormatException numberFormatException) {
            timeFrameType = Calendar.HOUR;
        }

        /**
         * setting new columns/rows
         */
        m_gridLayout.setColumns(columns);
        m_gridLayout.setRows(rows);

        int i = 0;

        Page.getCurrent().getStyles().add(".box { margin: 5px; background-color: #444; border: 1px solid #999; border-top: 0; overflow: auto; }");
        Page.getCurrent().getStyles().add(".text { color:#ffffff; line-height: 11px; font-size: 9px; font-family: 'Lucida Grande', Verdana, sans-serif; font-weight: bold; }");
        Page.getCurrent().getStyles().add(".margin { margin:5px; }");

        /**
         * adding the components
         */
        for (int y = 0; y < m_gridLayout.getRows(); y++) {
            for (int x = 0; x < m_gridLayout.getColumns(); x++) {
                String graphUrl = m_dashletSpec.getParameters().get("graphUrl" + i);

                if (graphUrl != null && !"".equals(graphUrl)) {
                    Image image = new Image(null, new ExternalResource(m_rrdGraphHelper.imageUrlForGraph(m_dashletSpec.getParameters().get("graphUrl" + i), width, height, timeFrameType, timeFrameValue)));
                    VerticalLayout verticalLayout = new VerticalLayout();

                    HorizontalLayout horizontalLayout = new HorizontalLayout();
                    horizontalLayout.addStyleName("box");
                    horizontalLayout.setWidth("100%");
                    horizontalLayout.setHeight("42px");

                    VerticalLayout leftLayout = new VerticalLayout();
                    leftLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
                    leftLayout.addStyleName("margin");

                    Label labelFrom = new Label(m_dashletSpec.getParameters().get("nodeLabel" + i));
                    labelFrom.addStyleName("text");

                    Label labelTo = new Label(m_dashletSpec.getParameters().get("resourceTypeLabel" + i) + ": " + m_dashletSpec.getParameters().get("resourceLabel" + i));
                    labelTo.addStyleName("text");

                    leftLayout.addComponent(labelFrom);
                    leftLayout.addComponent(labelTo);

                    horizontalLayout.addComponent(leftLayout);
                    horizontalLayout.setExpandRatio(leftLayout, 1.0f);

                    verticalLayout.addComponent(horizontalLayout);
                    verticalLayout.addComponent(image);
                    verticalLayout.setWidth(image.getWidth() + "px");

                    m_gridLayout.addComponent(verticalLayout, x, y);

                    verticalLayout.setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
                    verticalLayout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
                    m_gridLayout.setComponentAlignment(verticalLayout, Alignment.MIDDLE_CENTER);
                }
                i++;
            }
        }
    }
}
