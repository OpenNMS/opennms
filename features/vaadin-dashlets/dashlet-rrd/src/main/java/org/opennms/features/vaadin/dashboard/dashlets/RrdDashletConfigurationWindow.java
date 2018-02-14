/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.dashlets;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.OnmsResourceType;

import com.vaadin.data.Property;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

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
     * the KSC report factory
     */
    private KSC_PerformanceReportFactory kscPerformanceReportFactory;

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
        FormLayout middleFormLayout = new FormLayout();
        FormLayout rightFormLayout = new FormLayout();

        /**
         * creating the columns and rows selection fields
         */
        m_columnsSelect = new NativeSelect();
        m_columnsSelect.setCaption("Columns");
        m_columnsSelect.setDescription("Number of columns");
        m_columnsSelect.setImmediate(true);
        m_columnsSelect.setNewItemsAllowed(false);
        m_columnsSelect.setMultiSelect(false);
        m_columnsSelect.setInvalidAllowed(false);
        m_columnsSelect.setNullSelectionAllowed(false);

        m_rowsSelect = new NativeSelect();
        m_rowsSelect.setCaption("Rows");
        m_rowsSelect.setDescription("Number of rows");
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
        m_widthField.setDescription("Width of graphs");
        m_widthField.setValue(m_dashletSpec.getParameters().get("width"));

        m_heightField = new TextField();
        m_heightField.setCaption("Graph Height");
        m_heightField.setDescription("Height of graphs");
        m_heightField.setValue(m_dashletSpec.getParameters().get("height"));

        m_timeFrameValue = new TextField("Timeframe value");
        m_timeFrameValue.setDescription("Timeframe value");
        m_timeFrameValue.setValue(m_dashletSpec.getParameters().get("timeFrameValue"));

        m_timeFrameType = new NativeSelect("Timeframe type");
        m_timeFrameType.setDescription("Timeframe type");
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
        middleFormLayout.addComponent(m_rowsSelect);
        middleFormLayout.addComponent(m_heightField);
        middleFormLayout.addComponent(m_timeFrameType);

        /**
         * KSC import stuff
         */
        Button importButton = new Button("KSC Import");
        importButton.setDescription("Import KSC-report");
        final NativeSelect selectKSCReport = new NativeSelect();
        selectKSCReport.setDescription("KSC-report selection");
        selectKSCReport.setCaption("KSC Report");
        selectKSCReport.setImmediate(true);
        selectKSCReport.setNewItemsAllowed(false);
        selectKSCReport.setMultiSelect(false);
        selectKSCReport.setInvalidAllowed(false);
        selectKSCReport.setNullSelectionAllowed(false);
        selectKSCReport.setImmediate(true);

        kscPerformanceReportFactory = KSC_PerformanceReportFactory.getInstance();

        Map<Integer, String> mapOfKscReports = kscPerformanceReportFactory.getReportList();

        if (mapOfKscReports.size() == 0) {
            importButton.setEnabled(false);
        }

        for (Map.Entry<Integer, String> entry : mapOfKscReports.entrySet()) {
            selectKSCReport.addItem(entry.getKey());

            selectKSCReport.setItemCaption(entry.getKey(), entry.getValue());

            if (selectKSCReport.getValue() == null) {
                selectKSCReport.setValue(entry.getKey());
            }
        }

        importButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                importKscReport(Integer.valueOf(selectKSCReport.getValue().toString()));
            }
        });

        rightFormLayout.addComponent(selectKSCReport);
        rightFormLayout.addComponent(importButton);

        /**
         * setting up the layout
         */
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.setMargin(true);

        horizontalLayout.addComponent(leftFormLayout);
        horizontalLayout.addComponent(middleFormLayout);
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
        cancel.setDescription("Cancel editing");
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
        ok.setDescription("Save properties and close");

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

                for (int y = 0; y < m_gridLayout.getRows(); y++) {
                    for (int x = 0; x < m_gridLayout.getColumns(); x++) {
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

    private void setRrdGraphEntryFromKscReportGraph(RrdGraphEntry rrdGraphEntry, Graph graph) {

        String graphLabel, graphId, graphUrl, nodeId, nodeLabel, resourceLabel, resourceTypeId, resourceTypeLabel;

        String[] graphTypeArr = graph.getGraphtype().split("\\.");
        String[] resourceIdArr = graph.getResourceId().orElse("").split("\\.");

        nodeId = resourceIdArr[0].split("[\\[\\]]")[1];
        String resourceTypeName = resourceIdArr[1].split("[\\[\\]]")[0];

        OnmsNode onmsNode = m_nodeDao.get(nodeId);
        nodeLabel = onmsNode.getLabel();

        Map<OnmsResourceType, List<OnmsResource>> resourceTypeListMap = m_rrdGraphHelper.getResourceTypeMapForNodeId(nodeId);

        for (Map.Entry<OnmsResourceType, List<OnmsResource>> entry : resourceTypeListMap.entrySet()) {
            OnmsResourceType onmsResourceType = entry.getKey();

            if (resourceTypeName.equals(onmsResourceType.getName())) {
                resourceTypeId = "node[" + nodeId + "]." + resourceTypeName;
                resourceTypeLabel = onmsResourceType.getLabel();
                List<OnmsResource> onmsResourceList = entry.getValue();

                for (OnmsResource onmsResource : onmsResourceList) {

                    String onmsResourceId = onmsResource.getId().toString();

                    if (onmsResourceId.equals(graph.getResourceId())) {
                        resourceLabel = onmsResource.getLabel();

                        Map<String, String> resultsMap = m_rrdGraphHelper.getGraphResultsForResourceId(onmsResource.getId());
                        Map<String, String> nameTitleMapping = m_rrdGraphHelper.getGraphNameTitleMappingForResourceId(onmsResource.getId());

                        graphId = onmsResourceId + "." + nameTitleMapping.get(graph.getGraphtype());

                        graphLabel = nameTitleMapping.get(graph.getGraphtype());
                        graphUrl = resultsMap.get(graph.getGraphtype());

                        rrdGraphEntry.setNodeId(nodeId);
                        rrdGraphEntry.setNodeLabel(nodeLabel);
                        rrdGraphEntry.setResourceTypeId(resourceTypeId);
                        rrdGraphEntry.setResourceTypeLabel(resourceTypeLabel);
                        rrdGraphEntry.setResourceId(onmsResourceId);
                        rrdGraphEntry.setResourceLabel(resourceLabel);
                        rrdGraphEntry.setGraphId(graphId);
                        rrdGraphEntry.setGraphLabel(graphLabel);
                        rrdGraphEntry.setGraphUrl(graphUrl);

                        break;
                    }
                }
                break;
            }
        }
    }

    /**
     * Import the KSC report with the given name
     */
    private void importKscReport(int reportId) {
        Report report = kscPerformanceReportFactory.getReportByIndex(reportId);

        int columns = Math.max(1, report.getGraphsPerLine().orElse(1));

        int rows = report.getGraphs().size() / columns;

        if (rows == 0) {
            rows = 1;
        }

        if (report.getGraphs().size() % columns > 0) {
            rows++;
        }

        for (int y = 0; y < m_gridLayout.getRows(); y++) {
            for (int x = 0; x < m_gridLayout.getColumns(); x++) {
                if (x >= columns || y >= rows) {
                    m_gridLayout.removeComponent(x, y);
                }
            }
        }

        m_columnsSelect.setValue(columns);
        m_rowsSelect.setValue(rows);

        m_gridLayout.setColumns(columns);
        m_gridLayout.setRows(rows);

        int timeFrameValue = 1;
        int timeFrameType = Calendar.HOUR;

        int i = 0;

        for (int y = 0; y < m_gridLayout.getRows(); y++) {
            for (int x = 0; x < m_gridLayout.getColumns(); x++) {

                if (m_gridLayout.getComponent(x, y) == null) {
                    RrdGraphEntry rrdGraphEntry = new RrdGraphEntry(m_nodeDao, m_rrdGraphHelper, x, y);
                    rrdGraphEntry.setPreviewTimeFrame(timeFrameType, timeFrameValue);
                    m_gridLayout.addComponent(rrdGraphEntry, x, y);
                }

                RrdGraphEntry rrdGraphEntry = (RrdGraphEntry) m_gridLayout.getComponent(x, y);

                /**
                 * setting the values if defined in the KSC report
                 */
                if (i < report.getGraphs().size()) {
                    final int index = i;
                    setRrdGraphEntryFromKscReportGraph(rrdGraphEntry, report.getGraphs().get(index));
                }

                rrdGraphEntry.update();

                i++;
            }
        }
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

        for (int y = 0; y < m_gridLayout.getRows(); y++) {
            for (int x = 0; x < m_gridLayout.getColumns(); x++) {
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
        for (int y = 0; y < m_gridLayout.getRows(); y++) {
            for (int x = 0; x < m_gridLayout.getColumns(); x++) {
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

        for (int y = 0; y < m_gridLayout.getRows(); y++) {
            for (int x = 0; x < m_gridLayout.getColumns(); x++) {
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
