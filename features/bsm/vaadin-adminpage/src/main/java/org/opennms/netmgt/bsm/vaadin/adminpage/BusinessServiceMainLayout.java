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

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.vaadin.data.Container;
import com.vaadin.data.Container.ItemSetChangeEvent;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
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

    /**
     * the table instance
     */
    private final TreeTable m_table = new TreeTable();

    /**
     * the bean item container for the listed Business Service DTOs
     */
    private final BeanContainer<Long, BusinessServiceRow> m_beanContainer = new BeanContainer<>(BusinessServiceRow.class);

    /**
     * Used to allocate unique IDs to the table rows.
     */
    private AtomicLong m_rowIdCounter = new AtomicLong();

    public BusinessServiceMainLayout(BusinessServiceManager businessServiceManager) {
        m_businessServiceManager = Objects.requireNonNull(businessServiceManager);

        setSizeFull();

        // Create button
        final Button createButton = UIHelper.createButton("New Business Service", null, FontAwesome.PLUS_SQUARE, (Button.ClickListener) event -> {
            final BusinessService businessService = m_businessServiceManager.createBusinessService();
            final BusinessServiceEditWindow window = new BusinessServiceEditWindow(businessService, m_businessServiceManager);
            window.addCloseListener(e -> refreshTable());
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

        // Refresh
        final Button refreshButton = UIHelper.createButton("Refresh Table", null, FontAwesome.REFRESH, (Button.ClickListener) event -> {
            refreshTable();
        });
        refreshButton.setId("refreshButton");

        // Reload daemon
        final Button reloadButton = UIHelper.createButton("Reload Daemon", "Reloads the Business Service State Machine", FontAwesome.RETWEET, (Button.ClickListener) event -> {
            m_businessServiceManager.triggerDaemonReload();
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
         * now setup the table...
         */
        m_table.setSizeFull();
        m_table.setContainerDataSource(m_beanContainer);

        /**
         * ...and configure the visible columns
         */
        m_table.setVisibleColumns("name");

        // Add the "LINKS" columns
        m_table.addGeneratedColumn("links", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 7113848887128656685L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                final HorizontalLayout layout = new HorizontalLayout();
                final BusinessServiceStateMachine stateMachine = m_businessServiceManager.getStateMachine();
                final BusinessService businessService = m_beanContainer.getItem(itemId).getBean().getBusinessService();
                final Status status = stateMachine.getOperationalStatus(businessService);
                if (status != null) {
                    // Build the query string
                    final List<BasicNameValuePair> urlParms = Lists.newArrayList(
                            new BasicNameValuePair("focus-vertices", businessService.getId().toString()),
                            new BasicNameValuePair("szl", "1"),
                            new BasicNameValuePair("layout", "Hierarchy Layout"),
                            new BasicNameValuePair("provider", "Business Services")
                            );
                    final String queryString = URLEncodedUtils.format(urlParms, Charset.forName("UTF-8"));

                    // Generate the link
                    final Link link = new Link("View in Topology UI", new ExternalResource(
                            String.format("/opennms/topology?%s", queryString)));
                    link.setIcon(FontAwesome.EXTERNAL_LINK_SQUARE);
                    // This app is typically access in an iframe, so we open the URL in a new window/tab
                    link.setTargetName("_blank");
                    layout.addComponent(link);
                    layout.setComponentAlignment(link, Alignment.MIDDLE_CENTER);
                } else {
                    Label label = new Label("N/A");
                    label.setDescription("Try reloading the daemon and refreshing the table.");
                    label.setWidth(null);
                    layout.addComponent(label);
                }
                return layout;
            }
        });

        /**
         * add edit and delete buttons
         */
        m_table.addGeneratedColumn("edit / delete", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 7113848887128656685L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setSpacing(true);

                Button editButton = new Button("Edit", FontAwesome.PENCIL_SQUARE_O);
                editButton.setId("editButton-" + m_beanContainer.getItem(itemId).getBean().getName());

                editButton.addClickListener(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy((Button.ClickListener) event -> {
                    final Long businessServiceId = m_beanContainer.getItem(itemId).getBean().getBusinessService().getId();
                    BusinessService businessService = m_businessServiceManager.getBusinessServiceById(businessServiceId);
                    final BusinessServiceEditWindow window = new BusinessServiceEditWindow(businessService, m_businessServiceManager);
                    window.addCloseListener(e -> refreshTable());

                    getUI().addWindow(window);
                }));
                layout.addComponent(editButton);

                Button deleteButton = new Button("Delete", FontAwesome.TRASH_O);
                deleteButton.setId("deleteButton-" + m_beanContainer.getItem(itemId).getBean().getName());

                deleteButton.addClickListener((Button.ClickListener)event -> {
                    final Long businessServiceId = m_beanContainer.getItem(itemId).getBean().getBusinessService().getId();
                    BusinessService businessService = m_businessServiceManager.getBusinessServiceById(businessServiceId);
                    if (businessService.getParentServices().isEmpty() && businessService.getChildEdges().isEmpty()) {
                        UIHelper.getCurrent(TransactionAwareUI.class).runInTransaction(() -> {
                            m_businessServiceManager.getBusinessServiceById(businessServiceId).delete();
                            refreshTable();
                        });
                    } else {
                        new org.opennms.netmgt.vaadin.core.ConfirmationDialog()
                            .withOkAction((org.opennms.netmgt.vaadin.core.ConfirmationDialog.Action) UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy(new org.opennms.netmgt.vaadin.core.ConfirmationDialog.Action() {
                                @Override
                                public void execute(org.opennms.netmgt.vaadin.core.ConfirmationDialog window) {
                                    m_businessServiceManager.getBusinessServiceById(businessServiceId).delete();
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
                layout.addComponent(deleteButton);

                return layout;
            }
        });

        m_table.setColumnExpandRatio("name", 5);
        m_table.setColumnExpandRatio("links", 1);
        m_table.setColumnExpandRatio("edit / delete", 1);

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

    private void createRowForVertex(BusinessServiceGraph graph, GraphVertex graphVertex, BusinessServiceRow parentRow,
            com.google.common.collect.Table<Long, Optional<Long>, Boolean> collapseState) {
        final BusinessService businessService = graphVertex.getBusinessService();
        if (businessService == null) {
            return;
        }

        final long rowId = m_rowIdCounter.incrementAndGet();
        final Long parentBusinessServiceId = parentRow != null ? parentRow.getBusinessService().getId() : null;
        final BusinessServiceRow row = new BusinessServiceRow(rowId, businessService, parentBusinessServiceId);
        m_beanContainer.addBean(row);
        if (parentRow != null) {
            m_table.setParent(rowId, parentRow.getId());
        }

        // Restore the previous collapsed state
        Boolean wasCollapsed = collapseState.get(businessService.getId(), Optional.ofNullable(parentBusinessServiceId));
        m_table.setCollapsed(rowId, wasCollapsed != null ? wasCollapsed : true); // Collapse by default

        // Recurse with all of the children
        graph.getOutEdges(graphVertex).stream()
            .map(e -> graph.getOpposite(graphVertex, e))
            .filter(v -> v.getBusinessService() != null)
            .sorted((v1, v2) -> v1.getBusinessService().getName().compareTo(v2.getBusinessService().getName()))
            .forEach(v -> createRowForVertex(graph, v, row, collapseState));
    }

    /**
     * Refreshes table entries.
     */
    private void refreshTable() {
        // Gather the current collapse state
        final com.google.common.collect.Table<Long, Optional<Long>, Boolean> collapseState = HashBasedTable.create();
        for (Long itemId : m_beanContainer.getItemIds()) {
            final BusinessServiceRow row = m_beanContainer.getItem(itemId).getBean();
            collapseState.put(row.getBusinessService().getId(), Optional.ofNullable(row.getParentBusinessServiceId()), m_table.isCollapsed(itemId));
        }

        // Clear the container
        m_beanContainer.setBeanIdProperty("id");
        m_beanContainer.removeAllItems();
        m_rowIdCounter.set(0);

        // Build a graph using all of the business services stored in the database
        // We don't use the existing graph, since it only contains the services know by the state machine
        final BusinessServiceGraph graph = m_businessServiceManager.getGraph(m_businessServiceManager.getAllBusinessServices());

        // Recursively generate the table rows, starting with the roots
        graph.getVerticesByLevel(0).stream()
            .filter(v -> v.getBusinessService() != null)
            .sorted((v1, v2) -> v1.getBusinessService().getName().compareTo(v2.getBusinessService().getName()))
            .forEach(v -> createRowForVertex(graph, v, null, collapseState));

        for (Object itemId: m_table.getContainerDataSource().getItemIds()) {
            // Disable the collapse flag on items without any children
            m_table.setChildrenAllowed(itemId, m_table.hasChildren(itemId));
        }

        fireItemSetChange();
    }

    /**
     * Lets the ContainerStrategy know that we changed the item set, so
     * it can adjust the hierarchy accordingly.
     */
    private void fireItemSetChange() {
        m_table.containerItemSetChange(new ItemSetChangeEvent() {
            private static final long serialVersionUID = 1L;
            @Override
            public Container getContainer() {
                return m_beanContainer;
            }
        });
    }
}
