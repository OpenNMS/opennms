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
package org.opennms.features.vaadin.surveillanceviews.ui.dashboard;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.opennms.features.topology.api.support.InfoWindow;
import org.opennms.features.vaadin.surveillanceviews.service.SurveillanceViewService;
import org.opennms.netmgt.model.OnmsCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.themes.BaseTheme;

/**
 * This class represents a table displaying the node RTC calculations for the surveillance view's dashboard.
 *
 * @author Christian Pape
 */
public class SurveillanceViewNodeRtcTable extends SurveillanceViewDetailTable {
    /**
     * the logger instance
     */
    private static final Logger LOG = LoggerFactory.getLogger(SurveillanceViewNodeRtcTable.class);
    /**
     * the bean containeer for storing the RTC calculations
     */
    private BeanItemContainer<SurveillanceViewService.NodeRtc> m_beanItemContainer = new BeanItemContainer<SurveillanceViewService.NodeRtc>(SurveillanceViewService.NodeRtc.class);
    /**
     * the refresh future
     */
    protected ListenableFuture<List<SurveillanceViewService.NodeRtc>> m_future;

    /**
     * Constructor for instantiating this component.
     *
     * @param surveillanceViewService the surveillance view service to be used
     * @param enabled                 the flag should links be enabled?
     */
    public SurveillanceViewNodeRtcTable(SurveillanceViewService surveillanceViewService, boolean enabled) {
        /**
         * call the super constructor
         */
        super("Outages", surveillanceViewService, enabled);

        /**
         * set the datasource
         */
        setContainerDataSource(m_beanItemContainer);

        /**
         * set the base style name
         */
        addStyleName("surveillance-view");

        /**
         * add node column
         */
        addGeneratedColumn("node", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, Object itemId, Object propertyId) {
                final SurveillanceViewService.NodeRtc nodeRtc = (SurveillanceViewService.NodeRtc) itemId;

                Button button = new Button(nodeRtc.getNode().getLabel());
                button.setPrimaryStyleName(BaseTheme.BUTTON_LINK);
                button.setEnabled(m_enabled);
                button.addStyleName("white");

                button.addClickListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent clickEvent) {
                        final URI currentLocation = getUI().getPage().getLocation();
                        final String contextRoot = VaadinServlet.getCurrent().getServletContext().getContextPath();
                        final String redirectFragment = contextRoot + "/element/node.jsp?quiet=true&node=" + nodeRtc.getNode().getId();

                        LOG.debug("node {} clicked, current location = {}, uri = {}", nodeRtc.getNode().getId(), currentLocation, redirectFragment);

                        try {
                            SurveillanceViewNodeRtcTable.this.getUI().addWindow(new InfoWindow(new URL(currentLocation.toURL(), redirectFragment), new InfoWindow.LabelCreator() {
                                @Override
                                public String getLabel() {
                                    return "Node Info " + nodeRtc.getNode().getId();
                                }
                            }));
                        } catch (MalformedURLException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }
                });

                return button;
            }
        });

        /**
         * add currentOutages column
         */
        addGeneratedColumn("currentOutages", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object columnId) {
                SurveillanceViewService.NodeRtc nodeRtc = (SurveillanceViewService.NodeRtc) itemId;
                return getImageSeverityLayout(nodeRtc.getDownServiceCount() + " of " + nodeRtc.getServiceCount());
            }
        });

        /**
         * add availability column
         */
        addGeneratedColumn("availability", new ColumnGenerator() {
            @Override
            public Object generateCell(Table table, final Object itemId, Object propertyId) {
                SurveillanceViewService.NodeRtc nodeRtc = (SurveillanceViewService.NodeRtc) itemId;
                return getImageSeverityLayout(nodeRtc.getAvailabilityAsString());
            }
        });

        /**
         * set cell style generator that handles the two severities for RTC calculations
         */
        setCellStyleGenerator(new CellStyleGenerator() {
            @Override
            public String getStyle(Table table, Object itemId, Object propertyId) {
                String style = null;
                SurveillanceViewService.NodeRtc nodeRtc = (SurveillanceViewService.NodeRtc) itemId;

                if (!"node".equals(propertyId)) {
                    if (nodeRtc.getAvailability() == 1.0) {
                        style = "rtc-normal";
                    } else {
                        style = "rtc-critical";
                    }
                }
                return style;
            }
        });

        /**
         * set the column headers
         */
        setColumnHeader("node", "Node");
        setColumnHeader("currentOutages", "Current Outages");
        setColumnHeader("availability", "24 Hour Availability");

        setColumnExpandRatio("node", 1.0f);
        setColumnExpandRatio("currentOutages", 1.0f);
        setColumnExpandRatio("availability", 1.0f);

        /**
         * define the visible columns
         */
        setVisibleColumns(new Object[]{"node", "currentOutages", "availability"});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshDetails(final Set<OnmsCategory> rowCategories, final Set<OnmsCategory> colCategories) {
        if (m_future != null && !m_future.isDone()) {
            return;
        }

        m_future = getSurveillanceViewService().getExecutorService().submit(new Callable<List<SurveillanceViewService.NodeRtc>>() {
            @Override
            public List<SurveillanceViewService.NodeRtc> call() throws Exception {
                /**
                 * calculate and retrieve the RTC instances
                 */
                return getSurveillanceViewService().getNodeRtcsForCategories(rowCategories, colCategories);
            }
        });

        m_future.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    final List<SurveillanceViewService.NodeRtc> nodeRtcs = m_future.get();
                    getUI().access(new Runnable() {
                        @Override
                        public void run() {
                            /**
                             * empty the container
                             */
                            m_beanItemContainer.removeAllItems();

                            /**
                             * add items to the container
                             */
                            if (nodeRtcs != null && !nodeRtcs.isEmpty()) {
                                for (SurveillanceViewService.NodeRtc nodeRtc : nodeRtcs) {
                                    m_beanItemContainer.addItem(nodeRtc);
                                }
                            }

                            /**
                             * sort the iterms
                             */
                            sort(new Object[]{"node"}, new boolean[]{true});

                            /**
                             * refresh the table
                             */
                            refreshRowCache();
                        }
                    });
                } catch (InterruptedException e) {
                    LOG.error("Interrupted", e);
                } catch (ExecutionException e) {
                    LOG.error("Exception in task", e.getCause());
                }
            }
        }, MoreExecutors.directExecutor());
    }
}
