package org.opennms.features.vaadin.surveillanceviews.ui;

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

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.model.SurveillanceViewConfiguration;
import org.opennms.features.vaadin.surveillanceviews.model.View;

import java.util.List;

public class SurveillanceViewsConfigList extends VerticalLayout {
    private VerticalLayout m_verticalLayout = new VerticalLayout();
    private SurveillanceViewConfiguration m_surveillanceViewConfiguration = SurveillanceViewProvider.getInstance().getSurveillanceViewConfiguration();

    public SurveillanceViewsConfigList() {

        HorizontalLayout upperHorizontalLayout = new HorizontalLayout();
        Label label = new Label("Surveillance views configurations");
        label.addStyleName("configuration-title");
        upperHorizontalLayout.addComponent(label);

        upperHorizontalLayout.addComponent(label);
        Button helpButton = new Button("Help");
        helpButton.setDescription("Display help and usage");

        helpButton.setStyleName("small");

/*
        helpButton.addClickListener();
*/
        upperHorizontalLayout.addComponent(helpButton);
        upperHorizontalLayout.setWidth(100, Unit.PERCENTAGE);

        upperHorizontalLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        upperHorizontalLayout.setComponentAlignment(helpButton, Alignment.MIDDLE_RIGHT);

        addComponent(upperHorizontalLayout);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        final Button addButton = new Button("Add surveillance view");

        addButton.setStyleName("small");
        addButton.setDescription("Add a new dashlet instance");

        addButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent clickEvent) {
                View view = new View();

                if (!m_surveillanceViewConfiguration.getViews().contains(view)) {
                    m_surveillanceViewConfiguration.getViews().add(view);

                    SurveillanceViewProvider.getInstance().save();
                }

                m_verticalLayout.addComponent(new SurveillanceViewConfigEntry(view));
            }
        });

        List<View> views = m_surveillanceViewConfiguration.getViews();

        for (View view : views) {
            m_verticalLayout.addComponent(new SurveillanceViewConfigEntry(view));
        }

/**
 * Adding the layout components to this component
 */
        FormLayout formLayout = new FormLayout();
        formLayout.addComponent(addButton);
        horizontalLayout.addComponent(formLayout);

        addComponent(horizontalLayout);
        addComponent(m_verticalLayout);

    }
}
