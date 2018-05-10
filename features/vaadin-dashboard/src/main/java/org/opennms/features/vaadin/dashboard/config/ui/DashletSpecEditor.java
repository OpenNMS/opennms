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

package org.opennms.features.vaadin.dashboard.config.ui;

import com.vaadin.data.Property;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import org.opennms.features.vaadin.dashboard.config.DashletSelector;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletFactory;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.features.vaadin.dashboard.model.Wallboard;
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
     * Title textfield
     */
    private TextField m_titleField;
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
        gridLayout.setColumns(6);
        gridLayout.setRows(1);
        gridLayout.setMargin(true);

        /**
         * Priority field setup, layout and adding listener and validator
         */
        final TextField priorityField = new TextField();
        priorityField.setValue(String.valueOf(dashletSpec.getPriority()));
        priorityField.setImmediate(true);
        priorityField.setCaption("Priority");
        priorityField.setDescription("Priority of this dashlet");

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
        boostPriorityField.setDescription("Boost priority of this dashlet");

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
        durationField.setId("opsboard.duration");
        durationField.setValue(String.valueOf(dashletSpec.getDuration()));
        durationField.setImmediate(true);
        durationField.setCaption("Duration");
        durationField.setDescription("Duration for this dashlet");

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
        boostDurationField.setDescription("Boost duration for this dashlet");

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

        boolean boostable = m_dashletSelector.getDashletFactoryForName(m_dashletSpec.getDashletName()).isBoostable();

        boostPriorityField.setEnabled(boostable);
        boostDurationField.setEnabled(boostable);

        /**
         * Setting up the dashlet selection
         */

        m_dashletSelect = new NativeSelect();
        m_dashletSelect.setId("opsboard.type");
        m_dashletSelect.setCaption("Dashlet");

        updateDashletSelection(dashletSelector.getDashletFactoryList());

        m_dashletSelect.setImmediate(true);
        m_dashletSelect.setNewItemsAllowed(false);
        m_dashletSelect.setNullSelectionItemId("Undefined");
        m_dashletSelect.setNullSelectionAllowed(false);
        m_dashletSelect.select(dashletSpec.getDashletName());
        m_dashletSelect.setDescription("Dashlet selection");

        m_dashletSelect.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                if (m_savingDisabled) {
                    return;
                }

                if (valueChangeEvent.getProperty().getValue() == null) {
                    m_dashletSpec.setDashletName("Undefined");
                } else {
                    m_dashletSpec.setDashletName(valueChangeEvent.getProperty().getValue().toString());
                    m_dashletSelect.removeItem("Undefined");
                }

                m_dashletSpec.getParameters().clear();

                Map<String, String> requiredParameters = m_dashletSelector.getDashletFactoryForName(m_dashletSpec.getDashletName()).getRequiredParameters();

                for (Map.Entry<String, String> entry : requiredParameters.entrySet()) {
                    m_dashletSpec.getParameters().put(entry.getKey(), entry.getValue());
                }

                m_propertiesButton.setEnabled(requiredParameters.size() > 0);

                boolean boostable = m_dashletSelector.getDashletFactoryForName(m_dashletSpec.getDashletName()).isBoostable();

                boostPriorityField.setEnabled(boostable);
                boostDurationField.setEnabled(boostable);

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Dashlet");
            }
        });

        m_titleField = new TextField();
        m_titleField.setId("opsboard.title");
        m_titleField.setValue(dashletSpec.getTitle());
        m_titleField.setImmediate(true);
        m_titleField.setCaption("Title");
        m_titleField.setDescription("Title for this dashlet instance");

        m_titleField.addValueChangeListener(new Property.ValueChangeListener() {
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                m_dashletSpec.setTitle((String) valueChangeEvent.getProperty().getValue());
                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Title");
            }
        });

        FormLayout f1 = new FormLayout();
        f1.addComponent(m_dashletSelect);
        f1.addComponent(m_titleField);

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
        m_propertiesButton.setDescription("Open properties dialog for this dashlet");

        /**
         * ...and the remove button
         */
        Button removeButton = new Button("Remove");
        removeButton.setDescription("Remove this dashlet entry");

        FormLayout f4 = new FormLayout();
        f4.addComponent(m_propertiesButton);
        f4.addComponent(removeButton);

        removeButton.addClickListener(new Button.ClickListener() {
            public void buttonClick(Button.ClickEvent clickEvent) {
                m_wallboardEditor.removeDashletSpecEditor(DashletSpecEditor.this);
            }
        });

        removeButton.setStyleName("small");

        Button upButton = new Button();
        upButton.setStyleName("small");
        upButton.setIcon(new ThemeResource("../runo/icons/16/arrow-up.png"));
        upButton.setDescription("Move this a dashlet entry one position up");

        Button downButton = new Button();
        downButton.setStyleName("small");
        downButton.setIcon(new ThemeResource("../runo/icons/16/arrow-down.png"));
        downButton.setDescription("Move this a dashlet entry one position down");

        FormLayout f5 = new FormLayout();
        f5.addComponent(upButton);
        f5.addComponent(downButton);

        Button previewButton = new Button("Preview");
        previewButton.setStyleName("small");
        previewButton.setDescription("Preview this single dashlet entry");

        Wallboard wallboard = new Wallboard();
        wallboard.getDashletSpecs().add(m_dashletSpec);

        previewButton.addClickListener(new PreviewClickListener(this, wallboard));

        FormLayout f6 = new FormLayout();
        f6.addComponent(previewButton);

        upButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                m_wallboardEditor.swapDashletSpec(m_dashletSpec, -1);
            }
        });

        downButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                m_wallboardEditor.swapDashletSpec(m_dashletSpec, +1);
            }
        });

        /**
         * Adding the different {@link FormLayout} instances to a {@link GridLayout}
         */
        f1.setMargin(true);
        f2.setMargin(true);
        f3.setMargin(true);
        f4.setMargin(true);
        f5.setMargin(true);
        f6.setMargin(true);

        gridLayout.addComponent(f1);
        gridLayout.addComponent(f2);
        gridLayout.addComponent(f3);
        gridLayout.addComponent(f4);
        gridLayout.addComponent(f5);
        gridLayout.addComponent(f6);

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
            if (!"Undefined".equals(dashletFactory.getName())) {
                m_dashletSelect.addItem(dashletFactory.getName());
            }
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
