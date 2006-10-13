package org.opennms.netmgt.poller.jmx;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.ApplicationContext;
import org.springframework.context.access.DefaultLocatorFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class RemotePollerBackEnd implements RemotePollerBackEndMBean {

    private static final String NAME = "PollerBackEnd";
    
    private ClassPathXmlApplicationContext m_context;
    int m_status = Fiber.START_PENDING;
    
    
    // used only for testing
    ApplicationContext getContext() {
        return m_context;
    }

    public void init() {
        ThreadCategory.setPrefix(NAME);
    }

    public void start() {
        ThreadCategory.setPrefix(NAME);
        m_status = Fiber.STARTING;
        ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());

        BeanFactoryLocator bfl = DefaultLocatorFactory.getInstance();
        BeanFactoryReference bf = bfl.useBeanFactory("pollerBackEndContext");
        m_context = (ClassPathXmlApplicationContext) bf.getFactory();

        ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());
        m_status = Fiber.RUNNING;
    }

    public void stop() {
        ThreadCategory.setPrefix(NAME);
        m_status = Fiber.STOP_PENDING;
        m_context.close();
        
        
        m_status = Fiber.STOPPED;
    }

    public String status() {
        ThreadCategory.setPrefix(NAME);
        return Fiber.STATUS_NAMES[m_status];
    }

    public int getStatus() {
        return m_status;
    }

    public String getStatusText() {
        return status();
    }
    
}
