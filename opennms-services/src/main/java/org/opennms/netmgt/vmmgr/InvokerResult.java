/**
 * 
 */
package org.opennms.netmgt.vmmgr;

import javax.management.ObjectInstance;

import org.opennms.netmgt.config.service.Service;

class InvokerResult {
    private Service m_service;
    private ObjectInstance m_mbean;
    private Object m_result;
    private Throwable m_throwable;
    
    public InvokerResult(Service service, ObjectInstance mbean, Object result, Throwable throwable) {
        m_service = service;
        m_mbean = mbean;
        m_result = result;
        m_throwable = throwable;
    }
    
    public ObjectInstance getMbean() {
        return m_mbean;
    }
    
    public Object getResult() {
        return m_result;
    }
    
    public Throwable getThrowable() {
        return m_throwable;
    }

    public Service getService() {
        return m_service;
    }

}