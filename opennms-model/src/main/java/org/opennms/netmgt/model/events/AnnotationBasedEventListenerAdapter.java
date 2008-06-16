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
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.model.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.opennms.netmgt.model.events.annotations.EventExceptionHandler;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.model.events.annotations.EventPostProcessor;
import org.opennms.netmgt.model.events.annotations.EventPreProcessor;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * AnnotationBasedEventListenerAdapter
 *
 * @author brozow
 */
public class AnnotationBasedEventListenerAdapter implements StoppableEventListener, InitializingBean, DisposableBean {
    
    private volatile String m_name = null;
    private volatile Object m_annotatedListener;
    private volatile EventSubscriptionService m_subscriptionService;

    private final Map<String, Method> m_ueiToHandlerMap = new HashMap<String, Method>();
    private final List<Method> m_eventPreProcessors = new LinkedList<Method>();
    private final List<Method> m_eventPostProcessors = new LinkedList<Method>();
    private final SortedSet<Method> m_exceptionHandlers = new TreeSet<Method>(createExceptionHandlerComparator());
    
    public  AnnotationBasedEventListenerAdapter(String name, Object annotatedListener, EventSubscriptionService subscriptionService) {
        m_name = name;
        m_annotatedListener = annotatedListener;
        m_subscriptionService = subscriptionService;
        afterPropertiesSet();
    }
    
    public AnnotationBasedEventListenerAdapter(Object annotatedListener, EventSubscriptionService subscriptionService) {
        this(null, annotatedListener, subscriptionService);
    }
    
    public AnnotationBasedEventListenerAdapter() {
        // this is here to support dependency injection style 
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#getName()
     */
    public String getName() {
        return m_name;
    }
    
    public void setName(String name) {
        m_name = name;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#onEvent(org.opennms.netmgt.xml.event.Event)
     */
    public void onEvent(Event event) {
        if (event.getUei() == null) {
            return;
        }
        
        
        Method method = m_ueiToHandlerMap.get(event.getUei());
        
        if (method == null) {
            throw new IllegalArgumentException("Received an event for which we have no handler!");
        }
        
        try {
            
            preprocessEvent(event);
            
            processEvent(event, method);
            
            postprocessEvent(event);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalAccessException e) {
            throw new UndeclaredThrowableException(e);
        } catch (InvocationTargetException e) {
            handleException(event, e.getCause());
        }
    }

    protected void postprocessEvent(Event event) throws IllegalAccessException,
            InvocationTargetException {
        for(Method m : m_eventPostProcessors) {
            processEvent(event, m);
        }
    }

    protected void processEvent(Event event, Method method)
            throws IllegalAccessException, InvocationTargetException {
        method.invoke(m_annotatedListener, event);
    }

    protected void preprocessEvent(Event event) throws IllegalAccessException,
            InvocationTargetException {
        for(Method m : m_eventPreProcessors) {
            processEvent(event, m);
        }
    }
    
    

    protected void handleException(Event event, Throwable cause) {
        for(Method method : m_exceptionHandlers) {
            if (ClassUtils.isAssignableValue(method.getParameterTypes()[1], cause)) {
                try {
                    method.invoke(m_annotatedListener, event, cause);
                    
                    // we found the correct handler to we are done
                    break;
                } catch (Exception e) {
                    throw new UndeclaredThrowableException(e);
                }
            }
        }
    }

    public void setAnnotatedListener(Object annotatedListener) {
        m_annotatedListener = annotatedListener;
    }

    public void afterPropertiesSet() {
        Assert.state(m_subscriptionService != null, "subscriptionService must be set");        
        Assert.state(m_annotatedListener != null, "must set the annotatedListener property");
        
        EventListener listenerInfo = findEventListenerAnnotation(m_annotatedListener);
        
        Assert.state(listenerInfo != null, "value of annotatedListener property of class "+m_annotatedListener.getClass()+" must be annotated as "+EventListener.class.getName());
        
        
        if (m_name == null) {
            m_name = listenerInfo.name();
        }
        
        populatePreProcessorList();
        
        populateUeiToHandlerMap();
        
        populatePostProcessorList();
        
        populateExeptionHandlersSet();
        
        m_subscriptionService.addEventListener(this, new HashSet<String>(m_ueiToHandlerMap.keySet()));

    }

    private EventListener findEventListenerAnnotation(Object annotatedListener) {
        return annotatedListener.getClass().getAnnotation(EventListener.class);
    }

    private void populateExeptionHandlersSet() {
        
        Method[] methods = m_annotatedListener.getClass().getMethods();
        for(Method method : methods) {
            if (method.isAnnotationPresent(EventExceptionHandler.class)) {
                validateMethodAsEventExceptionHandler(method);
                m_exceptionHandlers.add(method);
            }
        }
        

    }
    
    private void validateMethodAsEventExceptionHandler(Method method) {
        Assert.state(method.getParameterTypes().length == 2, "Invalid number of paremeters. EventExceptionHandler methods must take 2 arguments with types (Event, ? extends Throwable)");
        Assert.state(ClassUtils.isAssignable(Event.class, method.getParameterTypes()[0]), "First parameter of incorrent type. EventExceptionHandler first paramenter must be of type Event");
        Assert.state(ClassUtils.isAssignable(Throwable.class, method.getParameterTypes()[1]), "Second parameter of incorrent type. EventExceptionHandler second paramenter must be of type ? extends Throwable");
    }

    private static class ClassComparator<T> implements Comparator<Class<? extends T>> {
        public int compare(Class<? extends T> lhsType, Class<? extends T> rhsType) {
            return ClassUtils.isAssignable(lhsType, rhsType) ? 1 : -1;
        }
    }

    private Comparator<Method> createExceptionHandlerComparator() {
        final ClassComparator<Throwable> classComparator = new ClassComparator<Throwable>();
        
        Comparator<Method> comparator = new Comparator<Method>() {

            public int compare(Method left, Method right) {
                Class<? extends Throwable> lhsType = left.getParameterTypes()[1].asSubclass(Throwable.class);
                Class<? extends Throwable> rhsType = right.getParameterTypes()[1].asSubclass(Throwable.class);
                
                EventExceptionHandler leftHandlerInfo = AnnotationUtils.findAnnotation(left, EventExceptionHandler.class);
                EventExceptionHandler rightHandlerInfo = AnnotationUtils.findAnnotation(right, EventExceptionHandler.class);
                
                if (leftHandlerInfo.order() == rightHandlerInfo.order()) {
                    return classComparator.compare(lhsType, rhsType);
                } else {
                    return leftHandlerInfo.order() - rightHandlerInfo.order();
                }
            }
            
        };
        
        return comparator;

    }

    private void populatePostProcessorList() {
        
        Method[] methods = m_annotatedListener.getClass().getMethods();
        for(Method method : methods) {
            if (method.isAnnotationPresent(EventPostProcessor.class)) {
                validateMethodAsEventHandler(method);
                m_eventPostProcessors.add(method);
            }
        }
    }

    private void populatePreProcessorList() {
        
        Method[] methods = m_annotatedListener.getClass().getMethods();
        for(Method method : methods) {
            EventPreProcessor ann = AnnotationUtils.findAnnotation(method, EventPreProcessor.class);
            if (ann != null) {
                validateMethodAsEventHandler(method);
                m_eventPreProcessors.add(method);
            }
        }
    }

    private void populateUeiToHandlerMap() {
        Method[] methods = m_annotatedListener.getClass().getMethods();
        
        for(Method method : methods) {
            EventHandler handlerInfo = AnnotationUtils.findAnnotation(method, EventHandler.class);
            if (handlerInfo != null) {
                String uei = handlerInfo.uei();
                Assert.state(!m_ueiToHandlerMap.containsKey(uei), "Cannot define method "+method+" as a handler for event "+uei+" since "+m_ueiToHandlerMap.get(uei)+" is already defined as a handler");
                validateMethodAsEventHandler(method);
                m_ueiToHandlerMap.put(uei, method);
            }
        }

        Assert.state(!m_ueiToHandlerMap.isEmpty(), "annotatedListener must have public EventHandler annotated methods");

    }

    private void validateMethodAsEventHandler(Method method) {
        Assert.state(method.getParameterTypes().length == 1, "Invalid number of paremeters for method "+method+". EventHandler methods must take a single event argument");
        Assert.state(method.getParameterTypes()[0].isAssignableFrom(Event.class), "Parameter of incorrent type for method "+method+". EventHandler methods must take a single event argument");
    }
    
    public void stop() {
        m_subscriptionService.removeEventListener(this);
    }

    public void destroy() throws Exception {
        stop();
    }

    public void setEventSubscriptionService(EventSubscriptionService subscriptionService) {
        m_subscriptionService = subscriptionService;
    }

}
