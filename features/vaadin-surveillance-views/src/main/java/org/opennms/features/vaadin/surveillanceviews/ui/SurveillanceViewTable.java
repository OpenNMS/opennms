package org.opennms.features.vaadin.surveillanceviews.ui;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import org.opennms.features.vaadin.surveillanceviews.model.ColumnDef;
import org.opennms.features.vaadin.surveillanceviews.model.RowDef;
import org.opennms.features.vaadin.surveillanceviews.model.View;

/**
 * Created by chris on 02.02.15.
 */
public class SurveillanceViewTable extends Table {

    enum TableSelectionMode {
        ALL_SELECTED, ROW_SELECTED, COLUMN_SELECTED, ITEM_SELECTED
    }

    private TableSelectionMode selectionType = TableSelectionMode.ALL_SELECTED;
    private Object selectedItemId, selectedPropertyId;

    public SurveillanceViewTable(View view) {
        setSizeFull();

        setSelectable(true);
        setMultiSelect(true);
        setImmediate(true);
        setSelectable(false);

        addGeneratedColumn("", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                return itemId;
            }
        });

        for (ColumnDef columnDef : view.getColumns()) {
            addGeneratedColumn(columnDef.getLabel(), new Table.ColumnGenerator() {
                public Object generateCell(Table source, final Object itemId, Object columnId) {
                    Label label = new Label("&nbsp;&nbsp;&nbsp;bla");
                    label.setContentMode(ContentMode.HTML);
                    return label;
                }
            });
        }

        for (RowDef rowDef : view.getRows()) {
            addItem(rowDef.getLabel());
            setItemCaption(rowDef.getLabel(), rowDef.getLabel());
        }

        addItemClickListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent itemClickEvent) {
                String selectedColumn = (String) itemClickEvent.getPropertyId();
                if (!"".equals(selectedColumn)) {
                    Notification.show("Item clicked");
                    selectionType = TableSelectionMode.ITEM_SELECTED;
                    selectedItemId = itemClickEvent.getItemId();
                    selectedPropertyId = itemClickEvent.getPropertyId();
                } else {
                    Notification.show("Row clicked");
                    selectionType = TableSelectionMode.ROW_SELECTED;
                    selectedItemId = itemClickEvent.getItemId();
                }

                markAsDirtyRecursive();
            }
        });

        addHeaderClickListener(new HeaderClickListener() {
            @Override
            public void headerClick(HeaderClickEvent headerClickEvent) {
                if ("".equals(headerClickEvent.getPropertyId())) {
                    selectionType = TableSelectionMode.ALL_SELECTED;
                    Notification.show("All clicked");
                } else {
                    selectionType = TableSelectionMode.COLUMN_SELECTED;
                    selectedPropertyId = headerClickEvent.getPropertyId();
                    Notification.show("Header clicked");
                }
                markAsDirtyRecursive();
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(final Table source, final Object itemId, final Object propertyId) {
                String style = null;

                if (selectionType == TableSelectionMode.ALL_SELECTED) {
                    return style;
                }

                if (selectionType == TableSelectionMode.COLUMN_SELECTED) {
                    if (selectedPropertyId.equals(propertyId)) {
                        style = "marked";
                        System.out.println("return getStyle()");
                    }
                }

                if (selectionType == TableSelectionMode.ROW_SELECTED) {
                    if (selectedItemId.equals(itemId) && !"".equals(propertyId)) {
                        style = "marked";
                        System.out.println("return getStyle()");
                    }
                }

                if (selectionType == TableSelectionMode.ITEM_SELECTED) {
                    if (selectedItemId.equals(itemId) && selectedPropertyId.equals(propertyId)) {
                        style = "marked";
                        System.out.println("return getStyle()");
                    }
                }

                return style;
            }
        });
    }
}

