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

import com.vaadin.data.Property;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * The editor component used for editing a single {@link DashletSpec} entry inside a {@link WallboardEditor}.
 *
 * @author Christian Pape
 */
public class DashletSpecEditor extends Panel {
    /**
     * The {@link DashletSpec} instance this editor component is associated with.
     */
    private DashletSpec m_dashletSpec;

    /**
     * The {@link WallboardEditor} instance this component belongs to.
     */
    private WallboardEditor m_wallboardEditor;

    /**
     * The {@link NativeSelect} instance for selecting available dashlet factories.
     */
    private NativeSelect m_dashletSelect;

    /**
     * Helper variable for disabling saving of data.
     */
    private boolean m_savingDisabled = false;

    /**
     * The used {@link DashletSelector} used for querying available dashlet factories
     */
    private DashletSelector m_dashletSelector;

    /**
     * The button used for opening the properties window.
     */
    private Button m_propertiesButton;

    /**
     * Constructor for the DashletSpecEditor.
     *
     * @param wallboardEditor the {@link WallboardEditor} wallboard editor this editor belongs to
     * @param dashletSelector the {@link DashletSelector} used to query available {@link DashletFactory} instances
     * @param dashletSpec     the associated {@link DashletSpec} instance
     */
    public DashletSpecEditor(WallboardEditor wallboardEditor, DashletSelector dashletSelector, DashletSpec dashletSpec) {
        /**
         * Setting the member fields
         */
        this.m_wallboardEditor = wallboardEditor;
        this.m_dashletSpec = dashletSpec;
        this.m_dashletSelector = dashletSelector;

        /**
         * Setting defaults
         */

        DashletFactory dashletFactory = dashletSelector.getDashletFactoryForName(dashletSpec.getDashletName());

        final Map<String, String> requiredParameters = dashletFactory.getRequiredParameters();

        for (Map.Entry<String, String> entry : requiredParameters.entrySet()) {
            if (!dashletSpec.getParameters().containsKey(entry.getKey())) {
                dashletSpec.getParameters().put(entry.getKey(), requiredParameters.get(entry.getKey()));
            }
        }

        /**
         * Setting up this component with size and layout
         */
        setWidth(100.0f, Unit.PERCENTAGE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.setColumns(4);
        gridLayout.setRows(1);

        gridLayout.setMargin(true);

        /**
         * Setting up the dashlet selection
         */

        m_dashletSelect = new NativeSelect();

        m_dashletSelect.setCaption("Dashlet");

        updateDashletSelection(dashletSelector.getDashletFactoryList());

        m_dashletSelect.setImmediate(true);
        m_dashletSelect.setNewItemsAllowed(false);
        m_dashletSelect.setNullSelectionItemId("Undefined");
        m_dashletSelect.select(dashletSpec.getDashletName());

        m_dashletSelect.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (m_savingDisabled) {
                    return;
                }

                if (valueChangeEvent.getProperty().getValue() == null) {
                    m_dashletSpec.setDashletName("Undefined");
                } else {
                    m_dashletSpec.setDashletName(valueChangeEvent.getProperty().getValue().toString());
                }

                m_dashletSpec.getParameters().clear();

                Map<String, String> requiredParameters = m_dashletSelector.getDashletFactoryForName(m_dashletSpec.getDashletName()).getRequiredParameters();

                for (Map.Entry<String, String> entry : requiredParameters.entrySet()) {
                    m_dashletSpec.getParameters().put(entry.getKey(), entry.getValue());
                }

                m_propertiesButton.setEnabled(requiredParameters.size() > 0);

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Dashlet");
            }
        });

        FormLayout f1 = new FormLayout();
        f1.addComponent(m_dashletSelect);

        /**
         * Priority field setup, layout and adding listener and validator
         */
        final TextField priorityField = new TextField();
        priorityField.setValue(String.valueOf(dashletSpec.getPriority()));
        priorityField.setImmediate(true);
        priorityField.setCaption("Priority");

        priorityField.addValidator(new AbstractStringValidator("Only numbers allowed here") {
            @Override
            protected boolean isValidValue(String s) {
                try {
                    Integer.parseInt(s);
                } catch (NumberFormatException numberFormatException) {
                    return false;
                }
                return true;
            }
        });

        priorityField.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (priorityField.isValid()) {
                    m_dashletSpec.setPriority(Integer.valueOf((String) valueChangeEvent.getProperty().getValue()));
                    WallboardProvider.getInstance().save();
                    ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Priority");
                }
            }
        });

        /**
         * Boost priority field setup, layout and adding listener and validator
         */
        final TextField boostPriorityField = new TextField();
        boostPriorityField.setValue(String.valueOf(dashletSpec.getBoostPriority()));
        boostPriorityField.setImmediate(true);
        boostPriorityField.setCaption("Boost-Priority");

        boostPriorityField.addValidator(new AbstractStringValidator("Only numbers allowed here") {
            @Override
            protected boolean isValidValue(String s) {
                try {
                    Integer.parseInt(s);
                } catch (NumberFormatException numberFormatException) {
                    return false;
                }
                return true;
            }
        });

        boostPriorityField.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (boostPriorityField.isValid()) {
                    m_dashletSpec.setBoostPriority(Integer.valueOf((String) valueChangeEvent.getProperty().getValue()));
                    WallboardProvider.getInstance().save();
                    ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Priority");
                }
            }
        });


        /**
         * Duration field setup, layout and adding listener and validator
         */
        final TextField durationField = new TextField();
        durationField.setValue(String.valueOf(dashletSpec.getDuration()));
        durationField.setImmediate(true);
        durationField.setCaption("Duration");

        durationField.addValidator(new AbstractStringValidator("Only numbers allowed here") {
            @Override
            protected boolean isValidValue(String s) {
                try {
                    Integer.parseInt(s);
                } catch (NumberFormatException numberFormatException) {
                    return false;
                }
                return true;
            }
        });

        durationField.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (durationField.isValid()) {
                    m_dashletSpec.setDuration(Integer.valueOf((String) valueChangeEvent.getProperty().getValue()));
                    WallboardProvider.getInstance().save();
                    ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Duration");
                }
            }
        });

        /**
         * Boost duration field setup, layout and adding listener and validator
         */
        final TextField boostDurationField = new TextField();
        boostDurationField.setValue(String.valueOf(dashletSpec.getBoostDuration()));
        boostDurationField.setImmediate(true);
        boostDurationField.setCaption("Boost-Duration");

        boostDurationField.addValidator(new AbstractStringValidator("Only numbers allowed here") {
            @Override
            protected boolean isValidValue(String s) {
                try {
                    Integer.parseInt(s);
                } catch (NumberFormatException numberFormatException) {
                    return false;
                }
                return true;
            }
        });

        boostDurationField.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (boostDurationField.isValid()) {
                    m_dashletSpec.setBoostDuration(Integer.valueOf((String) valueChangeEvent.getProperty().getValue()));
                    WallboardProvider.getInstance().save();
                    ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Duration");
                }
            }
        });

        /**
         * Adding the required input fields and buttons to several {@link FormLayout} instances for better layout.
         */
        FormLayout f2 = new FormLayout();
        f2.addComponent(priorityField);
        f2.addComponent(durationField);

        FormLayout f3 = new FormLayout();
        f3.addComponent(boostPriorityField);
        f3.addComponent(boostDurationField);

        /**
         * Adding the properties button...
         */
        m_propertiesButton = new Button("Properties");

        m_propertiesButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                DashletConfigurationWindow configurationWindow = m_dashletSelector.getDashletFactoryForName(m_dashletSpec.getDashletName()).configurationWindow(m_dashletSpec);
                getUI().addWindow(configurationWindow);
            }
        });

        m_propertiesButton.setEnabled(m_dashletSelector.getDashletFactoryForName(m_dashletSpec.getDashletName()).getRequiredParameters().size() > 0);

        m_propertiesButton.setStyleName("small");

        /**
         * ...and the remove button
         */
        Button removeButton = new Button("Remove");

        FormLayout f4 = new FormLayout();
        f4.addComponent(m_propertiesButton);
        f4.addComponent(removeButton);

        removeButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent clickEvent) {
                m_wallboardEditor.removeDashletSpecEditor(DashletSpecEditor.this);
            }
        });

        removeButton.setStyleName("small");

        /**
         * Adding the different {@link FormLayout} instances to a {@link GridLayout}
         */
        gridLayout.addComponent(f1);
        gridLayout.addComponent(f2);
        gridLayout.addComponent(f3);
        gridLayout.addComponent(f4);

        setContent(gridLayout);
    }

    /**
     * Method for updating the {@link NativeSelect} instance to display the available {@link DashletFactory} instances.
     *
     * @param factoryList the list of available {@link DashletFactory} instances
     */
    public void updateDashletSelection(List<DashletFactory> factoryList) {
        m_savingDisabled = true;

        String savedSelection = (m_dashletSelect.getValue() == null ? "Undefined" : m_dashletSelect.getValue().toString());

        if (!m_dashletSelect.removeAllItems()) {
            LoggerFactory.getLogger(DashletSpecEditor.class).warn("problem removing items");
        }

        for (DashletFactory dashletFactory : factoryList) {
            m_dashletSelect.addItem(dashletFactory.getName());
        }

        m_dashletSelect.select(savedSelection);

        m_savingDisabled = false;
    }

    /**
     * Returns the associated {@link DashletSpec} of this editor component.
     *
     * @return the {@link DashletSpec} instance
     */
    public DashletSpec getDashletSpec() {
        return m_dashletSpec;
    }
}
