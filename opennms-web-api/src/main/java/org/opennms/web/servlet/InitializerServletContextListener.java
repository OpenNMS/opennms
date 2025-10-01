/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.servlet;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.opennms.web.category.CategoryList;
import org.opennms.web.category.RTCPostSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener is specified in the web.xml to listen to
 * <code>ServletContext</code> lifecyle events. On startup it calls
 * {@link ServletInitializer#init(javax.servlet.ServletContext)} to set 
 * up OpenNMS-specific properties and starts an RTC subscription timer. On
 * shutdown it stops the RTC timer.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 */
public class InitializerServletContextListener implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(InitializerServletContextListener.class);

    private Timer rtcCheckTimer = null;

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Initialize the common configuration properties for the web UI.
        try {
            ServletInitializer.init(event.getServletContext());
            LOG.info("Initialized servlet systems successfully");
        } catch (ServletException e) {
            LOG.error("Error while initializing servlet systems: {}", e, e);
        } catch (Throwable e) {
            LOG.error("Error while initializing user, group, or view factory: {}", e, e);
        }

        // Create a new Timer that will periodically resubscribe to RTC category updates.
        // This timer is set to execute every 20 seconds. This is relatively frequent but
        // the subscription events will only be sent if categories are judged to be 'disconnected'
        // due to their timestamps not being updated inside {@link CategoryModel}.
        try {
            rtcCheckTimer = new Timer();
            rtcCheckTimer.schedule(new RTCPostSubscriberTimerTask(), new Date(), 20000);
        } catch (ServletException e) {
            LOG.error("Error while initializing RTC check timer: {}", e, e);
        }
    }


    /**
     * Cancel the RTC subscription timer.
     */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (rtcCheckTimer != null) {
            rtcCheckTimer.cancel();
            rtcCheckTimer = null;
        }
    }

    /**
     * This task is used to call {@link RTCPostSubscriber#subscribeAll(String)} if the categories have
     * not been updated recently or ever (such as at startup).
     */
    public static class RTCPostSubscriberTimerTask extends TimerTask {
        private static CategoryList m_categorylist;

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

            LOG.debug("OpenNMS is disconnected -- attempting RTC POST subscription");

            try {
                RTCPostSubscriber.subscribeAll("WebConsoleView");
                LOG.debug("RTC POST subscription event sent successfully");
            } catch (Throwable e) {
                LOG.error("Error subscribing to RTC POSTs: {}", e, e);
            }
        }
        
    }
}
