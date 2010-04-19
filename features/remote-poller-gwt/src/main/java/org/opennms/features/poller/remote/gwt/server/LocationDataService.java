package org.opennms.features.poller.remote.gwt.server;

import java.util.Collection;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

public interface LocationDataService {
	public LocationInfo getLocationInfo(final String locationName);
	public LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def, boolean includeStatus);
	public LocationDetails getLocationDetails(final String locationName);
	public LocationDetails getLocationDetails(final OnmsMonitoringLocationDefinition def);
	public Collection<LocationInfo> getUpdatedLocationsBetween(final Date startDate, final Date endDate);
	public GWTLatLng getLatLng(final OnmsMonitoringLocationDefinition def);
	public void handleAllMonitoringLocationDefinitions(final Collection<LocationDefHandler> handlers);
	public void handleAllApplications(final Collection<ApplicationHandler> appHandlers);
}
