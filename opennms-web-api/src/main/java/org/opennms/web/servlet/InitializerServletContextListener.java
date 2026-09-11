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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes the OpenNMS-specific servlet context properties when the web
 * application starts.
 */
public class InitializerServletContextListener implements ServletContextListener {
    private static final Logger LOG = LoggerFactory.getLogger(InitializerServletContextListener.class);

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            ServletInitializer.init(event.getServletContext());
            LOG.info("Initialized servlet systems successfully");
        } catch (ServletException e) {
            LOG.error("Error while initializing servlet systems: {}", e, e);
        } catch (Throwable e) {
            LOG.error("Error while initializing user, group, or view factory: {}", e, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
