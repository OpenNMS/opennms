/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.bsm.service.BusinessServiceSearchCriteriaBuilder;
import org.opennms.netmgt.bsm.service.model.Status;

import com.vaadin.data.Property;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class BSMConfigurationWindow extends DashletConfigurationWindow {
    /**
     * The {@link DashletSpec} to be used
     */
    private DashletSpec m_dashletSpec;
    /**
     * The fields for storing the parameters
     */
    private CheckBox m_filterByNameCheckBox, m_filterByAttributeCheckBox, m_filterBySeverityCheckBox;
    private TextField m_nameTextField, m_attributeKeyTextField, m_attributeValueTextField, m_limitTextField, m_columnCountBoardTextField, m_columnCountPanelTextField;
    private NativeSelect m_severitySelect;
    private NativeSelect m_compareOperatorSelect;
    private NativeSelect m_orderBy;
    private NativeSelect m_orderSequence;

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link DashletSpec} to be edited
     */
    public BSMConfigurationWindow(DashletSpec dashletSpec) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;

        /**
         * Setting up the base layouts
         */

        setHeight(91, Unit.PERCENTAGE);
        setWidth(60, Unit.PERCENTAGE);

        /**
         * Retrieve the config...
         */

        boolean filterByName = BSMConfigHelper.getBooleanForKey(getDashletSpec().getParameters(), "filterByName");
        String nameValue = BSMConfigHelper.getStringForKey(getDashletSpec().getParameters(), "nameValue","");
        boolean filterByAttribute = BSMConfigHelper.getBooleanForKey(getDashletSpec().getParameters(), "filterByAttribute");
        String attributeKey = BSMConfigHelper.getStringForKey(getDashletSpec().getParameters(), "attributeKey","");
        String attributeValue = BSMConfigHelper.getStringForKey(getDashletSpec().getParameters(), "attributeValue","");
        boolean filterBySeverity = BSMConfigHelper.getBooleanForKey(getDashletSpec().getParameters(), "filterBySeverity");
        String severityValue = BSMConfigHelper.getStringForKey(getDashletSpec().getParameters(), "severityValue",Status.WARNING.name());
        String severityCompareOperator = BSMConfigHelper.getStringForKey(getDashletSpec().getParameters(), "severityCompareOperator",BusinessServiceSearchCriteriaBuilder.CompareOperator.GreaterOrEqual.name());
        String orderBy = BSMConfigHelper.getStringForKey(getDashletSpec().getParameters(), "orderBy", BusinessServiceSearchCriteriaBuilder.Order.Name.name());
        String orderSequence = BSMConfigHelper.getStringForKey(getDashletSpec().getParameters(), "orderSequence",BusinessServiceSearchCriteriaBuilder.Sequence.Ascending.name());
        int resultsLimit = BSMConfigHelper.getIntForKey(getDashletSpec().getParameters(), "resultsLimit", 10);
        int columnCountBoard = BSMConfigHelper.getIntForKey(getDashletSpec().getParameters(), "columnCountBoard", 10);
        int columnCountPanel = BSMConfigHelper.getIntForKey(getDashletSpec().getParameters(), "columnCountPanel", 5);

        /**
         * Adding the "Filter By Name" panel
         */

        m_filterByNameCheckBox = new CheckBox();
        m_filterByNameCheckBox.setCaption("Enable");
        m_filterByNameCheckBox.setDescription("Filter by Business Service name");

        VerticalLayout nameLayout = new VerticalLayout();
        nameLayout.setSpacing(true);
        nameLayout.setMargin(true);
        nameLayout.setSizeFull();

        m_nameTextField = new TextField("Name (REGEXP)");
        m_nameTextField.setEnabled(false);

        addToComponent(nameLayout, m_filterByNameCheckBox);
        addToComponent(nameLayout, m_nameTextField);

        Panel namePanel = new Panel();
        namePanel.setCaption("Filter by Name");
        namePanel.setContent(nameLayout);

        m_filterByNameCheckBox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                m_nameTextField.setEnabled(m_filterByNameCheckBox.getValue());
            }
        });

        m_nameTextField.setValue(nameValue);
        m_filterByNameCheckBox.setValue(filterByName);

        /**
         * Adding the "Filter By Attribute" panel
         */

        m_filterByAttributeCheckBox = new CheckBox();
        m_filterByAttributeCheckBox.setCaption("Enable");
        m_filterByAttributeCheckBox.setDescription("Filter by Business Service attribute");

        VerticalLayout attributeLayout = new VerticalLayout();
        attributeLayout.setSpacing(true);
        attributeLayout.setMargin(true);
        attributeLayout.setSizeFull();

        m_attributeKeyTextField = new TextField("Key");
        m_attributeKeyTextField.setEnabled(false);
        m_attributeValueTextField = new TextField("Value (REGEXP)");
        m_attributeValueTextField.setEnabled(false);
        addToComponent(attributeLayout, m_filterByAttributeCheckBox);
        addToComponent(attributeLayout, m_attributeKeyTextField);
        addToComponent(attributeLayout, m_attributeValueTextField);

        Panel attributePanel = new Panel();
        attributePanel.setCaption("Filter by Attribute");
        attributePanel.setContent(attributeLayout);

        m_filterByAttributeCheckBox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                m_attributeKeyTextField.setEnabled(m_filterByAttributeCheckBox.getValue());
                m_attributeValueTextField.setEnabled(m_filterByAttributeCheckBox.getValue());
            }
        });

        m_attributeKeyTextField.setValue(attributeKey);
        m_attributeValueTextField.setValue(attributeValue);
        m_filterByAttributeCheckBox.setValue(filterByAttribute);

        /**
         * Adding the "Filter By Severity" panel
         */

        m_filterBySeverityCheckBox = new CheckBox();
        m_filterBySeverityCheckBox.setCaption("Enable");
        m_filterBySeverityCheckBox.setDescription("Filter by Business Service severity");

        VerticalLayout severityLayout = new VerticalLayout();
        severityLayout.setSpacing(true);
        severityLayout.setMargin(true);
        severityLayout.setSizeFull();

        m_severitySelect = new NativeSelect("Severity");
        m_severitySelect.setEnabled(false);
        m_severitySelect.setNullSelectionAllowed(false);
        m_severitySelect.setMultiSelect(false);

        for (Status eachStatus : Status.values()) {
            m_severitySelect.addItem(eachStatus.name());
        }

        m_compareOperatorSelect = new NativeSelect("Comparator");
        m_compareOperatorSelect.setEnabled(false);
        m_compareOperatorSelect.setNullSelectionAllowed(false);
        m_compareOperatorSelect.setMultiSelect(false);

        m_compareOperatorSelect.addItem(BusinessServiceSearchCriteriaBuilder.CompareOperator.Lower.name());
        m_compareOperatorSelect.addItem(BusinessServiceSearchCriteriaBuilder.CompareOperator.LowerOrEqual.name());
        m_compareOperatorSelect.addItem(BusinessServiceSearchCriteriaBuilder.CompareOperator.Equal.name());
        m_compareOperatorSelect.addItem(BusinessServiceSearchCriteriaBuilder.CompareOperator.GreaterOrEqual.name());
        m_compareOperatorSelect.addItem(BusinessServiceSearchCriteriaBuilder.CompareOperator.Greater.name());

        addToComponent(severityLayout, m_filterBySeverityCheckBox);
        addToComponent(severityLayout, m_severitySelect);
        addToComponent(severityLayout, m_compareOperatorSelect);

        Panel severityPanel = new Panel();
        severityPanel.setCaption("Filter by Severity");
        severityPanel.setContent(severityLayout);

        m_filterBySeverityCheckBox.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                m_severitySelect.setEnabled(m_filterBySeverityCheckBox.getValue());
                m_compareOperatorSelect.setEnabled(m_filterBySeverityCheckBox.getValue());
            }
        });

        m_severitySelect.setValue(severityValue);
        m_compareOperatorSelect.setValue(severityCompareOperator);
        m_filterBySeverityCheckBox.setValue(filterBySeverity);

        /**
         * Adding the "Results" panel
         */

        VerticalLayout limitLayout = new VerticalLayout();
        limitLayout.setSpacing(true);
        limitLayout.setMargin(true);
        limitLayout.setSizeFull();

        m_limitTextField = new TextField("Limit");

        m_orderBy = new NativeSelect("Order by");
        m_orderBy.setNullSelectionAllowed(false);
        m_orderBy.setMultiSelect(false);

        m_orderBy.addItem(BusinessServiceSearchCriteriaBuilder.Order.Name.name());
        m_orderBy.addItem(BusinessServiceSearchCriteriaBuilder.Order.Severity.name());
        m_orderBy.addItem(BusinessServiceSearchCriteriaBuilder.Order.Level.name());

        m_orderSequence = new NativeSelect("Asc/Desc ");
        m_orderSequence.setNullSelectionAllowed(false);
        m_orderSequence.setMultiSelect(false);

        m_orderSequence.addItem("Ascending");
        m_orderSequence.addItem("Descending");

        m_columnCountBoardTextField = new TextField("Ops Board Column Count");
        m_columnCountBoardTextField.addValidator(new AbstractStringValidator("Number greater zero expected") {
            @Override
            protected boolean isValidValue(String value) {
                try {
                    int i = Integer.parseInt(value);
                    return i > 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        m_columnCountPanelTextField = new TextField("Ops Panel Column Count");
        m_columnCountPanelTextField.addValidator(new AbstractStringValidator("Number greater zero expected") {
            @Override
            protected boolean isValidValue(String value) {
                try {
                    int i = Integer.parseInt(value);
                    return i > 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        addToComponent(limitLayout, m_limitTextField);
        addToComponent(limitLayout, m_orderBy);
        addToComponent(limitLayout, m_orderSequence);
        addToComponent(limitLayout, m_columnCountBoardTextField);
        addToComponent(limitLayout, m_columnCountPanelTextField);

        Panel limitPanel = new Panel();
        limitPanel.setSizeFull();
        limitPanel.setCaption("Results");
        limitPanel.setContent(limitLayout);

        m_limitTextField.setValue(String.valueOf(resultsLimit));
        m_orderBy.setValue(orderBy);
        m_orderSequence.setValue(orderSequence);
        m_columnCountBoardTextField.setValue(String.valueOf(columnCountBoard));
        m_columnCountPanelTextField.setValue(String.valueOf(columnCountPanel));

        m_limitTextField.addValidator(new AbstractStringValidator("Number greater or equal zero expected") {
            @Override
            protected boolean isValidValue(String value) {
                try {
                    int i = Integer.parseInt(value);
                    return i >= 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        /**
         * Create the main layout...
         */

        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.setWidth(100, Unit.PERCENTAGE);
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        verticalLayout.addComponent(namePanel);
        verticalLayout.addComponent(attributePanel);

        HorizontalLayout bottomLayout = new HorizontalLayout(severityPanel, limitPanel);
        bottomLayout.setSpacing(true);
        bottomLayout.setSizeFull();
        bottomLayout.setWidth(100, Unit.PERCENTAGE);

        verticalLayout.addComponent(bottomLayout);

        /**
         * Using an additional {@link HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();

        buttonLayout.setMargin(true);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("100%");

        Label label = new Label("Note: Multiple enabled filter constraints will be combined by a logical AND.");
        buttonLayout.addComponent(label);
        buttonLayout.setExpandRatio(label, 1.0f);

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
        buttonLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");
        ok.setDescription("Save properties and close");
        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!m_limitTextField.isValid() || !m_columnCountPanelTextField.isValid() || !m_columnCountBoardTextField.isValid()) {
                    return;
                }

                m_dashletSpec.getParameters().put("filterByName", (m_filterByNameCheckBox.getValue() ? "true" : "false"));

                if (m_filterByNameCheckBox.getValue()) {
                    m_dashletSpec.getParameters().put("nameValue", m_nameTextField.getValue());
                } else {
                    m_dashletSpec.getParameters().put("nameValue", "");
                }

                m_dashletSpec.getParameters().put("filterByAttribute", (m_filterByAttributeCheckBox.getValue() ? "true" : "false"));

                if (m_filterByAttributeCheckBox.getValue()) {
                    m_dashletSpec.getParameters().put("attributeKey", m_attributeKeyTextField.getValue());
                    m_dashletSpec.getParameters().put("attributeValue", m_attributeValueTextField.getValue());
                } else {
                    m_dashletSpec.getParameters().put("attributeKey", "");
                    m_dashletSpec.getParameters().put("attributeValue", "");
                }

                m_dashletSpec.getParameters().put("filterBySeverity", (m_filterBySeverityCheckBox.getValue() ? "true" : "false"));

                if (m_filterBySeverityCheckBox.getValue() && m_severitySelect.getValue() != null) {
                    m_dashletSpec.getParameters().put("severityValue", m_severitySelect.getValue().toString());
                } else {
                    m_dashletSpec.getParameters().put("severityValue", Status.WARNING.getLabel());
                }

                if (m_filterBySeverityCheckBox.getValue() && m_compareOperatorSelect.getValue() != null) {
                    m_dashletSpec.getParameters().put("severityCompareOperator", m_compareOperatorSelect.getValue().toString());
                } else {
                    m_dashletSpec.getParameters().put("severityCompareOperator", BusinessServiceSearchCriteriaBuilder.CompareOperator.GreaterOrEqual.name());
                }

                if (m_orderBy.getValue() != null) {
                    m_dashletSpec.getParameters().put("orderBy", m_orderBy.getValue().toString());
                } else {
                    m_dashletSpec.getParameters().put("orderBy", BusinessServiceSearchCriteriaBuilder.Order.Name.name());
                }

                if (m_orderSequence.getValue() != null) {
                    m_dashletSpec.getParameters().put("orderSequence", m_orderSequence.getValue().toString());
                } else {
                    m_dashletSpec.getParameters().put("orderSequence", "Ascending");
                }

                m_dashletSpec.getParameters().put("resultsLimit", m_limitTextField.getValue().toString());
                m_dashletSpec.getParameters().put("columnCountBoard", m_columnCountBoardTextField.getValue().toString());
                m_dashletSpec.getParameters().put("columnCountPanel", m_columnCountPanelTextField.getValue().toString());

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

    /**
     * Adds a component to a given vertical layout and applies some sizing and formatting options.
     *
     * @param verticalLayout the vertical layout
     * @param component      the component to be added
     */
    private void addToComponent(VerticalLayout verticalLayout, Component component) {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);
        Label label = new Label(component.getCaption());
        label.setWidth(200, Unit.PIXELS);
        component.setSizeFull();
        component.setCaption(null);
        horizontalLayout.addComponent(label);
        horizontalLayout.addComponent(component);
        horizontalLayout.setExpandRatio(component, 1.0f);
        verticalLayout.addComponent(horizontalLayout);
    }

    /**
     * Returns the associated dashlet specification.
     *
     * @return the dashlet specification
     */
    private DashletSpec getDashletSpec() {
        return m_dashletSpec;
    }
}
