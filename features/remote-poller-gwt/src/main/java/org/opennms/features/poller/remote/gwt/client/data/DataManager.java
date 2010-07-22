package org.opennms.features.poller.remote.gwt.client.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.GWTBounds;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.features.poller.remote.gwt.client.utils.BoundsBuilder;

public class DataManager {

    private final Map<String, LocationInfo> m_locations = new HashMap<String, LocationInfo>();
    private final Map<String,ApplicationInfo> m_applications = new HashMap<String,ApplicationInfo>();
    
    public Map<String, LocationInfo> getLocationsMap() {
        return m_locations;
    }

    public Map<String, ApplicationInfo> getApplicationsMap() {
        return m_applications;
    }

    public void createOrUpdateApplication(final ApplicationInfo applicationInfo) {
        if (applicationInfo.getLocations().size() == 0) {
            applicationInfo.setPriority(Long.MAX_VALUE);
        } else {
            applicationInfo.setPriority(0L);
            for (final String location : applicationInfo.getLocations()) {
                final LocationInfo locationInfo = getLocationsMap().get(location);
                if (locationInfo != null) {
                    applicationInfo.setPriority(applicationInfo.getPriority() + locationInfo.getPriority());
                }
            }
        }
        getApplicationsMap().put(applicationInfo.getName(), applicationInfo);
    }

    public void updateLocation(final LocationInfo locationInfo) {
        getLocationsMap().put(locationInfo.getName(), locationInfo);
    }

    public TreeSet<String> getAllApplicationNames() {
        return new TreeSet<String>(getApplicationsMap().keySet());
    }

    public TreeSet<String> getAllLocationNames() {
        return new TreeSet<String>(getLocationsMap().keySet());
    }

    public List<String> getAllTags() {
        final List<String> retval = new ArrayList<String>();
        
        for (final LocationInfo location : getLocationsMap().values()) {
            retval.addAll(location.getTags());
        }
        return retval;
    }

    /** {@inheritDoc} 
     * @param name TODO*/
    public ApplicationInfo getApplicationInfo(final String name) {
        if (name == null) {
            return null;
        }
    
        return getApplicationsMap().get(name);
    }

    /** {@inheritDoc} 
     * @param locationName TODO*/
    public LocationInfo getLocation(String locationName) {
        return getLocationsMap().get(locationName);
    }

    public GWTBounds getLocationBounds() {
        BoundsBuilder bldr = new BoundsBuilder();
        
        for (final LocationInfo l : getLocationsMap().values()) {
            bldr.extend(l.getLatLng());
        }
        return bldr.getBounds();
    }

    public Collection<LocationInfo> getLocations() {
        return getLocationsMap().values();
    }

    public ArrayList<ApplicationInfo> getApplications() {
        ArrayList<ApplicationInfo> applicationList = new ArrayList<ApplicationInfo>();
        
        applicationList.addAll(getApplicationsMap().values());
        Collections.sort(applicationList);
        return applicationList;
    }

    public ApplicationInfo removeApplication(final String applicationName) {
        final ApplicationInfo info = getApplicationInfo(applicationName);
        if (info != null) {
            getApplicationsMap().remove(applicationName);
        }
        return info;
    }

    public List<LocationInfo> getMatchingLocations(LocationFilter filter) {
        final ArrayList<LocationInfo> locations = new ArrayList<LocationInfo>();
        
        for (final LocationInfo location : getLocations()) {
            
            if (filter.matches(location)) {
                locations.add(location);
            }
        }
        return locations;
    }

    public void updateLocations(Collection<LocationInfo> locations) {
        for(LocationInfo location : locations) {
            // Update the location information in the model
            updateLocation(location);
        }
    }
}
