package org.opennms.features.topology.plugins.ncs.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.Criteria;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.LoggerFactory;


public class NCSCriteriaServiceManager {
    
    private Map<String, List<ServiceRegistration<Criteria>>> m_registrationMap = new HashMap<String, List<ServiceRegistration<Criteria>>>();
    private BundleContext m_bundleContext;
    
    public void registerCriteria(Criteria ncsCriteria, String sessionId) {
        //This is to get around an issue with the NCSPathProvider when registering a service with different namespaces
        //removeAllServicesForSession(sessionId);
        removeServicesForSessionWithNamespace(sessionId, ncsCriteria.getNamespace());
        
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("sessionId", sessionId);
        properties.put("namespace", ncsCriteria.getNamespace());
        
        ServiceRegistration<Criteria> registeredService = m_bundleContext.registerService(Criteria.class, ncsCriteria, properties);
        
        if(m_registrationMap.containsKey(sessionId)) {
            List<ServiceRegistration<Criteria>> list = m_registrationMap.get(sessionId);
            list.add(registeredService);
        } else {
            ArrayList<ServiceRegistration<Criteria>> serviceList = new ArrayList<ServiceRegistration<Criteria>>();
            serviceList.add(registeredService);
            m_registrationMap.put(sessionId, serviceList);
        }
        
    }
    
    private void removeServicesForSessionWithNamespace(String sessionId, String namespace) {
        if(m_registrationMap.containsKey(sessionId)) {
            List<ServiceRegistration<Criteria>> serviceList = m_registrationMap.get(sessionId);
            ServiceRegistration<Criteria> removedService = null;
            for(ServiceRegistration<Criteria> serviceReg : serviceList) {
                try {
                    String namespaceProperty = (String) serviceReg.getReference().getProperty("namespace");
                    if(namespaceProperty.equals(namespace) ) {
                        serviceReg.unregister();
                        removedService = serviceReg;
                    }
                } catch( IllegalStateException e) {
                    removedService = serviceReg;
                }
            }
            if(removedService != null) serviceList.remove(removedService);
        }
    }
    
    
    private void removeAllServicesForSession(String sessionId) {
        if(m_registrationMap.containsKey(sessionId)) {
            List<ServiceRegistration<Criteria>> serviceList = m_registrationMap.get(sessionId);
            for(ServiceRegistration<Criteria> serviceReg : serviceList) {
                try {
                    serviceReg.unregister();
                } catch(IllegalStateException e) {
                    LoggerFactory.getLogger(this.getClass()).warn("Attempted to unregister a service that is already unregistered {}", e);
                }
            }
            
            serviceList.clear();
        }
    }
    
    protected void removeAllServices() {
        for(String key : m_registrationMap.keySet()) {
            removeAllServicesForSession(key);
        }
    }


    public void setBundleContext(BundleContext context) {
        m_bundleContext = context;
        m_bundleContext.addBundleListener(new BundleListener() {

            @Override
            public void bundleChanged(BundleEvent event) {
                // TODO Auto-generated method stub
                switch(event.getType()) {
                    case BundleEvent.STOPPING:
                        removeAllServices();
                }
                
            }
            
        });
    }


    public boolean isCriteriaRegistered(String namespace, String sessionId) {
        List<ServiceRegistration<Criteria>> registrationList = m_registrationMap.get(sessionId);
        
        if(registrationList != null) {
            for(ServiceRegistration<Criteria> critRegistration : registrationList) {
                String namespaceProperty = (String) critRegistration.getReference().getProperty("namespace");
                if(namespaceProperty.equals( namespace )) {
                    return true;
                }    
            }
        }
        
        return false;
    }


    public void unregisterCriteria(String namespace, String sessionId) {
        List<ServiceRegistration<Criteria>> registrationList = m_registrationMap.get(sessionId);
        
        List<ServiceRegistration<Criteria>> clearedList = new ArrayList<ServiceRegistration<Criteria>>();
        for(ServiceRegistration<Criteria> criteriaRegistration : registrationList) {
            String namespaceProperty = (String) criteriaRegistration.getReference().getProperty("namespace");
            if(namespaceProperty.equals( namespace )) {
                criteriaRegistration.unregister();
                clearedList.add(criteriaRegistration);
            }
        }
        
        if(clearedList.size() > 0) {
            registrationList.removeAll(clearedList);
        }
        
    }


    public void addCriteriaServiceListener(ServiceListener listener, String sessionId, String namespace) {
        try {
            m_bundleContext.addServiceListener( listener, 
                    "(&(objectClass=org.opennms.features.topology.api.topo.Criteria)(sessionId=" + sessionId + ")(namespace=" + namespace + "))");
        } catch (InvalidSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public void removeCriteriaServiceListener(ServiceListener listener) {
        m_bundleContext.removeServiceListener(listener);
    }

}
