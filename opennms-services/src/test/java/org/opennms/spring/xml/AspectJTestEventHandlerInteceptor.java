package org.opennms.spring.xml;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.core.Ordered;

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
