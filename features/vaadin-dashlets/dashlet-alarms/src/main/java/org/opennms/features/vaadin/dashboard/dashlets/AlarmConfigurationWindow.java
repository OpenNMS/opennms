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
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderComponent;
import org.opennms.features.vaadin.dashboard.config.ui.editors.CriteriaBuilderHelper;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.model.*;

/**
 * This class represents the configuration window for alarm dashlets.
 *
 * @author Christian Pape
 */
public class AlarmConfigurationWindow extends DashletConfigurationWindow {
    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;
    /**
     * The field for storing the 'boostSeverity' parameter
     */
    private NativeSelect m_boostedSeveritySelect;

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link DashletSpec} to be edited
     */
    public AlarmConfigurationWindow(DashletSpec dashletSpec) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;

        /**
         * Setting up the base layouts
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setHeight(100, Unit.PERCENTAGE);
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        /**
         * Adding the selection box
         */
        m_boostedSeveritySelect = new NativeSelect();
        m_boostedSeveritySelect.setCaption("Boosted Severity");
        m_boostedSeveritySelect.setMultiSelect(false);
        m_boostedSeveritySelect.setNullSelectionAllowed(false);
        m_boostedSeveritySelect.setInvalidAllowed(false);
        m_boostedSeveritySelect.setNewItemsAllowed(false);

        for (OnmsSeverity onmsSeverity : OnmsSeverity.values()) {
            m_boostedSeveritySelect.addItem(onmsSeverity.name());
        }

        String boostSeverity = m_dashletSpec.getParameters().get("boostSeverity");

        if (boostSeverity == null || "".equals(boostSeverity)) {
            boostSeverity = OnmsSeverity.CLEARED.name();
        }

        m_boostedSeveritySelect.setValue(boostSeverity);

        verticalLayout.addComponent(m_boostedSeveritySelect);

        /**
         * Setting up the {@link CriteriaBuilderComponent} component
         */
        CriteriaBuilderHelper criteriaBuilderHelper = new CriteriaBuilderHelper(OnmsAlarm.class, OnmsNode.class, OnmsEvent.class, OnmsCategory.class);

        final CriteriaBuilderComponent criteriaBuilderComponent = new CriteriaBuilderComponent(criteriaBuilderHelper, m_dashletSpec.getParameters().get("criteria"));

        verticalLayout.addComponent(criteriaBuilderComponent);
        verticalLayout.setExpandRatio(criteriaBuilderComponent, 1.0f);

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
                m_dashletSpec.getParameters().put("criteria", criteriaBuilderComponent.getCriteria());
                m_dashletSpec.getParameters().put("boostSeverity", String.valueOf(m_boostedSeveritySelect.getValue()));

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
