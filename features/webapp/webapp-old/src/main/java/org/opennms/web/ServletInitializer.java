//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jan 21: Use a DataSource instead of a DbConnectionFactory. - dj@opennms.org
// 2008 Jan 21: Clean up init. - dj@opennms.org
// 2007 Apr 05: Don't initialize log4j ourselves; let it happen in web.xml at
//              the hands of Spring's log4j initializer. - dj@opennms.org
// 2003 Feb 02: Changed Log4J initialization.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.opennms.core.resource.Vault;
import org.opennms.core.resource.db.DbConnectionFactory;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataSourceFactory;

/**
 * Encapsulates all initialization and configuration needed by the OpenNMS
 * servlets and JSPs.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class ServletInitializer extends Object {
    /**
     * A reference to the factory we set in {@link Vault Vault}during
     * {@link #init init}so we can destroy it in {@link #destroy destroy}.
     * 
     * <p>
     * Maybe there's a better way to do this then storing a reference? Should we
     * just add a method to <code>Vault</code>?
     * </p>
     * 
     * <p>
     * This reference also serves as a flag to determine whether or not this
     * class has been initialized yet. If it is null, the class has not yet been
     * initialized.
     * </p>
     */
    protected static DbConnectionFactory factory;

    /**
     * Private, empty constructor so that this class cannot be instantiated
     * outside of itself.
     */
    private ServletInitializer() {
    }

    /**
     * Initialize servlet and JSP configuration on the first invocation of this
     * method. All other invocations are ignored. This method is synchronized to
     * ensure only the first invocation performs the initialization.
     * 
     * <p>
     * Call this method in the <code>init</code> method of your servlet or
     * JSP. It will read the servlet initialization parameters from the
     * <code>ServletConfig</code> and <code>ServletContext</code> and
     * OpenNMS configuration files.
     * </p>
     * 
     * @param context
     *            the <code>ServletContext</code> instance in which your
     *            servlet is running
     */
    public synchronized static void init(ServletContext context) throws ServletException {
        if (context == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        /*
         * All ThreadCategory instances in the WebUI should use this as their
         * category prefix
         */
        ThreadCategory.setPrefix("OpenNMS.WEB");

        if (factory == null) {
            Properties properties = new Properties(System.getProperties());

            try {
                /*
                 * First, check if opennms.home is set, if so, we already have properties
                 * because we're in Jetty.
                 */
                if (properties.getProperty("opennms.home") == null) {
                    // If not, load properties from configuration.properties
                    loadPropertiesFromContextResource(context, properties, "/WEB-INF/configuration.properties");

                    // Make sure that we now have opennms.home set
                    if (properties.getProperty("opennms.home") == null) {
                        throw new ServletException("The opennms.home context parameter must be set.");
                    }
                }
            } catch (IOException e) {
                throw new ServletException("Could not load configuration.properties", e);
            }

            String homeDir = properties.getProperty("opennms.home");

            /*
             * Now that we've got opennms.home, load $OPENNMS_HOME/etc/opennms.properties
             * in case it isn't--but if anything is already set, we don't override it.
             */
            Properties opennmsProperties = new Properties();
            
            try {
                loadPropertiesFromFile(opennmsProperties, homeDir + File.separator + "etc" + File.separator + "opennms.properties");
            } catch (IOException e) {
                throw new ServletException("Could not load opennms.properties", e);
            }

            try {
                loadPropertiesFromContextResource(context, opennmsProperties, "/WEB-INF/version.properties");
            } catch (IOException e) {
                throw new ServletException("Could not load version.properties", e);
            }

            for (Enumeration<Object> opennmsKeys = opennmsProperties.keys(); opennmsKeys.hasMoreElements(); ) {
                Object key = opennmsKeys.nextElement();
                if (!properties.containsKey(key)) {
                    properties.put(key, opennmsProperties.get(key));
                }
            }

            Enumeration<?> initParamNames = context.getInitParameterNames();
            while (initParamNames.hasMoreElements()) {
                String name = (String) initParamNames.nextElement();
                properties.put(name, context.getInitParameter(name));
            }

            Vault.setProperties(properties);
            Vault.setHomeDir(homeDir);
            
            try {
                DataSourceFactory.init();
            } catch (Exception e) {
                throw new ServletException("Could not initialize data source factory: " + e, e);
            }
            
            Vault.setDataSource(DataSourceFactory.getInstance());
        }
    }

    private static void loadPropertiesFromFile(Properties opennmsProperties, String propertiesFile) throws FileNotFoundException, ServletException, IOException {
        InputStream configurationStream = new FileInputStream(propertiesFile);
        if (configurationStream == null) {
            throw new ServletException("Could not load properties from file '" + propertiesFile + "'");
        } else {
            opennmsProperties.load(configurationStream);
            configurationStream.close();
        }
    }

    private static void loadPropertiesFromContextResource(ServletContext context, Properties properties, String propertiesResource) throws ServletException, IOException {
        InputStream configurationStream = context.getResourceAsStream(propertiesResource);
        if (configurationStream == null) {
            throw new ServletException("Could not load properties from resource '" + propertiesResource + "'");
        } else {
            properties.load(configurationStream);
            configurationStream.close();
        }
    }

    /**
     * Releases all shared resources on the first invocation of this method. All
     * other invocations are ignored. This method is synchronized to ensure only
     * the first invocation performs the destruction.
     * 
     * <p>
     * Call this method in the <code>destroy</code> method of your servlet or
     * JSP.
     * </p>
     * 
     * @param context
     *            the <code>ServletContext</code> instance in which your
     *            servlet is running
     */
    public synchronized static void destroy(ServletContext context) throws ServletException {
        try {
            if (factory != null) {
                factory.destroy();
                factory = null;
            }
        } catch (Exception e) {
            throw new ServletException("Could not destroy the database connection factory", e);
        }
    }

    /**
     * Return the absolute pathname of where OpenNMS's configuration can be
     * found.
     * 
     * @deprecated Use {@link Vault#getHomeDir Vault.getHomeDir}instead.
     */
    public static String getHomeDir() {
        return (Vault.getHomeDir());
    }

}
