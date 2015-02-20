/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.model.ColumnDef;
import org.opennms.features.vaadin.surveillanceviews.model.RowDef;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewDetailTable;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.SurveillanceStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SurveillanceViewTable extends Table {

    private SurveillanceViewService m_surveillanceViewService;

    enum TableSelectionMode {
        ALL_SELECTED, ROW_SELECTED, COLUMN_SELECTED, ITEM_SELECTED
    }

    private TableSelectionMode m_selectionType = TableSelectionMode.ALL_SELECTED;
    private Object m_selectedItemId, m_selectedPropertyId;
    private List<SurveillanceViewDetailTable> m_detailTables = new ArrayList<SurveillanceViewDetailTable>();

    private Set<OnmsCategory> m_selectedRowCategories = null;
    private Set<OnmsCategory> m_selectedColumnCategories = null;

    private Set<OnmsCategory> m_allRowCategories = new HashSet<>();
    private Set<OnmsCategory> m_allColumnCategories = new HashSet<>();

    private SurveillanceStatus[][] m_cells;

    private Map<String, OnmsCategory> m_onmsCategoryMap = new HashMap<>();

    private boolean m_enabled, m_dashboard;

    public SurveillanceViewTable(final View view, SurveillanceViewService surveillanceViewService, boolean dashboard, boolean enabled) {
        super(null);

        this.m_surveillanceViewService = surveillanceViewService;
        this.m_enabled = enabled;
        this.m_dashboard = dashboard;

        m_cells = m_surveillanceViewService.calculateCellStatus(view);

        List<OnmsCategory> onmsCategories = m_surveillanceViewService.getOnmsCategories();

        for (OnmsCategory onmsCategory : onmsCategories) {
            m_onmsCategoryMap.put(onmsCategory.getName(), onmsCategory);
        }

        setSizeUndefined();
        setWidth(100, Unit.PERCENTAGE);

        setSelectable(true);
        setMultiSelect(true);
        setImmediate(true);
        setSelectable(false);

        addStyleName("surveillance-view");

        addGeneratedColumn("", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                Label label = new Label((String) itemId);
                label.setSizeFull();
                label.addStyleName("white");
                return label;
            }
        });

        setColumnHeader("", view.getName());

        for (ColumnDef columnDef : view.getColumns()) {
            m_allColumnCategories.addAll(getOnmsCategoriesForNames(columnDef.getCategoryNames()));

            addGeneratedColumn(columnDef.getLabel(), new Table.ColumnGenerator() {
                public Object generateCell(Table source, final Object itemId, Object columnId) {

                    int rowIndex = view.getRows().indexOf(view.getRowDef((String) itemId));
                    int colIndex = view.getColumns().indexOf(view.getColumnDef((String) columnId));
                    SurveillanceStatus surveillanceStatus = m_cells[rowIndex][colIndex];

                    Label label = new Label(surveillanceStatus.getDownEntityCount() + " of " + surveillanceStatus.getTotalEntityCount());

                    label.setSizeFull();
                    label.addStyleName(surveillanceStatus.getStatus().toLowerCase());
                    return label;
                }
            });
        }

        for (RowDef rowDef : view.getRows()) {
            m_allRowCategories.addAll(getOnmsCategoriesForNames(rowDef.getCategoryNames()));

            addItem(rowDef.getLabel());
            setItemCaption(rowDef.getLabel(), rowDef.getLabel());
        }

        m_selectedRowCategories = m_allRowCategories;
        m_selectedColumnCategories = m_allColumnCategories;

        this.setPageLength(this.getItemIds().size());

        if (m_dashboard) {
            addItemClickListener(new ItemClickEvent.ItemClickListener() {
                @Override
                public void itemClick(ItemClickEvent itemClickEvent) {
                    String selectedColumn = (String) itemClickEvent.getPropertyId();
                    if (!"".equals(selectedColumn)) {
                        m_selectionType = TableSelectionMode.ITEM_SELECTED;
                        m_selectedItemId = itemClickEvent.getItemId();
                        m_selectedPropertyId = itemClickEvent.getPropertyId();

                        Notification.show(m_selectedItemId + "/" + m_selectedPropertyId + " selected");

                        m_selectedRowCategories = getOnmsCategoriesForNames(view.getRowDef((String) itemClickEvent.getItemId()).getCategoryNames());
                        m_selectedColumnCategories = getOnmsCategoriesForNames(view.getColumnDef((String) itemClickEvent.getPropertyId()).getCategoryNames());
                    } else {
                        m_selectionType = TableSelectionMode.ROW_SELECTED;
                        m_selectedItemId = itemClickEvent.getItemId();

                        Notification.show(m_selectedItemId + " selected");

                        m_selectedRowCategories = getOnmsCategoriesForNames(view.getRowDef((String) itemClickEvent.getItemId()).getCategoryNames());
                        m_selectedColumnCategories = m_allColumnCategories;
                    }

                    updateDetailsTable();
                    markAsDirtyRecursive();
                }
            });

            addHeaderClickListener(new HeaderClickListener() {
                @Override
                public void headerClick(HeaderClickEvent headerClickEvent) {
                    if ("".equals(headerClickEvent.getPropertyId())) {
                        m_selectionType = TableSelectionMode.ALL_SELECTED;

                        m_selectedRowCategories = m_allRowCategories;
                        m_selectedColumnCategories = m_allColumnCategories;

                        Notification.show("All entries selected");
                    } else {
                        m_selectionType = TableSelectionMode.COLUMN_SELECTED;
                        m_selectedPropertyId = headerClickEvent.getPropertyId();

                        m_selectedRowCategories = m_allRowCategories;
                        m_selectedColumnCategories = getOnmsCategoriesForNames(view.getColumnDef((String) headerClickEvent.getPropertyId()).getCategoryNames());

                        Notification.show(m_selectedPropertyId + " selected");
                    }

                    updateDetailsTable();
                    markAsDirtyRecursive();
                }
            });
        }

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                String style = null;

                if (m_selectionType == TableSelectionMode.ALL_SELECTED) {
                    return style;
                }

                if (m_selectionType == TableSelectionMode.COLUMN_SELECTED) {
                    if (m_selectedPropertyId.equals(propertyId)) {
                        style = "marked";
                    }
                }

                if (m_selectionType == TableSelectionMode.ROW_SELECTED) {
                    if (m_selectedItemId.equals(itemId) && !"".equals(propertyId)) {
                        style = "marked";
                    }
                }

                if (m_selectionType == TableSelectionMode.ITEM_SELECTED) {
                    if (m_selectedItemId.equals(itemId) && m_selectedPropertyId.equals(propertyId)) {
                        style = "marked";
                    }
                }

                return style;
            }
        });
    }

    private Set<OnmsCategory> getOnmsCategoriesForNames(Collection<String> collection) {
        Set<OnmsCategory> onmsCategories = new HashSet<>();
        for (String name : collection) {
            onmsCategories.add(m_onmsCategoryMap.get(name));
        }
        return onmsCategories;
    }

    private void updateDetailsTable() {
        for (SurveillanceViewDetailTable surveillanceViewDetailTable : m_detailTables) {
            surveillanceViewDetailTable.refreshDetails(m_selectedRowCategories, m_selectedColumnCategories);
        }
    }

    public void addDetailsTable(SurveillanceViewDetailTable surveillanceViewDetailTable) {
        m_detailTables.add(surveillanceViewDetailTable);
        surveillanceViewDetailTable.refreshDetails(m_selectedRowCategories, m_selectedColumnCategories);
    }
}

