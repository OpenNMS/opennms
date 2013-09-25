/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.features.vaadin.dashboard.dashlets;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.dao.api.NodeDao;

import java.util.Calendar;

/**
 * This class is used to create a configuration window for the {@link RrdDashlet}.
 *
 * @author Christian Pape
 */
public class RrdDashletConfigurationWindow extends DashletConfigurationWindow {
    /**
     * the {@link DashletSpec} to be modified
     */
    private DashletSpec m_dashletSpec;
    /**
     * the node dao instance
     */
    private NodeDao m_nodeDao;
    /**
     * the rrd graph helper instance
     */
    private RrdGraphHelper m_rrdGraphHelper;

    /**
     * the fields for storing the 'width' and  'height' parameter
     */
    private TextField m_widthField, m_heightField;

    /**
     * the selection fields for storing 'rows' and 'columns'
     */
    private NativeSelect m_columnsSelect, m_rowsSelect;

    /**
     * the selection fields for storing 'timeframeValue' and 'timeframeType'
     */
    private NativeSelect m_timeFrameType;
    private TextField m_timeFrameValue;

    /**
     * the grid layout instance
     */
    private GridLayout m_gridLayout;

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link DashletSpec} to be edited
     */
    public RrdDashletConfigurationWindow(DashletSpec dashletSpec, RrdGraphHelper rrdGraphHelper, NodeDao nodeDao) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;
        m_nodeDao = nodeDao;
        m_rrdGraphHelper = rrdGraphHelper;

        /**
         * creating the grid layout
         */
        m_gridLayout = new GridLayout();
        m_gridLayout.setSizeFull();
        m_gridLayout.setColumns(1);
        m_gridLayout.setRows(1);

        /**
         * setting up the layouts
         */
        FormLayout leftFormLayout = new FormLayout();
        FormLayout rightFormLayout = new FormLayout();

        /**
         * creating the columns and rows selection fields
         */
        m_columnsSelect = new NativeSelect();
        m_columnsSelect.setCaption("Columns");
        m_columnsSelect.setImmediate(true);
        m_columnsSelect.setNewItemsAllowed(false);
        m_columnsSelect.setMultiSelect(false);
        m_columnsSelect.setInvalidAllowed(false);
        m_columnsSelect.setNullSelectionAllowed(false);

        m_rowsSelect = new NativeSelect();
        m_rowsSelect.setCaption("Rows");
        m_rowsSelect.setImmediate(true);
        m_rowsSelect.setNewItemsAllowed(false);
        m_rowsSelect.setMultiSelect(false);
        m_rowsSelect.setInvalidAllowed(false);
        m_rowsSelect.setNullSelectionAllowed(false);

        for (int i = 1; i < 5; i++) {
            m_columnsSelect.addItem(i);
            m_rowsSelect.addItem(i);
        }

        /**
         * setting the values/defaults
         */
        int columns;
        int rows;

        try {
            columns = Integer.parseInt(m_dashletSpec.getParameters().get("columns"));
        } catch (NumberFormatException numberFormatException) {
            columns = 1;
        }

        try {
            rows = Integer.parseInt(m_dashletSpec.getParameters().get("rows"));
        } catch (NumberFormatException numberFormatException) {
            rows = 1;
        }

        m_columnsSelect.setValue(columns);
        m_rowsSelect.setValue(rows);

        /**
         * width and height fields
         */
        m_widthField = new TextField();
        m_widthField.setCaption("Graph Width");
        m_widthField.setValue(m_dashletSpec.getParameters().get("width"));

        m_heightField = new TextField();
        m_heightField.setCaption("Graph Height");
        m_heightField.setValue(m_dashletSpec.getParameters().get("height"));

        m_timeFrameValue = new TextField("Timeframe value");
        m_timeFrameValue.setValue(m_dashletSpec.getParameters().get("timeFrameValue"));

        m_timeFrameType = new NativeSelect("Timeframe type");
        m_timeFrameType.setNullSelectionAllowed(false);
        m_timeFrameType.setMultiSelect(false);
        m_timeFrameType.setNewItemsAllowed(false);

        m_timeFrameType.setItemCaptionMode(AbstractSelect.ItemCaptionMode.EXPLICIT);
        m_timeFrameType.addItem(String.valueOf(Calendar.MINUTE));
        m_timeFrameType.setItemCaption(String.valueOf(Calendar.MINUTE), "Minute");

        m_timeFrameType.addItem(String.valueOf(Calendar.HOUR_OF_DAY));
        m_timeFrameType.setItemCaption(String.valueOf(Calendar.HOUR_OF_DAY), "Hour");

        m_timeFrameType.addItem(String.valueOf(Calendar.DAY_OF_YEAR));
        m_timeFrameType.setItemCaption(String.valueOf(Calendar.DAY_OF_YEAR), "Day");

        m_timeFrameType.addItem(String.valueOf(Calendar.WEEK_OF_YEAR));
        m_timeFrameType.setItemCaption(String.valueOf(Calendar.WEEK_OF_YEAR), "Week");

        m_timeFrameType.addItem(String.valueOf(Calendar.MONTH));
        m_timeFrameType.setItemCaption(String.valueOf(Calendar.MONTH), "Month");

        m_timeFrameType.addItem(String.valueOf(Calendar.YEAR));
        m_timeFrameType.setItemCaption(String.valueOf(Calendar.YEAR), "Year");

        m_timeFrameType.setValue(m_dashletSpec.getParameters().get("timeFrameType"));

        m_timeFrameType.setImmediate(true);
        m_timeFrameValue.setImmediate(true);

        m_timeFrameType.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                updatePreview();
            }
        });

        m_timeFrameValue.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                updatePreview();
            }
        });

        /**
         * initial creation of the grid
         */
        recreateCells(columns, rows);

        /**
         * creating the value listeners for columns/rows
         */
        m_columnsSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                recreateCells(Integer.valueOf(valueChangeEvent.getProperty().getValue().toString()), m_gridLayout.getRows());
            }
        });

        m_rowsSelect.addValueChangeListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent valueChangeEvent) {
                recreateCells(m_gridLayout.getColumns(), Integer.valueOf(valueChangeEvent.getProperty().getValue().toString()));
            }
        });

        leftFormLayout.addComponent(m_columnsSelect);
        leftFormLayout.addComponent(m_widthField);
        leftFormLayout.addComponent(m_timeFrameValue);
        rightFormLayout.addComponent(m_rowsSelect);
        rightFormLayout.addComponent(m_heightField);
        rightFormLayout.addComponent(m_timeFrameType);

        /**
         * setting up the layout
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(true);

        horizontalLayout.addComponent(leftFormLayout);
        horizontalLayout.addComponent(rightFormLayout);

        /**
         * Using an additional {@link com.vaadin.ui.HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();

        buttonLayout.setMargin(true);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("100%");

        /**
         * Adding the cancel button...
         */
        Button cancel = new Button("Cancel");
        cancel.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        buttonLayout.addComponent(cancel);
        buttonLayout.setExpandRatio(cancel, 1.0f);
        buttonLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");

        ok.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                /**
                 * saving the data
                 */
                m_dashletSpec.getParameters().put("width", m_widthField.getValue().toString());
                m_dashletSpec.getParameters().put("height", m_heightField.getValue().toString());
                m_dashletSpec.getParameters().put("columns", m_columnsSelect.getValue().toString());
                m_dashletSpec.getParameters().put("rows", m_rowsSelect.getValue().toString());

                int timeFrameValue;
                int timeFrameType;

                try {
                    timeFrameValue = Integer.parseInt(m_timeFrameValue.getValue().toString());
                } catch (NumberFormatException numberFormatException) {
                    timeFrameValue = 1;
                }

                try {
                    timeFrameType = Integer.parseInt(m_timeFrameType.getValue().toString());
                } catch (NumberFormatException numberFormatException) {
                    timeFrameType = Calendar.HOUR;
                }

                m_dashletSpec.getParameters().put("timeFrameType", String.valueOf(timeFrameType));
                m_dashletSpec.getParameters().put("timeFrameValue", String.valueOf(timeFrameValue));

                int i = 0;

                for (int x = 0; x < m_gridLayout.getColumns(); x++) {
                    for (int y = 0; y < m_gridLayout.getRows(); y++) {
                        RrdGraphEntry rrdGraphEntry = (RrdGraphEntry) m_gridLayout.getComponent(x, y);
                        m_dashletSpec.getParameters().put("nodeLabel" + i, rrdGraphEntry.getNodeLabel());
                        m_dashletSpec.getParameters().put("nodeId" + i, rrdGraphEntry.getNodeId());
                        m_dashletSpec.getParameters().put("resourceTypeLabel" + i, rrdGraphEntry.getResourceTypeLabel());
                        m_dashletSpec.getParameters().put("resourceTypeId" + i, rrdGraphEntry.getResourceTypeId());
                        m_dashletSpec.getParameters().put("resourceId" + i, rrdGraphEntry.getResourceId());
                        m_dashletSpec.getParameters().put("resourceLabel" + i, rrdGraphEntry.getResourceLabel());
                        m_dashletSpec.getParameters().put("graphLabel" + i, rrdGraphEntry.getGraphLabel());
                        m_dashletSpec.getParameters().put("graphId" + i, rrdGraphEntry.getGraphId());
                        m_dashletSpec.getParameters().put("graphUrl" + i, rrdGraphEntry.getGraphUrl());

                        i++;
                    }
                }

                WallboardProvider.getInstance().save();
                ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");

                close();
            }
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        buttonLayout.addComponent(ok);

        /**
         * Adding the layout and setting the content
         */

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);

        verticalLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(m_gridLayout);
        verticalLayout.addComponent(buttonLayout);
        verticalLayout.setExpandRatio(m_gridLayout, 2.0f);
        verticalLayout.setSizeFull();

        setContent(verticalLayout);
    }

    /**
     * Updates the preview images on timeframe changes.
     */
    private void updatePreview() {
        /**
         * getting the timeframe values
         */
        int timeFrameValue;
        int timeFrameType;

        try {
            timeFrameValue = Integer.parseInt(m_timeFrameValue.getValue().toString());
        } catch (NumberFormatException numberFormatException) {
            timeFrameValue = 1;
        }

        try {
            timeFrameType = Integer.parseInt(m_timeFrameType.getValue().toString());
        } catch (NumberFormatException numberFormatException) {
            timeFrameType = Calendar.HOUR;
        }

        for (int x = 0; x < m_gridLayout.getColumns(); x++) {
            for (int y = 0; y < m_gridLayout.getRows(); y++) {
                if (m_gridLayout.getComponent(x, y) != null) {
                    ((RrdGraphEntry) m_gridLayout.getComponent(x, y)).setPreviewTimeFrame(timeFrameType, timeFrameValue);
                }
            }
        }
    }

    /**
     * This method sets up the grid with {@link RrdGraphEntry} components
     *
     * @param columns the number of columns
     * @param rows    the number of rows
     */
    private void recreateCells(int columns, int rows) {
        /**
         * removing old entries
         */
        for (int x = 0; x < m_gridLayout.getColumns(); x++) {
            for (int y = 0; y < m_gridLayout.getRows(); y++) {
                if (x >= columns || y >= rows) {
                    m_gridLayout.removeComponent(x, y);
                }
            }
        }

        /**
         * setting the new columns/rows
         */
        m_gridLayout.setColumns(columns);
        m_gridLayout.setRows(rows);

        /**
         * getting the timeframe values
         */
        int timeFrameValue;
        int timeFrameType;

        try {
            timeFrameValue = Integer.parseInt(m_timeFrameValue.getValue().toString());
        } catch (NumberFormatException numberFormatException) {
            timeFrameValue = 1;
        }

        try {
            timeFrameType = Integer.parseInt(m_timeFrameType.getValue().toString());
        } catch (NumberFormatException numberFormatException) {
            timeFrameType = Calendar.HOUR;
        }

        /**
         * adding the new entries
         */
        int i = 0;

        for (int x = 0; x < m_gridLayout.getColumns(); x++) {
            for (int y = 0; y < m_gridLayout.getRows(); y++) {
                if (m_gridLayout.getComponent(x, y) == null) {
                    RrdGraphEntry rrdGraphEntry = new RrdGraphEntry(m_nodeDao, m_rrdGraphHelper, x, y);

                    rrdGraphEntry.setPreviewTimeFrame(timeFrameType, timeFrameValue);
                    /**
                     * setting the values if defined in the {@link DashletSpec}
                     */
                    if (m_dashletSpec.getParameters().containsKey("nodeId" + i)) {
                        rrdGraphEntry.setNodeId(m_dashletSpec.getParameters().get("nodeId" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("nodeLabel" + i)) {
                        rrdGraphEntry.setNodeLabel(m_dashletSpec.getParameters().get("nodeLabel" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("resourceTypeId" + i)) {
                        rrdGraphEntry.setResourceTypeId(m_dashletSpec.getParameters().get("resourceTypeId" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("resourceTypeLabel" + i)) {
                        rrdGraphEntry.setResourceTypeLabel(m_dashletSpec.getParameters().get("resourceTypeLabel" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("resourceId" + i)) {
                        rrdGraphEntry.setResourceId(m_dashletSpec.getParameters().get("resourceId" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("resourceLabel" + i)) {
                        rrdGraphEntry.setResourceLabel(m_dashletSpec.getParameters().get("resourceLabel" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("graphId" + i)) {
                        rrdGraphEntry.setGraphId(m_dashletSpec.getParameters().get("graphId" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("graphLabel" + i)) {
                        rrdGraphEntry.setGraphLabel(m_dashletSpec.getParameters().get("graphLabel" + i));
                    }

                    if (m_dashletSpec.getParameters().containsKey("graphUrl" + i)) {
                        rrdGraphEntry.setGraphUrl(m_dashletSpec.getParameters().get("graphUrl" + i));
                    }

                    rrdGraphEntry.update();

                    m_gridLayout.addComponent(rrdGraphEntry, x, y);
                }
                i++;
            }
        }
    }
}
