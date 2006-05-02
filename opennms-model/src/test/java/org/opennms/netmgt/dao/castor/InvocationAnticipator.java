/**
 * 
 */
package org.opennms.netmgt.dao.castor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.opennms.netmgt.dao.castor.collector.DataCollectionConfigFileTest;

public class InvocationAnticipator implements InvocationHandler {
    HashMap counts = new HashMap();
    HashMap anticipatedCounts = new HashMap();

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int currentCount = 0;
        if (counts.get(method.getName()) != null)
            currentCount = ((Integer)counts.get(method.getName())).intValue();
        counts.put(method.getName(), new Integer(currentCount+1));

        return null;
    }

    public int getCount(String methodName) {
        if (counts.get(methodName) == null) return 0;
        return ((Integer)counts.get(methodName)).intValue();
    }

    public void anticipateCalls(int count, String methodName) {
       anticipatedCounts.put(methodName, new Integer(count));
    }
    
    public void verify() {
        ensureAnticipatedWereReceived();
        ensureNoUnanticipated();
    }

    private void ensureNoUnanticipated() {
        HashSet unexpected = new HashSet(counts.keySet());
        unexpected.removeAll(anticipatedCounts.keySet());
        if (!unexpected.isEmpty())
            DataCollectionConfigFileTest.fail("Unexpected call to method "+unexpected.iterator().next());
    }

    private void ensureAnticipatedWereReceived() {
        for (Iterator it = anticipatedCounts.keySet().iterator(); it.hasNext();) {
            String methodName = (String) it.next();
            DataCollectionConfigFileTest.assertEquals("Unexpected callCount for method "+methodName, anticipatedCounts.get(methodName), counts.get(methodName));
        }
    }
}