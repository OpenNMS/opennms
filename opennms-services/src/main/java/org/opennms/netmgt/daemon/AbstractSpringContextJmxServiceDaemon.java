package org.opennms.netmgt.daemon;

import org.apache.log4j.Category;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class AbstractSpringContextJmxServiceDaemon implements BaseOnmsMBean {

    private ClassPathXmlApplicationContext m_context;

    private int m_status = Fiber.START_PENDING;

    public AbstractSpringContextJmxServiceDaemon() {
        super();
    }
    
    protected abstract String getSpringContext();

    protected abstract String getLoggingPrefix();

    /**
     * This is here for unit tests to use.
     */
    protected ApplicationContext getContext() {
        return m_context;
    }

    public final void init() {
        setLoggingCategory();
    }

    public final void start() {
        setLoggingCategory();
        
        setStatus(Fiber.STARTING);
        log().debug("SPRING: thread.classLoader=" + Thread.currentThread().getContextClassLoader());
    
        m_context = BeanUtils.getFactory(getSpringContext(), ClassPathXmlApplicationContext.class);
    
        log().debug("SPRING: context.classLoader= "+ m_context.getClassLoader());
        setStatus(Fiber.RUNNING);
    }

    public final void stop() {
        setLoggingCategory();
        
        setStatus(Fiber.STOP_PENDING);
        m_context.close();
        
        
        setStatus(Fiber.STOPPED);
    }

    public final int getStatus() {
        return m_status;
    }

    private void setStatus(int status) {
        m_status = status;
    }

    public final String status() {
        setLoggingCategory();
        
        return Fiber.STATUS_NAMES[getStatus()];
    }

    public final String getStatusText() {
        return status();
    }

    private Category log() {
        return ThreadCategory.getInstance();
    }

    private void setLoggingCategory() {
        ThreadCategory.setPrefix(getLoggingPrefix());
    }


}