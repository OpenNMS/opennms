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
package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.FieldEvents;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.features.vaadin.dashboard.model.Wallboard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a component for editing {@link Wallboard} configurations.
 *
 * @author Christian Pape
 */
public class WallboardEditor extends VerticalLayout {
    /**
     * The {@link TabSheet.Tab} object used in the {@link WallboardConfigView} instance to represent this view
     */
    private TabSheet.Tab m_tab;
    /**
     * The {@link VerticalLayout} instance used to display the {@link DashletSpecEditor} views
     */
    private VerticalLayout m_verticalLayout = new VerticalLayout();
    /**
     * The {@link DashletSelector} used for querying configuration data
     */
    private DashletSelector m_dashletSelector;
    /**
     * The associated {@link Wallboard} instance
     */
    private Wallboard m_wallboard;
    /**
     * A map for holding the {@link DashletSpec} and {@link DashletSpecEditor} instances
     */
    private Map<DashletSpec, DashletSpecEditor> m_dashletSpecEditorMap = new HashMap<DashletSpec, DashletSpecEditor>();

    /**
     * Constructor used for instantiating a new object.
     *
     * @param dashletSelector the {@link DashletSelector} to be used
     * @param wallboard       the associated {@link Wallboard} instance
     */
    public WallboardEditor(DashletSelector dashletSelector, Wallboard wallboard) {
        /**
         * Setting the member fields
         */
        this.m_dashletSelector = dashletSelector;
        this.m_wallboard = wallboard;

        /**
         * Adding the {@link DashletSpec} instances
         */
        for (DashletSpec dashletSpec : wallboard.getDashletSpecs()) {
            addDashletSpec(dashletSpec);
        }

        /**
         * Setting up layout component and adding text field and button
         */
        setMargin(true);

        HorizontalLayout upperHorizontalLayout = new HorizontalLayout();
        Label label = new Label("Wallboard configuration");
        label.addStyleName("configuration-title");
        upperHorizontalLayout.addComponent(label);

        upperHorizontalLayout.addComponent(label);
        Button helpButton = new Button("Help");
        helpButton.setStyleName("small");
        helpButton.addClickListener(new HelpClickListener(this, m_dashletSelector));

        upperHorizontalLayout.addComponent(helpButton);
        upperHorizontalLayout.setWidth(100, Unit.PERCENTAGE);

        upperHorizontalLayout.setComponentAlignment(label, Alignment.MIDDLE_LEFT);
        upperHorizontalLayout.setComponentAlignment(helpButton, Alignment.MIDDLE_RIGHT);

        addComponent(upperHorizontalLayout);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        final Button addButton = new Button("Add dashlet");

        addButton.setStyleName("small");

        addButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent clickEvent) {
                addDashletSpec(new DashletSpec());
            }
        });

        final TextField titleField = new TextField();
        titleField.setValue(wallboard.getTitle());
        titleField.setImmediate(true);
        titleField.addValidator(new AbstractStringValidator("Title must be unique") {
            @Override
            protected boolean isValidValue(String s) {
                return (!WallboardProvider.getInstance().containsWallboard(s) || WallboardProvider.getInstance().getWallboard(s).equals(m_wallboard)) && !"".equals(s);
            }
        });

        titleField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
                AbstractTextField source = (AbstractTextField) textChangeEvent.getSource();
                source.setValue(textChangeEvent.getText());
                if (source.isValid()) {
                    m_tab.setCaption(textChangeEvent.getText());
                    m_wallboard.setTitle(textChangeEvent.getText());
                    WallboardProvider.getInstance().save();
                    ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Title");
                }
            }
        });

        titleField.setCaption("Title");

        final Button previewButton = new Button("Preview");
        previewButton.setStyleName("small");
        previewButton.addClickListener(new PreviewClickListener(this, m_wallboard));

        /**
         * Adding the layout components to this component
         */
        FormLayout formLayout1 = new FormLayout();
        formLayout1.addComponent(titleField);
        horizontalLayout.addComponent(formLayout1);

        FormLayout formLayout2 = new FormLayout();
        formLayout2.addComponent(addButton);
        horizontalLayout.addComponent(formLayout2);

        FormLayout formLayout3 = new FormLayout();
        formLayout3.addComponent(previewButton);
        horizontalLayout.addComponent(formLayout3);

        addComponent(horizontalLayout);
        addComponent(m_verticalLayout);
    }

    /**
     * Method used for updating the {@link DashletFactory} list
     *
     * @param serviceList the available {@link DashletFactory} instances
     */
    public void updateServiceList(List<DashletFactory> serviceList) {
        for (DashletSpecEditor dashletSpecEditor : m_dashletSpecEditorMap.values()) {
            dashletSpecEditor.updateDashletSelection(serviceList);
        }
        ((WallboardConfigUI) getUI()).notifyMessage("Configuration change", "Dashlet list modified");
    }

    /**
     * Returns the associated {@link Wallboard} instance.
     *
     * @return the associated {@link Wallboard} instance
     */
    public Wallboard getWallboard() {
        return m_wallboard;
    }

    /**
     * Method to set the {@link TabSheet.Tab} this view belongs to.
     *
     * @param tab the {@link TabSheet.Tab} to be set
     */
    public void setTab(TabSheet.Tab tab) {
        m_tab = tab;
    }

    /**
     * This method removes the given {@link DashletSpecEditor}.
     *
     * @param dashletSpecEditor the {@link DashletSpecEditor} to be removed
     */
    public void removeDashletSpecEditor(DashletSpecEditor dashletSpecEditor) {
        m_verticalLayout.removeComponent(dashletSpecEditor);
        m_dashletSpecEditorMap.remove(dashletSpecEditor.getDashletSpec());
        m_wallboard.getDashletSpecs().remove(dashletSpecEditor.getDashletSpec());

        WallboardProvider.getInstance().save();
    }

    /**
     * This method adds a given {@link DashletSpec}.
     *
     * @param dashletSpec the {@link DashletSpec} to be added
     */
    private void addDashletSpec(DashletSpec dashletSpec) {
        DashletSpecEditor dashletSpecEditor = new DashletSpecEditor(this, m_dashletSelector, dashletSpec);

        m_dashletSpecEditorMap.put(dashletSpec, dashletSpecEditor);
        m_verticalLayout.addComponent(dashletSpecEditor);

        if (!m_wallboard.getDashletSpecs().contains(dashletSpec)) {
            m_wallboard.getDashletSpecs().add(dashletSpec);

            WallboardProvider.getInstance().save();
        }
    }
}
