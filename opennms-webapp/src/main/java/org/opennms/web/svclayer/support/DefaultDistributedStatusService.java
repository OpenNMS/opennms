/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.svclayer.support;

import static org.opennms.core.utils.InetAddressUtils.toInteger;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.dao.api.LocationMonitorDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.OnmsLocationMonitor.MonitorStatus;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.web.api.Util;
import org.opennms.web.command.DistributedStatusDetailsCommand;
import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.opennms.web.svclayer.SimpleWebTable.Cell;
import org.opennms.web.svclayer.model.RelativeTimePeriod;
import org.opennms.web.svclayer.support.DistributedStatusHistoryModel.ServiceGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

/**
 * <p>DefaultDistributedStatusService class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class DefaultDistributedStatusService implements DistributedStatusService, InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultDistributedStatusService.class);

    private MonitoredServiceDao m_monitoredServiceDao;
    private LocationMonitorDao m_locationMonitorDao;
    private ApplicationDao m_applicationDao;
    private ResourceDao m_resourceDao;
    private GraphDao m_graphDao;
    private boolean m_layoutApplicationsVertically = false;
    
    private static final MonitoredServiceComparator MONITORED_SERVICE_COMPARATOR = new MonitoredServiceComparator();
    private static final ServiceGraphComparator SERVICE_GRAPH_COMPARATOR = new ServiceGraphComparator();
    private static final LocationStatusComparator LOCATION_STATUS_COMPARATOR = new LocationStatusComparator();
    private static final PollStatus NO_RECORDED_STATUS;
    
    static {
        NO_RECORDED_STATUS = PollStatus.unknown("No status recorded for this service from this location");
        NO_RECORDED_STATUS.setTimestamp(null);
    }

    public enum Severity {
        INDETERMINATE("Indeterminate"),
        NORMAL("Normal"),
        WARNING("Warning"),
        CRITICAL("Critical");
        
        private final String m_style;

        private Severity(String style) {
            m_style = style;
        }
        
        public String getStyle() {
            return m_style;
        }
    }
    
    public static class MonitoredServiceComparator implements Comparator<OnmsMonitoredService>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = 3000643751074224389L;

        @Override
        public int compare(OnmsMonitoredService o1, OnmsMonitoredService o2) {
            int diff;
            diff = o1.getIpInterface().getNode().getLabel().compareToIgnoreCase(o2.getIpInterface().getNode().getLabel());
            if (diff != 0) {
                return diff;
            }

            diff = toInteger(o1.getIpAddress()).compareTo(toInteger(o2.getIpAddress()));
            if (diff != 0) {
                return diff;
            }

            return o1.getServiceName().compareToIgnoreCase(o2.getServiceName());
        }
    }

    /**
     * Comparator for ServiceGraph objects. 
     * Orders objects with no errors and then those with errors and orders within
     * each of these groups by the service ordering (see MonitoredServiceComparator).
     * 
     * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
     */
    public static class ServiceGraphComparator implements Comparator<ServiceGraph>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -1365958323886041945L;

        @Override
        public int compare(ServiceGraph o1, ServiceGraph o2) {
            if ((o1.getErrors().length == 0 && o2.getErrors().length == 0)
                    || (o1.getErrors().length > 0 && o2.getErrors().length > 0)) {
                return MONITORED_SERVICE_COMPARATOR.compare(o1.getService(), o2.getService());
            } else if (o1.getErrors().length > 0) {
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    public static class LocationStatusComparator implements Comparator<OnmsLocationSpecificStatus>, Serializable {
        /**
         * 
         */
        private static final long serialVersionUID = -5854706886193427256L;

        @Override
        public int compare(OnmsLocationSpecificStatus o1, OnmsLocationSpecificStatus o2) {
            if ((o1.getPollResult().isUnknown() && o2.getPollResult().isUnknown())
                    || (!o1.getPollResult().isUnknown() && !o2.getPollResult().isUnknown())) {
                return o1.getMonitoredService().compareTo(o2.getMonitoredService());
            } else if (o1.getPollResult().isUnknown()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
    
    
    
    /**
     * <p>getApplicationCount</p>
     *
     * @return a int.
     */
    @Override
    public int getApplicationCount() {
        return m_applicationDao.countAll();
    }

    /** {@inheritDoc} */
    @Override
    public SimpleWebTable createStatusTable(DistributedStatusDetailsCommand command, Errors errors) {
        SimpleWebTable table = new SimpleWebTable(); 
        table.setErrors(errors);
        
        // Already had some validation errors, so don't bother doing anything 
        if (table.getErrors().hasErrors()) {
            return table;
        }
        
        table.setTitle("Distributed status view for " + command.getApplication() + " from " + command.getLocation() + " location");

        List<OnmsLocationSpecificStatus> status = findLocationSpecificStatus(command, table.getErrors());
        
        // No data was found, and an error was probably added, so just return
        if (status == null) {
            return table;
        }
        
        table.addColumn("Node", "");
        table.addColumn("Monitor", "");
        table.addColumn("Service", "");
        table.addColumn("Status", "");
        table.addColumn("Response", "");
        table.addColumn("Last Status Change", "");
        table.addColumn("Last Update", "");
        
        SortedSet<OnmsLocationSpecificStatus> sortedStatus = new TreeSet<OnmsLocationSpecificStatus>(LOCATION_STATUS_COMPARATOR);
        sortedStatus.addAll(status);        
        for (OnmsLocationSpecificStatus s : sortedStatus) {
            table.newRow();
            table.addCell(s.getMonitoredService().getIpInterface().getNode().getLabel(), 
                          getStyleForPollResult(s.getPollResult()),
                          "element/node.jsp?node=" + s.getMonitoredService().getIpInterface().getNode().getId());
            table.addCell(s.getLocationMonitor().getDefinitionName() + "-"
                          + s.getLocationMonitor().getId(),
                          "",
                          "distributed/locationMonitorDetails.htm?monitorId=" + s.getLocationMonitor().getId()); 
            table.addCell(s.getMonitoredService().getServiceName(), "",
                          "element/service.jsp?ifserviceid="
                          + s.getMonitoredService().getId());
            table.addCell(s.getPollResult().getStatusName(),
                          "bright");
            table.addCell(getResponseText(s.getPollResult()), "");
            table.addCell(reDateify(s.getPollResult().getTimestamp()), "");
            table.addCell(reDateify(s.getLocationMonitor().getLastCheckInTime()), "");
        }
        
        return table;
    }

    /**
     * TODO: Use the enum for these string values
     */
    private String getStyleForPollResult(PollStatus status) {
        if (status.isAvailable()) {
            return "Normal";
        } else if (status.isUnresponsive()) {
            return "Warning";
        } else if (status.isUnknown()) {
            return "Indeterminate";
        } else {
            return "Critical";
        }
    }

    private String getResponseText(PollStatus status) {
        if (status.isAvailable()) {
            Double responseTime = status.getResponseTime();
            if (responseTime != null && responseTime >= 0) {
            	return responseTime + "ms";
            } else {
                return "";
            }
        } else {
            return status.getReason(); 
        }
    }
    
    /**
     * Convert any Date into a fresh, brand-new java.util.Date.
     * We use this so that we get reliable results from Date.toString(),
     * since things like java.sql.Date have a different toString() format.
     * 
     * @param date input date
     * @return brand spankin' new java.util.Date
     */
    private Date reDateify(Date date) {
        if (date == null) {
            return null;
        } else {
            return new Date(date.getTime());
        }
    }


    /**
     * <p>findLocationSpecificStatus</p>
     *
     * @param command a {@link org.opennms.web.command.DistributedStatusDetailsCommand} object.
     * @param errors a {@link org.springframework.validation.Errors} object.
     * @return a {@link java.util.List} object or null if no location monitors are registered for the specified location and application tuple
     */
    protected List<OnmsLocationSpecificStatus> findLocationSpecificStatus(DistributedStatusDetailsCommand command, Errors errors) throws IllegalArgumentException {
        String locationName = command.getLocation();
        String applicationName = command.getApplication();

        Assert.notNull(locationName, "location cannot be null");
        Assert.notNull(applicationName, "application cannot be null");
        
        OnmsMonitoringLocationDefinition location = m_locationMonitorDao.findMonitoringLocationDefinition(locationName);
        if (location == null) {
            throw new IllegalArgumentException("Could not find location for "
                                               + "location name \""
                                               + locationName + "\"");
        }
        
        OnmsApplication application = m_applicationDao.findByName(applicationName);
        if (application == null) {
            throw new IllegalArgumentException("Could not find application "
                                               + "for application name \""
                                               + applicationName + "\"");
        }

        Collection<OnmsLocationMonitor> locationMonitors = m_locationMonitorDao.findByLocationDefinition(location);
        
        if (locationMonitors.size() == 0) {
            errors.reject("location.no-monitors",
                          new Object[] { applicationName, locationName },
                          "No remote pollers have registered for this "
                          + "application and location");
            return null;
        }
        
        List<OnmsLocationMonitor> sortedLocationMonitors = new ArrayList<OnmsLocationMonitor>(locationMonitors);
        Collections.sort(sortedLocationMonitors);
        
        Collection<OnmsMonitoredService> services = m_monitoredServiceDao.findByApplication(application);
        
        List<OnmsMonitoredService> sortedServices = new ArrayList<OnmsMonitoredService>(services);
        Collections.sort(sortedServices);
                                                                     
        List<OnmsLocationSpecificStatus> status = new LinkedList<OnmsLocationSpecificStatus>();
        for (OnmsMonitoredService service : sortedServices) {
            for (OnmsLocationMonitor locationMonitor : sortedLocationMonitors) {
                OnmsLocationSpecificStatus currentStatus = m_locationMonitorDao.getMostRecentStatusChange(locationMonitor, service);
                if (currentStatus == null) {
                    status.add(new OnmsLocationSpecificStatus(locationMonitor, service, NO_RECORDED_STATUS));
                } else {
                    status.add(currentStatus);
                }
            }
        }

        return status;
    }

    /** {@inheritDoc} */
    @Override
    public SimpleWebTable createFacilityStatusTable(Date start, Date end) {
        Assert.notNull(start, "argument start cannot be null");
        Assert.notNull(end, "argument end cannot be null");
        if (!start.before(end)) {
            throw new IllegalArgumentException("start date (" + start + ") must be older than end date (" + end + ")");
        }
        
        SimpleWebTable table = new SimpleWebTable();
        
        List<OnmsMonitoringLocationDefinition> locationDefinitions = m_locationMonitorDao.findAllMonitoringLocationDefinitions();

        Collection<OnmsApplication> applications = m_applicationDao.findAll();
        if (applications.size() == 0) {
            throw new IllegalArgumentException("there are no applications");
        }
        
        List<OnmsApplication> sortedApplications = new ArrayList<OnmsApplication>(applications);
        Collections.sort(sortedApplications);
        
        Collection<OnmsLocationSpecificStatus> mostRecentStatuses = m_locationMonitorDao.getAllMostRecentStatusChanges();

        Collection<OnmsLocationSpecificStatus> statusesPeriod = new HashSet<OnmsLocationSpecificStatus>();
        statusesPeriod.addAll(m_locationMonitorDao.getAllStatusChangesAt(start));
        statusesPeriod.addAll(m_locationMonitorDao.getStatusChangesBetween(start, end));

        table.setTitle("Distributed Status Summary");
        
        table.addColumn("Area", "");
        table.addColumn("Location", "");
        for (OnmsApplication application : sortedApplications) {
            table.addColumn(application.getName(), "");
        }
        
        for (OnmsMonitoringLocationDefinition locationDefinition : locationDefinitions) {
            Collection<OnmsLocationMonitor> monitors = m_locationMonitorDao.findByLocationDefinition(locationDefinition);
            
            table.newRow();
            table.addCell(locationDefinition.getArea(), "");
            table.addCell(locationDefinition.getName(), "");
            
            for (OnmsApplication application : sortedApplications) {
                Collection<OnmsMonitoredService> memberServices = m_monitoredServiceDao.findByApplication(application);
                Severity status = calculateCurrentStatus(monitors, memberServices, mostRecentStatuses);
            
                Set<OnmsLocationSpecificStatus> selectedStatuses = filterStatus(statusesPeriod, monitors, memberServices);
                
                if (selectedStatuses.size() > 0) {
                    String percentage = calculatePercentageUptime(memberServices, selectedStatuses, start, end);
                    table.addCell(percentage, status.getStyle(), createHistoryPageUrl(locationDefinition, application));
                } else {
                    table.addCell("No data", status.getStyle());
                }
            }
        }
        
        if (isLayoutApplicationsVertically()) {
            SimpleWebTable newTable = new SimpleWebTable();
            newTable.setErrors(table.getErrors());
            newTable.setTitle(table.getTitle());
            
            newTable.addColumn("Application");
            for (List<Cell> row : table.getRows()) {
                // The location is in the second row
                newTable.addColumn(row.get(1).getContent(), row.get(1).getStyleClass());
            }
            
            for (Cell columnHeader : table.getColumnHeaders().subList(2, table.getColumnHeaders().size())) {
                // This is the index into collumn list of the old table to get the data for the current application
                int rowColumnIndex = newTable.getRows().size() + 2;
                
                newTable.newRow();
                newTable.addCell(columnHeader.getContent(), columnHeader.getStyleClass());
                
                for (List<Cell> row : table.getRows()) {
                    newTable.addCell(row.get(rowColumnIndex).getContent(), row.get(rowColumnIndex).getStyleClass(), row.get(rowColumnIndex).getLink());
                }
            }
            
            return newTable;
        }
        
        return table;
    }
    
   

    /**
     * Filter a collection of OnmsLocationSpecificStatus based on a
     * collection of monitors and a collection of monitored services.
     * A specific OnmsLocationSpecificStatus instance will only be
     * returned if its OnmsLocationMonitor is in the collection of
     * monitors and its OnmsMonitoredService is in the collection of
     * services.
     * 
     * @param statuses
     * @param monitors
     * @param services
     * @return filtered list
     */
    private Set<OnmsLocationSpecificStatus> filterStatus(Collection<OnmsLocationSpecificStatus> statuses,
                                                         Collection<OnmsLocationMonitor> monitors,
                                                         Collection<OnmsMonitoredService> services) {
        Set<OnmsLocationSpecificStatus> filteredStatuses = new HashSet<OnmsLocationSpecificStatus>();
        
        for (OnmsLocationSpecificStatus status : statuses) {
            if (!monitors.contains(status.getLocationMonitor())) {
                continue;
            }
        
            if (!services.contains(status.getMonitoredService())) {
                continue;
            }

            filteredStatuses.add(status);
        }

        return filteredStatuses;
    }

    /**
     * <p>calculateCurrentStatus</p>
     *
     * @param monitors a {@link java.util.Collection} object.
     * @param applicationServices a {@link java.util.Collection} object.
     * @param statuses a {@link java.util.Collection} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultDistributedStatusService.Severity} object.
     */
    public Severity calculateCurrentStatus(
            Collection<OnmsLocationMonitor> monitors,
            Collection<OnmsMonitoredService> applicationServices,
            Collection<OnmsLocationSpecificStatus> statuses) {
        int goodMonitors = 0;
        int badMonitors = 0;
        
        for (OnmsLocationMonitor monitor : monitors) {
            if (monitor == null || monitor.getStatus() != MonitorStatus.STARTED) {
                continue;
            }
            
            Severity status = calculateCurrentStatus(monitor, applicationServices, statuses);
            
            if (status == Severity.NORMAL) {
                goodMonitors++;
            } else if (status != Severity.INDETERMINATE) {
                badMonitors++;
            }
        }
        
        if (goodMonitors == 0 && badMonitors == 0) {
            return Severity.INDETERMINATE; // No current responses
        } else if (goodMonitors != 0 && badMonitors == 0) {
            return Severity.NORMAL; // No bad responses
        } else if (goodMonitors == 0 && badMonitors != 0) {
            return Severity.CRITICAL; // All bad responses
        } else if (goodMonitors != 0 && badMonitors != 0){
            return Severity.WARNING; // Some bad responses
        } else {
            throw new IllegalStateException("Shouldn't have gotten here. "
                                            + "good monitors = "
                                            + goodMonitors
                                            + ", bad monitors = "
                                            + badMonitors);
        }
    }
    
    /**
     * <p>calculateCurrentStatus</p>
     *
     * @param monitor a {@link org.opennms.netmgt.model.OnmsLocationMonitor} object.
     * @param applicationServices a {@link java.util.Collection} object.
     * @param statuses a {@link java.util.Collection} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultDistributedStatusService.Severity} object.
     */
    public Severity calculateCurrentStatus(OnmsLocationMonitor monitor,
            Collection<OnmsMonitoredService> applicationServices,
            Collection<OnmsLocationSpecificStatus> statuses) {
        Set<PollStatus> pollStatuses = new HashSet<PollStatus>();
        
        for (OnmsMonitoredService service : applicationServices) {
            boolean foundIt = false;
            for (OnmsLocationSpecificStatus status : statuses) {
                if (status.getMonitoredService().equals(service) && status.getLocationMonitor().equals(monitor)) {
                    pollStatuses.add(status.getPollResult());
                    foundIt = true;
                    break;
                }
            }
            if (!foundIt) {
                pollStatuses.add(PollStatus.unknown("No status found for this service"));
                LOG.debug("Did not find status for service {} in application.  Setting status for it to unknown.", service);
            }
        }
        
        return calculateStatus(pollStatuses);
    }       
    
    /**
     * <p>calculateStatus</p>
     *
     * @param pollStatuses a {@link java.util.Collection} object.
     * @return a {@link org.opennms.web.svclayer.support.DefaultDistributedStatusService.Severity} object.
     */
    public Severity calculateStatus(Collection<PollStatus> pollStatuses) {
        int goodStatuses = 0;
        int badStatuses = 0;
        
        for (PollStatus pollStatus : pollStatuses) {
            if (pollStatus.isAvailable()) {
                goodStatuses++;
            } else if (!pollStatus.isUnknown()) {
                badStatuses++;
            }
        }

        if (goodStatuses == 0 && badStatuses == 0) {
            return Severity.INDETERMINATE;
        } else if (goodStatuses > 0 && badStatuses == 0) {
            return Severity.NORMAL;
        } else {
            return Severity.CRITICAL;
        }
    }

    /**
     * Calculate the percentage of time that all services are up for this
     * application on this remote monitor.
     *
     * @param applicationServices services to report on
     * @param statuses status entries to use for report
     * @param startDate start date.  The report starts on this date.
     * @param endDate end date.  The report ends the last millisecond prior
     * this date.
     * @return representation of the percentage uptime out to three decimal
     * places.  Null is returned if there is no data.
     */
    public String calculatePercentageUptime(
            Collection<OnmsMonitoredService> applicationServices,
            Collection<OnmsLocationSpecificStatus> statuses,
            Date startDate, Date endDate) {
        /*
         * The methodology is as such:
         * 1) Sort the status entries by their timestamp;
         * 2) Create a Map of each monitored service with a default
         *    PollStatus of unknown.
         * 3) Iterate through the sorted list of status entries until
         *    we hit a timestamp that is not within our time range or
         *    run out of entries.
         *    a) Along the way, update the status Map with the current
         *       entry's status, and calculate the current status.
         *    b) If the current timestamp is before the start time, store
         *       the current status so we can use it once we cross over
         *       into our time range and then continue.
         *    c) If the previous status is normal, then count up the number
         *       of milliseconds since the previous state change entry in
         *       the time range (or the beginning of the range if this is
         *       the first entry in within the time range), and add that
         *       a counter of "normal" millseconds.
         *    d) Finally, save the current date and status for later use.
         * 4) Perform the same computation in 3c, except count the number
         *    of milliseconds since the last state change entry (or the
         *    start time if there were no entries) and the end time, and add
         *    that to the counter of "normal" milliseconds.
         * 5) Divide the "normal" milliseconds counter by the total number
         *    of milliseconds in our time range and compute and return a
         *    percentage.
         */

        List<OnmsLocationSpecificStatus> sortedStatuses =
            new LinkedList<OnmsLocationSpecificStatus>(statuses);
        Collections.sort(sortedStatuses, new Comparator<OnmsLocationSpecificStatus>(){
            @Override
            public int compare(OnmsLocationSpecificStatus o1, OnmsLocationSpecificStatus o2) {
                return o1.getPollResult().getTimestamp().compareTo(o2.getPollResult().getTimestamp());
            }
        });

        HashMap<OnmsMonitoredService,PollStatus> serviceStatus =
            new HashMap<OnmsMonitoredService,PollStatus>();
        for (OnmsMonitoredService service : applicationServices) {
            serviceStatus.put(service, PollStatus.unknown("No history for this service from this location"));
        }
        
        float normalMilliseconds = 0f;
        
        Date lastDate = startDate;
        Severity lastStatus = Severity.CRITICAL;
        
        for (OnmsLocationSpecificStatus status : sortedStatuses) {
            Date currentDate = status.getPollResult().getTimestamp();

            if (!currentDate.before(endDate)) {
                // We're at or past the end date, so we're done processing
                break;
            }
            
            serviceStatus.put(status.getMonitoredService(), status.getPollResult());
            Severity currentStatus = calculateStatus(serviceStatus.values());
            
            if (currentDate.before(startDate)) {
                /*
                 * We're not yet to a date that is inside our time period, so
                 * we don't need to check the status and adjust the
                 * normalMilliseconds variable, but we do need to save the
                 * status so we have an up-to-date status when we cross the
                 * start date.
                 */
                lastStatus = currentStatus;
                continue;
            }
            
            /*
             * Because we *just* had a state change, we want to look at the
             * value of the *last* status.
             */
            if (lastStatus == Severity.NORMAL) {
                long milliseconds = currentDate.getTime() - lastDate.getTime();
                normalMilliseconds += milliseconds;
            }
            
            lastDate = currentDate;
            lastStatus = currentStatus;
        }
        
        if (lastStatus == Severity.NORMAL) {
            long milliseconds = endDate.getTime() - lastDate.getTime();
            normalMilliseconds += milliseconds;
        }

        float percentage = normalMilliseconds /
            (endDate.getTime() - startDate.getTime()) * 100;
        return new DecimalFormat("0.000").format((double) percentage) + "%";
    }

    private String createHistoryPageUrl(
            OnmsMonitoringLocationDefinition locationDefinition,
            OnmsApplication application) {

        List<String> params = new ArrayList<String>(2);
        params.add("location=" + Util.encode(locationDefinition.getName()));
        params.add("application=" + Util.encode(application.getName()));
        
        return "distributedStatusHistory.htm"
            + "?"
            + StringUtils.collectionToDelimitedString(params, "&");
    }

    /** {@inheritDoc} */
    @Override
    public DistributedStatusHistoryModel createHistoryModel(
            String locationName, String monitorId, String applicationName,
            String timeSpan, String previousLocationName) {
        List<String> errors = new LinkedList<String>();
        
        List<OnmsMonitoringLocationDefinition> locationDefinitions = m_locationMonitorDao.findAllMonitoringLocationDefinitions();

        List<RelativeTimePeriod> periods = Arrays.asList(RelativeTimePeriod.getDefaultPeriods());

        Collection<OnmsApplication> applications = m_applicationDao.findAll();
        List<OnmsApplication> sortedApplications = new ArrayList<OnmsApplication>(applications);
        Collections.sort(sortedApplications);

        OnmsMonitoringLocationDefinition location = new OnmsMonitoringLocationDefinition();
        if (locationName == null) {
            if (!locationDefinitions.isEmpty()) {
                location = locationDefinitions.get(0);
            }
        } else {
            location = m_locationMonitorDao.findMonitoringLocationDefinition(locationName);
            if (location == null) {
                errors.add("Could not find location definition '" + locationName + "'");
                if (!locationDefinitions.isEmpty()) {
                    location = locationDefinitions.get(0);
                }
            }
        }
        
        int monitorIdInt = -1;
        
        if (monitorId != null && monitorId.length() > 0) {
            try {
                monitorIdInt = WebSecurityUtils.safeParseInt(monitorId);
            } catch (NumberFormatException e) {
                errors.add("Monitor ID '" + monitorId + "' is not an integer");
            }
        }

        OnmsApplication application = new OnmsApplication();
        if (applicationName == null) {
            if (!sortedApplications.isEmpty()) {
                application = sortedApplications.get(0);
            }
        } else {
            application = m_applicationDao.findByName(applicationName);
            if (application == null) {
                errors.add("Could not find application '" + applicationName + "'");
                if (!sortedApplications.isEmpty()) {
                    application = sortedApplications.get(0);
                }
            }
        }
        
        Collection<OnmsLocationMonitor> monitors = m_locationMonitorDao.findByLocationDefinition(location);
        List<OnmsLocationMonitor> sortedMonitors = new LinkedList<OnmsLocationMonitor>(monitors);
        Collections.sort(sortedMonitors);

        OnmsLocationMonitor monitor = null;
        if (monitorIdInt != -1 && location.getName().equals(previousLocationName)) {
            for (OnmsLocationMonitor m : sortedMonitors) {
                if (m.getId().equals(monitorIdInt)) {
                    monitor = m;
                    break;
                }
            }
        }
        
        if (monitor == null && !sortedMonitors.isEmpty()) {
            monitor = sortedMonitors.get(0);
        }
        
        RelativeTimePeriod period = RelativeTimePeriod.getPeriodByIdOrDefault(timeSpan);
        
        /*
         * Initialize the heirarchy under the service so that we don't get
         * a LazyInitializationException later when the JSP page is pulling
         * data out of the model object.
         */
        Collection<OnmsMonitoredService> memberServices = m_monitoredServiceDao.findByApplication(application);
        for (OnmsMonitoredService service : memberServices) {
            m_locationMonitorDao.initialize(service.getIpInterface());
            m_locationMonitorDao.initialize(service.getIpInterface().getNode());
        }

        Collection<OnmsMonitoredService> applicationMemberServices = m_monitoredServiceDao.findByApplication(application);
        if (applicationMemberServices.isEmpty()) {
            errors.add("There are no services in the application '" + applicationName + "'");
        }
        
        DistributedStatusHistoryModel model = new DistributedStatusHistoryModel(locationDefinitions,
                                                 sortedApplications,
                                                 sortedMonitors,
                                                 periods,
                                                 location,
                                                 application,
                                                 applicationMemberServices,
                                                 monitor,
                                                 period,
                                                 errors);
        initializeGraphUrls(model);
        return model;
    }
    
    private void initializeGraphUrls(DistributedStatusHistoryModel model) {
        if (model.getChosenMonitor() != null) {
        
            Collection<OnmsMonitoredService> services = model.getChosenApplicationMemberServices();
        
            long[] times = model.getChosenPeriod().getStartAndEndTimes();
        
            SortedSet<ServiceGraph> serviceGraphs = new TreeSet<ServiceGraph>(SERVICE_GRAPH_COMPARATOR);
            for (OnmsMonitoredService service : services) {
                serviceGraphs.add(getServiceGraphForService(model.getChosenMonitor(), service, times));
            }
        
            model.setServiceGraphs(serviceGraphs);

        }
    }

    private ServiceGraph getServiceGraphForService(OnmsLocationMonitor locMon, OnmsMonitoredService service, long[] times) {
        OnmsResource resource = m_resourceDao.getResourceForIpInterface(service.getIpInterface(), locMon);
        if (resource == null) {
            return new ServiceGraph(service, new String[] { "Resource could not be found.  Has any response time data been collected for this service from this remote poller?" });
        }
        
        String graphName = service.getServiceName().toLowerCase();
        try {
            m_graphDao.getPrefabGraph(graphName);
        } catch (ObjectRetrievalFailureException e) {
            return new ServiceGraph(service, new String[] { "Graph definition could not be found for '" + graphName + "'.  A graph definition needs to be created for this service." });
        }
        
        PrefabGraph[] prefabGraphs = m_graphDao.getPrefabGraphsForResource(resource);
        for (PrefabGraph graph : prefabGraphs) {
            if (graph.getName().equals(graphName)) {
                String url = "graph/graph.png"
                    + "?report=" + Util.encode(graph.getName())
                    + "&resourceId=" + Util.encode(resource.getId())
                    + "&start=" + times[0] + "&end=" + times[1];
                return new ServiceGraph(service, url);
            }
        }
        
        return new ServiceGraph(service, new String[] { "Graph could not be found for '" + graphName + "' on this resource.  Has any response time data been collected for this service from this remote poller and is the graph definition correct?" });
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_monitoredServiceDao != null, "property monitoredServiceDao cannot be null");
        Assert.state(m_locationMonitorDao != null, "property locationMonitorDao cannot be null");
        Assert.state(m_applicationDao != null, "property applicationDao cannot be null");
        Assert.state(m_resourceDao != null, "property resourceDao cannot be null");
        Assert.state(m_graphDao != null, "property graphDao cannot be null");
    }
    

    /**
     * <p>setMonitoredServiceDao</p>
     *
     * @param monitoredServiceDao a {@link org.opennms.netmgt.dao.api.MonitoredServiceDao} object.
     */
    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
        
    }

    /**
     * <p>setLocationMonitorDao</p>
     *
     * @param locationMonitorDao a {@link org.opennms.netmgt.dao.api.LocationMonitorDao} object.
     */
    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
        
    }
    
    /**
     * <p>setApplicationDao</p>
     *
     * @param applicationDao a {@link org.opennms.netmgt.dao.api.ApplicationDao} object.
     */
    public void setApplicationDao(ApplicationDao applicationDao) {
        m_applicationDao = applicationDao;
        
    }

    /**
     * <p>getResourceDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public ResourceDao getResourceDao() {
        return m_resourceDao;
    }

    /**
     * <p>setResourceDao</p>
     *
     * @param resourceDao a {@link org.opennms.netmgt.dao.api.ResourceDao} object.
     */
    public void setResourceDao(ResourceDao resourceDao) {
        m_resourceDao = resourceDao;
    }

    /**
     * <p>getGraphDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.GraphDao} object.
     */
    public GraphDao getGraphDao() {
        return m_graphDao;
    }

    /**
     * <p>setGraphDao</p>
     *
     * @param graphDao a {@link org.opennms.netmgt.dao.api.GraphDao} object.
     */
    public void setGraphDao(GraphDao graphDao) {
        m_graphDao = graphDao;
    }
    
    /**
     * <p>setLayoutApplicationsVertically</p>
     *
     * @param layoutApplicationsVertically a boolean.
     */
    public void setLayoutApplicationsVertically(boolean layoutApplicationsVertically) {
        m_layoutApplicationsVertically = layoutApplicationsVertically;
    }
    
    /**
     * <p>isLayoutApplicationsVertically</p>
     *
     * @return a boolean.
     */
    public boolean isLayoutApplicationsVertically() {
        return m_layoutApplicationsVertically;
    }

}
