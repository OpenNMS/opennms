/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.vaadin.core.UIHelper;

import com.vaadin.event.FieldEvents;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents the main  Vaadin component for editing Business Service definitions.
 *
 * @author Markus Neumann <markus@opennms.com>
 * @author Christian Pape <christian@opennms.org>
 */
public class BusinessServiceMainLayout extends VerticalLayout {
    private static final long serialVersionUID = -6753816061488048389L;

    /**
     * the Business Service Manager instance
     */
    private final BusinessServiceManager m_businessServiceManager;

    private final BusinessServiceTreeTable m_table;

    public BusinessServiceMainLayout(BusinessServiceManager businessServiceManager) {
        m_businessServiceManager = Objects.requireNonNull(businessServiceManager);
        m_table = new BusinessServiceTreeTable(businessServiceManager);

        setSizeFull();

        // Create button
        final Button createButton = UIHelper.createButton("New Business Service", null, FontAwesome.PLUS_SQUARE, (Button.ClickListener) event -> {
            final BusinessService businessService = m_businessServiceManager.createBusinessService();
            final BusinessServiceEditWindow window = new BusinessServiceEditWindow(businessService, m_businessServiceManager);
            window.addCloseListener(e -> m_table.refresh());
            getUI().addWindow(window);
        });
        createButton.setId("createButton");

        // Collapse all
        final Button collapseButton = UIHelper.createButton("Collapse All", null, FontAwesome.FOLDER, (Button.ClickListener) event -> {
            m_table.getContainerDataSource().getItemIds().forEach(id -> m_table.setCollapsed(id, true));
        });
        collapseButton.setId("collapseButton");

        // Expand all
        final Button expandButton = UIHelper.createButton("Expand All", null, FontAwesome.FOLDER_OPEN, (Button.ClickListener) event -> {
            m_table.getContainerDataSource().getItemIds().forEach(id -> m_table.setCollapsed(id, false));
        });
        expandButton.setId("expandButton");

        final TextField filterTextField = new TextField();
        filterTextField.setInputPrompt("Filter");
        filterTextField.setId("filterTextField");
        filterTextField.setImmediate(true);

        filterTextField.addTextChangeListener(new FieldEvents.TextChangeListener() {
            @Override
            public void textChange(FieldEvents.TextChangeEvent textChangeEvent) {
                m_table.setBusinessServiceNameFilter(textChangeEvent.getText());
                m_table.expandForBusinessServiceNameFilter();
            }
        });

        // Clear Filter
        final Button clearFilterButton = UIHelper.createButton("Clear Filter", null, FontAwesome.TIMES_CIRCLE, (Button.ClickListener) event -> {
            filterTextField.setValue("");
            m_table.setBusinessServiceNameFilter(null);
            m_table.getContainerDataSource().getItemIds().forEach(id -> m_table.setCollapsed(id, true));
        });

        clearFilterButton.setId("clearButton");

        // Refresh
        final Button refreshButton = UIHelper.createButton("Refresh Table", null, FontAwesome.REFRESH, (Button.ClickListener) event -> {
            m_table.refresh();
        });
        refreshButton.setId("refreshButton");

        // Reload daemon
        final Button reloadButton = UIHelper.createButton("Reload Daemon", "Reloads the Business Service State Machine", FontAwesome.RETWEET, (Button.ClickListener) event -> {
            m_businessServiceManager.triggerDaemonReload();
            Notification.show("Reloading", "Business Service daemon is being reloaded.", Type.TRAY_NOTIFICATION);
        });
        reloadButton.setId("reloadButton");

        // Group the create and collapse buttons on the left
        HorizontalLayout leftButtonGroup = new HorizontalLayout();
        leftButtonGroup.setSpacing(true);
        leftButtonGroup.addComponent(createButton);
        leftButtonGroup.addComponent(collapseButton);
        leftButtonGroup.addComponent(expandButton);
        leftButtonGroup.setDefaultComponentAlignment(Alignment.TOP_LEFT);

        // Group the refresh and reload buttons to the right
        HorizontalLayout rightButtonGroup = new HorizontalLayout();
        rightButtonGroup.setSpacing(true);
        rightButtonGroup.addComponent(filterTextField);
        rightButtonGroup.addComponent(clearFilterButton);
        rightButtonGroup.addComponent(refreshButton);
        rightButtonGroup.addComponent(reloadButton);
        rightButtonGroup.setDefaultComponentAlignment(Alignment.TOP_RIGHT);

        // Build the upper layout
        HorizontalLayout upperLayout = new HorizontalLayout();
        upperLayout.setSpacing(true);
        upperLayout.addComponent(leftButtonGroup);
        upperLayout.addComponent(rightButtonGroup);
        upperLayout.setComponentAlignment(leftButtonGroup, Alignment.TOP_LEFT);
        upperLayout.setComponentAlignment(rightButtonGroup, Alignment.TOP_RIGHT);
        upperLayout.setWidth(100, Unit.PERCENTAGE);
        addComponent(upperLayout);

        // Add some space between the upper layout and the table
        Label sz = new Label("");
        sz.setWidth(null);
        sz.setHeight(5, Unit.PIXELS);
        addComponent(sz);

        /**
         * add the table to the layout
         */
        addComponent(m_table);
        setExpandRatio(m_table, 1.0f);

        /**
         * initial refresh of table
         */
        m_table.refresh();
    }

    /**
     * Returns the Business Service Manager instance associated with this instance.
     *
     * @return the instance of the associated Business Service Manager
     */
    public BusinessServiceManager getBusinessServiceManager() {
        return m_businessServiceManager;
    }

}
