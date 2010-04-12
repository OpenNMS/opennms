package org.opennms.features.poller.remote.gwt.server;

import java.util.Collection;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

public interface LocationDataService {
	public LocationInfo getLocationInfo(final String locationName);
	public LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def);
	public LocationDetails getLocationDetails(final String locationName);
	public LocationDetails getLocationDetails(final OnmsMonitoringLocationDefinition def);
	public Collection<OnmsMonitoringLocationDefinition> findAllMonitoringLocationDefinitions();
	public void saveMonitoringLocationDefinitions(final Collection<OnmsMonitoringLocationDefinition> definitions);
	public Collection<LocationInfo> getUpdatedLocationsBetween(final Date startDate, final Date endDate);
}
