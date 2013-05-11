/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
import org.opennms.features.poller.remote.gwt.client.remoteevents.MapRemoteEventHandler;
import org.opennms.features.poller.remote.gwt.client.utils.BoundsBuilder;

public class DataManager implements MapRemoteEventHandler {

    private final Map<String, LocationInfo> m_locations = new HashMap<String, LocationInfo>();
    private final Map<String,ApplicationInfo> m_applications = new HashMap<String,ApplicationInfo>();
    
    public Map<String, LocationInfo> getLocationsMap() {
        return m_locations;
    }

    public Map<String, ApplicationInfo> getApplicationsMap() {
        return m_applications;
    }

    @Override
    public void updateApplication(final ApplicationInfo applicationInfo) {
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

    @Override
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

    @Override
    public void removeApplication(final String applicationName) {
        getApplicationsMap().remove(applicationName);
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

    @Override
    public void updateLocations(Collection<LocationInfo> locations) {
        for(LocationInfo location : locations) {
            // Update the location information in the model
            updateLocation(location);
        }
    }

    @Override
    public void updateComplete() {
        // TODO Auto-generated method stub
        
    }

    public int getLocationCount() {
        return m_locations.size();
    }
}
