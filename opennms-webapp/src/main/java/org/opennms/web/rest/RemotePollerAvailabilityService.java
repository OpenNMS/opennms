package org.opennms.web.rest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.hibernate.criterion.Restrictions;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsLocationAvailDataPoint;
import org.opennms.netmgt.model.OnmsLocationAvailDefinition;
import org.opennms.netmgt.model.OnmsLocationAvailDefinitionList;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinitionList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.web.rest.support.TimeChunker;
import org.opennms.web.rest.support.TimeChunker.TimeChunk;
import org.opennms.web.svclayer.support.DefaultDistributedStatusService.Severity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@Component
@PerRequest
@Path("remotelocations")
@Transactional
public class RemotePollerAvailabilityService extends OnmsRestService {


    @Autowired
    LocationMonitorDao m_locationMonitorDao;
    
    @Autowired
    ApplicationDao m_applicationDao;
    
    @Autowired
    MonitoredServiceDao m_monitoredServiceDao;
    
    @Autowired
    NodeDao m_nodeDao;
    
    @Context
    UriInfo m_uriInfo;
    
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public OnmsMonitoringLocationDefinitionList getRemoteLocationList(){
        List<OnmsMonitoringLocationDefinition> monitors = m_locationMonitorDao.findAllMonitoringLocationDefinitions();
        return new OnmsMonitoringLocationDefinitionList(monitors);
    }
    
    /**
     * Currently only here for world IPv6 day, returns all nodelabels. 
     * @return
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("participants")
    public String getParticipants(){
        List<OnmsNode> nodes = m_nodeDao.findAll();
        StringBuffer retVal = new StringBuffer();
        
        retVal.append("{\"participants\":[");
        for(int i  = 0; i < nodes.size(); i++) {
            OnmsNode node = nodes.get(i);
            if(i == 0) {
                retVal.append("{\"name\":\"" + node.getLabel() + "\"}");
            }else {
                retVal.append(",{\"name\":\"" + node.getLabel() + "\"}");
            }
        }
        retVal.append("]}");
        
        return retVal.toString();
        
    }
    
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("availability")
    public OnmsLocationAvailDefinitionList getAvailability() {
        MultivaluedMap<String, String> queryParameters = m_uriInfo.getQueryParameters();
        return getAvailabilityList(createTimeChunker(queryParameters), getSortedApplications(), null, getSelectedServices(queryParameters));
    }
    
    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("availability/{location}")
    public OnmsLocationAvailDefinitionList getAvailabilityByLocation(@PathParam("location") String location) {
        MultivaluedMap<String, String> queryParameters = m_uriInfo.getQueryParameters();
        
        Collection<OnmsMonitoredService> services = getSelectedServices(queryParameters);
        
        OnmsMonitoringLocationDefinition locationDefinition = m_locationMonitorDao.findMonitoringLocationDefinition(location);
        Collection<OnmsLocationMonitor> monitors = m_locationMonitorDao.findByLocationDefinition(locationDefinition);
        
        OnmsLocationAvailDefinitionList availList = getAvailabilityList(createTimeChunker(queryParameters), getSortedApplications(), monitors, services);
        
        return availList;
    }

    private Collection<OnmsMonitoredService> getSelectedServices(MultivaluedMap<String, String> queryParameters) {
        
        if(queryParameters.containsKey("host")) {
            String host = queryParameters.getFirst("host");
            OnmsCriteria criteria = new OnmsCriteria(OnmsMonitoredService.class);
            criteria.createAlias("ipInterface", "ipInterface", OnmsCriteria.LEFT_JOIN);
            criteria.createAlias("ipInterface.node", "node", OnmsCriteria.LEFT_JOIN);
            criteria.add(Restrictions.eq("node.label", host));
        
            return m_monitoredServiceDao.findMatching(criteria);
        }else {
            return m_monitoredServiceDao.findAll();
        }
    }
    
    private TimeChunker createTimeChunker(MultivaluedMap<String, String> params) {
        TimeChunker timeChunker;
        Date start = getStartTime(params);
        Date end = getEndTime(params);
        if((end.getTime() - start.getTime()) < TimeChunker.MINUTE) {
            throw new IllegalArgumentException("The endTime has to be after the startTime by 5 minutes.\nCurrently the startTime is " + start + " and endTime is " + end);
        }
        
        timeChunker = new TimeChunker(getResolution(params), start.getTime(), end.getTime() - start.getTime());
        return timeChunker;
    }

    private List<OnmsApplication> getSortedApplications() {
        List<OnmsApplication> sortedApplications;
        Collection<OnmsApplication> applications = m_applicationDao.findAll();
        
        if (applications.size() == 0) {
            throw new IllegalArgumentException("there are no applications");
        }
        
        sortedApplications = new ArrayList<OnmsApplication>(applications);
        Collections.sort(sortedApplications);
        return sortedApplications;
    }

    /**
     * 
     * @param timeChunker
     * @param sortedApplications
     * @param selectedMonitors 
     * @param selectedHost TODO
     * @param locationDefinitions
     * @return
     */
    private OnmsLocationAvailDefinitionList getAvailabilityList(TimeChunker timeChunker, List<OnmsApplication> sortedApplications, Collection<OnmsLocationMonitor> selectedMonitors, Collection<OnmsMonitoredService> selectedServices) {
        
        OnmsLocationAvailDefinitionList availList = new OnmsLocationAvailDefinitionList();
        
        while(timeChunker.hasNext()) {
            TimeChunk timeChunk = timeChunker.getNextSegment();
            
            Collection<OnmsLocationSpecificStatus> statusesPeriod = new HashSet<OnmsLocationSpecificStatus>();
            statusesPeriod.addAll(m_locationMonitorDao.getAllStatusChangesAt(timeChunk.getStartDate()));
            statusesPeriod.addAll(m_locationMonitorDao.getStatusChangesBetween(timeChunk.getStartDate(), timeChunk.getEndDate()));
            
            OnmsLocationAvailDataPoint point = new OnmsLocationAvailDataPoint();
            point.setTime(timeChunk.getEndDate());
            
            for(OnmsApplication application : sortedApplications) {
                
                Collection<OnmsLocationMonitor> monitors;
                if(selectedMonitors != null) {
                    monitors = selectedMonitors;
                } else {
                    monitors = m_locationMonitorDao.findByApplication(application);
                }
                
                Set<OnmsLocationSpecificStatus> selectedStatuses = filterStatus(statusesPeriod, (List<OnmsLocationMonitor>) monitors, selectedServices);
                
                point.addAvailDefinition(new OnmsLocationAvailDefinition(application.getName(), calculatePercentageUptime(selectedServices, selectedStatuses, timeChunk.getStartDate(), timeChunk.getEndDate())));
                
            }
            
            availList.add(point);
        }
        
        return availList;
    }
    
    private int getResolution(MultivaluedMap<String, String> params) {
        if(params.containsKey("resolution")) {
            String resolution = params.getFirst("resolution");
            if(resolution.equalsIgnoreCase("minute")) {
                return TimeChunker.MINUTE;
            } else if(resolution.equalsIgnoreCase("hourly")) {
                return TimeChunker.HOURLY;
            }else if(resolution.equalsIgnoreCase("daily")){
                return TimeChunker.DAILY;
            }else {
                return TimeChunker.MINUTE;
            }
            
        } else {
            return TimeChunker.MINUTE;
        }
        
    }

    private Date getEndTime(MultivaluedMap<String, String> params) {
        if(params.containsKey("endTime")) {
            String value = params.getFirst("endTime");
            return new Date(Long.valueOf(value));
        } else {
            return new Date();
        }
    }
    
    private Date getStartTime(MultivaluedMap<String, String> params) {
        if(params.containsKey("startTime")) {
            String startTime = params.getFirst("startTime");
            return new Date(Long.valueOf(startTime));
        } else {
            Calendar calendar = Calendar.getInstance();
            return new GregorianCalendar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0).getTime();
        }
        
    }

    private String calculatePercentageUptime(Collection<OnmsMonitoredService> applicationServices, Set<OnmsLocationSpecificStatus> statuses, Date startDate, Date endDate) {
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

        List<OnmsLocationSpecificStatus> sortedStatuses = new LinkedList<OnmsLocationSpecificStatus>(statuses);
        
        Collections.sort(sortedStatuses, new Comparator<OnmsLocationSpecificStatus>(){
            public int compare(OnmsLocationSpecificStatus o1, OnmsLocationSpecificStatus o2) {
                return o1.getPollResult().getTimestamp().compareTo(o2.getPollResult().getTimestamp());
            }
        });

        HashMap<OnmsMonitoredService,PollStatus> serviceStatus = new HashMap<OnmsMonitoredService,PollStatus>();
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
        return new DecimalFormat("0.000").format((double) percentage);
    }

    private Severity calculateStatus(Collection<PollStatus> pollStatuses) {
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

    private Set<OnmsLocationSpecificStatus> filterStatus(Collection<OnmsLocationSpecificStatus> statuses, List<OnmsLocationMonitor> monitors, Collection<OnmsMonitoredService> services) {
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

}
