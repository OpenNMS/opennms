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

package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.features.topology.link.Layout;
import org.opennms.features.topology.link.TopologyLinkBuilder;
import org.opennms.features.topology.link.TopologyProvider;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.BusinessServiceStateMachine;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.vaadin.core.TransactionAwareUI;
import org.opennms.netmgt.vaadin.core.UIHelper;

import com.google.common.base.Strings;
import com.google.common.collect.HashBasedTable;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ContainerHierarchicalWrapper;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;

/**
 * {@link TreeTable} for {@link BusinessServiceRow} objects.
 */
public class BusinessServiceTreeTable extends TreeTable {

    private final BusinessServiceManager businessServiceManager;
    private String businessServiceNameFilter = null;

    public BusinessServiceTreeTable(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);

        setSizeFull();
        setContainerDataSource(new BusinessServiceContainer());

        // Add the "LINKS" columns
        addGeneratedColumn("links", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 7113848887128656685L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                final HorizontalLayout layout = new HorizontalLayout();
                final BusinessServiceStateMachine stateMachine = businessServiceManager.getStateMachine();
                final BusinessService businessService = getItem(itemId).getBean().getBusinessService();
                final Status status = stateMachine.getOperationalStatus(businessService);
                if (status != null) {
                    final String topologyLink = new TopologyLinkBuilder()
                            .focus(businessService.getId().toString())
                            .szl(1)
                            .layout(Layout.HIERARCHY)
                            .provider(TopologyProvider.BUSINESS_SERVICE)
                            .getLink();

                    // Generate the link
                    final Link link = new Link("View in Topology UI", new ExternalResource(topologyLink));
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

        // add edit and delete buttons
        addGeneratedColumn("edit / delete", new Table.ColumnGenerator() {
            private static final long serialVersionUID = 7113848887128656685L;

            @Override
            public Object generateCell(Table source, Object itemId, Object columnId) {
                HorizontalLayout layout = new HorizontalLayout();
                layout.setSpacing(true);

                Button editButton = new Button("Edit", FontAwesome.PENCIL_SQUARE_O);
                editButton.setId("editButton-" + getItem(itemId).getBean().getName());

                editButton.addClickListener(UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy((Button.ClickListener) event -> {
                    final BusinessServiceEditWindow window = new BusinessServiceEditWindow(getItem(itemId).getBean().getBusinessService(),
                                                                                           businessServiceManager);
                    window.addCloseListener(e -> refresh());

                    getUI().addWindow(window);
                }));
                layout.addComponent(editButton);

                Button deleteButton = new Button("Delete", FontAwesome.TRASH_O);
                deleteButton.setId("deleteButton-" + getItem(itemId).getBean().getName());

                deleteButton.addClickListener((Button.ClickListener)event -> {
                    BusinessService businessService = getItem(itemId).getBean().getBusinessService();
                    if (businessService.getParentServices().isEmpty() && businessService.getChildEdges().isEmpty()) {
                        UIHelper.getCurrent(TransactionAwareUI.class).runInTransaction(() -> {
                            businessServiceManager.getBusinessServiceById(businessService.getId()).delete();
                            refresh();
                        });
                    } else {
                        new org.opennms.netmgt.vaadin.core.ConfirmationDialog()
                                .withOkAction((org.opennms.netmgt.vaadin.core.ConfirmationDialog.Action) UIHelper.getCurrent(TransactionAwareUI.class).wrapInTransactionProxy(new org.opennms.netmgt.vaadin.core.ConfirmationDialog.Action() {
                                    @Override
                                    public void execute(org.opennms.netmgt.vaadin.core.ConfirmationDialog window) {
                                        businessServiceManager.getBusinessServiceById(businessService.getId()).delete();
                                        refresh();
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

        setColumnExpandRatio("name", 5);
        setColumnExpandRatio("links", 1);
        setColumnExpandRatio("edit / delete", 1);

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(Table source, Object itemId, Object propertyId) {
                if (!Strings.isNullOrEmpty(BusinessServiceTreeTable.this.businessServiceNameFilter) &&
                        source != null &&
                        itemId != null &&
                        BusinessServiceFilter.NAME_PROPERTY.equals(propertyId)) {
                    Item item = source.getItem(itemId);
                    if (item != null) {
                        Property<?> property = item.getItemProperty(BusinessServiceFilter.NAME_PROPERTY);
                        if (property != null) {
                            if (property.getValue() != null) {
                                String value = property.getValue().toString();
                                if (!value.toLowerCase().contains(BusinessServiceTreeTable.this.businessServiceNameFilter)) {
                                    return "grey";
                                }
                            }
                        }
                    }
                }

                return null;
            }
        });
    }

    @Override
    public BeanItem<BusinessServiceRow> getItem(Object itemId) {
        return (BeanItem<BusinessServiceRow>) super.getItem(itemId);
    }

    public void setBusinessServiceNameFilter(String businessServiceNameFilter) {
        this.businessServiceNameFilter = businessServiceNameFilter == null ? "" : businessServiceNameFilter.toLowerCase();
        refresh();
    }

    public void expandForBusinessServiceNameFilter() {
        if (Strings.isNullOrEmpty(businessServiceNameFilter)) {
            return;
        }

        for (Object itemId : getItemIds()) {
            expandSuccessorsForBusinessServiceNameFilter(itemId);
        }
    }

    private void expandSuccessorsForBusinessServiceNameFilter(Object itemId) {
        if (itemId == null) {
            return;
        }

        if (getItem(itemId).getBean().getName().toLowerCase().contains(businessServiceNameFilter)) {
            setCollapsed(itemId, true);
            return;
        }

        setCollapsed(itemId, false);
        Collection<?> children = getChildren(itemId);

        if (children != null) {
            for (Object childItemId : children) {
                expandSuccessorsForBusinessServiceNameFilter(childItemId);
            }
        }
    }

    /**
     * Refreshes table entries.
     */
    public void refresh() {
        final com.google.common.collect.Table<Long, Optional<Long>, Boolean> expandState = getCurrentExpandState();
        final BusinessServiceContainer newContainer = new BusinessServiceContainer();

        if (!Strings.isNullOrEmpty(businessServiceNameFilter)) {
            newContainer.addContainerFilter(new BusinessServiceFilter(newContainer, businessServiceNameFilter));
        }

        // Build a graph using all of the business services stored in the database
        // We don't use the existing graph, since it only contains the services know by the state machine
        List<BusinessService> allBusinessServices = businessServiceManager.getAllBusinessServices();
        final BusinessServiceGraph graph = businessServiceManager.getGraph(allBusinessServices);

        // Recursively generate the table rows, starting with the roots
        graph.getVerticesByLevel(0).stream()
                .filter(v -> v.getBusinessService() != null)
                .sorted((v1, v2) -> v1.getBusinessService().getName().compareTo(v2.getBusinessService().getName()))
                .forEach(v -> newContainer.addRow(graph, v));

        // Make it hierarchical
        Hierarchical hierarchicalContainer = createHierarchicalContainer(newContainer);

        // Update datasource
        setContainerDataSource(hierarchicalContainer);
        setVisibleColumns("name", "links", "edit / delete"); // reset visible columns

        // Restore the previous collapsed state
        List<BusinessServiceRow> rows = getItemIds().stream().map(itemId -> getItem(itemId).getBean()).collect(Collectors.toList());
        applyExpandState(expandState, rows);
    }

    private void applyExpandState(com.google.common.collect.Table<Long, Optional<Long>, Boolean> collapseState, List<BusinessServiceRow> rows) {
        for (BusinessServiceRow row : rows) {
            Boolean wasCollapsed = collapseState.get(row.getBusinessService().getId(), Optional.ofNullable(row.getParentBusinessServiceId()));
            setCollapsed(row.getRowId(), wasCollapsed != null ? wasCollapsed : true); // Collapse by default
            if (hasChildren(row.getRowId())) {
                applyExpandState(collapseState, getChildren(row.getRowId()).stream()
                        .map(itemId -> getItem(itemId).getBean())
                        .collect(Collectors.toList()));
            }
        }
    }

    private com.google.common.collect.Table<Long, Optional<Long>, Boolean> getCurrentExpandState() {
        // Gather the current collapse state
        final com.google.common.collect.Table<Long, Optional<Long>, Boolean> collapseState = HashBasedTable.create();
        for (Object itemId : getItemIds()) {
            final BusinessServiceRow row = getItem(itemId).getBean();
            collapseState.put(row.getBusinessService().getId(), Optional.ofNullable(row.getParentBusinessServiceId()), isCollapsed(itemId));
        }
        return collapseState;
    }

    private static Hierarchical createHierarchicalContainer(BusinessServiceContainer newContainer) {
        final ContainerHierarchicalWrapper hierarchicalContainer = new ContainerHierarchicalWrapper(newContainer);

        // Set child/parent relation
        for(Map.Entry<Long, Long> eachEntry : newContainer.getRowIdToParentRowIdMapping().entrySet()) {
            hierarchicalContainer.setParent(eachEntry.getKey(), eachEntry.getValue());
        }

        // Disable the collapse flag on items without any children
        for (Object itemId : hierarchicalContainer.getItemIds()) {
            hierarchicalContainer.setChildrenAllowed(itemId, hierarchicalContainer.hasChildren(itemId));
        }
        return hierarchicalContainer;
    }
}
