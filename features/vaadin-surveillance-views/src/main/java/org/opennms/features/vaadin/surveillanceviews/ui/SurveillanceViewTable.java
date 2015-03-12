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
import org.opennms.features.vaadin.surveillanceviews.ui.dashboard.SurveillanceViewDetail;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the surveillance view table itself.
 *
 * @author Christian Pape
 */
public class SurveillanceViewTable extends Table {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewTable.class);
    /**
     * the surveillance view service
     */
    private SurveillanceViewService m_surveillanceViewService;
    /**
     * selected item id and property id
     */
    private Object m_selectedItemId, m_selectedPropertyId;
    /**
     * the list of detail tables
     */
    private List<SurveillanceViewDetail> m_detailTables = new ArrayList<SurveillanceViewDetail>();
    /**
     * the selected row categories
     */
    private Set<OnmsCategory> m_selectedRowCategories = null;
    /**
     * the selected column categories
     */
    private Set<OnmsCategory> m_selectedColumnCategories = null;
    /**
     * all row categories
     */
    private Set<OnmsCategory> m_allRowCategories = new HashSet<>();
    /**
     * all column categories
     */
    private Set<OnmsCategory> m_allColumnCategories = new HashSet<>();
    /**
     * the surveillance cell states
     */
    private SurveillanceStatus[][] m_cells;
    /**
     * the category map
     */
    private Map<String, OnmsCategory> m_onmsCategoryMap = new HashMap<>();
    /**
     * the view to be displayed
     */
    private View m_view;
    /**
     * flag whether links are enabled
     */
    private boolean m_enabled;
    /**
     * flag whether dashboard should be displayed
     */
    private boolean m_dashboard;

    /**
     * Constructor for instatiating this component.
     *
     * @param view                    the view to be displayed
     * @param surveillanceViewService the surveillance view service
     * @param dashboard               should the dashboard be displayed?
     * @param enabled                 should links be enabled?
     */
    public SurveillanceViewTable(final View view, SurveillanceViewService surveillanceViewService, boolean dashboard, boolean enabled) {
        /**
         * call the super constructor
         */
        super(null);
        /**
         * set the fields
         */
        this.m_surveillanceViewService = surveillanceViewService;
        this.m_enabled = enabled;
        this.m_dashboard = dashboard;
        this.m_view = view;
        /**
         * initialize this component with the view
         */
        refresh();

        /**
         * fill the categories map
         */
        List<OnmsCategory> onmsCategories = m_surveillanceViewService.getOnmsCategories();

        for (OnmsCategory onmsCategory : onmsCategories) {
            m_onmsCategoryMap.put(onmsCategory.getName(), onmsCategory);
        }

        /**
         * initialize the table features
         */
        setSizeUndefined();
        setWidth(100, Unit.PERCENTAGE);

        setSelectable(true);
        setMultiSelect(true);
        setImmediate(true);
        setSelectable(false);

        /**
         * set the base style name
         */
        addStyleName("surveillance-view");

        /**
         * add row header column
         */
        addGeneratedColumn("", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                Label label = new Label((String) itemId);
                label.setSizeFull();
                label.addStyleName("white");
                return label;
            }
        });

        /**
         * set header title for the row header column
         */
        setColumnHeader("", view.getName());
        setColumnExpandRatio("", 1.0f);

        /**
         * create the other columns
         */
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

            setColumnExpandRatio(columnDef.getLabel(), 1.0f);
        }

        /**
         * gather all row categories
         */
        for (RowDef rowDef : view.getRows()) {
            m_allRowCategories.addAll(getOnmsCategoriesForNames(rowDef.getCategoryNames()));

            addItem(rowDef.getLabel());
            setItemCaption(rowDef.getLabel(), rowDef.getLabel());
        }
        /**
         * per default all is selected
         */
        m_selectedRowCategories = m_allRowCategories;
        m_selectedColumnCategories = m_allColumnCategories;

        /**
         * page length is equal to the row count
         */
        this.setPageLength(this.getItemIds().size());

        /**
         * if dashboard is enabled...
         */
        if (m_dashboard) {
            /**
             * ...add a click listener for cells...
             */
            addItemClickListener(new ItemClickEvent.ItemClickListener() {
                @Override
                public void itemClick(ItemClickEvent itemClickEvent) {
                    String selectedColumn = (String) itemClickEvent.getPropertyId();
                    if (!"".equals(selectedColumn)) {
                        /**
                         * this handles cell clicks
                         */
                        m_selectedItemId = itemClickEvent.getItemId();
                        m_selectedPropertyId = itemClickEvent.getPropertyId();

                        Notification.show(m_selectedItemId + "/" + m_selectedPropertyId + " selected");

                        m_selectedRowCategories = getOnmsCategoriesForNames(view.getRowDef((String) itemClickEvent.getItemId()).getCategoryNames());
                        m_selectedColumnCategories = getOnmsCategoriesForNames(view.getColumnDef((String) itemClickEvent.getPropertyId()).getCategoryNames());
                    } else {
                        /**
                         * this handles row clicks
                         */
                        m_selectedItemId = itemClickEvent.getItemId();

                        Notification.show(m_selectedItemId + " selected");

                        m_selectedRowCategories = getOnmsCategoriesForNames(view.getRowDef((String) itemClickEvent.getItemId()).getCategoryNames());
                        m_selectedColumnCategories = m_allColumnCategories;
                    }

                    updateDetailsTable();
                    markAsDirtyRecursive();
                }
            });

            /**
             * ...and a header click listener...
             */
            addHeaderClickListener(new HeaderClickListener() {
                @Override
                public void headerClick(HeaderClickEvent headerClickEvent) {
                    if ("".equals(headerClickEvent.getPropertyId())) {
                        /**
                         * this handles the upper-left cell
                         */
                        m_selectedRowCategories = m_allRowCategories;
                        m_selectedColumnCategories = m_allColumnCategories;

                        Notification.show("All entries selected");
                    } else {
                        /**
                         * this handles the rest of the header cells
                         */
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
    }

    /**
     * refreshes this surveillance view
     */
    public synchronized void refresh() {
        m_cells = m_surveillanceViewService.calculateCellStatus(m_view);

        refreshRowCache();
        updateDetailsTable();
        markAsDirtyRecursive();
    }

    /**
     * Returns a set of OpenNMS categories for a given collection of view categories.
     *
     * @param collection the collection of view categories
     * @return the set of OpenNMS categories
     */
    private Set<OnmsCategory> getOnmsCategoriesForNames(Collection<String> collection) {
        Set<OnmsCategory> onmsCategories = new HashSet<>();
        for (String name : collection) {
            onmsCategories.add(m_onmsCategoryMap.get(name));
        }
        return onmsCategories;
    }

    /**
     * Refreshes all associated detail tables.
     */
    private void updateDetailsTable() {
        for (SurveillanceViewDetail surveillanceViewDetail : m_detailTables) {
            surveillanceViewDetail.refreshDetails(m_selectedRowCategories, m_selectedColumnCategories);
        }
    }

    /**
     * Associates a detail table with this surveillance view table.
     *
     * @param surveillanceViewDetail the detail table to add
     */
    public void addDetailsTable(SurveillanceViewDetail surveillanceViewDetail) {
        m_detailTables.add(surveillanceViewDetail);
        surveillanceViewDetail.refreshDetails(m_selectedRowCategories, m_selectedColumnCategories);
    }
}

