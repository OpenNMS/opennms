package org.opennms.web;

import javax.servlet.ServletContextEvent;
import org.springframework.web.util.Log4jWebConfigurer;

public class Log4jConfigListener implements javax.servlet.ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        if (! event.getServletContext().getServerInfo().toLowerCase().contains("jetty")) {
            Log4jWebConfigurer.initLogging(event.getServletContext());
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        if (! event.getServletContext().getServerInfo().toLowerCase().contains("jetty")) {
            Log4jWebConfigurer.shutdownLogging(event.getServletContext());
        }
    }

}
