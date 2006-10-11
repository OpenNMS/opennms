package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
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
        table.setTitle(applicationLabel + " view from location " + locationName);
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
            table.newRow();
        }
        
        return table;
    }

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
        
        for (OnmsMonitoredService service : services) {
            status.add(m_locationMonitorDao.getMostRecentStatusChange(locationMonitor, service));
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

    public SimpleWebTable createFacilityStatusTable() {
        SimpleWebTable table = new SimpleWebTable();
        
        Collection<OnmsMonitoringLocationDefinition> locationDefinitions =
            m_locationMonitorDao.findAllMonitoringLocationDefinitions();
        Collection<OnmsApplication> applications =
            m_applicationDao.findAll();
        
        Collection<OnmsLocationSpecificStatus> statuses =
            m_locationMonitorDao.getAllMostRecentStatusChanges();
        
        table.addColumn("Area", "simpleWebTableRowLabel");
        table.addColumn("Location", "simpleWebTableRowLabel");
        // XXX should sort by application label, first
        for (OnmsApplication application : applications) {
            table.addColumn(application.getLabel(), "simpleWebTableRowLabel");
        }
        
        // XXX should sort by area, then name, first
        for (OnmsMonitoringLocationDefinition locationDefinition : locationDefinitions) {
            OnmsLocationMonitor monitor = m_locationMonitorDao.findByLocationDefinition(locationDefinition);
            
            table.newRow();
            table.addCell(locationDefinition.getArea(), "simpleWebTableRowLabel");
            table.addCell(locationDefinition.getName(), "simpleWebTableRowLabel");
            
            for (OnmsApplication application : applications) {
                Set<PollStatus> pollStatuses = new HashSet<PollStatus>();
                for (OnmsMonitoredService service : application.getMemberServices()) {
                    for (OnmsLocationSpecificStatus status : statuses) {
                        if (status.getMonitoredService().equals(service)
                                && status.getLocationMonitor().equals(monitor)) {
                            pollStatuses.add(status.getPollResult());
                        } else {
                            pollStatuses.add(PollStatus.unknown());
                        }
                    }
                }
                
                /*
                 * XXX We aren't doing anything for warning, because we don't
                 * have a warning state available, right now.
                 */
                String status = "Normal";
                if (monitor == null || monitor.getLastCheckInTime() == null
                        || monitor.getLastCheckInTime().before(new Date(System.currentTimeMillis() - 300000))) {
                    // XXX spec says "Red", which would be Critical
                    status = "Indeterminate";
                } else {
                    for (PollStatus pollStatus : pollStatuses) {
                        if (!pollStatus.isAvailable()) {
                            status = "Critical";
                            break;
                        }
                    }
                }
                
                // XXX I really need to think about how to do the percentages
                
                table.addCell("Percentage not calculated", status,
                              createDetailsPageUrl(locationDefinition, application));
            }
        }
        
        table.newRow();
        
        return table;
    }
    
    private String createDetailsPageUrl(OnmsMonitoringLocationDefinition locationDefinition,
            OnmsApplication application) {

        List<String> params = new ArrayList<String>(2);
        params.add("location=" + Util.encode(locationDefinition.getName()));
        params.add("application=" + Util.encode(application.getLabel()));
        
        return "distributedStatusDetails.htm"
            + "?"
            + StringUtils.collectionToDelimitedString(params, "&");
    }

}
