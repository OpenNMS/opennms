/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 12, 2007
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.spring.xml;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.Ordered;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
@Aspect
public class AspectJTestEventHandlerInteceptor implements Ordered {
    
    @Pointcut("execution(* *..AspectJTestEventHandler.*(..))")
    public void testMethods() {}
    
    @Pointcut("@annotation(org.opennms.netmgt.utils.annotations.EventHandler)")
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
    public void onEvent(ProceedingJoinPoint pjp, Event event) throws Throwable {
        preEvent(event);
        
        try {
            pjp.proceed();
            postEvent(event);
        } catch (RuntimeException ex) {
            handleException(event, ex);
        }
    }

    private void handleException(Event event, RuntimeException ex) {
        System.err.println("handleException");
        m_handledExceptionCount++;
    }

    private void postEvent(Event event) {
        System.err.println("postEvent");
        m_postEventCount++;
    }

    private void preEvent(Event event) {
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

    public int getOrder() {
        return m_order;
    }

}
