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
package org.opennms.spring.xml;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.opennms.netmgt.events.api.model.IEvent;
import org.springframework.core.Ordered;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@Aspect
public class AspectJITEventHandlerInteceptor implements Ordered {
    
    @Pointcut("execution(* *..AspectJITEventHandler.*(..))")
    public void testMethods() {}
    
    @Pointcut("@annotation(org.opennms.netmgt.events.api.annotations.EventHandler)")
    public void eventHandlers() {}
    
    @Pointcut("testMethods() && eventHandlers()")
    public void testEventHandlers() {}
    
    
    private int m_preEventCount;
    private int m_postEventCount;
    private int m_handledExceptionCount;
    private int m_order = 0;
    
    public int getPreEventCount() {
        return m_preEventCount;
    }

    public int getPostEventCount() {
        return m_postEventCount;
    }

    public int getHandledExceptionCount() {
        return m_handledExceptionCount;
    }
    
    @Around("testEventHandlers() && args(event)")
    public void onEvent(ProceedingJoinPoint pjp, IEvent event) throws Throwable {
        preEvent(event);
        
        try {
            pjp.proceed();
            postEvent(event);
        } catch (RuntimeException ex) {
            handleException(event, ex);
        }
    }

    private void handleException(IEvent event, RuntimeException ex) {
        System.err.println("handleException");
        m_handledExceptionCount++;
    }

    private void postEvent(IEvent event) {
        System.err.println("postEvent");
        m_postEventCount++;
    }

    private void preEvent(IEvent event) {
        System.err.println("preEvent");
        m_preEventCount++;
    }

    public void reset() {
        m_preEventCount = 0;
        m_postEventCount = 0;
        m_handledExceptionCount = 0;
    }
    
    public void setOrder(int order) {
        m_order = order;
    }

    @Override
    public int getOrder() {
        return m_order;
    }

}
