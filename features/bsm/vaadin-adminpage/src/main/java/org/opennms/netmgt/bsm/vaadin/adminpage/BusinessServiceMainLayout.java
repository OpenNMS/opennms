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

import java.util.Objects;

import com.google.common.base.Strings;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessServiceDTO;

public class BusinessServiceMainLayout extends VerticalLayout {

    private BusinessServiceManager businessServiceManager;
    private final Table table;
    private final BeanItemContainer<BusinessServiceDTO> beanItemContainer = new BeanItemContainer<>(BusinessServiceDTO.class);

    public BusinessServiceMainLayout(BusinessServiceManager businessServiceManager) {

        this.businessServiceManager = businessServiceManager;
        Objects.requireNonNull(businessServiceManager);

        setSizeFull();

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        final TextField createField = new TextField();
        createField.setInputPrompt("NewBusinessService");

        Button createButton = new Button("Create");
        createButton.addClickListener((Button.ClickListener) event -> {
            if (!"".equals(Strings.nullToEmpty(createField.getValue()).trim())) {
                BusinessServiceDTO businessServiceDTO = new BusinessServiceDTO();
                businessServiceDTO.setName(createField.getValue().trim());
                getUI().addWindow(new BusinessServiceEditWindow(businessServiceDTO, BusinessServiceMainLayout.this));
                createField.setValue("");
                refreshTable();
            }
        });

        horizontalLayout.addComponent(createField);
        horizontalLayout.addComponent(createButton);
        addComponent(horizontalLayout);
        setComponentAlignment(horizontalLayout, Alignment.TOP_RIGHT);

        table = new Table();
        table.setContainerDataSource(beanItemContainer);
        table.setVisibleColumns("id", "name");

        table.addGeneratedColumn("edit", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button editButton = new Button("edit");
                editButton.addClickListener((Button.ClickListener) event -> {
                    getUI().addWindow(new BusinessServiceEditWindow((BusinessServiceDTO)itemId, BusinessServiceMainLayout.this));
                    refreshTable();
                });
                return editButton;
            }
        });

        table.addGeneratedColumn("delete", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button deleteButton = new Button("delete");
                deleteButton.addClickListener((Button.ClickListener) event -> {
                    businessServiceManager.delete(((BusinessServiceDTO)itemId).getId());
                    refreshTable();
                });
                return deleteButton;
            }
        });
        addComponent(table);
        setExpandRatio(table, 1.0f);
        table.setSizeFull();
        refreshTable();
    }

    public BusinessServiceManager getBusinessServiceManager() {
        return businessServiceManager;
    }


    public void refreshTable() {
        beanItemContainer.removeAllItems();
        beanItemContainer.addAll(businessServiceManager.findAll());
        System.out.println(businessServiceManager.findAll().get(0).getId() + "");
    }
}
