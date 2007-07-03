//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jul 03: Use Java 5 generics and format code a bit. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
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

    private HashMap<String, Integer> m_counts = new HashMap<String, Integer>();
    private HashMap<String, Integer> m_anticipatedCounts = new HashMap<String, Integer>();
    private Class m_clazz;
    private InvocationHandler m_handler = new NullInvocationHandler();

    public InvocationAnticipator(Class clazz) {
        m_clazz = clazz;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int currentCount = 0;
        if (m_counts.get(method.getName()) != null) {
            currentCount = m_counts.get(method.getName()).intValue();
        }
        m_counts.put(method.getName(), new Integer(currentCount+1));

        return m_handler.invoke(proxy, method, args);
    }
    
    public void setInvocationHandler(InvocationHandler handler) {
        m_handler  = handler;
    }

    public int getCount(String methodName) {
        if (m_counts.get(methodName) == null) {
            return 0;
        }
        return m_counts.get(methodName).intValue();
    }
    
    public int getAnticipatedCount(String methodName) {
        if (m_anticipatedCounts.get(methodName) == null) {
            return 0;
        }
        return m_anticipatedCounts.get(methodName).intValue();
    }

    public void anticipateCalls(int count, String methodName) {
       m_anticipatedCounts.put(methodName, new Integer(count));
    }
    
    public void verify() {
        ensureAnticipatedWereReceived();
        ensureNoUnanticipated();
    }

    private void ensureNoUnanticipated() {
        HashSet<String> unexpected = new HashSet<String>(m_counts.keySet());
        unexpected.removeAll(m_anticipatedCounts.keySet());
        if (!unexpected.isEmpty()) {
            String method = unexpected.iterator().next();
            DataCollectionConfigFileTest.fail("Unexpected call to method "+method+".  It was called "+getCount(method)+" times");
        }
    }

    private void ensureAnticipatedWereReceived() {
        for (Iterator<String> it = m_anticipatedCounts.keySet().iterator(); it.hasNext();) {
            String methodName = it.next();
            DataCollectionConfigFileTest.assertEquals("Unexpected callCount for method "+methodName, getAnticipatedCount(methodName), getCount(methodName));
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { m_clazz }, this);
    }
}