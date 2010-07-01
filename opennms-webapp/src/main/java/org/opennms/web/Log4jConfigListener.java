package org.opennms.web;

import javax.servlet.ServletContextEvent;
import org.springframework.web.util.Log4jWebConfigurer;

/**
 * <p>Log4jConfigListener class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class Log4jConfigListener implements javax.servlet.ServletContextListener {

    /** {@inheritDoc} */
    public void contextInitialized(ServletContextEvent event) {
        if (! event.getServletContext().getServerInfo().toLowerCase().contains("jetty")) {
            Log4jWebConfigurer.initLogging(event.getServletContext());
        }
    }

    /** {@inheritDoc} */
    public void contextDestroyed(ServletContextEvent event) {
        if (! event.getServletContext().getServerInfo().toLowerCase().contains("jetty")) {
            Log4jWebConfigurer.shutdownLogging(event.getServletContext());
        }
    }

}
