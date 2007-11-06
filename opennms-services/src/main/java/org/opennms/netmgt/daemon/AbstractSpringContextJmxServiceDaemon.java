package org.opennms.netmgt.daemon;

import java.lang.reflect.UndeclaredThrowableException;

import org.apache.log4j.Category;
import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public abstract class AbstractSpringContextJmxServiceDaemon implements BaseOnmsMBean {

    public static final String DAEMON_BEAN_NAME = "daemon";

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
  
        log().debug("SPRING: thread.classLoader=" + Thread.currentThread().getContextClassLoader());

        m_context = BeanUtils.getFactory(getSpringContext(), ClassPathXmlApplicationContext.class);

        log().debug("SPRING: context.classLoader= "+ m_context.getClassLoader());
    }
    
    public final void start() {
        setLoggingCategory();
        
        setStatus(Fiber.STARTING);
        SpringServiceDaemon daemon = (SpringServiceDaemon) m_context.getBean(DAEMON_BEAN_NAME, SpringServiceDaemon.class);
        try {
            daemon.start();
        } catch (Throwable t) {
            log().error("Could not start daemon: " + t, t);
            
            try {
                stop();
            } catch (Throwable tt) {
                log().error("Could not stop daemon after it failed to start: " + tt, tt);
            }
            
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new UndeclaredThrowableException(t);
            }
        }
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
