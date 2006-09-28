package org.opennms.netmgt.poller.remote.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.LocationMonitorDao;
import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsLocationMonitor;
import org.opennms.netmgt.model.OnmsLocationSpecificStatusChange;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsMonitoringLocationDefinition;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.remote.PollConfiguration;
import org.opennms.netmgt.poller.remote.PollerBackEnd;
import org.opennms.netmgt.poller.remote.PollerConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

public class DefaultPollerBackEnd implements PollerBackEnd, InitializingBean {

    private LocationMonitorDao m_locMonDao;
    private MonitoredServiceDao m_monSvcDao;
    private PollerConfig m_pollerConfig;

    public Collection<OnmsMonitoringLocationDefinition> getMonitoringLocations() {
        return m_locMonDao.findAllMonitoringLocationDefinitions();
    }

    public PollerConfiguration getPollerConfiguration(int locationMonitorId) {
        
        OnmsLocationMonitor mon = m_locMonDao.get(locationMonitorId);
        String pollingPackageName = mon.getLocationDefinition().getPollingPackageName();
        
        Package pkg = m_pollerConfig.getPackage(pollingPackageName);
        
        ServiceSelector selector = m_pollerConfig.getServiceSelectorForPackage(pkg);

        Collection<OnmsMonitoredService> services = m_monSvcDao.findMatchingServices(selector);
        
        List<PollConfiguration> configs = new ArrayList<PollConfiguration>(services.size());
        
        for (OnmsMonitoredService monSvc : services) {
            Service serviceConfig = m_pollerConfig.getServiceInPackage(monSvc.getServiceName(), pkg);
            long interval = serviceConfig.getInterval();
            Map parameters = getParameterMap(serviceConfig);
            configs.add(new PollConfiguration(monSvc, parameters, interval));
            
        }
        
        return new SimplePollerConfiguration(configs.toArray(new PollConfiguration[configs.size()]));
        
        
    }
    
    private static class SimplePollerConfiguration implements PollerConfiguration {
        
        private PollConfiguration[] m_pollConfigs;
        
        SimplePollerConfiguration(PollConfiguration[] pollConfigs) {
            m_pollConfigs = pollConfigs;
        }

        public PollConfiguration[] getConfigurationForPoller() {
            return m_pollConfigs;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    private Map getParameterMap(Service serviceConfig) {
        Map<String, String> paramMap = new HashMap<String, String>();
        Enumeration<Parameter> serviceParms = serviceConfig.enumerateParameter();
        while(serviceParms.hasMoreElements()) {
            Parameter serviceParm = serviceParms.nextElement();
            paramMap.put(serviceParm.getKey(), serviceParm.getValue());
        }
        return paramMap;
    }

    public boolean pollerCheckingIn(int locationMonitorId,
            Date currentConfigurationVersion) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public boolean pollerStarting(int locationMonitorId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void pollerStopping(int locationMonitorId) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public int registerLocationMonitor(String monitoringLocationId) {
        
        OnmsMonitoringLocationDefinition def = m_locMonDao.findMonitoringLocationDefinition(monitoringLocationId);
        
        OnmsLocationMonitor mon = new OnmsLocationMonitor();
        mon.setLocationDefinition(def);
        
        m_locMonDao.save(mon);
        
        return mon.getId();
       
        
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_locMonDao, "The LocationMonitorDao must be set");
        Assert.notNull(m_monSvcDao, "The MonitoredServiceDao must be set");
        Assert.notNull(m_pollerConfig, "The PollerConfig must be set");
    }

    public void setLocationMonitorDao(LocationMonitorDao locMonDao) {
        m_locMonDao = locMonDao;
    }
    
    public void setMonitoredServiceDao(MonitoredServiceDao monSvcDao) {
        m_monSvcDao = monSvcDao;
    }

    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    public void reportResult(int locationMonitorID, int serviceId, PollStatus status) {
        
        OnmsLocationMonitor locationMonitor = m_locMonDao.get(locationMonitorID);
        OnmsMonitoredService monSvc = m_monSvcDao.get(serviceId);
        
        OnmsLocationSpecificStatusChange currentStatus = m_locMonDao.getMostRecentStatusChange(locationMonitor, monSvc);
        
        if (!currentStatus.equals(status)) {
            OnmsLocationSpecificStatusChange newStatus = new OnmsLocationSpecificStatusChange(locationMonitor, monSvc, status);
            m_locMonDao.saveStatusChange(newStatus);
        }
    }

}
