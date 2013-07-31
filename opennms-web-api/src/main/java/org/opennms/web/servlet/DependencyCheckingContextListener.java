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

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>DependencyCheckingContextListener class.</p>
 *
 * @author <a href="dj@opennms.org">DJ Gregor</a>
 */
public class DependencyCheckingContextListener implements ServletContextListener {
	
	private static final Logger LOG = LoggerFactory.getLogger(DependencyCheckingContextListener.class);

    private static final String IGNORE_ERRORS_PROPERTY = "dontBlameOpenNMS";
    private static final String IGNORE_ERRORS_MESSAGE = "but don't blame OpenNMS for any errors that occur without switching back to a supported JVM and setting the property back to 'false', first.";
    
    /** {@inheritDoc} */
    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }

    /** {@inheritDoc} */
    @Override
    public void contextInitialized(ServletContextEvent event) {
        Boolean skipJvm = new Boolean(System.getProperty("opennms.skipjvmcheck"));
        if (!skipJvm) {
            checkJvmName(event.getServletContext());
        }
    }

    private void checkJvmName(ServletContext context) {
        final String systemProperty = "java.vm.name";
        final String[] acceptableProperties = { "HotSpot(TM)", "BEA JRockit", "OpenJDK" };
        
        String vmName = System.getProperty(systemProperty);
        if (vmName == null) {
            logAndOrDie(context, "System property '" + systemProperty + "' is not set so we can't figure out if this version of Java is supported");
        }

        boolean ok = false;
        for (String systemPropertyMatch : acceptableProperties) {
            if (vmName.contains(systemPropertyMatch)) {
                ok = true;
            }
        }

        if (ok) {
            LOG.info("System property '{}' appears to contain a suitable JVM signature ('{}') -- congratulations!  ;)", systemProperty, vmName);
        } else {
            logAndOrDie(context, "System property '" + systemProperty + "' does not contain a suitable JVM signature ('" + vmName + "').  OpenNMS recommends the official Sun JVM.");
        }
    }

    private void logAndOrDie(ServletContext context, String message) {
        String webXmlPath = context.getRealPath("/WEB-INF/web.xml");
        
        if (Boolean.parseBoolean(context.getInitParameter(IGNORE_ERRORS_PROPERTY))) {
            LOG.warn(message);
            LOG.warn("Context parameter '{}' is set in {}, so the above warning is not fatal,  {}", IGNORE_ERRORS_PROPERTY, webXmlPath, IGNORE_ERRORS_MESSAGE);
        } else {
            String howToFixMessage = "You can edit " + webXmlPath + " and change the value for the '" + IGNORE_ERRORS_PROPERTY + "' context parameter from 'false' to 'true', " + IGNORE_ERRORS_MESSAGE;

            LOG.error(message);
            LOG.error(howToFixMessage);
            
            throw new RuntimeException(message + "  " + howToFixMessage);
        }
    }

}
