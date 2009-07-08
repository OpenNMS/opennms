/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
