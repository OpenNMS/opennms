/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.vaadin.dashboard.dashlets;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.opennms.features.vaadin.components.graph.GraphContainer;
import org.opennms.netmgt.dao.api.NodeDao;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.themes.BaseTheme;

public class RrdGraphEntry extends Panel {
    /**
     * x and y position of this entry
     */
    private int m_x, m_y;

    /**
     * Properties to be set by the configuration window
     */
    private String m_graphId, m_nodeId, m_resourceTypeId, m_resourceId, m_graphUrl, m_graphLabel, m_nodeLabel, m_resourceTypeLabel, m_resourceLabel;

    /**
     * the select/remove buttons
     */
    private Button m_changeButton = new Button();
    private Button m_removeButton = new Button("Remove");

    /**
     * create layout for storing the graph
     */
    private VerticalLayout m_graphLayout = new VerticalLayout();

    /**
     * labels for node and graph information
     */
    private Label m_nodeLabelComponent = new Label();
    private Label m_graphLabelComponent = new Label();

    /**
     * the preview's timeframe values
     */

    int m_calendarField = java.util.Calendar.HOUR;
    int m_calendarDiff = 1;

    /**
     * Constrcutor for creating new instances of this class.
     *
     * @param nodeDao        the node dao instance to be used
     * @param rrdGraphHelper the rrd graph helper instancce to be used
     * @param x              the x-position of this entry
     * @param y              the y-position of this entry
     */
    public RrdGraphEntry(final NodeDao nodeDao, final RrdGraphHelper rrdGraphHelper, int x, int y) {
        /**
         * setting the member fields
         */
        this.m_x = x;
        this.m_y = y;

        /**
         * setting up the buttons
         */
        m_changeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                GraphSelectionWindow graphSelectionWindow = new GraphSelectionWindow(nodeDao, rrdGraphHelper, RrdGraphEntry.this);

                getUI().addWindow(graphSelectionWindow);
            }
        });

        m_removeButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                setGraphId(null);
                setGraphLabel(null);
                setGraphUrl(null);
                setNodeId(null);
                setNodeLabel(null);
                setResourceId(null);
                setResourceLabel(null);
                setResourceTypeId(null);
                setResourceTypeLabel(null);

                update();
            }
        });

        m_removeButton.addStyleName(BaseTheme.BUTTON_LINK);
        m_changeButton.addStyleName(BaseTheme.BUTTON_LINK);

        /**
         * setting up the layout
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        m_graphLayout.setSizeUndefined();
        m_graphLayout.setWidth(200, Unit.PIXELS);
        m_graphLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);

        /**
         * adding the components
         */
        verticalLayout.addComponent(m_nodeLabelComponent);
        verticalLayout.addComponent(m_graphLabelComponent);
        verticalLayout.addComponent(m_changeButton);
        verticalLayout.addComponent(m_removeButton);
        verticalLayout.addComponent(m_graphLayout);

        m_nodeLabelComponent.setSizeUndefined();
        m_graphLabelComponent.setSizeUndefined();

        verticalLayout.setWidth(100, Unit.PERCENTAGE);

        setSizeFull();

        /**
         * inject the preview style
         */
        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent attachEvent) {
                getUI().getPage().getStyles().add(".preview { width:175px; }");
            }
        });

        /**
         * initial update
         */
        update();

        /**
         * setting the content
         */
        setContent(verticalLayout);
    }

    /**
     * This methods sets the timeframe for the preview image.
     */
    public void setPreviewTimeFrame(int calendarField, int calendarDiff) {
        m_calendarField = calendarField;
        m_calendarDiff = calendarDiff;
        update();
    }

    /**
     * Updates the labels and buttons according to the properties set.
     */
    public void update() {
        m_graphLayout.removeAllComponents();
        if (m_graphId == null) {
            m_nodeLabelComponent.setValue("No Rrd graph");
            m_graphLabelComponent.setValue("selected");
            m_changeButton.setCaption("Select Rrd graph");
            m_removeButton.setVisible(false);
        } else {
            String graphName = RrdGraphHelper.getGraphNameFromQuery(m_graphUrl);
            GraphContainer graph = new GraphContainer(graphName, m_resourceId);
            graph.setTitle(m_graphLabel);
            // Setup the time span
            Calendar cal = new GregorianCalendar();
            graph.setEnd(cal.getTime());
            cal.add(m_calendarField, -m_calendarDiff);
            graph.setStart(cal.getTime());
            // Use all of the available width
            graph.setWidthRatio(1.0d);
            graph.setHeightRatio(0.2d);
            m_graphLayout.addComponent(graph);

            m_graphLabelComponent.setValue(m_resourceTypeLabel + ": " + m_resourceLabel);
            m_nodeLabelComponent.setValue(getNodeLabel());
            m_changeButton.setCaption("Change Rrd graph");
            m_removeButton.setVisible(true);
        }
    }

    public String getGraphId() {
        return m_graphId;
    }

    public void setGraphId(String graphId) {
        this.m_graphId = graphId;
    }

    public String getNodeId() {
        return m_nodeId;
    }

    public void setNodeId(String nodeId) {
        this.m_nodeId = nodeId;
    }

    public String getResourceTypeId() {
        return m_resourceTypeId;
    }

    public void setResourceTypeId(String resourceTypeId) {
        this.m_resourceTypeId = resourceTypeId;
    }

    public String getResourceId() {
        return m_resourceId;
    }

    public void setResourceId(String resourceId) {
        this.m_resourceId = resourceId;
    }

    public String getGraphLabel() {
        return m_graphLabel;
    }

    public void setGraphLabel(String graphLabel) {
        this.m_graphLabel = graphLabel;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.m_nodeLabel = nodeLabel;
    }

    public String getResourceTypeLabel() {
        return m_resourceTypeLabel;
    }

    public void setResourceTypeLabel(String resourceTypeLabel) {
        this.m_resourceTypeLabel = resourceTypeLabel;
    }

    public String getResourceLabel() {
        return m_resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        this.m_resourceLabel = resourceLabel;
    }

    public String getGraphUrl() {
        return m_graphUrl;
    }

    public void setGraphUrl(String graphUrl) {
        this.m_graphUrl = graphUrl;
    }
}
