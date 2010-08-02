package org.opennms.features.poller.remote.gwt.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

public interface ApplicationView {

    /**
     * <p>updateTimestamp</p>
     */
    public abstract void updateTimestamp();

    public abstract Set<Status> getSelectedStatuses();

    public abstract void initialize();

    public abstract void updateSelectedApplications( Set<ApplicationInfo> applications);

    public abstract void updateLocationList(
            ArrayList<LocationInfo> locationsForLocationPanel);

    public abstract void setSelectedTag(String selectedTag, List<String> allTags);

    public abstract void updateApplicationList(
            ArrayList<ApplicationInfo> applications);

    public abstract void updateApplicationNames(
            TreeSet<String> allApplicationNames);

    public abstract void fitMapToLocations(GWTBounds locationBounds);

    public abstract GWTBounds getMapBounds();

    public abstract void showLocationDetails(final String locationName,
            String htmlTitle, String htmlContent);

    public abstract void placeMarker(final GWTMarkerState markerState);

}