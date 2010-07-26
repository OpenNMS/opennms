package org.opennms.features.poller.remote.gwt.server;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.features.poller.remote.gwt.client.ApplicationDetails;
import org.opennms.features.poller.remote.gwt.client.ApplicationInfo;
import org.opennms.features.poller.remote.gwt.client.GWTLatLng;
import org.opennms.features.poller.remote.gwt.client.StatusDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationDetails;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;

/**
 * <p>LocationDataService interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface LocationDataService {
    /**
     * <p>getLocationInfo</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    public LocationInfo getLocationInfo(final String locationName);
    /**
     * <p>getLocationInfo</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    public LocationInfo getLocationInfo(final OnmsMonitoringLocationDefinition def);
    /**
     * <p>getApplicationInfo</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public ApplicationInfo getApplicationInfo(final String applicationName);
    /**
     * <p>getApplicationInfo</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public ApplicationInfo getApplicationInfo(final OnmsApplication app);
    /**
     * <p>getApplicationInfo</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @param status a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationInfo} object.
     */
    public ApplicationInfo getApplicationInfo(final OnmsApplication app, final StatusDetails status);
    /**
     * <p>getLocationDetails</p>
     *
     * @param locationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
     */
    public LocationDetails getLocationDetails(final String locationName);
    /**
     * <p>getLocationDetails</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationDetails} object.
     */
    public LocationDetails getLocationDetails(final OnmsMonitoringLocationDefinition def);
    /**
     * <p>getApplicationDetails</p>
     *
     * @param applicationName a {@link java.lang.String} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    public ApplicationDetails getApplicationDetails(final String applicationName);
    /**
     * <p>getApplicationDetails</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.ApplicationDetails} object.
     */
    public ApplicationDetails getApplicationDetails(final OnmsApplication app);
    /**
     * <p>getUpdatedLocationsBetween</p>
     *
     * @param startDate a {@link java.util.Date} object.
     * @param endDate a {@link java.util.Date} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<LocationInfo> getUpdatedLocationsBetween(final Date startDate, final Date endDate);
    /**
     * <p>getLatLng</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @param geocode a boolean.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public GWTLatLng getLatLng(final OnmsMonitoringLocationDefinition def, boolean geocode);
    /**
     * <p>handleAllMonitoringLocationDefinitions</p>
     *
     * @param handlers a {@link java.util.Collection} object.
     */
    public void handleAllMonitoringLocationDefinitions(final Collection<LocationDefHandler> handlers);
    /**
     * <p>handleAllApplications</p>
     *
     * @param appHandlers a {@link java.util.Collection} object.
     */
    public void handleAllApplications(final Collection<ApplicationHandler> appHandlers);
    /**
     * <p>getLocationInfoForMonitor</p>
     *
     * @param monitorId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     */
    public LocationInfo getLocationInfoForMonitor(final Integer monitorId);
    /**
     * <p>getApplicationsForLocation</p>
     *
     * @param info a {@link org.opennms.features.poller.remote.gwt.client.location.LocationInfo} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<ApplicationInfo> getApplicationsForLocation(final LocationInfo info);
    
    /**
     * <p>getInfoForAllLocations</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<LocationInfo> getInfoForAllLocations();
    /**
     * <p>getInfoForAllApplications</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<ApplicationInfo> getInfoForAllApplications();
    /**
     * <p>getStatusDetailsForLocation</p>
     *
     * @param def a {@link org.opennms.netmgt.model.OnmsMonitoringLocationDefinition} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public StatusDetails getStatusDetailsForLocation(OnmsMonitoringLocationDefinition def);
    /**
     * <p>getStatusDetailsForApplication</p>
     *
     * @param app a {@link org.opennms.netmgt.model.OnmsApplication} object.
     * @return a {@link org.opennms.features.poller.remote.gwt.client.StatusDetails} object.
     */
    public StatusDetails getStatusDetailsForApplication(OnmsApplication app);
    

}
