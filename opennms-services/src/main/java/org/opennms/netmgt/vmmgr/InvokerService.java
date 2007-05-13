/**
 * 
 */
package org.opennms.netmgt.vmmgr;

import java.util.ArrayList;
import java.util.List;

import javax.management.ObjectInstance;

import org.opennms.netmgt.config.service.Service;

class InvokerService {
    private Service m_service;
    private ObjectInstance m_mbean;
    private Throwable m_badThrowable;

    /**
     * No public constructor.  Use @{link InvokerService#createServiceArray(Service[])}.
     */
    private InvokerService(Service service) {
        setService(service);
    }
    
    static List<InvokerService> createServiceList(Service[] services) {
        List<InvokerService> invokerServices = new ArrayList<InvokerService>(services.length);
        
        for (Service service : services) {
            invokerServices.add(new InvokerService(service));
        }
        
        return invokerServices;
    }
    
    void setBadThrowable(Throwable badThrowable) {
        m_badThrowable = badThrowable;
    }
    
    Throwable getBadThrowable() {
        return m_badThrowable;
    }
    
    ObjectInstance getMbean() {
        return m_mbean;
    }
    
    void setMbean(ObjectInstance mbean) {
        m_mbean = mbean;
    }
    
    Service getService() {
        return m_service;
    }
    
    private void setService(Service service) {
        m_service = service;
    }

    public boolean isBadService() {
        return (m_badThrowable != null);
    }
}