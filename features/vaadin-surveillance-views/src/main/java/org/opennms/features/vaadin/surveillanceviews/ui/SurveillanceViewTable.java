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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SurveillanceViewTable extends Table {

    private SurveillanceViewService m_surveillanceViewService;

    enum TableSelectionMode {
        ALL_SELECTED, ROW_SELECTED, COLUMN_SELECTED, ITEM_SELECTED
    }

    private TableSelectionMode m_selectionType = TableSelectionMode.ALL_SELECTED;
    private Object m_selectedItemId, m_selectedPropertyId;
    private List<SurveillanceViewDetailTable> m_detailTables = new ArrayList<SurveillanceViewDetailTable>();

    private Set<String> m_selectedRowCategories = null;
    private Set<String> m_selectedColumnCategories = null;

    SurveillanceStatus[][] cells;

    public SurveillanceViewTable(final View view, SurveillanceViewService surveillanceViewService) {

        this.m_surveillanceViewService = surveillanceViewService;

        cells = m_surveillanceViewService.calculateCellStatus(view);

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
                label.addStyleName("clear");
                return label;
            }
        });


        for (ColumnDef columnDef : view.getColumns()) {
            addGeneratedColumn(columnDef.getLabel(), new Table.ColumnGenerator() {
                public Object generateCell(Table source, final Object itemId, Object columnId) {

                    int rowIndex = view.getRows().indexOf(view.getRowDef((String) itemId));
                    int colIndex = view.getColumns().indexOf(view.getColumnDef((String) columnId));
                    SurveillanceStatus surveillanceStatus = cells[rowIndex][colIndex];

                    Label label = new Label(surveillanceStatus.getDownEntityCount() + " of " + surveillanceStatus.getTotalEntityCount());

                    label.setSizeFull();
                    label.addStyleName(surveillanceStatus.getStatus().toLowerCase());
                    return label;
                }
            });
        }

        for (RowDef rowDef : view.getRows()) {
            addItem(rowDef.getLabel());
            setItemCaption(rowDef.getLabel(), rowDef.getLabel());
        }

        this.setPageLength(this.getItemIds().size());

        addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                String selectedColumn = (String) itemClickEvent.getPropertyId();
                if (!"".equals(selectedColumn)) {
                    Notification.show("Item clicked");
                    m_selectionType = TableSelectionMode.ITEM_SELECTED;
                    m_selectedItemId = itemClickEvent.getItemId();
                    m_selectedPropertyId = itemClickEvent.getPropertyId();

                    m_selectedRowCategories = view.getRowDef((String) itemClickEvent.getItemId()).getCategoryNames();
                    m_selectedColumnCategories = view.getColumnDef((String) itemClickEvent.getPropertyId()).getCategoryNames();

                } else {
                    Notification.show("Row clicked");
                    m_selectionType = TableSelectionMode.ROW_SELECTED;
                    m_selectedItemId = itemClickEvent.getItemId();

                    m_selectedRowCategories = view.getRowDef((String) itemClickEvent.getItemId()).getCategoryNames();
                    m_selectedColumnCategories = null;
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

                    m_selectedRowCategories = null;
                    m_selectedColumnCategories = null;

                    Notification.show("All clicked");
                } else {
                    m_selectionType = TableSelectionMode.COLUMN_SELECTED;
                    m_selectedPropertyId = headerClickEvent.getPropertyId();

                    m_selectedRowCategories = null;
                    m_selectedColumnCategories = view.getColumnDef((String) headerClickEvent.getPropertyId()).getCategoryNames();

                    Notification.show("Header clicked");
                }

                updateDetailsTable();

                markAsDirtyRecursive();
            }
        });

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
                        System.out.println("return getStyle()");
                    }
                }

                if (m_selectionType == TableSelectionMode.ROW_SELECTED) {
                    if (m_selectedItemId.equals(itemId) && !"".equals(propertyId)) {
                        style = "marked";
                        System.out.println("return getStyle()");
                    }
                }

                if (m_selectionType == TableSelectionMode.ITEM_SELECTED) {
                    if (m_selectedItemId.equals(itemId) && m_selectedPropertyId.equals(propertyId)) {
                        style = "marked";
                        System.out.println("return getStyle()");
                    }
                }

                return style;
            }
        });
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

