/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.servlet;

import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.category.CategoryList;
import org.opennms.web.category.RTCPostSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes our internal servlet systems at servlet container startup, and
 * destroys any pool resources at servlet container shutdown.
 *
 * This listener is specified in the web.xml to listen to
 * <code>ServletContext</code> lifecyle events. On startup it calls
 * ServletInitializer.init and initializes the UserFactory, GroupFactory. On
 * shutdown it calls ServletInitializer.destroy.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public class InitializerServletContextListener implements ServletContextListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(InitializerServletContextListener.class);


    private Timer rtcCheckTimer = null;

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            /*
             * Initialize the scarce resource policies (db connections) and
             * common configuration.
             */
            ServletInitializer.init(event.getServletContext());

            LOG.info("Initialized servlet systems successfully");
        } catch (ServletException e) {
            LOG.error("Error while initializing servlet systems: {}", e, e);
        } catch (Throwable e) {
            LOG.error("Error while initializing user, group, or view factory: {}", e, e);
        }

        try {
            rtcCheckTimer = new Timer();
            rtcCheckTimer.schedule(new RTCPostSubscriberTimerTask(), new Date(), 130000);
        } catch (ServletException e) {
            LOG.error("Error while initializing RTC check timer: {}", e, e);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            /*
             * Let the scarce resource policies release any shared
             * resouces (db connections).
             */
            ServletInitializer.destroy(event.getServletContext());

            // Report success.
            LOG.info("Destroyed servlet systems successfully");
        } catch (ServletException e) {
            LOG.error("Error while destroying servlet systems: {}", e, e);
        }

        if (rtcCheckTimer != null) {
            rtcCheckTimer.cancel();
            rtcCheckTimer = null;
        }
    }

    public class RTCPostSubscriberTimerTask extends TimerTask {
        private CategoryList m_categorylist;

        public RTCPostSubscriberTimerTask() throws ServletException {
            m_categorylist = new CategoryList();
        }

        @Override
        public void run() {
            try {
                if (!m_categorylist.isDisconnected()) {
                    return;
                }
            } catch (Throwable e) {
                LOG.error("Error checking if OpenNMS is disconnected: {}", e, e);
                return;
            }

            LOG.info("OpenNMS is disconnected -- attempting RTC POST subscription");

            try {
                RTCPostSubscriber.subscribeAll("WebConsoleView");
                LOG.info("RTC POST subscription event sent successfully");
            } catch (EventProxyException e) {
                if (e.getCause() instanceof ConnectException) {
                    LOG.info("RTC POST failed due to ConnectException: {}", e.getCause().toString());
                } else {
                    LOG.error("Error subscribing to RTC POSTs: {}", e, e);
                }
            } catch (Throwable e) {
                LOG.error("Error subscribing to RTC POSTs: {}", e, e);
            }
        }
        
    }
}
