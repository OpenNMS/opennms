package org.opennms.features.poller.remote.gwt.client;

import java.util.List;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.gen2.table.client.FixedWidthFlexTable;
import com.google.gwt.gen2.table.client.FixedWidthGrid;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class LocationPanel extends Composite {

	interface Binder extends UiBinder<Widget, LocationPanel> { }
	interface SelectionStyle extends CssResource {
		String selectedRow();
		String alternateRow();
		String upStatus();
		String downStatus();
		String marginalStatus();
		String unknownStatus();
	}

	private static final Binder BINDER = GWT.create(Binder.class);
	private transient HandlerManager m_eventBus;
	
	@UiField FlexTable m_locations;
	@UiField SelectionStyle selectionStyle;
	
	ScrollTable m_scrollTable;
    private FixedWidthGrid m_dataTable;
    private FixedWidthFlexTable m_headerTable;
	
	public LocationPanel() {
		super();
		initWidget(BINDER.createAndBindUi(this));
		
		m_dataTable = new FixedWidthGrid();
        m_headerTable = new FixedWidthFlexTable();
        m_scrollTable = new ScrollTable(m_dataTable, m_headerTable);
	}

	@UiHandler("m_locations")
	void onTableClicked(final ClickEvent event) {
		final Cell cell = m_locations.getCellForEvent(event);
		
	    if (cell != null) {
	    	final int row = cell.getRowIndex();
	    	final String locationName = m_locations.getText(row, 1);
	    	styleRow(row);
	    	selectLocation(locationName);
	    }
	}

	private void selectLocation(final String locationName) {
	    m_eventBus.fireEvent(new LocationPanelSelectEvent(locationName));
    }

	private void styleRow(final int row) {
		if (row != -1) {
			final String style = selectionStyle.selectedRow();
			
			for(int i = 0; i < m_locations.getRowCount(); i++) {
			    if(i == row) {
			        m_locations.getRowFormatter().addStyleName(i, style);
			    }else {
			        m_locations.getRowFormatter().removeStyleName(i, style);
			    }
			}
			
		}
	}

	public void update(final LocationManager locationManager) {
		if (locationManager == null) {
			return;
		}
		
		int count = 0;
		List<Location> visibleLocations = locationManager.getVisibleLocations();
		
		long aTotal = 0;
		long bTotal = 0;
		long cTotal = 0;
		long dTotal = 0;
		long eTotal = 0;
		
        for (Location location : visibleLocations) {
            
            long aStart = System.currentTimeMillis();
            String statusStyle = getStatusStyle(location);
            aTotal += System.currentTimeMillis() - aStart;
            
            long cStart = System.currentTimeMillis();
            
            m_locations.setText(count, 1, location.getLocationInfo().getName());
            cTotal += System.currentTimeMillis() - cStart;
            
            long bStart = System.currentTimeMillis();
            m_locations.setText(count, 0, "&nbsp;");
            m_locations.getCellFormatter().addStyleName(count, 0, statusStyle);
            bTotal +=  System.currentTimeMillis() - bStart;
            
            long dStart = System.currentTimeMillis();
		    m_locations.setText(count, 2, location.getLocationInfo().getArea());
		    dTotal += System.currentTimeMillis() - dStart;
		    
		    long eStart = System.currentTimeMillis();
		    if(count %2 != 0) {
		        m_locations.getRowFormatter().addStyleName(count, selectionStyle.alternateRow());
		    }
		    eTotal += System.currentTimeMillis() - eStart;
		    
			count++;
			
		}
		
		while (m_locations.getRowCount() > count) {
			m_locations.removeRow(m_locations.getRowCount() - 1);
		}

		Window.alert("aTotal: " + aTotal + "\nbTotal: " + bTotal + "\ncTotal: " + cTotal + "\ndTotal: " + dTotal + "\neTotal: " + eTotal);
		
	}

    private String getStatusStyle(Location location) {
        switch(location.getLocationInfo().getMonitorStatus()) {
            case UP:
                 return selectionStyle.upStatus();
            case DOWN:
                return selectionStyle.downStatus();
            case MARGINAL:
                return selectionStyle.marginalStatus();
            case UNKNOWN:
                return selectionStyle.unknownStatus();
            default:
                return selectionStyle.unknownStatus();
        }
        
    }



	public void setEventBus(final HandlerManager eventBus) {
	    m_eventBus = eventBus;
	    addEventHandlers(m_eventBus);
	}
	
	private void addEventHandlers(final HandlerManager eventBus) {
	    eventBus.addHandler(LocationsUpdatedEvent.getType(), new LocationsUpdatedEventHandler() {
            
            public void onLocationsUpdated(final LocationsUpdatedEvent e) {
                update(e.getLocationManager());
            }
            
        });
	}

}
