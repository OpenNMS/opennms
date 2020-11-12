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
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.karaf.main.Main;
import org.opennms.core.soa.support.OnmsOSGiBridgeActivator;
import org.osgi.framework.BundleContext;

/**
 * Listener which starts Apache Karaf as part of starting up the OpenNMS Webapp.
 */
public class WebAppListener implements ServletContextListener {

    private Main main;
    private ServletContext m_servletContext;
    private BundleContext m_framework;
    private OnmsOSGiBridgeActivator m_bridge = new OnmsOSGiBridgeActivator();

    private void log(final String message) {
        this.log(message, null);
    }

    private void log(final String message, final Throwable t) {
        m_servletContext.log("container.servlet.WebAppListener: " + message, t);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            m_servletContext = sce.getServletContext();
            this.log("servlet context initialized");

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

            final String root = karafRoot.getAbsolutePath();
            this.log("using Karaf root: " + root);

            final Properties karafProps = new Properties();
            karafProps.setProperty("karaf.home", root);
            karafProps.setProperty("karaf.base", root);
            karafProps.setProperty("karaf.data", root + File.separator + "data");
            karafProps.setProperty("karaf.log", root + File.separator + "logs");
            karafProps.setProperty("karaf.etc", root + File.separator + "etc");
            karafProps.setProperty("karaf.history", root + File.separator + "data" + File.separator + "history.txt");
            karafProps.setProperty("karaf.instances", root + File.separator + "instances");
            karafProps.setProperty("karaf.startLocalConsole", "false");
            karafProps.setProperty("karaf.startRemoteShell", "true");
            karafProps.setProperty("karaf.lock", "false");
            System.setProperties(karafProps);

            this.log("launching Karaf with properties: " + karafProps);
            main = new Main(new String[0]);
            main.launch();

            this.log("adding bundle context to Karaf servlet context");
            // get bundle context for registering service
            m_framework = main.getFramework().getBundleContext();

            // add bundle context to servlet context for Proxy Servlet
            m_servletContext.setAttribute(BundleContext.class.getName(), m_framework);

            this.log("adding bundle context to OpenNMS OSGi bridge");
            m_bridge.start(m_framework);
        } catch (final Throwable e) {
            this.log("unexpected exception while starting Karaf", e);
            main = null;
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            this.log("removing Karaf bundle context from OpenNMS OSGi bridge");
            m_bridge.stop(m_framework);
            // TODO unregister services form both registries when the osgi container stops

            this.log("stopping Karaf");
            if (main != null) {
                main.destroy();
            }
            this.log("Karaf stopped");
        } catch (final Throwable e) {
            this.log("unexpected exception while stopping Karaf", e);
            e.printStackTrace();
        }
    }


}
