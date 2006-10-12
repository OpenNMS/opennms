package org.opennms.web.svclayer.support;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.dao.ApplicationDao;
import org.opennms.netmgt.dao.CategoryDao;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatus;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.web.Util;
import org.opennms.web.svclayer.DistributedStatusService;
import org.opennms.web.svclayer.SimpleWebTable;
import org.springframework.util.StringUtils;

public class DefaultDistributedStatusService implements DistributedStatusService {

    private PollerConfig m_pollerConfig;
    private MonitoredServiceDao m_monitoredServiceDao;
    private LocationMonitorDao m_locationMonitorDao;
    private ApplicationDao m_applicationDao;
    private CategoryDao m_categoryDao;
    
    /*
     * XXX No unit tests
     * XXX Not sorting by category
     * XXX not dealing with the case where a node has multiple categories
     */
    public SimpleWebTable createStatusTable(String locationName, String applicationLabel) {
        List<OnmsLocationSpecificStatus> status =
            findLocationSpecificStatus(locationName, applicationLabel);
        
        SimpleWebTable table = new SimpleWebTable();
        
        table.setTitle("Distributed poller view for " + applicationLabel + " from " + locationName + " location");
        
        table.addColumn("Category", "simpleWebTableHeader");
        table.addColumn("Node", "simpleWebTableHeader");
        //table.addColumn("Instance", "simpleWebTableHeader");
        table.addColumn("Service", "simpleWebTableHeader");
        table.addColumn("Status", "simpleWebTableHeader");
        table.addColumn("Response Time", "simpleWebTableHeader");
        
        for (OnmsLocationSpecificStatus s : status) {
            OnmsNode node = s.getMonitoredService().getIpInterface().getNode();
            // XXX we should iterate over every category
            Collection<OnmsCategory> categories = m_categoryDao.findByNode(node);
            String category;
            if (categories == null || categories.size() == 0) {
                category = "";
            } else {
                category = categories.iterator().next().getName();
            }
            
            table.newRow();
            table.addCell(category, "simpleWebTableRowLabel");
            table.addCell(node.getLabel(), "simpleWebTableRowLabel");
            table.addCell(s.getMonitoredService().getServiceName(),
                          "simpleWebTableRowLabel");
            table.addCell(s.getPollResult().getStatusName(),
                          "simpleWebTableRowLabel");
            long responseTime = s.getPollResult().getResponseTime(); 
            if (responseTime >=0 ) {
                table.addCell(responseTime, "simpleWebTableRowLabel");
            } else {
                table.addCell("", "simpleWebTableRowLabel");
            }
        }
        
        return table;
    }

    /*
     * XXX what do we do if any of the DAO calls return null, or do they
     * throw DataAccessException?
     */
    protected List<OnmsLocationSpecificStatus> findLocationSpecificStatus(String locationName, String applicationLabel) {
        if (locationName == null) {
            throw new IllegalArgumentException("locationName cannot be null");
        }
        
        if (applicationLabel == null) {
            throw new IllegalArgumentException("applicationLabel cannot be null");
        }
        
        OnmsMonitoringLocationDefinition location =
            m_locationMonitorDao.findMonitoringLocationDefinition(locationName);
        
        OnmsApplication application =
            m_applicationDao.findByLabel(applicationLabel);

        OnmsLocationMonitor locationMonitor = m_locationMonitorDao.findByLocationDefinition(location);

        Package pkg = m_pollerConfig.getPackage(location.getPollingPackageName());

        ServiceSelector selector =
            m_pollerConfig.getServiceSelectorForPackage(pkg);
        
        Collection<OnmsMonitoredService> services =
            m_monitoredServiceDao.findMatchingServices(selector);
        
        Set<OnmsMonitoredService> applicationServices = application.getMemberServices();
        services.retainAll(applicationServices);

        List<OnmsLocationSpecificStatus> status = new LinkedList<OnmsLocationSpecificStatus>();
        
        List<OnmsMonitoredService> sortedServices = new ArrayList<OnmsMonitoredService>(services);
        Collections.sort(sortedServices, new Comparator<OnmsMonitoredService>() {
            public int compare(OnmsMonitoredService o1, OnmsMonitoredService o2) {
                return o1.getServiceName().compareTo(o2.getServiceName());
            }
        });
                                                                     
        for (OnmsMonitoredService service : sortedServices) {
            if (locationMonitor == null) {
                status.add(new OnmsLocationSpecificStatus(null, service, PollStatus.unknown("Distributed poller has never reported for this location")));
            } else {
                OnmsLocationSpecificStatus currentStatus = m_locationMonitorDao.getMostRecentStatusChange(locationMonitor, service);
                if (currentStatus == null) {
                    status.add(new OnmsLocationSpecificStatus(null, service, PollStatus.unknown("No status recorded for this service from this location")));
                } else {
                    status.add(currentStatus);
                }
            }
        }
        
        

        return status;
    }

    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
        
    }

    public void setLocationMonitorDao(LocationMonitorDao locationMonitorDao) {
        m_locationMonitorDao = locationMonitorDao;
        
    }
    
    public void setApplicationDao(ApplicationDao applicationDao) {
        m_applicationDao = applicationDao;
        
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
        
    }

    /*
     * XXX what do we do if any of the DAO calls return null, or do they
     * throw DataAccessException?
     */
    public SimpleWebTable createFacilityStatusTable(Date startDate,
            Date endDate) {
        SimpleWebTable table = new SimpleWebTable();
        
        List<OnmsMonitoringLocationDefinition> locationDefinitions =
            m_locationMonitorDao.findAllMonitoringLocationDefinitions();
        
        List<OnmsApplication> sortedApplications =
            new ArrayList<OnmsApplication>(m_applicationDao.findAll());
        Collections.sort(sortedApplications,
                         new Comparator<OnmsApplication>(){
            public int compare(OnmsApplication o1, OnmsApplication o2) {
                return o1.getLabel().compareTo(o2.getLabel());
            }
        });
        
        Collection<OnmsLocationSpecificStatus> statuses =
            m_locationMonitorDao.getAllMostRecentStatusChanges();
        
        table.setTitle("Distributed Poller Status Summary");
        
        table.addColumn("Area", "simpleWebTableRowLabel");
        table.addColumn("Location", "simpleWebTableRowLabel");
        for (OnmsApplication application : sortedApplications) {
            table.addColumn(application.getLabel(), "simpleWebTableRowLabel");
        }
        
        for (OnmsMonitoringLocationDefinition locationDefinition : locationDefinitions) {
            OnmsLocationMonitor monitor = m_locationMonitorDao.findByLocationDefinition(locationDefinition);
            
            table.newRow();
            table.addCell(locationDefinition.getArea(), "simpleWebTableRowLabel");
            table.addCell(locationDefinition.getName(), "simpleWebTableRowLabel");
            
            for (OnmsApplication application : sortedApplications) {
                String status = calculateCurrentStatus(monitor, application.getMemberServices(), statuses);
                String percentage = calculatePercentageUptime(monitor, application.getMemberServices(), startDate, endDate);

                table.addCell(percentage, status,
                              createDetailsPageUrl(locationDefinition, application));
            }
        }
        
        //table.newRow();
        
        return table;
    }
    
    public String calculateCurrentStatusWithMonitor(
            OnmsLocationMonitor monitor,
            Set<OnmsMonitoredService> applicationServices,
            Collection<OnmsLocationSpecificStatus> statuses) {
        if (monitor == null || monitor.getLastCheckInTime() == null
                || monitor.getLastCheckInTime().before(new Date(System.currentTimeMillis() - 300000))) {
            // XXX spec says "Red", which would be Critical
            return "Indeterminate";
        }
        
        return calculateCurrentStatus(monitor, applicationServices, statuses);
    }

    
    public String calculateCurrentStatus(OnmsLocationMonitor monitor,
            Set<OnmsMonitoredService> applicationServices,
            Collection<OnmsLocationSpecificStatus> statuses) {
        Set<PollStatus> pollStatuses = new HashSet<PollStatus>();
        
        for (OnmsMonitoredService service : applicationServices) {
            for (OnmsLocationSpecificStatus status : statuses) {
                if (status.getMonitoredService().equals(service)
                        && status.getLocationMonitor().equals(monitor)) {
                    pollStatuses.add(status.getPollResult());
                } else {
                    // XXX this doesn't seem right, but I'm too tired
                    pollStatuses.add(PollStatus.unknown());
                }
            }
        }
        
        return calculateStatus(pollStatuses);
    }       
    
    public String calculateStatus(Collection<PollStatus> pollStatuses) {
        /*
         * XXX We aren't doing anything for warning, because we don't
         * have a warning state available, right now.  Should unknown
         * be a warning state?
         */
        for (PollStatus pollStatus : pollStatuses) {
            if (!pollStatus.isAvailable()) {
                return "Critical";
            }
        }
        
        return "Normal";
    }

    /**
     * Calculate the percentage of time that all services are up for this
     * application on this remote monitor.
     * 
     * @param monitor remote monitor to report on
     * @param applicationServices services to report on
     * @param startDate start date.  The report starts on this date.
     * @param endDate end date.  The report ends the last millisecond prior
     * this date.
     * @return representation of the percentage uptime out to three decimal places
     */
    public String calculatePercentageUptime(OnmsLocationMonitor monitor,
            Set<OnmsMonitoredService> applicationServices,
            Date startDate, Date endDate) {
        
        Collection<OnmsLocationSpecificStatus> statuses =
            m_locationMonitorDao.getStatusChangesBetween(startDate, endDate);

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
            new ArrayList<OnmsLocationSpecificStatus>(statuses);
        Collections.sort(sortedStatuses, new Comparator<OnmsLocationSpecificStatus>(){
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
        String lastStatus = "Critical";
        
        for (OnmsLocationSpecificStatus status : sortedStatuses) {
            if (!status.getLocationMonitor().equals(monitor)) {
                continue;
            }

            Date currentDate = status.getPollResult().getTimestamp();

            if (!currentDate.before(endDate)) {
                // We're at or past the end date, so we're done processing
                break;
            }
            
            serviceStatus.put(status.getMonitoredService(), status.getPollResult());
            String currentStatus = calculateStatus(serviceStatus.values());
            
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
            if ("Normal".equals(lastStatus)) {
                long milliseconds = currentDate.getTime() - lastDate.getTime();
                normalMilliseconds += milliseconds;
            }
            
            lastDate = currentDate;
            lastStatus = currentStatus;
        }
        
        if ("Normal".equals(lastStatus)) {
            long milliseconds = endDate.getTime() - lastDate.getTime();
            normalMilliseconds += milliseconds;
        }

        float percentage = normalMilliseconds /
            (endDate.getTime() - startDate.getTime()) * 100;
        return new DecimalFormat("0.000").format((double) percentage) + "%";
    }

    private String createDetailsPageUrl(
            OnmsMonitoringLocationDefinition locationDefinition,
            OnmsApplication application) {

        List<String> params = new ArrayList<String>(2);
        params.add("location=" + Util.encode(locationDefinition.getName()));
        params.add("application=" + Util.encode(application.getLabel()));
        
        return "distributedStatusDetails.htm"
            + "?"
            + StringUtils.collectionToDelimitedString(params, "&");
    }

}
