/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.container.web;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.karaf.main.Main;
import org.osgi.framework.BundleContext;

public class WebAppListener implements ServletContextListener {

    private Main main;
    private ServletContext m_servletContext;
    private BundleContext m_framework;
    private OnmsOSGiBridgeActivator m_bridge = new OnmsOSGiBridgeActivator();
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            
            m_servletContext = sce.getServletContext();

            File karafRoot = new File(m_servletContext.getRealPath("/") + "/WEB-INF/karaf");

            final String opennmsHome = System.getProperty("opennms.home");
            if (opennmsHome != null) {
                karafRoot = new File(opennmsHome);
            }

            /*
            String karafHome = System.getProperty("karaf.home");
            if (karafHome != null) {
                karafRoot = new File(karafHome);
            }
            */

            m_servletContext.log("contextInitialized");

            // log4j class instances will leak into the OSGi classloader so we
            // need to tell log4j to ignore the thread context loader and to use
            // the same classloader for all classloading.
            //
            // @see https://issues.apache.org/jira/browse/FELIX-2108
            // @see http://www.mail-archive.com/announcements@jakarta.apache.org/msg00110.html
            //
            System.setProperty("log4j.ignoreTCL", "true");

            final String root = karafRoot.getAbsolutePath();
            m_servletContext.log("Root: " + root);
            System.setProperty("karaf.home", root);
            System.setProperty("karaf.base", root);
            System.setProperty("karaf.data", root + "/data");
            System.setProperty("karaf.history", root + "/data/history.txt");
            System.setProperty("karaf.instances", root + "/instances");
            System.setProperty("karaf.startLocalConsole", "false");
            System.setProperty("karaf.startRemoteShell", "true");
            System.setProperty("karaf.lock", "false");
            main = new Main(new String[0]);
            main.launch();
            
            // get bundle context for registering service
            m_framework = main.getFramework().getBundleContext();
            
            // add bundle context to servlet context for Proxy Servlet
            m_servletContext.setAttribute(BundleContext.class.getName(), m_framework);

            
            m_bridge.start(m_framework);

        } catch (final Throwable e) {
            main = null;
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            
            m_bridge.stop(m_framework);
            // TODO unregister services form both registries with the osgi container stops
            
            m_servletContext.log("contextDestroyed");
            if (main != null) {
                main.destroy();
            }
        } catch (final Throwable e) {
            e.printStackTrace();
        }
    }


}
