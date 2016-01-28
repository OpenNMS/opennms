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

import java.util.Objects;

import com.vaadin.data.util.BeanContainer;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;

import com.google.common.base.Strings;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * This class represents the main  Vaadin component for editing Business Service definitions.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceMainLayout extends VerticalLayout {
    /**
     * the Business Service Manager instance
     */
    private final BusinessServiceManager m_businessServiceManager;

    /**
     * the table instance
     */
    private final Table m_table;

    /**
     * the bean item container for the listed Business Service DTOs
     */
    private final BeanContainer<Long, BusinessService> m_beanContainer = new BeanContainer<>(BusinessService.class);

    public BusinessServiceMainLayout(BusinessServiceManager businessServiceManager) {
        m_businessServiceManager = Objects.requireNonNull(businessServiceManager);

        setSizeFull();

        /**
         * construct the upper layout for the createBusinessService button and field
         */
        HorizontalLayout upperLayout = new HorizontalLayout();

        // input field
        final TextField createTextField = new TextField();
        createTextField.setWidth(300.0f, Unit.PIXELS);
        createTextField.setInputPrompt("Business Service Name");
        createTextField.setId("createTextField");

        // create Button
        final Button createButton = new Button("Create");
        createButton.setId("createButton");
        createButton.addClickListener((Button.ClickListener) event -> {
            if (!"".equals(Strings.nullToEmpty(createTextField.getValue()).trim())) {
                final BusinessService businessService = m_businessServiceManager.createBusinessService();
                businessService.setName(createTextField.getValue().trim());
                createTextField.setValue("");

                final BusinessServiceEditWindow window = new BusinessServiceEditWindow(businessService, m_businessServiceManager);
                window.addCloseListener(e -> refreshTable());
                getUI().addWindow(window);
            }
        });

        /**
         * add to the upper layout
         */
        upperLayout.addComponent(reloadButton);
        upperLayout.addComponent(createTextField);
        upperLayout.addComponent(createButton);
        addComponent(upperLayout);
        /**
         * and set the upper-right alignment
         */
        setComponentAlignment(upperLayout, Alignment.TOP_RIGHT);

        /**
         * now construct the table...
         */
        m_table = new Table();
        m_table.setSizeFull();
        m_table.setContainerDataSource(m_beanContainer);

        /**
         * ...and configure the visible columns
         */
        m_table.setVisibleColumns("id", "name");

        /**
         * createBusinessService generated columns for modification of entries...
         */
        m_table.addGeneratedColumn("edit", new Table.ColumnGenerator() {
            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                Button editButton = new Button("edit");
                editButton.addStyleName("small");
                editButton.setId("editButton-" + itemId);

                editButton.addClickListener(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy((Button.ClickListener) event -> {
                    BusinessService businessService = m_businessServiceManager.getBusinessServiceById((Long) itemId);
                    final BusinessServiceEditWindow window = new BusinessServiceEditWindow(businessService, m_businessServiceManager);
                    window.addCloseListener(e -> refreshTable());

                    getUI().addWindow(window);
                }));
                return editButton;
            }
        });

        /**
         * ...and deletion of entries
         */
        m_table.addGeneratedColumn("delete", new Table.ColumnGenerator() {
                    @Override
                    public Object generateCell(Table source, Object itemId, Object columnId) {
                        Button deleteButton = new Button("delete");
                        deleteButton.addStyleName("small");
                        deleteButton.setId("deleteButton-" + itemId);

                        deleteButton.addClickListener((Button.ClickListener)event -> {
                            BusinessService businessService = m_businessServiceManager.getBusinessServiceById((Long) itemId);
                            if (businessService.getParentServices().isEmpty() && businessService.getChildEdges().isEmpty()) {
                                UIHelper.getCurrent(TransactionAwareUI.class).runInTransaction(() -> {
                                    m_businessServiceManager.getBusinessServiceById((Long) itemId).delete();
                                    refreshTable();
                                });
                            } else {
                                new org.opennms.netmgt.vaadin.core.ConfirmationDialog()
                                        .withOkAction(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy(new org.opennms.netmgt.vaadin.core.ConfirmationDialog.Action() {
                                            @Override
                                            public void execute(org.opennms.netmgt.vaadin.core.ConfirmationDialog window) {
                                                m_businessServiceManager.getBusinessServiceById((Long) itemId).delete();
                                                refreshTable();
                                            }
                                        }))
                                        .withOkLabel("Delete anyway")
                                        .withCancelLabel("Cancel")
                                        .withCaption("Warning")
                                        .withDescription("This entry is referencing or is referenced by other Business Services! Do you really want to delete this entry?")
                                        .open();
                            }
                        });
                        return deleteButton;
                    }
                }
        );

        /**
         * add the table to the layout
         */
        addComponent(m_table);
        setExpandRatio(m_table, 1.0f);

        /**
         * initial refresh of table
         */
        refreshTable();
    }

    /**
     * Returns the Business Service Manager instance associated with this instance.
     *
     * @return the instance of the associated Business Service Manager
     */
    public BusinessServiceManager getBusinessServiceManager() {
        return m_businessServiceManager;
    }

    /**
     * Refreshes the entries of the table used for listing the DTO instances.
     */
    private void refreshTable() {
        m_beanContainer.setBeanIdProperty("id");
        m_beanContainer.removeAllItems();
        m_beanContainer.addAll(m_businessServiceManager.getAllBusinessServices());
    }
}
