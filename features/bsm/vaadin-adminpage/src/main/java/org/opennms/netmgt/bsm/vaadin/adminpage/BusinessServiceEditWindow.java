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
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Set;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;
import org.opennms.netmgt.bsm.service.model.IpServiceDTO;

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
     * bean item container for IP services DTOs
     */
    private BeanItemContainer<IpServiceDTO> m_beanItemContainer = new BeanItemContainer<>(IpServiceDTO.class);

    /**
     * Constructor
     *
     * @param businessServiceDTO the Business Service DTO instance to be configured
     * @param businessServiceMainLayout the parent main layout
     */
    public BusinessServiceEditWindow(BusinessServiceDTO businessServiceDTO, BusinessServiceMainLayout businessServiceMainLayout) {
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
        m_beanItemContainer.addAll(m_businessServiceMainLayout.getBusinessServiceManager().getAllIpServiceDTO());

        /**
         * ...and basic properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(60, Unit.PERCENTAGE);
        setHeight(80, Unit.PERCENTAGE);

        /**
         * construct the main layout
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        /**
         * add save button
         */
        Button saveButton = new Button("Save");
        saveButton.setId("saveButton");
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                businessServiceDTO.setName(m_nameTextField.getValue().trim());
                businessServiceDTO.setIpServices((Set<IpServiceDTO>) m_ipServicesTwinColSelect.getValue());
                if (businessServiceDTO.getId() == null) {
                    businessServiceMainLayout.getBusinessServiceManager().save(businessServiceDTO);
                } else {
                    businessServiceMainLayout.getBusinessServiceManager().update(businessServiceDTO);
                }
                close();
                businessServiceMainLayout.refreshTable();
            }
        });

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
        m_nameTextField = new TextField();
        m_nameTextField.setId("nameField");
        m_nameTextField.setValue(businessServiceDTO.getName());
        m_nameTextField.setWidth(100.0f, Unit.PERCENTAGE);
        verticalLayout.addComponent(new Panel("Name", m_nameTextField));

        m_ipServicesTwinColSelect = new TwinColSelect();
        m_ipServicesTwinColSelect.setId("ipServiceSelect");
        m_ipServicesTwinColSelect.setWidth(100.0f, Unit.PERCENTAGE);
        m_ipServicesTwinColSelect.setSizeFull();

        m_ipServicesTwinColSelect.setContainerDataSource(m_beanItemContainer);
        m_ipServicesTwinColSelect.setValue(businessServiceDTO.getIpServices());

        /**
         * wrap the IP selection box in a Vaadin Panel
         */
        Panel ipServiceSelectPanel = new Panel("IP-Services", m_ipServicesTwinColSelect);
        ipServiceSelectPanel.setSizeFull();
        verticalLayout.addComponent(ipServiceSelectPanel);
        verticalLayout.setExpandRatio(ipServiceSelectPanel, 1.0f);

        /**
         * now add the button layout to the main layout
         */
        verticalLayout.addComponent(buttonLayout);
        verticalLayout.setComponentAlignment(buttonLayout, Alignment.BOTTOM_RIGHT);

        /**
         * set the window's content
         */
        setContent(verticalLayout);
    }
}
