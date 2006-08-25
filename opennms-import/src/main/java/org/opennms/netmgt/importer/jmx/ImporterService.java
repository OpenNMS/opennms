package org.opennms.netmgt.importer.jmx;

import org.opennms.core.fiber.Fiber;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ImporterService implements ImporterServiceMBean {
	
	private static final String NAME = org.opennms.netmgt.importer.ImporterService.NAME;
	
    private ClassPathXmlApplicationContext m_context;
    int m_status = Fiber.START_PENDING;
    
    
    // used only for testing
    ApplicationContext getContext() {
        return m_context;
    }

    public void init() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
    }

    public void start() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
        m_status = Fiber.STARTING;
        ThreadCategory.getInstance().debug("SPRING: thread.classLoader="+Thread.currentThread().getContextClassLoader());;
        m_context = new ClassPathXmlApplicationContext(new String[] { "/org/opennms/netmgt/importer/importer-context.xml" , "/META-INF/opennms/applicationContext-dao.xml" });
        ThreadCategory.getInstance().debug("SPRING: context.classLoader="+m_context.getClassLoader());
        m_status = Fiber.RUNNING;
    }

    public void stop() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
        m_status = Fiber.STOP_PENDING;
        m_context.close();
        
        
        m_status = Fiber.STOPPED;
    }

    public String status() {
    	ThreadCategory.setPrefix(ImporterService.NAME);
        return Fiber.STATUS_NAMES[m_status];
    }

	public int getStatus() {
		return m_status;
	}
	
	public String getStats() {
		return getImporterService().getStats();
	}

	private org.opennms.netmgt.importer.ImporterService getImporterService() {
		org.opennms.netmgt.importer.ImporterService importer = (org.opennms.netmgt.importer.ImporterService)m_context.getBean("modelImporter");
		return importer;
	}


}
