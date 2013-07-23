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

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.charts.ChartUtils;
import org.opennms.netmgt.config.charts.BarChart;

import java.util.Iterator;

/**
 * This class represents the configuration window for charts dashlets.
 *
 * @author Christian Pape
 */
public class ChartsConfigurationWindow extends DashletConfigurationWindow {
    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;
    /**
     * The field for storing the 'boostSeverity' parameter
     */
    private CheckBox m_maximizeWidth, m_maximizeHeight;
    private NativeSelect m_chartSelect;

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link DashletSpec} to be edited
     */
    public ChartsConfigurationWindow(DashletSpec dashletSpec) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;

        /**
         * Setting up the base layouts
         */

        setHeight(230, Unit.PIXELS);
        setWidth(40, Unit.PERCENTAGE);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setWidth(100, Unit.PERCENTAGE);
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        /**
         * Adding the checkboxes
         */
        m_maximizeWidth = new CheckBox();
        m_maximizeWidth.setCaption("Maximize width");

        m_maximizeHeight = new CheckBox();
        m_maximizeHeight.setCaption("Maximize width");

        String maximizeWidthString = m_dashletSpec.getParameters().get("maximizeWidth");
        String maximizeHeightString = m_dashletSpec.getParameters().get("maximizeHeight");

        boolean maximizeHeight = ("true".equals(maximizeHeightString) || "yes".equals(maximizeHeightString) || "1".equals(maximizeHeightString));
        boolean maximizeWidth = ("true".equals(maximizeWidthString) || "yes".equals(maximizeWidthString) || "1".equals(maximizeWidthString));

        m_maximizeWidth.setValue(maximizeWidth);
        m_maximizeHeight.setValue(maximizeHeight);

        verticalLayout.addComponent(m_maximizeWidth);
        verticalLayout.addComponent(m_maximizeHeight);

        m_chartSelect = new NativeSelect();
        m_chartSelect.setCaption("Chart");
        m_chartSelect.setNullSelectionAllowed(false);
        m_chartSelect.setInvalidAllowed(false);
        m_chartSelect.setNewItemsAllowed(false);

        String firstChartName = null;

        try {
            Iterator<BarChart> it = ChartUtils.getChartCollectionIterator();

            while (it.hasNext()) {
                BarChart chartConfig = (BarChart) it.next();

                if (firstChartName == null) {
                    firstChartName = chartConfig.getName();
                }

                m_chartSelect.addItem(chartConfig.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String chartName = m_dashletSpec.getParameters().get("chart");

        if (chartName == null || "".equals(chartName)) {
            chartName = firstChartName;
        }

        m_chartSelect.setValue(chartName);


        verticalLayout.addComponent(m_chartSelect);

        /**
         * Using an additional {@link com.vaadin.ui.HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();

        buttonLayout.setMargin(true);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("100%");
        /**
         * Adding the cancel button...
         */
        Button cancel = new Button("Cancel");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        buttonLayout.addComponent(cancel);
        buttonLayout.setExpandRatio(cancel, 1.0f);
        buttonLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");

        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                m_dashletSpec.getParameters().put("maximizeWidth", (m_maximizeWidth.getValue() ? "true" : "false"));
                m_dashletSpec.getParameters().put("maximizeHeight", (m_maximizeHeight.getValue() ? "true" : "false"));
                m_dashletSpec.getParameters().put("chart", String.valueOf(m_chartSelect.getValue()));

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        buttonLayout.addComponent(ok);

        /**
         * Adding the layout and setting the content
         */
        verticalLayout.addComponent(buttonLayout);

        setContent(verticalLayout);
    }
}
