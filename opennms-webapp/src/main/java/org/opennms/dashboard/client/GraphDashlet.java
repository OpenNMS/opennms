/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: February 20, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.dashboard.client;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class GraphDashlet extends Dashlet {
    /**
     * The offset from the current time (rounded by TIME_ROUNDING_INTERVAL) for the start time on graphs
     */
    private static final int TIME_START_OFFSET = - (7 * 24 * 60 * 60);

    /**
     * The interval on which we round the start and end times for graph timespans
     */ 
    private static final int TIME_ROUNDING_INTERVAL = (5 * 60);

    private SurveillanceServiceAsync m_surveillanceService;
    
    private GraphView m_view;
    
    private DashletLoader m_loader = new DashletLoader();
    
    public GraphDashlet(Dashboard dashboard) {
        super(dashboard, "Resource Graphs");
        setLoader(m_loader);
        
        m_view = new GraphView(this);
        setView(m_view);
    }
    
    

    public void setSurveillanceService(SurveillanceServiceAsync surveillanceService) {
        m_surveillanceService = surveillanceService;
    }
    
    public void setSurveillanceSet(SurveillanceSet set) {
        m_view.getTopLevelResourceLoader().load(set);
    }
    
    public class GraphView extends DashletView {
        private VerticalPanel m_panel = new VerticalPanel();
        private SimplePager m_pager = new SimplePager(new SimplePageable() {
            public void adjustPage(int direction) {
                m_prefabGraphListBox.adjustSelectedValue(direction);
            }
        });
        private ValidatedListBox m_topLevelResourceListBox = new ValidatedListBox(GraphDashlet.this);
        private ValidatedListBox m_childResourceListBox = new ValidatedListBox(GraphDashlet.this);
        private ValidatedListBox m_prefabGraphListBox = new ValidatedListBox(GraphDashlet.this);
        private ResourceGraph m_graph = new ResourceGraph();
        
        private TopLevelResourceLoader m_topLevelResourceLoader;
        private ChildResourceLoader m_childResourceLoader;
        private PrefabGraphLoader m_prefabGraphLoader;
        
        private TopLevelResourceChangeHandler m_topLevelResourceHandler = new TopLevelResourceChangeHandler();
        private ChildResourceChangeHandler m_childResourceHandler = new ChildResourceChangeHandler();
        private PrefabGraphChangeHandler m_prefabGraphHandler = new PrefabGraphChangeHandler();
        
        private String m_selectedResourceId = null;
        
        public GraphView(Dashlet dashlet) {
            super(dashlet);
            //m_panel.add(m_pager);
            m_panel.add(m_topLevelResourceListBox);
            m_panel.add(m_childResourceListBox);
            m_panel.add(m_prefabGraphListBox);
            m_panel.add(m_graph);
            
            m_topLevelResourceListBox.addChangeHandler(m_topLevelResourceHandler);
            m_topLevelResourceListBox.setDirectionalChangeHandler(m_topLevelResourceHandler);

            m_childResourceListBox.addChangeHandler(m_childResourceHandler);
            m_childResourceListBox.setDirectionalChangeHandler(m_childResourceHandler);
            m_childResourceListBox.setParent(m_topLevelResourceListBox);

            m_prefabGraphListBox.addChangeHandler(m_prefabGraphHandler);
            m_prefabGraphListBox.setDirectionalChangeHandler(m_prefabGraphHandler);
            m_prefabGraphListBox.setParent(m_childResourceListBox);
            
            m_topLevelResourceLoader = new TopLevelResourceLoader(m_topLevelResourceListBox);
            m_childResourceLoader = new ChildResourceLoader(m_childResourceListBox);
            m_prefabGraphLoader = new PrefabGraphLoader(m_prefabGraphListBox);
            
            initWidget(m_panel);
        }
        
        
        public void onDashLoad() {
            addToTitleBar(m_pager, DockPanel.CENTER);
        }



        public TopLevelResourceLoader getTopLevelResourceLoader() {
            return m_topLevelResourceLoader;
        }
        
        public class TopLevelResourceLoader extends ListBoxCallback {
            public TopLevelResourceLoader(ListBox listBox) {
                super(m_loader, listBox);
                setEmptyListItem("No nodes found", "");
            }

            public void load(SurveillanceSet surveillanceSet) {
                m_loader.loading();
                m_surveillanceService.getResources(surveillanceSet, this);
            }

            public void onDataLoaded(String[][] resources) {
                super.onDataLoaded(resources);

                // Trigger a change so sub-lists get loaded
                m_topLevelResourceHandler.onChange(null);
            }
        }

        public class TopLevelResourceChangeHandler extends DirectionalChangeHandler {
            public void onChange(ChangeEvent event, int direction) {
                String resourceId = m_view.m_topLevelResourceListBox.getSelectedValue();
                if (resourceId == null) {
                    return;
                }

                // Reload child resources since we just change the top-level resource
                m_childResourceLoader.load(resourceId, direction);
            }
        }

        public class ChildResourceLoader extends ListBoxCallback {
            public ChildResourceLoader(ListBox listBox) {
                super(m_loader, listBox);
                setNullListItem("No parent resource", "");
                setEmptyListItem("Parent resource has no child resources--parent resource selected", "");
            }

            public void load(String resourceId, int direction) {
                setDirection(direction);

                m_loader.loading("Loading data for resource...");
                m_surveillanceService.getChildResources(resourceId, this);
            }

            public void onDataLoaded(String[][] resources) {
                super.onDataLoaded(resources);

                // Trigger a change so sub-lists get loaded
                m_childResourceHandler.onChange(null, getDirection());
            }

        }

        public class ChildResourceChangeHandler extends DirectionalChangeHandler {
            public void onChange(ChangeEvent event, int direction) {
                String resourceId = m_view.m_childResourceListBox.getSelectedValue();
                if (resourceId == null) {
                    return;
                }

                if ("".equals(resourceId)) {
                    m_selectedResourceId = m_view.m_topLevelResourceListBox.getSelectedValue();
                } else {
                    m_selectedResourceId = resourceId;
                }

                // Reload prefab graphs since we just changed the resource
                m_prefabGraphLoader.load(m_selectedResourceId, direction);
            }
        }

        public class PrefabGraphLoader extends ListBoxCallback {
            public PrefabGraphLoader(ListBox listBox) {
                super(m_loader, listBox);
                setNullListItem("Nothing to graph", "");
                setEmptyListItem("There are no graphs to display for this resource", "");
            }

            public void load(String resourceId, int direction) {
                setDirection(direction);

                m_loader.loading("Loading data for child resource...");
                m_surveillanceService.getPrefabGraphs(resourceId, this);
            }

            public void onDataLoaded(String[][] prefabGraphs) {
                super.onDataLoaded(prefabGraphs);

                // Trigger a change so sub-lists get loaded
                m_prefabGraphHandler.onChange(null);
            }
        }

        public class PrefabGraphChangeHandler extends DirectionalChangeHandler {
            public void onChange(ChangeEvent event, int direction) {
                String name = m_view.m_prefabGraphListBox.getSelectedValue();
                if (name == null || "".equals(name)) {
                    m_view.m_graph.displayNoGraph();
                } else {
                    String[] times = getTimes();

                    m_view.m_graph.setGraph(m_selectedResourceId, name, times[0], times[1]);
                    prefetchAdjacentGraphs(times);
                }

            }

            private void prefetchAdjacentGraphs(String[] times) {
                String previousReport = m_view.m_prefabGraphListBox.getRelativeSelectedValue(-1);
                if (previousReport != null) {
                    m_view.m_graph.prefetchGraph(m_selectedResourceId, previousReport, times[0], times[1]);
                }

                String nextReport = m_view.m_prefabGraphListBox.getRelativeSelectedValue(1);
                if (nextReport != null) {
                    m_view.m_graph.prefetchGraph(m_selectedResourceId, nextReport, times[0], times[1]);
                }
            }

            /**
             * Returns start and end times as Strings, in standard Java milliseconds
             * values.  The time will be rounded to the nearest five minute interval
             * so when we prefetch graph images the URL will remain the same for that
             * interval, allowing the browser to use the prefetched image.
             */
            public String[] getTimes() {
                /*
                 * Get the current time and convert it from a long to integer so we
                 * can do reliable math in Javascript.
                 * 
                 * With GWT, a long is implemented in Javascript as a double since
                 * Javascript doesn't have a 64 bit integer type.  We want to make
                 * sure that the times that we return don't change even a millisecond,
                 * otherwise a graph that we prefetch might not be usable because the
                 * prefetched URL and the URL that we use when we want to show the
                 * image might not be the same.
                 * 
                 * FIXME This has a Y2038 issue where the signed integer will overflow.
                 */
                int now = (int) (System.currentTimeMillis() / 1000);

                int end = (now / TIME_ROUNDING_INTERVAL) * TIME_ROUNDING_INTERVAL; 
                int start = end + TIME_START_OFFSET;

                return new String[] { start + "000", end + "000" };
            }
        }
    }
}
