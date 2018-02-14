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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ExponentialPropagation;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverityAbove;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReduceFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.vaadin.core.KeyValueInputDialogWindow;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Modal dialog window used to edit the properties of a Business Service definition. This class will be
 * instantiated by the {@see BusinessServiceMainLayout} main layout.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceEditWindow extends Window {
    private static final long serialVersionUID = 6335020396458093845L;

    private final BusinessService m_businessService;

    /**
     * the name textfield
     */
    private TextField m_nameTextField;
    /**
     * Reduce function
     */
    private NativeSelect m_reduceFunctionNativeSelect;

    /**
     * Status
     */
    private NativeSelect m_thresholdStatusSelect;

    /**
     * the threshold textfield
     */
    private TextField m_thresholdTextField;

    /**
     * the exponential propagation base textfield
     */
    private TextField m_exponentialPropagationBaseTextField;
    /**
     * list of edges
     */
    private ListSelect m_edgesListSelect;
    /**
     * list of attributes
     */
    private ListSelect m_attributesListSelect;

    /**
     * set of already used Business Service names
     */
    private Set<String> m_businessServiceNames;

    /**
     * Constructor
     *
     * @param businessService the Business Service DTO instance to be configured
     */
    @SuppressWarnings("unchecked")
    public BusinessServiceEditWindow(BusinessService businessService,
                                     BusinessServiceManager businessServiceManager) {
        /**
         * set window title...
         */
        super("Business Service Edit");

        m_businessService = businessService;

        /**
         * ...and basic properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(650, Unit.PIXELS);
        setHeight(550, Unit.PIXELS);

        /**
         * create set for Business Service names
         */
        m_businessServiceNames = businessServiceManager.getAllBusinessServices().stream().map(BusinessService::getName).collect(Collectors.toSet());

        if (m_businessService.getName() != null) {
            m_businessServiceNames.remove(m_businessService.getName());
        }

        /**
         * construct the main layout
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        /**
         * add saveBusinessService button
         */
        Button saveButton = new Button("Save");
        saveButton.setId("saveButton");
        saveButton.addClickListener(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy(new Button.ClickListener() {
            private static final long serialVersionUID = -5985304347211214365L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!m_thresholdTextField.isValid() ||
                    !m_nameTextField.isValid() ||
                    !m_exponentialPropagationBaseTextField.isValid()) {
                    return;
                }

                final ReductionFunction reductionFunction = getReduceFunction();
                businessService.setName(m_nameTextField.getValue().trim());
                businessService.setReduceFunction(reductionFunction);
                businessService.save();
                close();
            }

            private ReductionFunction getReduceFunction() {
                try {
                    final ReductionFunction reductionFunction = ((Class<? extends ReductionFunction>) m_reduceFunctionNativeSelect.getValue()).newInstance();
                    reductionFunction.accept(new ReduceFunctionVisitor<Void>() {
                        @Override
                        public Void visit(HighestSeverity highestSeverity) {
                            return null;
                        }

                        @Override
                        public Void visit(HighestSeverityAbove highestSeverityAbove) {
                            highestSeverityAbove.setThreshold((Status) m_thresholdStatusSelect.getValue());
                            return null;
                        }

                        @Override
                        public Void visit(Threshold threshold) {
                            threshold.setThreshold(Float.parseFloat(m_thresholdTextField.getValue()));
                            return null;
                        }

                        @Override
                        public Void visit(ExponentialPropagation exponentialPropagation) {
                            exponentialPropagation.setBase(Double.parseDouble(m_exponentialPropagationBaseTextField.getValue()));
                            return null;
                        }
                    });
                    return reductionFunction;
                } catch (final InstantiationException | IllegalAccessException e) {
                    throw Throwables.propagate(e);
                }
            }
        }));

        /**
         * add the cancel button
         */
        Button cancelButton = new Button("Cancel");
        cancelButton.setId("cancelButton");
        cancelButton.addClickListener(new Button.ClickListener() {
            private static final long serialVersionUID = 5306168797758047745L;

            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        /**
         * add the buttons to a HorizontalLayout
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.addComponent(saveButton);
        buttonLayout.addComponent(cancelButton);

        /**
         * instantiate the input fields
         */
        m_nameTextField = new TextField("Business Service Name");
        m_nameTextField.setId("nameField");
        m_nameTextField.setNullRepresentation("");
        m_nameTextField.setNullSettingAllowed(true);
        m_nameTextField.setValue(businessService.getName());
        m_nameTextField.setWidth(100, Unit.PERCENTAGE);
        m_nameTextField.setRequired(true);
        m_nameTextField.focus();
        m_nameTextField.addValidator(new AbstractStringValidator("Name must be unique") {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean isValidValue(String value) {
                return value != null && !m_businessServiceNames.contains(value);
            }
        });
        verticalLayout.addComponent(m_nameTextField);

        /**
         * create the reduce function component
         */

        m_reduceFunctionNativeSelect = new NativeSelect("Reduce Function", ImmutableList.builder()
                .add(HighestSeverity.class)
                .add(Threshold.class)
                .add(HighestSeverityAbove.class)
                .add(ExponentialPropagation.class)
                .build());
        m_reduceFunctionNativeSelect.setId("reduceFunctionNativeSelect");
        m_reduceFunctionNativeSelect.setWidth(100.0f, Unit.PERCENTAGE);
        m_reduceFunctionNativeSelect.setNullSelectionAllowed(false);
        m_reduceFunctionNativeSelect.setMultiSelect(false);
        m_reduceFunctionNativeSelect.setImmediate(true);
        m_reduceFunctionNativeSelect.setNewItemsAllowed(false);

        /**
         * setting the captions for items
         */
        m_reduceFunctionNativeSelect.getItemIds().forEach(itemId -> m_reduceFunctionNativeSelect.setItemCaption(itemId, ((Class<?>) itemId).getSimpleName()));

        verticalLayout.addComponent(m_reduceFunctionNativeSelect);

        m_thresholdTextField = new TextField("Threshold");
        m_thresholdTextField.setId("thresholdTextField");
        m_thresholdTextField.setRequired(false);
        m_thresholdTextField.setEnabled(false);
        m_thresholdTextField.setImmediate(true);
        m_thresholdTextField.setWidth(100.0f, Unit.PERCENTAGE);
        m_thresholdTextField.setValue("0.0");
        m_thresholdTextField.addValidator(v -> {
            if (m_thresholdTextField.isEnabled()) {
                try {
                    final float value = Float.parseFloat(m_thresholdTextField.getValue());
                    if (0.0f >= value || value > 1.0) {
                        throw new NumberFormatException();
                    }
                } catch (final NumberFormatException e) {
                    throw new Validator.InvalidValueException("Threshold must be a positive number");
                }
            }
        });

        verticalLayout.addComponent(m_thresholdTextField);

        m_exponentialPropagationBaseTextField = new TextField("Base");
        m_exponentialPropagationBaseTextField.setId("exponentialPropagationBaseTextField");
        m_exponentialPropagationBaseTextField.setRequired(false);
        m_exponentialPropagationBaseTextField.setEnabled(false);
        m_exponentialPropagationBaseTextField.setImmediate(true);
        m_exponentialPropagationBaseTextField.setWidth(100.0f, Unit.PERCENTAGE);
        m_exponentialPropagationBaseTextField.setValue("2.0");
        m_exponentialPropagationBaseTextField.addValidator(v -> {
            if (m_exponentialPropagationBaseTextField.isEnabled()) {
                try {
                    final double value = Double.parseDouble(m_exponentialPropagationBaseTextField.getValue());
                    if (value <= 1.0) {
                        throw new NumberFormatException();
                    }
                } catch (final NumberFormatException e) {
                    throw new Validator.InvalidValueException("Base must be greater than 1.0");
                }
            }
        });

        verticalLayout.addComponent(m_exponentialPropagationBaseTextField);

        /**
         * Status selection for "Highest Severity Above"
         */
        m_thresholdStatusSelect = new NativeSelect("Threshold");
        m_thresholdStatusSelect.setId("thresholdStatusSelect");
        m_thresholdStatusSelect.setRequired(false);
        m_thresholdStatusSelect.setEnabled(false);
        m_thresholdStatusSelect.setImmediate(true);
        m_thresholdStatusSelect.setWidth(100.0f, Unit.PERCENTAGE);
        m_thresholdStatusSelect.setMultiSelect(false);
        m_thresholdStatusSelect.setNewItemsAllowed(false);
        m_thresholdStatusSelect.setNullSelectionAllowed(false);
        for (Status eachStatus : Status.values()) {
            m_thresholdStatusSelect.addItem(eachStatus);
        }
        m_thresholdStatusSelect.setValue(Status.INDETERMINATE);
        m_thresholdStatusSelect.getItemIds().forEach(itemId -> m_thresholdStatusSelect.setItemCaption(itemId, ((Status) itemId).getLabel()));
        verticalLayout.addComponent(m_thresholdStatusSelect);

        m_reduceFunctionNativeSelect.addValueChangeListener(ev -> {
            boolean thresholdFunction = m_reduceFunctionNativeSelect.getValue() == Threshold.class;
            boolean highestSeverityAboveFunction = m_reduceFunctionNativeSelect.getValue() == HighestSeverityAbove.class;
            boolean exponentialPropagationFunction = m_reduceFunctionNativeSelect.getValue() == ExponentialPropagation.class;

            int height = 550;

            if (thresholdFunction) {
                height += 45;
            }

            if (highestSeverityAboveFunction) {
                height += 40;
            }

            setHeight(height, Unit.PIXELS);

            setVisible(m_thresholdTextField, thresholdFunction);
            setVisible(m_thresholdStatusSelect, highestSeverityAboveFunction);
            setVisible(m_exponentialPropagationBaseTextField, exponentialPropagationFunction);
        });

        if (Objects.isNull(businessService.getReduceFunction())) {
            m_reduceFunctionNativeSelect.setValue(HighestSeverity.class);
        } else {
            m_reduceFunctionNativeSelect.setValue(businessService.getReduceFunction().getClass());

            businessService.getReduceFunction().accept(new ReduceFunctionVisitor<Void>() {
                @Override
                public Void visit(HighestSeverity highestSeverity) {
                    return null;
                }

                @Override
                public Void visit(HighestSeverityAbove highestSeverityAbove) {
                    m_thresholdStatusSelect.setValue(highestSeverityAbove.getThreshold());
                    return null;
                }

                @Override
                public Void visit(Threshold threshold) {
                    m_thresholdTextField.setValue(String.valueOf(threshold.getThreshold()));
                    return null;
                }

                @Override
                public Void visit(ExponentialPropagation exponentialPropagation) {
                    m_exponentialPropagationBaseTextField.setValue(String.valueOf(exponentialPropagation.getBase()));
                    return null;
                }
            });
        }

        /**
         * create the edges list box
         */
        m_edgesListSelect = new ListSelect("Edges");
        m_edgesListSelect.setId("edgeList");
        m_edgesListSelect.setWidth(100.0f, Unit.PERCENTAGE);
        m_edgesListSelect.setRows(10);
        m_edgesListSelect.setNullSelectionAllowed(false);
        m_edgesListSelect.setMultiSelect(false);
        refreshEdges();

        HorizontalLayout edgesListAndButtonLayout = new HorizontalLayout();

        edgesListAndButtonLayout.setWidth(100.0f, Unit.PERCENTAGE);

        VerticalLayout edgesButtonLayout = new VerticalLayout();
        edgesButtonLayout.setWidth(110.0f, Unit.PIXELS);
        edgesButtonLayout.setSpacing(true);

        Button addEdgeButton = new Button("Add Edge");
        addEdgeButton.setId("addEdgeButton");
        addEdgeButton.setWidth(110.0f, Unit.PIXELS);
        addEdgeButton.addStyleName("small");
        edgesButtonLayout.addComponent(addEdgeButton);
        addEdgeButton.addClickListener((Button.ClickListener) event -> {
            final BusinessServiceEdgeEditWindow window = new BusinessServiceEdgeEditWindow(businessService, businessServiceManager, null);
            window.addCloseListener(e -> refreshEdges());
            this.getUI().addWindow(window);
        });

        Button editEdgeButton = new Button("Edit Edge");
        editEdgeButton.setId("editEdgeButton");
        editEdgeButton.setEnabled(false);
        editEdgeButton.setWidth(110.0f, Unit.PIXELS);
        editEdgeButton.addStyleName("small");
        edgesButtonLayout.addComponent(editEdgeButton);
        editEdgeButton.addClickListener((Button.ClickListener) event -> {
            final BusinessServiceEdgeEditWindow window = new BusinessServiceEdgeEditWindow(businessService, businessServiceManager, (Edge) m_edgesListSelect.getValue());
            window.addCloseListener(e -> refreshEdges());
            this.getUI().addWindow(window);
        });

        final Button removeEdgeButton = new Button("Remove Edge");
        removeEdgeButton.setId("removeEdgeButton");
        removeEdgeButton.setEnabled(false);
        removeEdgeButton.setWidth(110.0f, Unit.PIXELS);
        removeEdgeButton.addStyleName("small");
        edgesButtonLayout.addComponent(removeEdgeButton);

        m_edgesListSelect.addValueChangeListener((Property.ValueChangeListener) event -> {
            removeEdgeButton.setEnabled(event.getProperty().getValue() != null);
            editEdgeButton.setEnabled(event.getProperty().getValue() != null);
        });

        removeEdgeButton.addClickListener((Button.ClickListener) event -> {
            if (m_edgesListSelect.getValue() != null) {
                removeEdgeButton.setEnabled(false);
                ((Edge) m_edgesListSelect.getValue()).delete();
                refreshEdges();
            }
        });

        edgesListAndButtonLayout.setSpacing(true);
        edgesListAndButtonLayout.addComponent(m_edgesListSelect);
        edgesListAndButtonLayout.setExpandRatio(m_edgesListSelect, 1.0f);

        edgesListAndButtonLayout.addComponent(edgesButtonLayout);
        edgesListAndButtonLayout.setComponentAlignment(edgesButtonLayout, Alignment.BOTTOM_CENTER);
        verticalLayout.addComponent(edgesListAndButtonLayout);

        /**
         * create the attributes list box
         */
        m_attributesListSelect = new ListSelect("Attributes");
        m_attributesListSelect.setId("attributeList");
        m_attributesListSelect.setWidth(100.0f, Unit.PERCENTAGE);
        m_attributesListSelect.setRows(10);
        m_attributesListSelect.setNullSelectionAllowed(false);
        m_attributesListSelect.setMultiSelect(false);

        refreshAttributes();

        HorizontalLayout attributesListAndButtonLayout = new HorizontalLayout();

        attributesListAndButtonLayout.setWidth(100.0f, Unit.PERCENTAGE);

        VerticalLayout attributesButtonLayout = new VerticalLayout();
        attributesButtonLayout.setWidth(110.0f, Unit.PIXELS);
        attributesButtonLayout.setSpacing(true);

        Button addAttributeButton = new Button("Add Attribute");
        addAttributeButton.setId("addAttributeButton");
        addAttributeButton.setWidth(110.0f, Unit.PIXELS);
        addAttributeButton.addStyleName("small");
        attributesButtonLayout.addComponent(addAttributeButton);
        addAttributeButton.addClickListener((Button.ClickListener) event -> {
            KeyValueInputDialogWindow keyValueInputDialogWindow = new KeyValueInputDialogWindow()
                    .withKeyFieldName("Key")
                    .withValueFieldName("Value")
                    .withCaption("Attribute")
                    .withKey("")
                    .withValue("")
                    .withOkAction(new KeyValueInputDialogWindow.Action() {
                        @Override
                        public void execute(KeyValueInputDialogWindow window) {
                            m_businessService.getAttributes().put(window.getKey(), window.getValue());
                            refreshAttributes();
                        }
                    })
                    .withKeyValidator(new AbstractStringValidator("Key must not be empty") {
                        private static final long serialVersionUID = 1L;
                        @Override
                        protected boolean isValidValue(String value) {
                            return !Strings.isNullOrEmpty(value);
                        }
                    })
                    .withKeyValidator(new AbstractStringValidator("Key must be unique") {
                        private static final long serialVersionUID = 1L;
                        @Override
                        protected boolean isValidValue(String value) {
                            return !m_businessService.getAttributes().containsKey(value);
                        }
                    }).focusKey();
            this.getUI().addWindow(keyValueInputDialogWindow);
            keyValueInputDialogWindow.focus();
        });

        Button editAttributeButton = new Button("Edit Attribute");
        editAttributeButton.setId("editAttributeButton");
        editAttributeButton.setEnabled(false);
        editAttributeButton.setWidth(110.0f, Unit.PIXELS);
        editAttributeButton.addStyleName("small");
        attributesButtonLayout.addComponent(editAttributeButton);
        editAttributeButton.addClickListener((Button.ClickListener) event -> {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) m_attributesListSelect.getValue();
            KeyValueInputDialogWindow keyValueInputDialogWindow = new KeyValueInputDialogWindow()
                    .withKeyFieldName("Key")
                    .withValueFieldName("Value")
                    .withCaption("Attribute")
                    .withKey(entry.getKey())
                    .disableKey()
                    .withValue(entry.getValue())
                    .withOkAction(new KeyValueInputDialogWindow.Action() {
                        @Override
                        public void execute(KeyValueInputDialogWindow window) {
                            m_businessService.getAttributes().put(window.getKey(), window.getValue());
                            refreshAttributes();
                        }
                    }).focusValue();
            this.getUI().addWindow(keyValueInputDialogWindow);
            keyValueInputDialogWindow.focus();
        });

        final Button removeAttributeButton = new Button("Remove Attribute");
        removeAttributeButton.setId("removeAttributeButton");
        removeAttributeButton.setEnabled(false);
        removeAttributeButton.setWidth(110.0f, Unit.PIXELS);
        removeAttributeButton.addStyleName("small");
        attributesButtonLayout.addComponent(removeAttributeButton);

        m_attributesListSelect.addValueChangeListener((Property.ValueChangeListener) event -> {
            removeAttributeButton.setEnabled(event.getProperty().getValue() != null);
            editAttributeButton.setEnabled(event.getProperty().getValue() != null);
        });

        removeAttributeButton.addClickListener((Button.ClickListener) event -> {
            if (m_attributesListSelect.getValue() != null) {
                removeAttributeButton.setEnabled(false);
                m_businessService.getAttributes().remove(((Map.Entry<String, String>) m_attributesListSelect.getValue()).getKey());
                refreshAttributes();
            }
        });

        attributesListAndButtonLayout.setSpacing(true);
        attributesListAndButtonLayout.addComponent(m_attributesListSelect);
        attributesListAndButtonLayout.setExpandRatio(m_attributesListSelect, 1.0f);

        attributesListAndButtonLayout.addComponent(attributesButtonLayout);
        attributesListAndButtonLayout.setComponentAlignment(attributesButtonLayout, Alignment.BOTTOM_CENTER);
        verticalLayout.addComponent(attributesListAndButtonLayout);

        /**
         * now add the button layout to the main layout
         */
        verticalLayout.addComponent(buttonLayout);
        verticalLayout.setExpandRatio(buttonLayout, 1.0f);

        verticalLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        /**
         * set the window's content
         */
        setContent(verticalLayout);
    }

    private void setVisible(Field<?> field, boolean visible) {
        field.setEnabled(visible);
        field.setRequired(visible);
        field.setVisible(visible);
    }

    @SuppressWarnings("unchecked")
    private void refreshAttributes() {
        m_attributesListSelect.removeAllItems();
        m_attributesListSelect.addItems(m_businessService.getAttributes().entrySet().stream()
                .sorted(Ordering.natural()
                        .onResultOf(Map.Entry::getKey))
                .collect(Collectors.toList()));
        m_attributesListSelect.getItemIds().forEach(item -> m_attributesListSelect.setItemCaption(item, ((Map.Entry<String, String>) item).getKey() + "=" + ((Map.Entry<String, String>) item).getValue()));
    }

    private void refreshEdges() {
        m_edgesListSelect.removeAllItems();
        m_edgesListSelect.addItems(m_businessService.getEdges().stream()
                                                    .map(e -> (Edge)e)
                                                    .sorted((e1, e2) -> getChildDescription(e1).compareTo(getChildDescription(e2)))
                                                    .collect(Collectors.toList()));
        m_edgesListSelect.getItemIds().forEach(item -> m_edgesListSelect.setItemCaption(item, describeEdge((Edge) item)));
    }

    public static String describeBusinessService(final BusinessService businessService) {
        return businessService.getName();
    }

    public static String describeIpService(final IpService ipService) {
        return describeIpService(ipService, null);
    }

    public static String describeIpService(final IpService ipService, final String friendlyName) {
        return String.format("%s %s %s%s",
                             ipService.getNodeLabel(),
                             ipService.getIpAddress(),
                             ipService.getServiceName(),
                             Strings.isNullOrEmpty(friendlyName) ? "" : " (" + friendlyName + ")");
    }

    private static String getEdgePrefix(Edge edge) {
        return edge.accept(new EdgeVisitor<String>() {

            @Override
            public String visit(IpServiceEdge edge) {
                return "IPSvc";
            }

            @Override
            public String visit(ReductionKeyEdge edge) {
                return "ReKey";
            }

            @Override
            public String visit(ChildEdge edge) {
                return "Child";
            }
        });
    }

    private static String getChildDescription(Edge edge) {
        return edge.accept(new EdgeVisitor<String>() {

            @Override
            public String visit(IpServiceEdge edge) {
                return describeIpService(edge.getIpService(), edge.getFriendlyName());
            }

            @Override
            public String visit(ReductionKeyEdge edge) {
                return describeReductionKey(edge.getReductionKey(), edge.getFriendlyName());
            }

            @Override
            public String visit(ChildEdge edge) {
                return describeBusinessService(edge.getChild());
            }
        });
    }

    public static String describeReductionKey(final String reductionKey, final String friendlyName) {
        return reductionKey + (Strings.isNullOrEmpty(friendlyName) ? "" : " (" + friendlyName + ")");
    }

    public static String describeEdge(final Edge edge) {
        String edgePrefix = getEdgePrefix(edge);
        String itemDescription = getChildDescription(edge);
        return String.format("%s: %s, Map: %s, Weight: %s",
                edgePrefix,
                itemDescription,
                edge.getMapFunction().getClass()== SetTo.class
                        ? String.format("%s (%s)", edge.getMapFunction().getClass().getSimpleName(),
                                                   ((SetTo)edge.getMapFunction()).getStatus())
                        : edge.getMapFunction().getClass().getSimpleName(),
                edge.getWeight());
    }
}
