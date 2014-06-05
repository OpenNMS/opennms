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

package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;

import java.util.Map;

/**
 * This class is used to display a custom configuration window for the Ksc dashlet.
 *
 * @author Christian Pape
 */
public class KscDashletConfigurationWindow extends DashletConfigurationWindow {

    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;
    /**
     * The field for storing the 'boostSeverity' parameter
     */
    private NativeSelect m_kscSelect;

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link org.opennms.features.vaadin.dashboard.model.DashletSpec} to be edited
     */
    public KscDashletConfigurationWindow(DashletSpec dashletSpec) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;

        setHeight(210, Unit.PIXELS);
        setWidth(40, Unit.PERCENTAGE);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setMargin(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setWidth(100, Unit.PERCENTAGE);
        formLayout.setSpacing(true);
        formLayout.setMargin(true);

        m_kscSelect = new NativeSelect();
        m_kscSelect.setDescription("Select KSC-report to be displayed");
        m_kscSelect.setCaption("KSC-Report");
        m_kscSelect.setImmediate(true);
        m_kscSelect.setNewItemsAllowed(false);
        m_kscSelect.setMultiSelect(false);
        m_kscSelect.setInvalidAllowed(false);
        m_kscSelect.setNullSelectionAllowed(false);
        m_kscSelect.setImmediate(true);

        final KSC_PerformanceReportFactory kscPerformanceReportFactory = KSC_PerformanceReportFactory.getInstance();

        Map<Integer, String> reportsMap = kscPerformanceReportFactory.getReportList();

        for (Map.Entry<Integer, String> entry : reportsMap.entrySet()) {
            m_kscSelect.addItem(entry.getKey());
            m_kscSelect.setItemCaption(entry.getKey(), entry.getValue());
            if (m_kscSelect.getValue() == null) {
                m_kscSelect.setValue(entry.getKey());
            }
        }

        String chartName = m_dashletSpec.getParameters().get("kscReport");

        if (chartName != null) {
            if (reportsMap.values().contains(chartName)) {
                m_kscSelect.setValue(chartName);
            }
        }

        formLayout.addComponent(m_kscSelect);

        m_kscSelect.setValue(chartName);
        m_kscSelect.setImmediate(true);

        horizontalLayout.addComponent(formLayout);

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
        cancel.setDescription("Cancel editing");
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
        ok.setDescription("Save properties and close");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Map<Integer, String> reportsMap = kscPerformanceReportFactory.getReportList();

                m_dashletSpec.getParameters().put("kscReport", reportsMap.get(m_kscSelect.getValue()));

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        buttonLayout.addComponent(ok);

        if (reportsMap.size() == 0) {
            m_kscSelect.setEnabled(false);
            ok.setEnabled(false);
        }

        /**
         * Adding the layout and setting the content
         */

        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(buttonLayout);

        setContent(verticalLayout);
    }
}
