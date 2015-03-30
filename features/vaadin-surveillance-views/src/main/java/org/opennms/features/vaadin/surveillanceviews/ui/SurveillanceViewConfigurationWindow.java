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

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.DefaultItemSorter;
import com.vaadin.data.validator.AbstractStringValidator;
import com.vaadin.event.ShortcutAction;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.opennms.features.vaadin.surveillanceviews.config.SurveillanceViewProvider;
import org.opennms.features.vaadin.surveillanceviews.model.ColumnDef;
import org.opennms.features.vaadin.surveillanceviews.model.Def;
import org.opennms.features.vaadin.surveillanceviews.model.RowDef;
import org.opennms.features.vaadin.surveillanceviews.model.View;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to display the surveillance view configuration dialog.
 *
 * @author Christian Pape
 */
public class SurveillanceViewConfigurationWindow extends Window {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewConfigurationWindow.class);

    /**
     * The constructor for instantiating this component.
     *
     * @param surveillanceViewService the surveillance view service to be used
     * @param view                    the view to edit
     * @param saveActionListener      the save action listener
     */
    public SurveillanceViewConfigurationWindow(final SurveillanceViewService surveillanceViewService, final View view, final SaveActionListener saveActionListener) {
        /**
         * Setting the title
         */
        super("Surveillance view configuration");

        /**
         * Setting the modal and size properties
         */
        setModal(true);
        setClosable(false);
        setResizable(false);
        setWidth(80, Sizeable.Unit.PERCENTAGE);
        setHeight(66, Sizeable.Unit.PERCENTAGE);

        /**
         * Title field
         */
        final TextField titleField = new TextField();
        titleField.setValue(view.getName());
        titleField.setImmediate(true);
        titleField.setCaption("Title");
        titleField.setDescription("Title of this surveillance view");
        titleField.setWidth(25, Unit.PERCENTAGE);

        /**
         * Adding simple validator
         */
        titleField.addValidator(new AbstractStringValidator("Please use an unique name for the surveillance view") {
            @Override
            protected boolean isValidValue(String string) {
                if ("".equals(string.trim())) {
                    return false;
                }
                if (SurveillanceViewProvider.getInstance().containsView(string) && !view.getName().equals(string)) {
                    return false;
                }
                return true;
            }
        });

        /**
         * Refresh seconds field setup and validator
         */
        final TextField refreshSecondsField = new TextField();
        refreshSecondsField.setValue(String.valueOf(view.getRefreshSeconds()));
        refreshSecondsField.setImmediate(true);
        refreshSecondsField.setCaption("Refresh seconds");
        refreshSecondsField.setDescription("Refresh duration in seconds");

        refreshSecondsField.addValidator(new AbstractStringValidator("Only numbers allowed here") {
            @Override
            protected boolean isValidValue(String s) {
                int number;
                try {
                    number = Integer.parseInt(s);
                } catch (NumberFormatException numberFormatException) {
                    return false;
                }
                return (number >= 0);
            }
        });

        /**
         * Columns table
         */
        final Table columnsTable = new Table();

        columnsTable.setSortEnabled(false);
        columnsTable.setWidth(25, Unit.PERCENTAGE);

        final BeanItemContainer<ColumnDef> columns = new BeanItemContainer<ColumnDef>(ColumnDef.class, view.getColumns());

        final Map<ColumnDef, Integer> columnOrder = new HashMap<>();

        int c = 0;
        for (ColumnDef columnDef : view.getColumns()) {
            columnOrder.put(columnDef, c++);
        }

        columnsTable.setContainerDataSource(columns);

        columnsTable.setVisibleColumns("label");
        columnsTable.setColumnHeader("label", "Columns");
        columnsTable.setColumnExpandRatio("label", 1.0f);
        columnsTable.setSelectable(true);
        columnsTable.setMultiSelect(false);

        /**
         * Create custom sorter
         */
        columns.setItemSorter(new DefaultItemSorter() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == null) {
                    if (o2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (o2 == null) {
                    return 1;
                }

                if (columnOrder.get(o1).intValue() == columnOrder.get(o2).intValue()) {
                    return 0;
                } else {
                    if (columnOrder.get(o1).intValue() > columnOrder.get(o2).intValue()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        });

        /**
         * Adding the buttons...
         */
        final Button columnsAddButton = new Button("Add");
        columnsAddButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                getUI().addWindow(new SurveillanceViewConfigurationCategoryWindow(surveillanceViewService, columnsTable.getItemIds(), new ColumnDef(), new SurveillanceViewConfigurationCategoryWindow.SaveActionListener() {
                    @Override
                    public void save(Def def) {
                        columns.addItem((ColumnDef) def);
                        columnOrder.put((ColumnDef) def, columnOrder.size());

                        columns.sort(new Object[]{"label"}, new boolean[]{true});
                        columnsTable.refreshRowCache();
                    }
                }));
            }
        });

        columnsAddButton.setEnabled(true);
        columnsAddButton.setStyleName("small");
        columnsAddButton.setDescription("Add column");
        columnsAddButton.setEnabled(true);

        final Button columnsEditButton = new Button("Edit");
        columnsEditButton.setEnabled(true);
        columnsEditButton.setStyleName("small");
        columnsEditButton.setDescription("Edit column");
        columnsEditButton.setEnabled(false);

        columnsEditButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                getUI().addWindow(new SurveillanceViewConfigurationCategoryWindow(surveillanceViewService, columnsTable.getItemIds(), (ColumnDef) columnsTable.getValue(), new SurveillanceViewConfigurationCategoryWindow.SaveActionListener() {
                    @Override
                    public void save(Def def) {
                        ColumnDef columnToBeReplaced = (ColumnDef) columnsTable.getValue();
                        int index = columnOrder.get(columnToBeReplaced);

                        columns.removeItem(columnToBeReplaced);
                        columnOrder.remove(columnToBeReplaced);

                        columns.addItem((ColumnDef) def);
                        columnOrder.put((ColumnDef) def, index);

                        columns.sort(new Object[]{"label"}, new boolean[]{true});
                        columnsTable.refreshRowCache();
                    }
                }));
            }
        });


        final Button columnsRemoveButton = new Button("Remove");
        columnsRemoveButton.setEnabled(true);
        columnsRemoveButton.setStyleName("small");
        columnsRemoveButton.setDescription("Remove column");
        columnsRemoveButton.setEnabled(false);

        columnsRemoveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                ColumnDef columnDef = (ColumnDef) columnsTable.getValue();
                if (columnDef != null) {
                    columnsTable.unselect(columnDef);
                    columns.removeItem(columnDef);
                }

                columnsTable.refreshRowCache();
            }
        });


        final Button columnUpButton = new Button();
        columnUpButton.setStyleName("small");
        columnUpButton.setIcon(new ThemeResource("../runo/icons/16/arrow-up.png"));
        columnUpButton.setDescription("Move this a column entry one position up");
        columnUpButton.setEnabled(false);

        columnUpButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                ColumnDef columnDef = (ColumnDef) columnsTable.getValue();
                if (columnDef != null) {
                    int columnDefIndex = columnOrder.get(columnDef);

                    ColumnDef columnDefToSwap = null;

                    for (Map.Entry<ColumnDef, Integer> entry : columnOrder.entrySet()) {
                        if (entry.getValue().intValue() == columnDefIndex - 1) {
                            columnDefToSwap = entry.getKey();
                            break;
                        }
                    }

                    if (columnDefToSwap != null) {
                        columnsTable.unselect(columnDef);
                        columnOrder.remove(columnDef);
                        columnOrder.remove(columnDefToSwap);
                        columnOrder.put(columnDef, columnDefIndex - 1);
                        columnOrder.put(columnDefToSwap, columnDefIndex);

                        columns.sort(new Object[]{"label"}, new boolean[]{true});
                        columnsTable.refreshRowCache();
                        columnsTable.select(columnDef);
                    }

                }
            }
        });

        final Button columnDownButton = new Button();
        columnDownButton.setStyleName("small");
        columnDownButton.setIcon(new ThemeResource("../runo/icons/16/arrow-down.png"));
        columnDownButton.setDescription("Move this a column entry one position down");
        columnDownButton.setEnabled(false);

        columnDownButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                ColumnDef columnDef = (ColumnDef) columnsTable.getValue();
                if (columnDef != null) {
                    int columnDefIndex = columnOrder.get(columnDef);

                    ColumnDef columnDefToSwap = null;

                    for (Map.Entry<ColumnDef, Integer> entry : columnOrder.entrySet()) {
                        if (entry.getValue().intValue() == columnDefIndex + 1) {
                            columnDefToSwap = entry.getKey();
                            break;
                        }
                    }

                    if (columnDefToSwap != null) {
                        columnsTable.unselect(columnDef);
                        columnOrder.remove(columnDef);
                        columnOrder.remove(columnDefToSwap);
                        columnOrder.put(columnDef, columnDefIndex + 1);
                        columnOrder.put(columnDefToSwap, columnDefIndex);

                        columns.sort(new Object[]{"label"}, new boolean[]{true});
                        columnsTable.refreshRowCache();
                        columnsTable.select(columnDef);
                    }
                }
            }
        });

        columnsTable.setSizeFull();

        columnUpButton.setSizeFull();
        columnDownButton.setSizeFull();
        columnsAddButton.setSizeFull();
        columnsEditButton.setSizeFull();
        columnsRemoveButton.setSizeFull();

        columnsTable.setImmediate(true);

        /**
         * ...and a listener
         */
        columnsTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                boolean somethingSelected = (columnsTable.getValue() != null);
                columnsRemoveButton.setEnabled(somethingSelected);
                columnsEditButton.setEnabled(somethingSelected);
                columnsAddButton.setEnabled(true);
                columnUpButton.setEnabled(somethingSelected && columnOrder.get(columnsTable.getValue()).intValue() > 0);
                columnDownButton.setEnabled(somethingSelected && columnOrder.get(columnsTable.getValue()).intValue() < columnOrder.size() - 1);
            }
        });

        /**
         * Rows table
         */
        final Table rowsTable = new Table();

        rowsTable.setSortEnabled(false);
        rowsTable.setWidth(25, Unit.PERCENTAGE);

        final BeanItemContainer<RowDef> rows = new BeanItemContainer<RowDef>(RowDef.class, view.getRows());

        final Map<RowDef, Integer> rowOrder = new HashMap<>();

        int r = 0;
        for (RowDef rowDef : view.getRows()) {
            rowOrder.put(rowDef, r++);
        }

        rowsTable.setContainerDataSource(rows);

        rowsTable.setVisibleColumns("label");
        rowsTable.setColumnHeader("label", "Rows");
        rowsTable.setColumnExpandRatio("label", 1.0f);
        rowsTable.setSelectable(true);
        rowsTable.setMultiSelect(false);

        /**
         * Create custom sorter
         */
        rows.setItemSorter(new DefaultItemSorter() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 == null) {
                    if (o2 == null) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                if (o2 == null) {
                    return 1;
                }

                if (rowOrder.get(o1).intValue() == rowOrder.get(o2).intValue()) {
                    return 0;
                } else {
                    if (rowOrder.get(o1).intValue() > rowOrder.get(o2).intValue()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }
        });

        /**
         * Adding the buttons...
         */
        final Button rowsAddButton = new Button("Add");
        rowsAddButton.setEnabled(true);
        rowsAddButton.setStyleName("small");
        rowsAddButton.setDescription("Add row");
        rowsAddButton.setEnabled(true);

        rowsAddButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                getUI().addWindow(new SurveillanceViewConfigurationCategoryWindow(surveillanceViewService, rowsTable.getItemIds(), new RowDef(), new SurveillanceViewConfigurationCategoryWindow.SaveActionListener() {
                    @Override
                    public void save(Def def) {
                        rows.addItem((RowDef) def);
                        rowOrder.put((RowDef) def, rowOrder.size());

                        rows.sort(new Object[]{"label"}, new boolean[]{true});
                        rowsTable.refreshRowCache();
                    }
                }));
            }
        });

        final Button rowsEditButton = new Button("Edit");
        rowsEditButton.setEnabled(true);
        rowsEditButton.setStyleName("small");
        rowsEditButton.setDescription("Edit row");
        rowsEditButton.setEnabled(false);

        rowsEditButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                getUI().addWindow(new SurveillanceViewConfigurationCategoryWindow(surveillanceViewService, rowsTable.getItemIds(), (RowDef) rowsTable.getValue(), new SurveillanceViewConfigurationCategoryWindow.SaveActionListener() {
                    @Override
                    public void save(Def def) {
                        RowDef rowToBeReplaced = (RowDef) rowsTable.getValue();
                        int index = rowOrder.get(rowToBeReplaced);

                        rows.removeItem(rowToBeReplaced);
                        rowOrder.remove(rowToBeReplaced);

                        rows.addItem((RowDef) def);
                        rowOrder.put((RowDef) def, index);

                        rows.sort(new Object[]{"label"}, new boolean[]{true});
                        rowsTable.refreshRowCache();
                    }
                }));
            }
        });


        final Button rowsRemoveButton = new Button("Remove");
        rowsRemoveButton.setEnabled(true);
        rowsRemoveButton.setStyleName("small");
        rowsRemoveButton.setDescription("Remove row");
        rowsRemoveButton.setEnabled(false);

        rowsRemoveButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                RowDef rowDef = (RowDef) rowsTable.getValue();
                if (rowDef != null) {
                    rowsTable.unselect(rowDef);
                    rows.removeItem(rowDef);
                }

                rowsTable.refreshRowCache();
            }
        });

        final Button rowUpButton = new Button();
        rowUpButton.setStyleName("small");
        rowUpButton.setIcon(new ThemeResource("../runo/icons/16/arrow-up.png"));
        rowUpButton.setDescription("Move this a row entry one position up");
        rowUpButton.setEnabled(false);

        rowUpButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                RowDef rowDef = (RowDef) rowsTable.getValue();
                if (rowDef != null) {
                    int rowDefIndex = rowOrder.get(rowDef);

                    RowDef rowDefToSwap = null;

                    for (Map.Entry<RowDef, Integer> entry : rowOrder.entrySet()) {
                        if (entry.getValue().intValue() == rowDefIndex - 1) {
                            rowDefToSwap = entry.getKey();
                            break;
                        }
                    }

                    if (rowDefToSwap != null) {
                        rowsTable.unselect(rowDef);
                        rowOrder.remove(rowDef);
                        rowOrder.remove(rowDefToSwap);
                        rowOrder.put(rowDef, rowDefIndex - 1);
                        rowOrder.put(rowDefToSwap, rowDefIndex);

                        rows.sort(new Object[]{"label"}, new boolean[]{true});
                        rowsTable.refreshRowCache();
                        rowsTable.select(rowDef);
                    }
                }
            }
        });

        final Button rowDownButton = new Button();
        rowDownButton.setStyleName("small");
        rowDownButton.setIcon(new ThemeResource("../runo/icons/16/arrow-down.png"));
        rowDownButton.setDescription("Move this a row entry one position down");
        rowDownButton.setEnabled(false);

        rowDownButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                RowDef rowDef = (RowDef) rowsTable.getValue();
                if (rowDef != null) {
                    int rowDefIndex = rowOrder.get(rowDef);

                    RowDef rowDefToSwap = null;

                    for (Map.Entry<RowDef, Integer> entry : rowOrder.entrySet()) {
                        if (entry.getValue().intValue() == rowDefIndex + 1) {
                            rowDefToSwap = entry.getKey();
                            break;
                        }
                    }

                    if (rowDefToSwap != null) {
                        rowsTable.unselect(rowDef);
                        rowOrder.remove(rowDef);
                        rowOrder.remove(rowDefToSwap);
                        rowOrder.put(rowDef, rowDefIndex + 1);
                        rowOrder.put(rowDefToSwap, rowDefIndex);

                        rows.sort(new Object[]{"label"}, new boolean[]{true});
                        rowsTable.refreshRowCache();
                        rowsTable.select(rowDef);
                    }
                }
            }
        });

        rowsTable.setSizeFull();

        rowUpButton.setSizeFull();
        rowDownButton.setSizeFull();
        rowsAddButton.setSizeFull();
        rowsEditButton.setSizeFull();
        rowsRemoveButton.setSizeFull();

        rowsTable.setImmediate(true);

        /**
         * ...and a listener
         */
        rowsTable.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                boolean somethingSelected = (rowsTable.getValue() != null);
                rowsRemoveButton.setEnabled(somethingSelected);
                rowsEditButton.setEnabled(somethingSelected);
                rowsAddButton.setEnabled(true);
                rowUpButton.setEnabled(somethingSelected && rowOrder.get(rowsTable.getValue()).intValue() > 0);
                rowDownButton.setEnabled(somethingSelected && rowOrder.get(rowsTable.getValue()).intValue() < rowOrder.size() - 1);
            }
        });

        /**
         * Create form layouts...
         */
        FormLayout baseFormLayout = new FormLayout();
        baseFormLayout.addComponent(titleField);
        baseFormLayout.addComponent(refreshSecondsField);

        FormLayout columnTableFormLayout = new FormLayout();
        columnTableFormLayout.addComponent(columnsAddButton);
        columnTableFormLayout.addComponent(columnsEditButton);
        columnTableFormLayout.addComponent(columnsRemoveButton);
        columnTableFormLayout.addComponent(columnUpButton);
        columnTableFormLayout.addComponent(columnDownButton);

        FormLayout rowTableFormLayout = new FormLayout();
        rowTableFormLayout.addComponent(rowsAddButton);
        rowTableFormLayout.addComponent(rowsEditButton);
        rowTableFormLayout.addComponent(rowsRemoveButton);
        rowTableFormLayout.addComponent(rowUpButton);
        rowTableFormLayout.addComponent(rowDownButton);

        /**
         * Adding the different {@link com.vaadin.ui.FormLayout} instances to a {@link com.vaadin.ui.GridLayout}
         */
        baseFormLayout.setMargin(true);
        columnTableFormLayout.setMargin(true);
        rowTableFormLayout.setMargin(true);

        GridLayout gridLayout = new GridLayout();
        gridLayout.setSizeFull();
        gridLayout.setColumns(4);
        gridLayout.setRows(1);
        gridLayout.setMargin(true);

        gridLayout.addComponent(rowsTable);
        gridLayout.addComponent(rowTableFormLayout);
        gridLayout.addComponent(columnsTable);
        gridLayout.addComponent(columnTableFormLayout);

        gridLayout.setColumnExpandRatio(1, 0.5f);
        gridLayout.setColumnExpandRatio(2, 1.0f);
        gridLayout.setColumnExpandRatio(3, 0.5f);
        gridLayout.setColumnExpandRatio(4, 1.0f);

        /**
         * Creating the vertical layout...
         */
        VerticalLayout verticalLayout = new VerticalLayout();

        verticalLayout.addComponent(baseFormLayout);
        verticalLayout.addComponent(gridLayout);

        /**
         * Using an additional {@link com.vaadin.ui.HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.setMargin(true);
        horizontalLayout.setSpacing(true);
        horizontalLayout.setWidth(100, Unit.PERCENTAGE);

        /**
         * Adding the cancel button...
         */
        Button cancel = new Button("Cancel");
        cancel.setDescription("Cancel editing properties");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        horizontalLayout.addComponent(cancel);
        horizontalLayout.setExpandRatio(cancel, 1);
        horizontalLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");
        ok.setDescription("Save properties and close");

        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (!titleField.isValid()) {
                    ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Error", "Please use an unique title", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (!refreshSecondsField.isValid()) {
                    ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Error", "Please enter a valid number in the \"Refresh seconds\" field", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                if (columns.getItemIds().isEmpty() || rows.getItemIds().isEmpty()) {
                    ((SurveillanceViewsConfigUI) getUI()).notifyMessage("Error", "You must define at least one row category and one column category", Notification.Type.ERROR_MESSAGE);
                    return;
                }

                View finalView = new View();

                for (ColumnDef columnDef : columns.getItemIds()) {
                    finalView.getColumns().add(columnDef);
                }

                for (RowDef rowDef : rows.getItemIds()) {
                    finalView.getRows().add(rowDef);
                }

                finalView.setName(titleField.getValue());
                finalView.setRefreshSeconds(Integer.parseInt(refreshSecondsField.getValue()));

                saveActionListener.save(finalView);

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        horizontalLayout.addComponent(ok);

        verticalLayout.addComponent(horizontalLayout);

        setContent(verticalLayout);
    }

    /**
     * Interface for a listner that will be invoked when OK is clicked
     */
    public static interface SaveActionListener {
        void save(View view);
    }
}
