package org.opennms.web.svclayer.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.web.graph.RelativeTimePeriod;

public class DistributedStatusHistoryModel {
    private List<OnmsMonitoringLocationDefinition> m_locations;
    private List<OnmsApplication> m_applications;
    private OnmsMonitoringLocationDefinition m_chosenLocation;
    private OnmsApplication m_chosenApplication;
    private Collection<OnmsMonitoredService> m_chosenApplicationMemberServices;
    private List<String> m_errors;
    private List<RelativeTimePeriod> m_periods;
    private RelativeTimePeriod m_chosenPeriod;
    private List<OnmsLocationMonitor> m_monitors;
    private OnmsLocationMonitor m_chosenMonitor;
    private Map<OnmsMonitoredService, String> m_httpGraphUrls;
    
    public DistributedStatusHistoryModel(
            List<OnmsMonitoringLocationDefinition> locations,
            List<OnmsApplication> applications,
            List<OnmsLocationMonitor> monitors,
            List<RelativeTimePeriod> periods,
            OnmsMonitoringLocationDefinition chosenLocation,
            OnmsApplication chosenApplication,
            Collection<OnmsMonitoredService> chosenApplicationMemberServices,
            OnmsLocationMonitor chosenMonitor,
            RelativeTimePeriod chosenPeriod,
            List<String> errors) {
        m_locations = locations;
        m_applications = applications;
        m_monitors = monitors;
        m_periods = periods;
        m_chosenLocation = chosenLocation;
        m_chosenApplication = chosenApplication;
        m_chosenApplicationMemberServices = chosenApplicationMemberServices;
        m_chosenMonitor = chosenMonitor;
        m_chosenPeriod = chosenPeriod;
        m_errors = errors;
        
        initHttpGraphUrls();
    }

    public List<OnmsApplication> getApplications() {
        return m_applications;
    }

    public List<OnmsMonitoringLocationDefinition> getLocations() {
        return m_locations;
    }

    public OnmsApplication getChosenApplication() {
        return m_chosenApplication;
    }

    public Collection<OnmsMonitoredService> getChosenApplicationMemberServices() {
        return m_chosenApplicationMemberServices;
    }

    public OnmsMonitoringLocationDefinition getChosenLocation() {
        return m_chosenLocation;
    }
    
    public List<String> getErrors() {
        return m_errors;
    }

    public RelativeTimePeriod getChosenPeriod() {
        return m_chosenPeriod;
    }

    public List<RelativeTimePeriod> getPeriods() {
        return m_periods;
    }

    public OnmsLocationMonitor getChosenMonitor() {
        return m_chosenMonitor;
    }

    public List<OnmsLocationMonitor> getMonitors() {
        return m_monitors;
    }
    
    public Map<OnmsMonitoredService, String> getHttpGraphUrls() {
        return m_httpGraphUrls;
    }
    
    // We need to init when we are constructed so lazy loading happens during our transaction
    private void initHttpGraphUrls() {
        if (m_chosenMonitor == null) {
            // nothing to create graphs for
            return;
        }
        
        Collection<OnmsMonitoredService> services =
            getChosenApplicationMemberServices();
        List<OnmsMonitoredService> sortedServices =
            new ArrayList<OnmsMonitoredService>(services);
        Collections.sort(sortedServices, new Comparator<OnmsMonitoredService>() {
            public int compare(OnmsMonitoredService o1, OnmsMonitoredService o2) {
                int diff;
                diff = o1.getIpInterface().getNode().getLabel().compareToIgnoreCase(o2.getIpInterface().getNode().getLabel());
                if (diff != 0) {
                    return diff;
                }
                
                diff = o1.getIpAddress().compareTo(o2.getIpAddress());
                if (diff != 0) {
                    return diff;
                }
                
                return o1.getServiceName().compareToIgnoreCase(o2.getServiceName());
            }
        });
        
        Map<OnmsMonitoredService, String> list =
            new LinkedHashMap<OnmsMonitoredService,String>(services.size());
        
        long[] times = getChosenPeriod().getStartAndEndTimes();
        
        for (OnmsMonitoredService service : sortedServices) {
            list.put(service, getHttpGraphUrlForService(service, times));
        }
        
        m_httpGraphUrls = list;
    }

    private String getHttpGraphUrlForService(OnmsMonitoredService service,
            long[] times) {
        return "graph/graph.png"
            + "?report=" + service.getServiceName().toLowerCase()
            + "&parentResourceType=node"
            + "&parentResource=" + service.getIpInterface().getNode().getId()
            + "&resourceType=distributedStatus"
            + "&resource=" + getChosenMonitor().getId()
                + "/" + service.getIpAddress()
            + "&type=performance"
            + "&start=" + times[0]
            + "&end=" + times[1];

        /*
            + "&rrd=" + getChosenMonitor().getId()
            + "%2F" + service.getIpAddress()
            + "%2F" + service.getServiceName().toLowerCase()
            + RrdUtils.getExtension();
*/
    }
    
}
