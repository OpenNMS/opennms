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
import org.opennms.features.vaadin.dashboard.model.AbstractDashlet;
import org.opennms.features.vaadin.dashboard.model.AbstractDashletComponent;
import org.opennms.features.vaadin.dashboard.model.Dashlet;
import org.opennms.features.vaadin.dashboard.model.DashletComponent;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.ui.Label;

/**
 * This class implements a {@link Dashlet} for displaying a Rrd graph.
 *
 * @author Christian Pape
 */
public class RrdDashlet extends AbstractDashlet {
    /**
     * The Rrd helper instance
     */
    RrdGraphHelper m_rrdGraphHelper;

    private DashletComponent m_wallboardComponent;

    private DashletComponent m_dashboardComponent;

    /**
     * Constructor for instantiating new objects.
     *
     * @param name           the name of the dashlet
     * @param dashletSpec    the {@link DashletSpec} to be used
     * @param rrdGraphHelper the rrd graph helper instance
     */
    public RrdDashlet(String name, DashletSpec dashletSpec, RrdGraphHelper rrdGraphHelper) {
        super(name, dashletSpec);
        /**
         * Setting the member fields
         */
        m_rrdGraphHelper = rrdGraphHelper;
    }

    @Override
    public DashletComponent getDashboardComponent(final UI ui) {
        if (m_dashboardComponent == null) {
            m_dashboardComponent = new AbstractDashletComponent() {
                private VerticalLayout m_verticalLayout = new VerticalLayout();

                {
                    m_verticalLayout.setCaption(getName());
                    m_verticalLayout.setSizeFull();
                    injectWallboardStyles();
                }

                private void injectWallboardStyles() {
                    ui.getPage().getStyles().add(".box { margin: 5px; background-color: #444; border: 1px solid #999; border-top: 0; overflow: auto; width: 100% }");
                    ui.getPage().getStyles().add(".text { color:#ffffff; line-height: 11px; font-size: 9px; font-family: 'Lucida Grande', Verdana, sans-serif; font-weight: bold; }");
                    ui.getPage().getStyles().add(".margin { margin:5px; }");
                }

                @Override
                public void refresh() {
                    /**
                     * removing old components
                     */
                    m_verticalLayout.removeAllComponents();

                    /**
                     * iniatizing the parameters
                     */
                    int columns = 0;
                    int rows = 0;
                    int width = 0;
                    int height = 0;

                    try {
                        columns = Integer.parseInt(getDashletSpec().getParameters().get("columns"));
                    } catch (NumberFormatException numberFormatException) {
                        columns = 1;
                    }

                    try {
                        rows = Integer.parseInt(getDashletSpec().getParameters().get("rows"));
                    } catch (NumberFormatException numberFormatException) {
                        rows = 1;
                    }

                    try {
                        width = Integer.parseInt(getDashletSpec().getParameters().get("width"));
                    } catch (NumberFormatException numberFormatException) {
                        width = 400;
                    }

                    try {
                        height = Integer.parseInt(getDashletSpec().getParameters().get("height"));
                    } catch (NumberFormatException numberFormatException) {
                        height = 100;
                    }

                    /**
                     * getting the timeframe values
                     */
                    int timeFrameValue;
                    int timeFrameType;

                    try {
                        timeFrameValue = Integer.parseInt(getDashletSpec().getParameters().get("timeFrameValue"));
                    } catch (NumberFormatException numberFormatException) {
                        timeFrameValue = 1;
                    }

                    try {
                        timeFrameType = Integer.parseInt(getDashletSpec().getParameters().get("timeFrameType"));
                    } catch (NumberFormatException numberFormatException) {
                        timeFrameType = Calendar.HOUR;
                    }

                    int i = 0;

                    Accordion accordion = new Accordion();
                    accordion.setSizeFull();

                    /**
                     * adding the components
                     */
                    for (int y = 0; y < rows; y++) {
                        for (int x = 0; x < columns; x++) {
                            String graphUrl = getDashletSpec().getParameters().get("graphUrl" + i);

                            if (graphUrl != null && !"".equals(graphUrl)) {
                                accordion.addTab(getGraphComponent(i, width, height, timeFrameType, timeFrameValue), getDashletSpec().getParameters().get("nodeLabel" + i) + "/" + getDashletSpec().getParameters().get("resourceTypeLabel" + i) + ": " + getDashletSpec().getParameters().get("resourceLabel" + i));
                            }
                            i++;
                        }
                    }

                    m_verticalLayout.addComponent(accordion);
                }

                @Override
                public Component getComponent() {
                    return m_verticalLayout;
                }
            };
        }

        return m_dashboardComponent;
    }


    @Override
    public DashletComponent getWallboardComponent(final UI ui) {
        if (m_wallboardComponent == null) {
            m_wallboardComponent = new AbstractDashletComponent() {
                private GridLayout m_gridLayout = new GridLayout();

                {
                    m_gridLayout.setCaption(getName());
                    m_gridLayout.setSizeFull();
                    m_gridLayout.setColumns(1);
                    m_gridLayout.setRows(1);
                    injectWallboardStyles();
                }

                /**
                 * Injects CSS styles in the current page
                 */
                private void injectWallboardStyles() {
                    ui.getPage().getStyles().add(".box { margin: 5px; background-color: #444; border: 1px solid #999; border-top: 0; overflow: auto; width: 100%; }");
                    ui.getPage().getStyles().add(".text { color:#ffffff; line-height: 11px; font-size: 9px; font-family: 'Lucida Grande', Verdana, sans-serif; font-weight: bold; }");
                    ui.getPage().getStyles().add(".margin { margin:5px; }");
                }

                @Override
                public void refresh() {
                    /**
                     * removing old components
                     */
                    m_gridLayout.removeAllComponents();

                    /**
                     * iniatizing the parameters
                     */
                    int columns = 0;
                    int rows = 0;
                    int width = 0;
                    int height = 0;

                    try {
                        columns = Integer.parseInt(getDashletSpec().getParameters().get("columns"));
                    } catch (NumberFormatException numberFormatException) {
                        columns = 1;
                    }

                    try {
                        rows = Integer.parseInt(getDashletSpec().getParameters().get("rows"));
                    } catch (NumberFormatException numberFormatException) {
                        rows = 1;
                    }

                    try {
                        width = Integer.parseInt(getDashletSpec().getParameters().get("width"));
                    } catch (NumberFormatException numberFormatException) {
                        width = 400;
                    }

                    try {
                        height = Integer.parseInt(getDashletSpec().getParameters().get("height"));
                    } catch (NumberFormatException numberFormatException) {
                        height = 100;
                    }

                    /**
                     * getting the timeframe values
                     */
                    int timeFrameValue;
                    int timeFrameType;

                    try {
                        timeFrameValue = Integer.parseInt(getDashletSpec().getParameters().get("timeFrameValue"));
                    } catch (NumberFormatException numberFormatException) {
                        timeFrameValue = 1;
                    }

                    try {
                        timeFrameType = Integer.parseInt(getDashletSpec().getParameters().get("timeFrameType"));
                    } catch (NumberFormatException numberFormatException) {
                        timeFrameType = Calendar.HOUR;
                    }

                    /**
                     * setting new columns/rows
                     */
                    m_gridLayout.setColumns(columns);
                    m_gridLayout.setRows(rows);

                    int i = 0;

                    /**
                     * adding the components
                     */
                    for (int y = 0; y < m_gridLayout.getRows(); y++) {
                        for (int x = 0; x < m_gridLayout.getColumns(); x++) {
                            String graphUrl = getDashletSpec().getParameters().get("graphUrl" + i);

                            if (graphUrl != null && !"".equals(graphUrl)) {
                                Component component = getGraphComponent(i, width, height, timeFrameType, timeFrameValue);
                                m_gridLayout.addComponent(component, x, y);
                                m_gridLayout.setComponentAlignment(component, Alignment.MIDDLE_CENTER);
                            }
                            i++;
                        }
                    }
                }

                @Override
                public Component getComponent() {
                    return m_gridLayout;
                }
            };
        }

        return m_wallboardComponent;
    }

    /**
     * Returns the graph component for a given graph of the {@link DashletSpec}.
     *
     * @param i              the entry id
     * @param width          the width
     * @param height         the height
     * @param timeFrameType  the timeframe type
     * @param timeFrameValue the timeframe value
     * @return the component
     */
    private Component getGraphComponent(int i, int width, int height, int timeFrameType, int timeFrameValue) {
        String graphTitle = getDashletSpec().getParameters().get("graphLabel" + i);
        String graphName = RrdGraphHelper.getGraphNameFromQuery(getDashletSpec().getParameters().get("graphUrl" + i));
        String resourceId = getDashletSpec().getParameters().get("resourceId" + i);

        GraphContainer graph = new GraphContainer(graphName, resourceId);
        graph.setTitle(graphTitle);
        // Setup the time span
        Calendar cal = new GregorianCalendar();
        graph.setEnd(cal.getTime());
        cal.add(timeFrameType, -timeFrameValue);
        graph.setStart(cal.getTime());
        // Use all of the available width
        graph.setWidthRatio(1.0d);

        VerticalLayout verticalLayout = new VerticalLayout();

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addStyleName("box");
        horizontalLayout.setHeight("42px");

        VerticalLayout leftLayout = new VerticalLayout();
        leftLayout.setDefaultComponentAlignment(Alignment.TOP_LEFT);
        leftLayout.addStyleName("margin");

        Label labelFrom = new Label(getDashletSpec().getParameters().get("nodeLabel" + i));
        labelFrom.addStyleName("text");

        Label labelTo = new Label(getDashletSpec().getParameters().get("resourceTypeLabel" + i) + ": " + getDashletSpec().getParameters().get("resourceLabel" + i));
        labelTo.addStyleName("text");

        leftLayout.addComponent(labelFrom);
        leftLayout.addComponent(labelTo);

        horizontalLayout.addComponent(leftLayout);
        horizontalLayout.setExpandRatio(leftLayout, 1.0f);

        verticalLayout.addComponent(horizontalLayout);
        verticalLayout.addComponent(graph);
        verticalLayout.setWidth(width, Unit.PIXELS);

        verticalLayout.setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
        verticalLayout.setComponentAlignment(graph, Alignment.MIDDLE_CENTER);
        verticalLayout.setMargin(true);

        return verticalLayout;
    }
}
