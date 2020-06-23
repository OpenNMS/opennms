/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.ipv6.summary.gui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint, LocationUpdateEventHandler, HostUpdateEventHandler {
    
    
    public class UpdateGraphCallback implements RequestCallback {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                updateTimelineChart(ChartUtils.convertJSONToDataTable(response.getText()));
            }

        }

        @Override
        public void onError(Request request, Throwable exception) {
            Window.alert("Error Occured updating graph, try refreshing");
        }

    }

    private FlowPanel m_flowPanel;
    AnnotatedTimeLine m_timeline;
    ChartService m_chartService;
    private Navigation m_nav;
    
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
      m_chartService = new DefaultChartService();
      
      Image img = new Image();
      img.setUrl("../images/logo.png");
      img.getElement().getStyle().setPaddingTop(14, Unit.PX);
      img.getElement().getStyle().setPaddingLeft(14, Unit.PX);
      
      FlowPanel header = new FlowPanel();
      header.getElement().setId("header");
      header.add(img);
      
      final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Unit.PX);
      dockLayoutPanel.addNorth(header, 75.00);
      RootLayoutPanel.get().add(dockLayoutPanel);
      
      
      m_nav = new Navigation();
      m_nav.addLocationUpdateEventHandler(this);
      m_nav.addHostUpdateEventHandler(this);
      
      m_flowPanel = new FlowPanel();
      
      
      Runnable timelineCallback = new Runnable() {

        public void run() {
            
            m_chartService.getAllLocationsAvailability(new RequestCallback() {

                @Override
                public void onResponseReceived(Request request,Response response) {
                    if(response.getStatusCode() == 200) {
                        m_timeline = new AnnotatedTimeLine(ChartUtils.convertJSONToDataTable(response.getText()), createTimelineOptions(), "440px", "250px");
                        
                        m_flowPanel.add(m_timeline);
                        m_flowPanel.add(m_nav);
                        dockLayoutPanel.add(m_flowPanel);
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    Window.alert("Error Initializing Chart");
                    
                }});
            
            
        }
          
      };
      
      VisualizationUtils.loadVisualizationApi(timelineCallback, AnnotatedTimeLine.PACKAGE);
      initializeNav();
  }
  
  private void initializeNav() {
    m_chartService.getAllLocations(new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                m_nav.loadLocations(ChartUtils.convertJSONToLocationList(response.getText()));
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            Window.alert("An error occured loading the locations");
        }
        
    });
    
    m_chartService.getAllParticipants(new RequestCallback() {

        @Override
        public void onResponseReceived(Request request, Response response) {
            if(response.getStatusCode() == 200) {
                m_nav.loadHosts(ChartUtils.convertJSONToParticipants(response.getText()));
            }
        }

        @Override
        public void onError(Request request, Throwable exception) {
            Window.alert("An error occured loading participants");
        }});
    
  }

  protected AnnotatedTimeLine.Options createTimelineOptions() {
      AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
      options.setDisplayAnnotations(false);
      options.setDisplayZoomButtons(false);
      options.setOption("dateFormat", "MMMM dd, yyyy");
      options.setOption("displayRangeSelector", true);
      options.setLegendPosition(AnnotatedTimeLine.AnnotatedLegendPosition.SAME_ROW);
      return options;
  }

  

  public void onHostUpdate(HostUpdateEvent event) {
      m_chartService.getAvailabilityByParticipant(event.getHost(), new UpdateGraphCallback());
  }

  protected void updateTimelineChart(DataTable dataTable) {
      m_timeline.draw(dataTable, createTimelineOptions());
  }

  public void onLocationUpdate(LocationUpdateEvent event) {
      m_chartService.getAvailabilityByLocation(event.getLocation(), new UpdateGraphCallback());
  }
  
}
