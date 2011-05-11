package org.opennms.isoc.ipv6.gui.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Float;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.visualization.client.AbstractDataTable;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.AnnotatedTimeLine;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint, LocationUpdateEventHandler, HostUpdateEventHandler {
    
    
    private FlowPanel m_flowPanel;
    AnnotatedTimeLine m_timeline;
    
  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
      FlowPanel navHolder = new FlowPanel();
      navHolder.getElement().getStyle().setFloat(Float.LEFT);
      
      Navigation nav = new Navigation();
      nav.loadLocations(new ArrayList<String>(Arrays.asList("Here", "There")));
      nav.loadHosts(new ArrayList<String>(Arrays.asList("google.com", "yahoo.com")));
      nav.addLocationUpdateEventHandler(this);
      nav.addHostUpdateEventHandler(this);
      navHolder.add(nav);
      
      m_flowPanel = new FlowPanel();
      m_flowPanel.add(navHolder);
      
      Runnable timelineCallback = new Runnable() {

        public void run() {
            m_timeline = new AnnotatedTimeLine(location1Data(), createTimelineOptions(), "800", "350");
            
            m_flowPanel.add(m_timeline);
            
            RootPanel.get().add(m_flowPanel);
            
            
        }
          
      };
      
      VisualizationUtils.loadVisualizationApi(timelineCallback, AnnotatedTimeLine.PACKAGE);
  }
  
  private void getDataTable() {
//      myServiceTest.getTableData( new AsyncCallback<AbstractDataTable>() {
//
//        public void onFailure(Throwable caught) {
//            Window.alert("RPC Failed");
//            
//        }
//
//        public void onSuccess(AbstractDataTable result) {
//            m_timeline.draw(result, createTimelineOptions());
//            
//        }});

        
  }
  
  protected AnnotatedTimeLine.Options createTimelineOptions() {
      AnnotatedTimeLine.Options options = AnnotatedTimeLine.Options.create();
      options.setDisplayAnnotations(true);
      options.setDisplayZoomButtons(false);
      options.setLegendPosition(AnnotatedTimeLine.AnnotatedLegendPosition.SAME_ROW);
      return options;
  }

  

  public void onHostUpdate(HostUpdateEvent event) {
      
  }

  public void onLocationUpdate(LocationUpdateEvent event) {
      if(event.getLocation().equalsIgnoreCase("there")) {
          m_timeline.draw(location2Data(), createTimelineOptions());
      } else {
          m_timeline.draw(location1Data(), createTimelineOptions());
      }
      
  }
  
  protected AbstractDataTable location1Data() {
      DataTable data = DataTable.create();
      data.addColumn(ColumnType.DATE, "Date");
      data.addColumn(ColumnType.NUMBER, "Quad A records");
      data.addColumn(ColumnType.STRING, "title1");
      data.addColumn(ColumnType.STRING, "text1");
      data.addColumn(ColumnType.NUMBER, "Single A Records");
      data.addColumn(ColumnType.STRING, "title2");
      data.addColumn(ColumnType.STRING, "text2");
      data.addColumn(ColumnType.NUMBER, "IPv6");
      data.addColumn(ColumnType.STRING, "title2");
      data.addColumn(ColumnType.STRING, "text2");
      data.addColumn(ColumnType.NUMBER, "IPv4");
      data.addColumn(ColumnType.STRING, "title2");
      data.addColumn(ColumnType.STRING, "text2");
      data.addRows(12);
      data.setValue(0, 0, new Date(1209614400000L));
      data.setValue(0, 1, 3500);
      data.setValue(0, 4, 40645);
      data.setValue(0, 7, 1000);
      data.setValue(0, 10, 60234);
      
      data.setValue(1, 0, new Date(1209700800000L));
      data.setValue(1, 1, 14045);
      data.setValue(1, 4, 20374);
      data.setValue(1, 7, 20567);
      data.setValue(1, 10, 10000);
      
      data.setValue(2, 0, new Date(1209787200000L));
      data.setValue(2, 1, 55022);
      data.setValue(2, 4, 50766);
      data.setValue(2, 7, 27001);
      data.setValue(2, 10, 35456);
      
      data.setValue(3, 0, new Date(1209873600000L));
      data.setValue(3, 1, 75284);
      data.setValue(3, 4, 14334);
      data.setValue(3, 5, "Outage");
      data.setValue(3, 6, "Google.com IPv6 outage");
      data.setValue(3, 7, 54678);
      data.setValue(3, 10, 23453);
      
      data.setValue(4, 0, new Date(1209960000000L));
      data.setValue(4, 1, 41476);
      data.setValue(4, 2, "Outage");
      data.setValue(4, 3, "yahoo.com outage at 3pm");
      data.setValue(4, 4, 66467);
      data.setValue(4, 7, 65478);
      data.setValue(4, 10, 27896);
      
      data.setValue(5, 0, new Date(1210046400000L));
      data.setValue(5, 1, 33322);
      data.setValue(5, 4, 39463);
      data.setValue(5, 7, 23980);
      data.setValue(5, 10, 50645);
      
      return data;
  }
  
  protected AbstractDataTable location2Data() {
      DataTable data = DataTable.create();
      data.addColumn(ColumnType.DATE, "Date");
      data.addColumn(ColumnType.NUMBER, "Quad A records");
      data.addColumn(ColumnType.STRING, "title1");
      data.addColumn(ColumnType.STRING, "text1");
      data.addColumn(ColumnType.NUMBER, "Single A Records");
      data.addColumn(ColumnType.STRING, "title2");
      data.addColumn(ColumnType.STRING, "text2");
      data.addColumn(ColumnType.NUMBER, "IPv6");
      data.addColumn(ColumnType.STRING, "title2");
      data.addColumn(ColumnType.STRING, "text2");
      data.addColumn(ColumnType.NUMBER, "IPv4");
      data.addColumn(ColumnType.STRING, "title2");
      data.addColumn(ColumnType.STRING, "text2");
      data.addRows(12);
      data.setValue(0, 0, new Date(1209614400000L));
      data.setValue(0, 1, Math.random() * 65000);
      data.setValue(0, 4, Math.random() * 65000);
      data.setValue(0, 7, Math.random() * 65000);
      data.setValue(0, 10, Math.random() * 65000);
      
      data.setValue(1, 0, new Date(1209700800000L));
      data.setValue(1, 1, Math.random() * 65000);
      data.setValue(1, 4, Math.random() * 65000);
      data.setValue(1, 7, Math.random() * 65000);
      data.setValue(1, 10, Math.random() * 65000);
      
      data.setValue(2, 0, new Date(1209787200000L));
      data.setValue(2, 1, Math.random() * 65000);
      data.setValue(2, 4, Math.random() * 65000);
      data.setValue(2, 7, Math.random() * 65000);
      data.setValue(2, 10, Math.random() * 65000);
      
      data.setValue(3, 0, new Date(1209873600000L));
      data.setValue(3, 1, Math.random() * 65000);
      data.setValue(3, 4, Math.random() * 65000);
      data.setValue(3, 5, "Outage");
      data.setValue(3, 6, "Google.com IPv6 outage");
      data.setValue(3, 7, Math.random() * 65000);
      data.setValue(3, 10, Math.random() * 65000);
      
      data.setValue(4, 0, new Date(1209960000000L));
      data.setValue(4, 1, 41476);
      data.setValue(4, 2, "Outage");
      data.setValue(4, 3, "yahoo.com outage at 3pm");
      data.setValue(4, 4, Math.random() * 65000);
      data.setValue(4, 7, Math.random() * 65000);
      data.setValue(4, 10, Math.random() * 65000);
      
      data.setValue(5, 0, new Date(1210046400000L));
      data.setValue(5, 1, Math.random() * 65000);
      data.setValue(5, 4, Math.random() * 65000);
      data.setValue(5, 7, Math.random() * 65000);
      data.setValue(5, 10, Math.random() * 65000);
      
      return data;
  }
  
}
