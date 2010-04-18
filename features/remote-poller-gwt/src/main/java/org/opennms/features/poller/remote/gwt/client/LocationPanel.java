package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEvent;
import org.opennms.features.poller.remote.gwt.client.events.MapPanelBoundsChangedEventHandler;
import org.opennms.features.poller.remote.gwt.client.events.SortOrderUpdateEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
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
		String flexTableHeader();
		String sortSelected();
		String upStatus();
		String downStatus();
		String marginalStatus();
		String unknownStatus();
	}

	private static final Binder BINDER = GWT.create(Binder.class);
	private transient HandlerManager m_eventBus;
	private transient List<HandlerRegistration> eventRegistrations = new ArrayList<HandlerRegistration>();
	
	@UiField PageableLocationList locationList;
	@UiField FlexTable m_locations;
	@UiField SelectionStyle selectionStyle;
	
	public LocationPanel() {
		super();
		initWidget(BINDER.createAndBindUi(this));
		m_locations.setText(0, 0, "Status");
		m_locations.setText(0, 1, "Name");
		m_locations.setText(0, 2, "Area");
		m_locations.getRowFormatter().addStyleName(0, selectionStyle.flexTableHeader());
		
	}

	@UiHandler("m_locations")
	void onTableClicked(final ClickEvent event) {
		final Cell cell = m_locations.getCellForEvent(event);
		
	    if (cell != null) {
	    	final int row = cell.getRowIndex();
	    	
	    	if(row != 0) {
	    	    final String locationName = m_locations.getText(row, 1);
	            styleRow(row);
	            selectLocation(locationName);
	    	}else {
	    	    final String cellName = m_locations.getText(row, cell.getCellIndex());
	    	    m_eventBus.fireEvent(new SortOrderUpdateEvent(cellName));
	    	    styleSorted(row, cellName);
	    	}
	    	
	    }
	}

    private void selectLocation(final String locationName) {
	    m_eventBus.fireEvent(new LocationPanelSelectEvent(locationName));
    }
    
    private void styleSorted(int row, String cellName) {
        Window.alert("cell name: " + cellName);
        for(int i = 0; i < m_locations.getCellCount(row); i++) {
            String currentCellName = m_locations.getText(row, i);
            if(currentCellName.equals(cellName)) {
                m_locations.getCellFormatter().addStyleName(row, i, selectionStyle.sortSelected());
            }else {
                m_locations.getCellFormatter().removeStyleName(row, i, selectionStyle.sortSelected());
            }
        }
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
			throw new IllegalStateException("No LocationManager available inside LocationPanel");
		}
		
		int count = 1;
		List<Location> visibleLocations = locationManager.getVisibleLocations();
		
		locationList.updateList(visibleLocations);
		
//        for (Location location : visibleLocations) {
//            
//            String statusStyle = getStatusStyle(location);
//            
//            
//            m_locations.setText(count, 1, location.getLocationInfo().getName());
//            
//            m_locations.setText(count, 0, "&nbsp;");
//            m_locations.getCellFormatter().addStyleName(count, 0, statusStyle);
//            
//		    m_locations.setText(count, 2, location.getLocationInfo().getArea());
//		    
//		    if(count %2 != 0) {
//		        m_locations.getRowFormatter().addStyleName(count, selectionStyle.alternateRow());
//		    }
//		    
//			count++;
//			
//		}
//		
//		while (m_locations.getRowCount() > count) {
//			m_locations.removeRow(m_locations.getRowCount() - 1);
//		}

		
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
	    // Remove any existing handler registrations
	    for (HandlerRegistration registration : eventRegistrations) {
	        registration.removeHandler();
	    }
	    m_eventBus = eventBus;
	    // eventRegistrations.add(m_eventBus.addHandler(MapPanelBoundsChangedEvent.TYPE, this));
	    // eventRegistrations.add(m_eventBus.addHandler(LocationsUpdatedEvent.TYPE, this));
	}
}
