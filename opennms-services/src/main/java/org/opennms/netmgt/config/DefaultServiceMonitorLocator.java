/**
 * 
 */
package org.opennms.netmgt.config;

import java.io.Serializable;
import java.util.Map;

import org.opennms.netmgt.dao.CastorObjectRetrievalFailureException;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;

public class DefaultServiceMonitorLocator implements ServiceMonitorLocator, Serializable {
    
    private static final long serialVersionUID = 1L;

    String m_serviceName;
    Class<? extends ServiceMonitor> m_serviceClass;
    
    public DefaultServiceMonitorLocator(String serviceName, Class<? extends ServiceMonitor> serviceClass) {
        m_serviceName = serviceName;
        m_serviceClass = serviceClass;
    }

    public ServiceMonitor getServiceMonitor() {
        try {
            ServiceMonitor mon = m_serviceClass.newInstance();
            mon.initialize((Map)null);
            return mon;
        } catch (InstantiationException e) {
            throw new CastorObjectRetrievalFailureException("Unable to instantiate monitor for service "
                    +m_serviceName+" with class-name "+m_serviceClass.getName(), e);
        } catch (IllegalAccessException e) {
            throw new CastorObjectRetrievalFailureException("Illegal access trying to instantiate monitor for service "
                    +m_serviceName+" with class-name "+m_serviceClass.getName(), e);
        }
    }

    public String getServiceName() {
        return m_serviceName;
    }

    public String getServiceLocatorKey() {
        return m_serviceClass.getName();
    }
    
}