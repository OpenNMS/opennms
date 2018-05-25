/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Collection;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.vaadin.data.Validator;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Modal dialog window used to edit the properties of a Business Service Edge definition.
 *
 * @author Dustin Frisch <dustin@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceEdgeEditWindow extends Window {

    private enum EdgeType {
        IP_SERVICE, REDUCTION_KEY, CHILD_SERVICE
    }

    private static final long serialVersionUID = -5780075265758041282L;

    /**
     * the maximum length for a user-defined friendly name
     */
    private static int FRIENDLY_NAME_MAXLENGTH = 30;

    /**
     * declaring the components
     */
    private final NativeSelect m_typeSelect;
    private final ComboBox m_childServiceComponent;
    private final ComboBox m_ipServiceComponent;
    private final TextField m_reductionKeyComponent;
    private final NativeSelect m_mapFunctionSelect;
    private final NativeSelect m_mapFunctionSeveritySelect;
    private final TextField m_weightField;
    private final TextField m_friendlyNameField;

    /**
     * Constructor
     *
     * @param businessService        the Business Service DTO instance to be configured
     * @param businessServiceManager the Business Service Manager
     */
    @SuppressWarnings("unchecked")
    public BusinessServiceEdgeEditWindow(final BusinessService businessService,
                                         final BusinessServiceManager businessServiceManager,
                                         final Edge edge) {
        super("Business Service Edge Edit");

        /**
         * Basic window setup
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(650, Unit.PIXELS);
        setHeight(325, Unit.PIXELS);

        /**
         * Creating the root layout...
         */
        final VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSpacing(true);
        rootLayout.setMargin(false);

        /**
         * ...and the nested layout
         */
        final FormLayout formLayout = new FormLayout();
        formLayout.setSpacing(true);
        formLayout.setMargin(true);

        /**
         * type selector box
         */
        m_typeSelect = new NativeSelect("Type");
        m_typeSelect.setId("edgeTypeSelector");
        m_typeSelect.setMultiSelect(false);
        m_typeSelect.setNewItemsAllowed(false);
        m_typeSelect.setNullSelectionAllowed(false);
        m_typeSelect.setRequired(true);
        m_typeSelect.addItem(EdgeType.CHILD_SERVICE);
        m_typeSelect.setItemCaption(EdgeType.CHILD_SERVICE, "Child Service");
        m_typeSelect.addItem(EdgeType.IP_SERVICE);
        m_typeSelect.setItemCaption(EdgeType.IP_SERVICE, "IP Service");
        m_typeSelect.addItem(EdgeType.REDUCTION_KEY);
        m_typeSelect.setItemCaption(EdgeType.REDUCTION_KEY, "Reduction Key");
        m_typeSelect.setWidth(100.0f, Unit.PERCENTAGE);
        formLayout.addComponent(m_typeSelect);

        // List of child services
        m_childServiceComponent = new ComboBox("Child Service");
        m_childServiceComponent.setId("childServiceList");
        m_childServiceComponent.setInputPrompt("No child service selected");
        m_childServiceComponent.setNewItemsAllowed(false);
        m_childServiceComponent.setNullSelectionAllowed(false);
        m_childServiceComponent.setWidth(100.0f, Unit.PERCENTAGE);
        m_childServiceComponent.setVisible(false);
        m_childServiceComponent.setImmediate(true);
        m_childServiceComponent.setValidationVisible(true);
        m_childServiceComponent.setFilteringMode(FilteringMode.CONTAINS);
        m_childServiceComponent.addItems(businessServiceManager.getFeasibleChildServices(businessService).stream()
                                                               .sorted(Ordering.natural()
                                                                               .onResultOf(BusinessServiceEditWindow::describeBusinessService))
                                                               .collect(Collectors.toList()));
        m_childServiceComponent.getItemIds().forEach(item -> m_childServiceComponent.setItemCaption(item, BusinessServiceEditWindow.describeBusinessService((BusinessService) item)));
        formLayout.addComponent(m_childServiceComponent);

        // List of IP services
        m_ipServiceComponent = new ComboBox("IP Service");
        m_ipServiceComponent.setId("ipServiceList");
        m_ipServiceComponent.setInputPrompt("No IP service selected");
        m_ipServiceComponent.setNewItemsAllowed(false);
        m_ipServiceComponent.setNullSelectionAllowed(false);
        m_ipServiceComponent.setWidth(100.0f, Unit.PERCENTAGE);
        m_ipServiceComponent.setVisible(false);
        m_ipServiceComponent.setImmediate(true);
        m_ipServiceComponent.setValidationVisible(true);
        m_ipServiceComponent.setFilteringMode(FilteringMode.CONTAINS);
        m_ipServiceComponent.addItems(businessServiceManager.getAllIpServices().stream()
                                                            .sorted(Ordering.natural()
                                                                            .onResultOf(BusinessServiceEditWindow::describeIpService))
                                                            .collect(Collectors.toList()));
        m_ipServiceComponent.getItemIds().forEach(item -> m_ipServiceComponent.setItemCaption(item, BusinessServiceEditWindow.describeIpService((IpService) item)));
        formLayout.addComponent(m_ipServiceComponent);

        /**
         * reduction key input field
         */
        m_reductionKeyComponent = new TextField("Reduction Key");
        m_reductionKeyComponent.setId("reductionKeyField");
        m_reductionKeyComponent.setWidth(100.0f, Unit.PERCENTAGE);
        m_reductionKeyComponent.setVisible(false);
        m_reductionKeyComponent.setImmediate(true);
        m_reductionKeyComponent.setValidationVisible(true);
        formLayout.addComponent(m_reductionKeyComponent);

        /**
         * the friendly name
         */

        m_friendlyNameField = new TextField("Friendly Name");
        m_friendlyNameField.setId("friendlyNameField");
        m_friendlyNameField.setWidth(100.0f, Unit.PERCENTAGE);
        m_friendlyNameField.setVisible(false);
        m_friendlyNameField.setImmediate(true);
        m_friendlyNameField.setValidationVisible(true);
        m_friendlyNameField.setNullSettingAllowed(true);
        m_friendlyNameField.setNullRepresentation("");
        m_friendlyNameField.setMaxLength(FRIENDLY_NAME_MAXLENGTH);
        formLayout.addComponent(m_friendlyNameField);

        /**
         * show and hide components
         */
        m_typeSelect.addValueChangeListener(event -> {
            m_childServiceComponent.setVisible(m_typeSelect.getValue() == EdgeType.CHILD_SERVICE);
            m_childServiceComponent.setRequired(m_typeSelect.getValue() == EdgeType.CHILD_SERVICE);
            m_ipServiceComponent.setVisible(m_typeSelect.getValue() == EdgeType.IP_SERVICE);
            m_ipServiceComponent.setRequired(m_typeSelect.getValue() == EdgeType.IP_SERVICE);
            m_reductionKeyComponent.setVisible(m_typeSelect.getValue() == EdgeType.REDUCTION_KEY);
            m_reductionKeyComponent.setRequired(m_typeSelect.getValue() == EdgeType.REDUCTION_KEY);
            m_friendlyNameField.setVisible(m_typeSelect.getValue() == EdgeType.REDUCTION_KEY || m_typeSelect.getValue() == EdgeType.IP_SERVICE);
        });

        /**
         * map function field
         */
        m_mapFunctionSelect = new NativeSelect("Map Function",
                                               ImmutableList.builder()
                                                            .add(Decrease.class)
                                                            .add(Identity.class)
                                                            .add(Ignore.class)
                                                            .add(Increase.class)
                                                            .add(SetTo.class)
                                                            .build());
        m_mapFunctionSelect.setId("mapFunctionSelector");
        m_mapFunctionSelect.setNullSelectionAllowed(false);
        m_mapFunctionSelect.setMultiSelect(false);
        m_mapFunctionSelect.setNewItemsAllowed(false);
        m_mapFunctionSelect.setRequired(true);
        m_mapFunctionSelect.setWidth(100.0f, Unit.PERCENTAGE);

        /**
         * setting the captions for items
         */
        m_mapFunctionSelect.getItemIds().forEach(itemId -> m_mapFunctionSelect.setItemCaption(itemId, ((Class<?>) itemId).getSimpleName()));

        formLayout.addComponent(m_mapFunctionSelect);

        /**
         * severity selection field
         */
        m_mapFunctionSeveritySelect = new NativeSelect("Severity");
        m_mapFunctionSeveritySelect.setMultiSelect(false);
        m_mapFunctionSeveritySelect.setNewItemsAllowed(false);
        m_mapFunctionSeveritySelect.setNullSelectionAllowed(false);
        m_mapFunctionSeveritySelect.setRequired(false);
        m_mapFunctionSeveritySelect.addItem(Status.CRITICAL);
        m_mapFunctionSeveritySelect.setItemCaption(Status.CRITICAL, "Critical");
        m_mapFunctionSeveritySelect.addItem(Status.MAJOR);
        m_mapFunctionSeveritySelect.setItemCaption(Status.MAJOR, "Major");
        m_mapFunctionSeveritySelect.addItem(Status.MINOR);
        m_mapFunctionSeveritySelect.setItemCaption(Status.MINOR, "Minor");
        m_mapFunctionSeveritySelect.addItem(Status.WARNING);
        m_mapFunctionSeveritySelect.setItemCaption(Status.WARNING, "Warning");
        m_mapFunctionSeveritySelect.addItem(Status.NORMAL);
        m_mapFunctionSeveritySelect.setItemCaption(Status.NORMAL, "Normal");
        m_mapFunctionSeveritySelect.addItem(Status.INDETERMINATE);
        m_mapFunctionSeveritySelect.setItemCaption(Status.INDETERMINATE, "Indeterminate");
        m_mapFunctionSeveritySelect.setWidth(100.0f, Unit.PERCENTAGE);
        m_mapFunctionSeveritySelect.setEnabled(false);
        m_mapFunctionSeveritySelect.setImmediate(true);
        m_mapFunctionSeveritySelect.setValidationVisible(true);
        formLayout.addComponent(m_mapFunctionSeveritySelect);

        /**
         * hide or show additional severity input field
         */
        m_mapFunctionSelect.addValueChangeListener(event -> {
            m_mapFunctionSeveritySelect.setEnabled(SetTo.class.equals(m_mapFunctionSelect.getValue()));
            m_mapFunctionSeveritySelect.setRequired(SetTo.class.equals(m_mapFunctionSelect.getValue()));
        });

        /**
         * the weight input field
         */
        m_weightField = new TextField("Weight");
        m_weightField.setId("weightField");
        m_weightField.setRequired(true);
        m_weightField.setWidth(100.0f, Unit.PERCENTAGE);
        m_weightField.addValidator(value -> {
            try {
                int intValue = Integer.parseInt((String) value);
                if (intValue <= 0) {
                    throw new Validator.InvalidValueException("Weight must be > 0");
                }
            } catch (final NumberFormatException e) {
                throw new Validator.InvalidValueException("Weight must be a number");
            }
        });
        m_weightField.setImmediate(true);
        m_weightField.setValidationVisible(true);
        formLayout.addComponent(m_weightField);

        /**
         * setting the defaults
         */
        m_typeSelect.setValue(EdgeType.CHILD_SERVICE);
        m_mapFunctionSelect.setValue(Identity.class);
        m_mapFunctionSeveritySelect.setValue(Status.INDETERMINATE);
        m_weightField.setValue(Integer.toString(Edge.DEFAULT_WEIGHT));

        /**
         * add the button layout...
         */
        final HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.setMargin(true);

        /**
         * ...and the save button
         */
        final Button saveButton = new Button(edge == null ? "Add Edge" : "Update Edge");
        saveButton.setId("saveEdgeButton");
        saveButton.addClickListener(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy((Button.ClickListener) event -> {
            if (!m_weightField.isValid()) return;
            if (!m_ipServiceComponent.isValid()) return;
            if (!m_childServiceComponent.isValid()) return;
            if (!m_reductionKeyComponent.isValid()) return;

            final MapFunction mapFunction = getMapFunction();
            final int weight = Integer.parseInt(m_weightField.getValue());

            /**
             * in the case edge is not null, remove the old object...
             */
            if (edge != null) {
                businessService.removeEdge(edge);
            }

            /**
             * ...and add the new edge
             */
            switch ((EdgeType) m_typeSelect.getValue()) {
                case CHILD_SERVICE:
                    businessService.addChildEdge((BusinessService) m_childServiceComponent.getValue(), mapFunction, weight);
                    break;
                case IP_SERVICE:
                    businessService.addIpServiceEdge((IpService) m_ipServiceComponent.getValue(), mapFunction, weight, m_friendlyNameField.getValue());
                    break;
                case REDUCTION_KEY:
                    businessService.addReductionKeyEdge(m_reductionKeyComponent.getValue(), mapFunction, weight, m_friendlyNameField.getValue());
                    break;
            }

            close();
        }));
        buttonLayout.addComponent(saveButton);

        /**
         * ...and a cancel button
         */
        final Button cancelButton = new Button("Cancel");
        cancelButton.setId("cancelEdgeButton");
        cancelButton.addClickListener((Button.ClickListener) event -> close());
        buttonLayout.addComponent(cancelButton);

        /**
         * when edge is not null, fill the components with values
         */
        if (edge != null) {
            edge.accept(new EdgeVisitor<Void>() {
                @Override
                public Void visit(IpServiceEdge edge) {
                    m_typeSelect.setValue(EdgeType.IP_SERVICE);

                    for (IpService ipService : (Collection<IpService>) m_ipServiceComponent.getItemIds()) {
                        if (ipService.getId() == edge.getIpService().getId()) {
                            m_ipServiceComponent.setValue(ipService);
                            break;
                        }
                    }
                    m_friendlyNameField.setValue(edge.getFriendlyName());
                    m_ipServiceComponent.setEnabled(false);
                    return null;
                }

                @Override
                public Void visit(ReductionKeyEdge edge) {
                    m_typeSelect.setValue(EdgeType.REDUCTION_KEY);
                    m_reductionKeyComponent.setValue(edge.getReductionKey());
                    m_friendlyNameField.setValue(edge.getFriendlyName());
                    m_reductionKeyComponent.setEnabled(false);
                    return null;
                }

                @Override
                public Void visit(ChildEdge edge) {
                    m_typeSelect.setValue(EdgeType.CHILD_SERVICE);
                    m_childServiceComponent.setValue(edge.getChild());
                    m_childServiceComponent.setEnabled(false);
                    return null;
                }
            });

            m_typeSelect.setEnabled(false);
            m_mapFunctionSelect.setValue(edge.getMapFunction().getClass());

            edge.getMapFunction().accept(new MapFunctionVisitor<Void>() {
                @Override
                public Void visit(Decrease decrease) {
                    m_mapFunctionSeveritySelect.setValue(Status.INDETERMINATE);
                    return null;
                }

                @Override
                public Void visit(Identity identity) {
                    m_mapFunctionSeveritySelect.setValue(Status.INDETERMINATE);
                    return null;
                }

                @Override
                public Void visit(Ignore ignore) {
                    m_mapFunctionSeveritySelect.setValue(Status.INDETERMINATE);
                    return null;
                }

                @Override
                public Void visit(Increase increase) {
                    m_mapFunctionSeveritySelect.setValue(Status.INDETERMINATE);
                    return null;
                }

                @Override
                public Void visit(SetTo setTo) {
                    m_mapFunctionSeveritySelect.setValue(((SetTo) edge.getMapFunction()).getStatus());
                    return null;
                }
            });

            m_weightField.setValue(String.valueOf(edge.getWeight()));
        }

        /**
         * now set the root layout
         */
        rootLayout.addComponent(formLayout);
        rootLayout.addComponent(buttonLayout);
        rootLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);
        setContent(rootLayout);
    }

    @SuppressWarnings("unchecked")
    private MapFunction getMapFunction() {
        try {
            final MapFunction mapFunction = ((Class<? extends MapFunction>) m_mapFunctionSelect.getValue()).newInstance();
            mapFunction.accept(new MapFunctionVisitor<Void>() {
                @Override
                public Void visit(Decrease decrease) {
                    return null;
                }

                @Override
                public Void visit(Identity identity) {
                    return null;
                }

                @Override
                public Void visit(Ignore ignore) {
                    return null;
                }

                @Override
                public Void visit(Increase increase) {
                    return null;
                }

                @Override
                public Void visit(SetTo setTo) {
                    setTo.setStatus((Status) m_mapFunctionSeveritySelect.getValue());
                    return null;
                }
            });
            return mapFunction;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw Throwables.propagate(e);
        }
    }
}
