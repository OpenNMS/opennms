/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2004, 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.web;

import java.net.ConnectException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.web.category.CategoryList;
import org.opennms.web.category.RTCPostSubscriber;

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
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class InitializerServletContextListener implements ServletContextListener {

    private Timer rtcCheckTimer = null;

    public void contextInitialized(ServletContextEvent event) {
        try {
            /*
             * Initialize the scarce resource policies (db connections) and
             * common configuration.
             */
            ServletInitializer.init(event.getServletContext());

            log().info("Initialized servlet systems successfully");
        } catch (ServletException e) {
            log().error("Error while initializing servlet systems: " + e, e);
        } catch (Exception e) {
            log().error("Error while initializing user, group, or view factory: " + e, e);
        }

        try {
            rtcCheckTimer = new Timer();
            rtcCheckTimer.schedule(new RTCPostSubscriberTimerTask(), new Date(), 130000);
        } catch (ServletException e) {
            log().error("Error while initializing RTC check timer: " + e, e);
        }
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            /*
             * Let the scarce resource policies release any shared
             * resouces (db connections).
             */
            ServletInitializer.destroy(event.getServletContext());

            // Report success.
            log().info("Destroyed servlet systems successfully");
        } catch (ServletException e) {
            log().error("Error while destroying servlet systems: " + e, e);
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

        public void run() {
            try {
                if (!m_categorylist.isDisconnected()) {
                    return;
                }
            } catch (Exception e) {
                log().error("Error checking if OpenNMS is disconnected: " + e, e);
                return;
            }

            log().info("OpenNMS is disconnected -- attempting RTC POST subscription");

            try {
                RTCPostSubscriber.subscribeAll("WebConsoleView");
                log().info("RTC POST subscription event sent successfully");
            } catch (EventProxyException e) {
                if (e.getCause() instanceof ConnectException) {
                    log().info("RTC POST failed due to ConnectException: " + e.getCause().toString());
                } else {
                    log().error("Error subscribing to RTC POSTs: " + e, e);
                }
            } catch (Exception e) {
                log().error("Error subscribing to RTC POSTs: " + e, e);
            }
        }
        
        private Category log() {
            return ThreadCategory.getInstance(getClass());
        }
    }
}
