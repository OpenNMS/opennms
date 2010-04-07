package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;

import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;

public abstract class GoogleMapsUtils {

//	private static Random generator = new Random();

	public static InfoWindowContent getInfoWindowForLocation(final Location location) {
		final LocationMonitorState state = location.getLocationMonitorState();

		int pollersStarted = state.getMonitorsStarted();
		int pollersStopped = state.getMonitorsStopped();
		int pollersDisconnected = state.getMonitorsDisconnected();
		Collection<String> serviceNames = state.getServiceNames();
		int servicesWithOutages = state.getServicesDown().size();
		int monitorsWithOutages = state.getMonitorsWithServicesDown().size();

		final VerticalPanel panel = new VerticalPanel();
		panel.add(new Label(location.getName()));
	
		final FlexTable table = new FlexTable();
		table.setCellPadding(0);
		table.setCellSpacing(0);
		table.setStyleName("statusTable");
		final RowFormatter rf = table.getRowFormatter();
	
		table.setText(0, 0, "Monitors:");
		table.setHTML(0, 1, pollersStarted + " started");
		table.setText(1, 0, "");
		table.setHTML(1, 1, pollersStopped + " stopped");
		table.setText(2, 0, "");
		table.setHTML(2, 1, pollersDisconnected + " disconnected");

		for (int i = 0; i < 3; i++) {
			rf.setStyleName(i, state.getStatus().getStyle());
		}
		
		if (pollersStarted > 0) {
			// If pollers are started, add on service information
			table.setText(3, 0, "Services:");
			table.setHTML(3, 1, servicesWithOutages + " outages (of " + serviceNames.size() + " services)");
			table.setText(4, 0, "");
			table.setHTML(4, 1, monitorsWithOutages + " poller reporting errors");

			for (int i = 3; i < 5; i++) {
				rf.setStyleName(i, ServiceStatus.UP.getStyle());
				if (servicesWithOutages > 0) {
					if (monitorsWithOutages == pollersStarted) {
						rf.setStyleName(i, ServiceStatus.DOWN.getStyle());
					} else {
						rf.setStyleName(i, ServiceStatus.MARGINAL.getStyle());
					}
				}
			}
		}
	
		panel.add(table);
		return new InfoWindowContent(panel);
	}

    public static GWTBounds toGWTBounds(LatLngBounds bounds) {
        return new GWTBounds(GoogleMapsUtils.toGWTLatLng(bounds.getSouthWest()), GoogleMapsUtils.toGWTLatLng(bounds.getNorthEast()));
    }
    
    public static LatLngBounds toLatLngBounds(GWTBounds bounds) {
        return LatLngBounds.newInstance(toLatLng(bounds.getSouthWestCorner()), toLatLng(bounds.getNorthEastCorner()));
    }

    public static LatLng toLatLng(final GWTLatLng latLng) {
    	return LatLng.newInstance(latLng.getLatitude(), latLng.getLongitude());
    }

    public static GWTLatLng toGWTLatLng(final LatLng latLng) {
        return new GWTLatLng(latLng.getLatitude(), latLng.getLongitude());
    }

}
