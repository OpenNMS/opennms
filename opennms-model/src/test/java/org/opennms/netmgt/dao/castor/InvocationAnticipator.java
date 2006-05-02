/**
 * 
 */
package org.opennms.netmgt.dao.castor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.opennms.netmgt.dao.castor.collector.DataCollectionConfigFileTest;

public class InvocationAnticipator implements InvocationHandler {
    public class NullInvocationHandler implements InvocationHandler {

        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            return null;
        }

    }

    private HashMap m_counts = new HashMap();
    private HashMap m_anticipatedCounts = new HashMap();
    private Class m_clazz;
    private InvocationHandler m_handler = new NullInvocationHandler();

    public InvocationAnticipator(Class clazz) {
        m_clazz = clazz;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int currentCount = 0;
        if (m_counts.get(method.getName()) != null)
            currentCount = ((Integer)m_counts.get(method.getName())).intValue();
        m_counts.put(method.getName(), new Integer(currentCount+1));

        return m_handler.invoke(proxy, method, args);
    }
    
    public void setInvocationHandler(InvocationHandler handler) {
        m_handler  = handler;
    }

    public int getCount(String methodName) {
        if (m_counts.get(methodName) == null) return 0;
        return ((Integer)m_counts.get(methodName)).intValue();
    }
    
    public int getAnticipatedCount(String methodName) {
        if (m_anticipatedCounts.get(methodName) == null) return 0;
        return ((Integer)m_anticipatedCounts.get(methodName)).intValue();
    }

    public void anticipateCalls(int count, String methodName) {
       m_anticipatedCounts.put(methodName, new Integer(count));
    }
    
    public void verify() {
        ensureAnticipatedWereReceived();
        ensureNoUnanticipated();
    }

    private void ensureNoUnanticipated() {
        HashSet unexpected = new HashSet(m_counts.keySet());
        unexpected.removeAll(m_anticipatedCounts.keySet());
        if (!unexpected.isEmpty()) {
            String method = (String)unexpected.iterator().next();
            DataCollectionConfigFileTest.fail("Unexpected call to method "+method+".  It was called "+getCount(method)+" times");
        }
    }

    private void ensureAnticipatedWereReceived() {
        for (Iterator it = m_anticipatedCounts.keySet().iterator(); it.hasNext();) {
            String methodName = (String) it.next();
            DataCollectionConfigFileTest.assertEquals("Unexpected callCount for method "+methodName, getAnticipatedCount(methodName), getCount(methodName));
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { m_clazz }, this);
    }
}