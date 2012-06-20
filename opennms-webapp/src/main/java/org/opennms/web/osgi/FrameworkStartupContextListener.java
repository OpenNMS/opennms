/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.osgi;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.osgid.Osgid;
import org.osgi.framework.BundleContext;

/**
 */
public class FrameworkStartupContextListener implements ServletContextListener {

    private Osgid m_service;

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        try {
            m_service.destroy();
            // Clean up the old Karaf temp directory
            FileUtils.deleteDirectory(new File(event.getServletContext().getRealPath("/") + File.separator + "WEB-INF" + File.separator + "karaf" + File.separator + "data"));
        } catch (Exception e) {
            LogUtils.errorf(this, e, "Could not start up OSGi container: %s", e.getMessage());
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            m_service = new Osgid();
            m_service.setHomeDirectory(sce.getServletContext().getRealPath("/") + File.separator + "WEB-INF" + File.separator + "karaf");
            m_service.start();
            sce.getServletContext().setAttribute(BundleContext.class.getName(), m_service.getBundleContext());
        } catch (Exception e) {
            LogUtils.errorf(this, e, "Could not start up OSGi container: %s", e.getMessage());
        }
    }
}
