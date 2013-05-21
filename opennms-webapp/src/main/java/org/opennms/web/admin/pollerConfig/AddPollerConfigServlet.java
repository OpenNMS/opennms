/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.admin.pollerConfig;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.config.CapsdConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.web.api.Util;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AddPollerConfigServlet extends HttpServlet {
    private static final long serialVersionUID = 8025629129971135727L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String user_id = request.getRemoteUser();

        PollerConfiguration pollerConfig = null;
        CapsdConfiguration capsdConfig = null;
        Map<String, Service> pollerServices = new HashMap<String, Service>();
        Map<String, ProtocolPlugin> capsdProtocols = new HashMap<String, ProtocolPlugin>();
        List<ProtocolPlugin> capsdColl = new ArrayList<ProtocolPlugin>();
        Properties props = new Properties();
        org.opennms.netmgt.config.poller.Package firstPackage;

        try {
            props.load(new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)));
            PollerConfigFactory.init();
            PollerConfig pollerFactory = PollerConfigFactory.getInstance();
            pollerConfig = pollerFactory.getConfiguration();

            if (pollerConfig == null) {
                // response.sendRedirect( "error.jsp?error=2");
                throw new ServletException("Poller configuration file is empty");
            }
            CapsdConfigFactory.init();
            CapsdConfig capsdFactory = CapsdConfigFactory.getInstance();
            capsdConfig = capsdFactory.getConfiguration();

            if (capsdConfig == null) {
                // response.sendRedirect( "error.jsp?error=3");
                throw new ServletException("Capsd configuration file is empty");
            }
        } catch (Throwable e) {
            throw new ServletException(e.getMessage());
        }

        pollerServices = new HashMap<String, Service>();
        Collection<org.opennms.netmgt.config.poller.Package> packageColl = pollerConfig.getPackageCollection();
        if (packageColl != null && packageColl.size() > 0) {
            firstPackage = packageColl.iterator().next();
            for (org.opennms.netmgt.config.poller.Package pkg : packageColl) {
                for(Service svcProp : pkg.getServiceCollection()) {
                    pollerServices.put(svcProp.getName(), svcProp);
                }
            }
        } else {
            throw new ServletException("Poller configuration file contains no packages.");
        }
        Collection<ProtocolPlugin> pluginColl = capsdConfig.getProtocolPluginCollection();
        if (pluginColl != null) {
            for (ProtocolPlugin plugin : pluginColl) {
                capsdColl.add(plugin);
                capsdProtocols.put(plugin.getProtocol(), plugin);
            }
        }
        String redirectSuccess = getServletConfig().getInitParameter("redirect.success");
        if (redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }

        {
            String check1 = request.getParameter("check1");
            String name1 = request.getParameter("name1");
            String protoArray1 = request.getParameter("protArray1");
            String port1 = request.getParameter("port1");

            List<String> checkedList = new ArrayList<String>();
            if (name1 != null && !name1.equals("")) {
                if (!addPollerInfo(pollerConfig, firstPackage, props, check1, name1, port1, user_id, protoArray1, response, request)) {
                    return;
                }
                checkedList.add(name1);
                if (addCapsdInfo(capsdConfig, firstPackage, props, name1, port1, user_id, protoArray1, response, request)) {
                    props.setProperty("service." + name1 + ".protocol", protoArray1);
                } else {
                    return;
                }
            }

            props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null);
            Writer poller_fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME)), "UTF-8");
            Writer capsd_fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME)), "UTF-8");
            try {
                Marshaller.marshal(pollerConfig, poller_fileWriter);
                Marshaller.marshal(capsdConfig, capsd_fileWriter);
            } catch (MarshalException e) {
                throw new ServletException(e);
            } catch (ValidationException e) {
                throw new ServletException(e);
            }
        }
        response.sendRedirect(redirectSuccess);
    }

    /**
     * <p>addCapsdInfo</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param port a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param protocol a {@link java.lang.String} object.
     * @param response a {@link javax.servlet.http.HttpServletResponse} object.
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.io.IOException if any.
     */
    private boolean addCapsdInfo(CapsdConfiguration capsdConfig, org.opennms.netmgt.config.poller.Package pkg, Properties props, String name, String port, String user, String protocol, HttpServletResponse response, HttpServletRequest request) throws ServletException, IOException {
        // Check to see if the name is duplicate of the already specified names
        // first.
        for (ProtocolPlugin svc : capsdConfig.getProtocolPluginCollection()) {
            if (svc.getProtocol().equals(name)) {
                // delete from the poller configuration.
                for (Service pollersvc : pkg.getServiceCollection()) {
                    if (pollersvc.getName().equals(name)) {
                        Collection<Service> tmpPoller = pkg.getServiceCollection();
                        if (tmpPoller.contains(pollersvc) && pollersvc.getName().equals(name)) {
                            tmpPoller.remove(pollersvc);
                            response.sendRedirect(Util.calculateUrlBase(request, "/admin/error.jsp?error=1&name=" + name));
                            return false;
                        }
                        break;
                    }
                }
                break;
            }
        }
        ProtocolPlugin pluginAdd = new ProtocolPlugin();
        pluginAdd.setProtocol(name);
        String className = (String) props.get("service." + protocol + ".capsd-class");
        if (className != null) {
            pluginAdd.setClassName(className);
            pluginAdd.setScan("on");
            pluginAdd.setUserDefined("true");

            // Set banner property
            org.opennms.netmgt.config.capsd.Property newprop = new org.opennms.netmgt.config.capsd.Property();
            String banner = "*";
            if (props.get("banner") != null) {
                banner = (String) props.get("banner");
            }
            newprop.setValue(banner);
            newprop.setKey("banner");
            pluginAdd.addProperty(newprop);

            // Add port(s) property
            newprop = new org.opennms.netmgt.config.capsd.Property();
            if (port != null && !port.equals("")) {
                newprop.setValue(port);
                if (port.indexOf(":") == -1) {
                    newprop.setKey("port");
                } else {
                    newprop.setKey("ports");
                }
                pluginAdd.addProperty(newprop);
            } else {
                if (props.get("service." + protocol + ".port") == null || ((String) props.get("service." + protocol + ".port")).equals("")) {
                    response.sendRedirect(Util.calculateUrlBase(request, "admin/error.jsp?error=0&name=" + "service." + protocol + ".port"));
                    return false;
                } else {
                    port = (String) props.get("service." + protocol + ".port");
                    newprop.setValue(port);
                    if (port.indexOf(":") == -1) {
                        newprop.setKey("port");
                    } else {
                        newprop.setKey("ports");
                    }
                    pluginAdd.addProperty(newprop);
                }
            }

            // Add timeout property
            newprop = new org.opennms.netmgt.config.capsd.Property();
            String timeout = "3000";
            if (props.get("timeout") != null) {
                timeout = (String) props.get("timeout");
            }
            newprop.setValue(timeout);
            newprop.setKey("timeout");
            pluginAdd.addProperty(newprop);

            // Add retry property
            newprop = new org.opennms.netmgt.config.capsd.Property();
            String retry = "3";
            if (props.get("retry") != null) {
                retry = (String) props.get("retry");
            }
            newprop.setValue(retry);
            newprop.setKey("retry");
            pluginAdd.addProperty(newprop);

            // Add the plugin to the capsdConfig
            capsdConfig.addProtocolPlugin(pluginAdd);

            // Everything worked, return true
            return true;
        } else {
            response.sendRedirect(Util.calculateUrlBase(request, "admin/error.jsp?error=0&name=" + "service." + protocol + ".capsd-class"));
            return false;
        }
    }

    /**
     * <p>addPollerInfo</p>
     *
     * @param bPolled a {@link java.lang.String} object.
     * @param name a {@link java.lang.String} object.
     * @param port a {@link java.lang.String} object.
     * @param user a {@link java.lang.String} object.
     * @param protocol a {@link java.lang.String} object.
     * @param response a {@link javax.servlet.http.HttpServletResponse} object.
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.io.IOException if any.
     */
    private boolean addPollerInfo(PollerConfiguration pollerConfig, org.opennms.netmgt.config.poller.Package pkg, Properties props, String bPolled, String name, String port, String user, String protocol, HttpServletResponse response, HttpServletRequest request) throws ServletException, IOException {
        // Check to see if the name is duplicate of the already specified names
        // first.
        for (Service svc : pkg.getServiceCollection()) {
            if (svc.getName().equals(name)) {
                response.sendRedirect(Util.calculateUrlBase(request, "admin/error.jsp?error=1&name=" + name));
                return false;
            }
        }

        if (pkg != null) {
            Service newService = new Service();
            newService.setName(name);
            if (bPolled != null) {
                newService.setStatus(bPolled);
            } else {
                newService.setStatus("off");
            }
            newService.setName(name);
            newService.setUserDefined("true");

            Collection<Monitor> monitorColl = pollerConfig.getMonitorCollection();
            Monitor newMonitor = new Monitor();
            String monitor = (String) props.get("service." + protocol + ".monitor");
            if (monitor != null) {
                newMonitor.setService(name);
                newMonitor.setClassName(monitor);
            } else {
                response.sendRedirect(Util.calculateUrlBase(request, "admin/error.jsp?error=0&name=" + "service." + protocol + ".monitor"));
                return false;
            }

            if (props.get("interval") != null) {
                newService.setInterval((new Long((String) props.get("interval"))).longValue());
            } else {
                newService.setInterval(300000);
            }

            org.opennms.netmgt.config.poller.Parameter newprop = new org.opennms.netmgt.config.poller.Parameter();
            String timeout = "3000";
            if (props.get("timeout") != null) {
                timeout = (String) props.get("timeout");
            }
            newprop.setValue(timeout);
            newprop.setKey("timeout");
            newService.addParameter(newprop);

            newprop = new org.opennms.netmgt.config.poller.Parameter();
            String banner = "*";
            if (props.get("banner") != null) {
                banner = (String) props.get("banner");
            }
            newprop.setValue(banner);
            newprop.setKey("banner");
            newService.addParameter(newprop);

            newprop = new org.opennms.netmgt.config.poller.Parameter();
            String retry = "3";
            if (props.get("retry") != null) {
                retry = (String) props.get("retry");
            }
            newprop.setValue(retry);
            newprop.setKey("retry");
            newService.addParameter(newprop);

            newprop = new org.opennms.netmgt.config.poller.Parameter();
            if (port == null || port.equals("")) {
                if (props.get("service." + protocol + ".port") == null || ((String) props.get("service." + protocol + ".port")).equals("")) {
                    newMonitor = null;
                    newService = null;
                    response.sendRedirect(Util.calculateUrlBase(request, "admin/error.jsp?error=0&name=" + "service." + protocol + ".port"));
                    return false;
                } else {
                    port = (String) props.get("service." + protocol + ".port");
                }
            }

            newprop.setValue(port);
            if (port.indexOf(":") != -1) {
                newprop.setKey("ports");
            } else { 
                newprop.setKey("port");
            }
            if (newMonitor != null && newService != null) {
                if (monitorColl == null) {
                    pollerConfig.addMonitor(0, newMonitor);
                } else {
                    pollerConfig.addMonitor(newMonitor);
                }
                newService.addParameter(newprop);
                pkg.addService(newService);
            }
        }
        return true;
    }
}
