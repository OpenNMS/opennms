/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.dao.jaxb;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.opennms.netmgt.dao.jaxb.collector.DataCollectionConfigFileTest;

public class InvocationAnticipator implements InvocationHandler {
    public class NullInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            return null;
        }

    }

    private HashMap<String, Integer> m_counts = new HashMap<String, Integer>();
    private HashMap<String, Integer> m_anticipatedCounts = new HashMap<String, Integer>();
    private Class<?> m_clazz;
    private InvocationHandler m_handler = new NullInvocationHandler();

    public InvocationAnticipator(Class<?> clazz) {
        m_clazz = clazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        int currentCount = 0;
        if (m_counts.get(method.getName()) != null) {
            currentCount = m_counts.get(method.getName()).intValue();
        }
        m_counts.put(method.getName(), Integer.valueOf(currentCount+1));

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
       m_anticipatedCounts.put(methodName, Integer.valueOf(count));
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