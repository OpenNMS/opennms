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

import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical;
import org.opennms.netmgt.vaadin.core.StringInputDialogWindow;

import com.google.common.collect.Sets;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

/**
 * Modal dialog window used to edit the properties of a Business Service definition. This class will be
 * instantiated by the {@see BusinessServiceMainLayout} main layout.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceEditWindow extends Window {
    /**
     * the parent main layout
     */
    private BusinessServiceMainLayout m_businessServiceMainLayout;
    /**
     * the name textfield
     */
    private TextField m_nameTextField;
    /**
     * the twin selection box used for selecting or deselecting IP services
     */
    private TwinColSelect m_ipServicesTwinColSelect;
    /**
     * the Business Services twin selection box
     */
    private TwinColSelect m_businessServicesTwinColSelect;
    /**
     * bean item container for IP services DTOs
     */
    private BeanContainer<Integer, IpService> m_ipServicesContainer = new BeanContainer<>(IpService.class);

    /**
     * bean item container for Business Services DTOs
     */
    private BeanContainer<Long, BusinessService> m_businessServicesContainer = new BeanContainer<>(BusinessService.class);
    /**
     * list of reduction keys
     */
    private ListSelect m_reductionKeyListSelect;

    /**
     * Constructor
     *
     * @param businessService the Business Service DTO instance to be configured
     * @param businessServiceMainLayout the parent main layout
     */
    public BusinessServiceEditWindow(BusinessService businessService, BusinessServiceMainLayout businessServiceMainLayout) {
        /**
         * set window title...
         */
        super("Business Service Edit");

        /**
         * set the member field...
         */
        this.m_businessServiceMainLayout = businessServiceMainLayout;

        /**
         * ...and query for IP services.
         */
        m_ipServicesContainer.setBeanIdProperty("id");
        m_ipServicesContainer.addAll(m_businessServiceMainLayout.getBusinessServiceManager().getAllIpServices());

        /**
         * ...and query for Business Services. Only add the Business Services that will not result in a loop...
         */
        m_businessServicesContainer.setBeanIdProperty("id");
        m_businessServicesContainer.addAll(m_businessServiceMainLayout.getBusinessServiceManager().getFeasibleChildServices(businessService));

        /**
         * ...and basic properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(60, Unit.PERCENTAGE);
        setHeight(85, Unit.PERCENTAGE);

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
            @Override
            public void buttonClick(Button.ClickEvent event) {
                businessService.setName(m_nameTextField.getValue().trim());

                // First clear
                businessService.setIpServiceEdges(Sets.newHashSet());
                businessService.setChildEdges(Sets.newHashSet());
                businessService.setReductionKeyEdges(Sets.newHashSet());

                // TODO BSM-98: Set according to ui selection
                ((Set<Integer>) m_ipServicesTwinColSelect.getValue()).forEach(itemId -> businessService.addIpServiceEdge(m_ipServicesContainer.getItem(itemId).getBean(), new Identity()));
                ((Set<Long>) m_businessServicesTwinColSelect.getValue()).forEach(itemId -> businessService.addChildEdge(m_businessServicesContainer.getItem(itemId).getBean(), new Identity()));
                m_reductionKeyListSelect.getItemIds().forEach(reductionKey -> businessService.addReductionKeyEdge((String) reductionKey, new Identity()));
                businessService.setReduceFunction(new MostCritical());
                businessService.save();
                close();
                businessServiceMainLayout.refreshTable();
            }
        }));

        /**
         * add the cancel button
         */
        Button cancelButton = new Button("Cancel");
        cancelButton.setId("cancelButton");
        cancelButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        /**
         * add the buttons to a HorizontalLayout
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.addComponent(saveButton);
        buttonLayout.addComponent(cancelButton);

        /**
         * instantiate the input fields
         */
        m_nameTextField = new TextField("Business Service Name");
        m_nameTextField.setId("nameField");
        m_nameTextField.setValue(businessService.getName());
        m_nameTextField.setWidth(100, Unit.PERCENTAGE);
        verticalLayout.addComponent(m_nameTextField);

        /**
         * create the IP-Services selection box
         */
        m_ipServicesTwinColSelect = new TwinColSelect();
        m_ipServicesTwinColSelect.setId("ipServiceSelect");
        m_ipServicesTwinColSelect.setWidth(99.0f, Unit.PERCENTAGE);
        m_ipServicesTwinColSelect.setLeftColumnCaption("Available IP-Services");
        m_ipServicesTwinColSelect.setRightColumnCaption("Selected IP-Services");
        m_ipServicesTwinColSelect.setRows(8);
        m_ipServicesTwinColSelect.setNewItemsAllowed(false);
        m_ipServicesTwinColSelect.setContainerDataSource(m_ipServicesContainer);
        m_ipServicesTwinColSelect.setValue(businessService.getIpServiceEdges().stream().map(edge -> edge.getIpService().getId()).collect(Collectors.toSet()));
        // manually set the item caption, otherwise .toString() is used which looks weired
        m_ipServicesTwinColSelect.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT_DEFAULTS_ID);
        m_ipServicesContainer.getItemIds().forEach(itemId -> {
            BeanItem<IpService> item = m_ipServicesContainer.getItem(itemId);
            IpService ipService = item.getBean();
            m_ipServicesTwinColSelect.setItemCaption(itemId, String.format("%s/%s/%s", ipService.getNodeLabel(), ipService.getIpAddress(), ipService.getServiceName()));
        });

        /**
         * create the Business Services selection box
         */
        m_businessServicesTwinColSelect = new TwinColSelect();
        m_businessServicesTwinColSelect.setId("businessServiceSelect");
        m_businessServicesTwinColSelect.setWidth(99.0f, Unit.PERCENTAGE);
        m_businessServicesTwinColSelect.setLeftColumnCaption("Available Business Services");
        m_businessServicesTwinColSelect.setRightColumnCaption("Selected Business Services");
        m_businessServicesTwinColSelect.setRows(8);
        m_businessServicesTwinColSelect.setContainerDataSource(m_businessServicesContainer);
        m_businessServicesTwinColSelect.setValue(businessService.getChildEdges().stream().map(edge -> edge.getChild().getId()).collect(Collectors.toSet()));
        m_businessServicesTwinColSelect.setItemCaptionMode(AbstractSelect.ItemCaptionMode.PROPERTY);
        m_businessServicesTwinColSelect.setItemCaptionPropertyId("name");

        /**
         * create the reduction key list box
         */
        m_reductionKeyListSelect = new ListSelect("Reduction Keys");
        m_reductionKeyListSelect.setId("reductionKeySelect");
        m_reductionKeyListSelect.setWidth(98.0f, Unit.PERCENTAGE);
        m_reductionKeyListSelect.setRows(8);
        m_reductionKeyListSelect.setNullSelectionAllowed(false);
        m_reductionKeyListSelect.setMultiSelect(false);
        m_reductionKeyListSelect.addItems(businessService.getReductionKeyEdges().stream().map(edge -> edge.getReductionKey()).collect(Collectors.toSet()));

        /**
         * wrap the reduction key list select box in a Vaadin Panel
         */
        verticalLayout.addComponent(m_ipServicesTwinColSelect);
        verticalLayout.addComponent(m_businessServicesTwinColSelect);

        HorizontalLayout reductionKeyListAndButtonLayout = new HorizontalLayout();

        reductionKeyListAndButtonLayout.setWidth(100.0f, Unit.PERCENTAGE);

        VerticalLayout reductionKeyButtonLayout = new VerticalLayout();
        reductionKeyButtonLayout.setWidth(140.0f, Unit.PIXELS);

        Button addReductionKeyBtn = new Button("Add reduction key");
        addReductionKeyBtn.setWidth(140.0f, Unit.PIXELS);
        addReductionKeyBtn.addStyleName("small");
        reductionKeyButtonLayout.addComponent(addReductionKeyBtn);
        addReductionKeyBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {

                new StringInputDialogWindow()
                        .withCaption("Enter Reduction Key")
                        .withFieldName("Reduction Key")
                        .withOkLabel("Save")
                        .withCancelLabel("Cancel")
                        .withValidator(new AbstractStringValidator("Input must not be empty") {
                            @Override
                            protected boolean isValidValue(String s) {
                                return (!"".equals(s));
                            }
                        })
                        .withOkAction(new StringInputDialogWindow.Action() {
                            @Override
                            public void execute(StringInputDialogWindow window) {
                                m_reductionKeyListSelect.addItem(window.getValue());
                            }
                        }).open();
            }
        });

        final Button removeReductionKeyBtn = new Button("Remove reduction key");
        removeReductionKeyBtn.setEnabled(false);
        removeReductionKeyBtn.setWidth(140.0f, Unit.PIXELS);
        removeReductionKeyBtn.addStyleName("small");
        reductionKeyButtonLayout.addComponent(removeReductionKeyBtn);

        m_reductionKeyListSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                removeReductionKeyBtn.setEnabled(event.getProperty().getValue() != null);
            }
        });

        removeReductionKeyBtn.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (m_reductionKeyListSelect.getValue() != null) {
                    m_reductionKeyListSelect.removeItem(m_reductionKeyListSelect.getValue());
                    removeReductionKeyBtn.setEnabled(false);
                }
            }
        });

        Button editPropagationRulesBtn = new Button("Edit propagation rules");
        editPropagationRulesBtn.setWidth(140.0f, Unit.PIXELS);
        editPropagationRulesBtn.addStyleName("small");
        reductionKeyButtonLayout.addComponent(editPropagationRulesBtn);

        /**
         * we're not using this button yet, so disable it
         */
        editPropagationRulesBtn.setEnabled(false);

        reductionKeyListAndButtonLayout.addComponent(m_reductionKeyListSelect);
        reductionKeyListAndButtonLayout.setExpandRatio(m_reductionKeyListSelect, 1.0f);
        reductionKeyListAndButtonLayout.addComponent(reductionKeyButtonLayout);
        reductionKeyListAndButtonLayout.setComponentAlignment(reductionKeyButtonLayout, Alignment.BOTTOM_CENTER);
        verticalLayout.addComponent(reductionKeyListAndButtonLayout);

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
}
