package org.opennms.features.poller.remote.gwt.client;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEvent;
import org.opennms.features.poller.remote.gwt.client.events.LocationsUpdatedEventHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTMLTable.Cell;

public class LocationPanel extends Composite {

	interface Binder extends UiBinder<Widget, LocationPanel> { }
	interface SelectionStyle extends CssResource {
		String selectedRow();
		String alternateRow();
	}

	private static final Binder BINDER = GWT.create(Binder.class);
	private static final int MAX_ROWS = 20;
	private int startIndex = 0;
	private transient HandlerManager m_eventBus;
	
	@UiField FlexTable m_locations;
	@UiField SelectionStyle selectionStyle;

	public LocationPanel() {
		super();
		initWidget(BINDER.createAndBindUi(this));
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
		for (Location location : locationManager.getVisibleLocations()) {
		    Image icon = new Image();
            icon.setUrl(location.getMarker().getIcon().getImageURL());
            
            m_locations.setWidget(count, 0, icon);
            m_locations.setText(count, 1, location.getName());
		    m_locations.setText(count, 2, location.getArea());
		    
		    if(count %2 != 0) {
		        m_locations.getRowFormatter().addStyleName(count, selectionStyle.alternateRow());
		    }
		    
			count++;
		}

		while (m_locations.getRowCount() > count) {
			m_locations.removeRow(m_locations.getRowCount() - 1);
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
