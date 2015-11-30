/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
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

public class BusinessServiceEditWindow extends Window {

    private BusinessServiceMainLayout businessServiceMainLayout;
    private TextField nameField;
    private TwinColSelect ipServiceSelect;
    private BeanItemContainer<IpServiceDTO> beanItemContainer = new BeanItemContainer<>(IpServiceDTO.class);


    public BusinessServiceEditWindow(BusinessServiceDTO businessServiceDTO, BusinessServiceMainLayout businessServiceMainLayout) {
        super("Business Service Edit");

        this.businessServiceMainLayout = businessServiceMainLayout;

        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(60, Unit.PERCENTAGE);
        setHeight(80, Unit.PERCENTAGE);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setSpacing(true);
        verticalLayout.setMargin(true);

        Button saveButton = new Button("Save");
        saveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                businessServiceDTO.setName(nameField.getValue().trim());
                businessServiceDTO.setIpServices((Set<IpServiceDTO>)ipServiceSelect.getValue());
                if (businessServiceDTO.getId() == null) {
                    businessServiceMainLayout.getBusinessServiceManager().save(businessServiceDTO);
                } else {
                    businessServiceMainLayout.getBusinessServiceManager().update(businessServiceDTO);
                }
                close();
                businessServiceMainLayout.refreshTable();
            }
        });

        Button closeButton = new Button("Cancel");
        closeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        nameField = new TextField();
        nameField.setValue(businessServiceDTO.getName());
        nameField.setWidth(100.0f, Unit.PERCENTAGE);
        verticalLayout.addComponent(new Panel("Name", nameField));

        ipServiceSelect = new TwinColSelect();
        ipServiceSelect.setWidth(100.0f, Unit.PERCENTAGE);
        ipServiceSelect.setSizeFull();
        beanItemContainer.addAll(businessServiceMainLayout.getBusinessServiceManager().getAllIpServiceDTO());
        ipServiceSelect.setContainerDataSource(beanItemContainer);
        ipServiceSelect.setValue(businessServiceDTO.getIpServices());

        Panel ipServiceSelectPanel = new Panel("IP-Services", ipServiceSelect);
        ipServiceSelectPanel.setSizeFull();
        verticalLayout.addComponent(ipServiceSelectPanel);

        verticalLayout.setExpandRatio(ipServiceSelectPanel, 1.0f);

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.addComponent(saveButton);
        horizontalLayout.addComponent(closeButton);
        verticalLayout.addComponent(horizontalLayout);
        verticalLayout.setComponentAlignment(horizontalLayout, Alignment.BOTTOM_RIGHT);

        setContent(verticalLayout);
    }
}
