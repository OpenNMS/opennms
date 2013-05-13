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

package org.opennms.web.admin.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
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

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class PollerConfigServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 2622622848304715121L;
    
    PollerConfiguration pollerConfig = null;

    CapsdConfiguration capsdConfig = null;

    protected String redirectSuccess;

    HashMap<String, Service> pollerServices = new HashMap<String, Service>();

    HashMap<String, ProtocolPlugin> capsdProtocols = new HashMap<String, ProtocolPlugin>();

    java.util.List<ProtocolPlugin> capsdColl = new ArrayList<ProtocolPlugin>();

    org.opennms.netmgt.config.poller.Package pkg = null;

    Collection<ProtocolPlugin> pluginColl = null;

    Properties props = new Properties();

    PollerConfig pollerFactory = null;

    CapsdConfig capsdFactory = null;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        try {
            props.load(new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)));
            PollerConfigFactory.init();
            pollerFactory = PollerConfigFactory.getInstance();
            pollerConfig = pollerFactory.getConfiguration();

            if (pollerConfig == null) {
                throw new ServletException("Poller Configuration file is empty");
            }
            CapsdConfigFactory.init();
            capsdFactory = CapsdConfigFactory.getInstance();
            capsdConfig = capsdFactory.getConfiguration();

            if (capsdConfig == null) {
                throw new ServletException("Poller Configuration file is empty");
            }
        } catch (Throwable e) {
            throw new ServletException(e.getMessage());
        }
        initPollerServices();
        initCapsdProtocols();
        this.redirectSuccess = config.getInitParameter("redirect.success");
        if (this.redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }
    }

    /**
     * <p>reloadFiles</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void reloadFiles() throws ServletException {
        ServletConfig config = this.getServletConfig();
        try {
            props.load(new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)));
            PollerConfigFactory.init();
            pollerFactory = PollerConfigFactory.getInstance();
            pollerConfig = pollerFactory.getConfiguration();

            if (pollerConfig == null) {
                throw new ServletException("Poller Configuration file is empty");
            }
            CapsdConfigFactory.init();
            capsdFactory = CapsdConfigFactory.getInstance();
            capsdConfig = capsdFactory.getConfiguration();

            if (capsdConfig == null) {
                throw new ServletException("Poller Configuration file is empty");
            }
        } catch (Throwable e) {
            throw new ServletException(e.getMessage());
        }
        initPollerServices();
        initCapsdProtocols();
        this.redirectSuccess = config.getInitParameter("redirect.success");
        if (this.redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }
    }

    /**
     * <p>initCapsdProtocols</p>
     */
    public void initCapsdProtocols() {
        pluginColl = capsdConfig.getProtocolPluginCollection();
        if (pluginColl != null) {
            for (ProtocolPlugin plugin : pluginColl) {
                capsdColl.add(plugin);
                capsdProtocols.put(plugin.getProtocol(), plugin);
            }
        }
    }

    /**
     * <p>initPollerServices</p>
     */
    public void initPollerServices() {
        Collection<org.opennms.netmgt.config.poller.Package> packageColl = pollerConfig.getPackageCollection();
        if (packageColl != null) {
            Iterator<org.opennms.netmgt.config.poller.Package> pkgiter = packageColl.iterator();
            if (pkgiter.hasNext()) {
                pkg = pkgiter.next();
                Collection<Service> svcColl = pkg.getServiceCollection();
                for (Service svcProp : svcColl) {
                    pollerServices.put(svcProp.getName(), svcProp);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        reloadFiles();

        String query = request.getQueryString();
        if (query != null) {
            java.util.List<String> checkedList = new ArrayList<String>();
            java.util.List<String> deleteList = new ArrayList<String>();

            props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null);
            StringTokenizer strTok = new StringTokenizer(query, "&");
            while (strTok.hasMoreTokens()) {
                String token = strTok.nextToken();
                if (token != null) {
                    StringTokenizer keyTokens = new StringTokenizer(token, "=");
                    String name = null;
                    if (keyTokens.hasMoreTokens()) {
                        name = (String) keyTokens.nextToken();
                    }
                    if (keyTokens.hasMoreTokens()) {
                        String checked = (String) keyTokens.nextToken();
                        if (name != null) {
                            if (name.indexOf("delete") == -1) // Not to be
                                                                // deleted
                            {
                                modifyPollerInfo(checked, name);
                                checkedList.add(name);
                            } else // Deleted
                            {
                                String deleteService = name.substring(0, name.indexOf("delete"));
                                deleteList.add(deleteService);
                            }
                        }
                    }
                }
            }
            adjustNonChecked(checkedList);
            deleteThese(deleteList);

            Writer poller_fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME)), "UTF-8");
            Writer capsd_fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME)), "UTF-8");
            try {
                Marshaller.marshal(pollerConfig, poller_fileWriter);
                Marshaller.marshal(capsdConfig, capsd_fileWriter);
            } catch (MarshalException e) {
                e.printStackTrace();
                throw new ServletException(e.getMessage());
            } catch (ValidationException e) {
                e.printStackTrace();
                throw new ServletException(e.getMessage());
            }
        }

        response.sendRedirect(this.redirectSuccess);
    }

    /**
     * <p>deleteCapsdInfo</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void deleteCapsdInfo(String name) {
        if (capsdProtocols.get(name) != null) {
            ProtocolPlugin tmpproto = capsdProtocols.get(name);
            capsdProtocols.remove(name);
            pluginColl = capsdProtocols.values();
            capsdColl.remove(tmpproto);
            capsdConfig.setProtocolPluginCollection(new ArrayList<ProtocolPlugin>(pluginColl));
        }
    }

    /**
     * <p>adjustNonChecked</p>
     *
     * @param checkedList a {@link java.util.List} object.
     */
    public void adjustNonChecked(java.util.List<String> checkedList) {
        if (pkg != null) {
            Collection<Service> svcColl = pkg.getServiceCollection();
            if (svcColl != null) {
                for (Service svc : svcColl) {
                    if (svc != null) {
                        if (!checkedList.contains(svc.getName())) {
                            if (svc.getStatus().equals("on")) {
                                svc.setStatus("off");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>deleteThese</p>
     *
     * @param deleteServices a {@link java.util.List} object.
     * @throws java.io.IOException if any.
     */
    public void deleteThese(java.util.List<String> deleteServices) throws IOException {
        for (String svcname : deleteServices) {
            if (pkg != null) {
                boolean flag = false;
                Collection<Service> svcColl = pkg.getServiceCollection();
                if (svcColl != null) {
                    for (Service svc : svcColl) {
                        if (svc != null) {
                            if (svc.getName().equals(svcname)) {
                                pkg.removeService(svc);
                                removeMonitor(svc.getName());
                                deleteCapsdInfo(svc.getName());
                                props.remove("service." + svc.getName() + ".protocol");
                                props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * <p>removeMonitor</p>
     *
     * @param service a {@link java.lang.String} object.
     */
    public void removeMonitor(String service) {
        // Add the new monitor with the protocol.
        Collection<Monitor> monitorColl = pollerConfig.getMonitorCollection();
        Monitor newMonitor = new Monitor();
        if (monitorColl != null) {
            for (Monitor mon : monitorColl) {
                if (mon != null) {
                    if (mon.getService().equals(service)) {
                        newMonitor.setService(service);
                        newMonitor.setClassName(mon.getClassName());
                        newMonitor.setParameterCollection(mon.getParameterCollection());
                        break;
                    }
                }
            }
            monitorColl.remove(newMonitor);
        }
    }

    /**
     * <p>modifyPollerInfo</p>
     *
     * @param bPolled a {@link java.lang.String} object.
     * @param protocol a {@link java.lang.String} object.
     */
    public void modifyPollerInfo(String bPolled, String protocol) {
        if (pkg != null) {
            Collection<Service> svcColl = pkg.getServiceCollection();
            if (svcColl != null) {
                for (Service svc : svcColl) {
                    if (svc != null) {
                        if (svc.getName().equals(protocol)) {
                            svc.setStatus(bPolled);
                            break;
                        }
                    }
                }
            }
        }
    }
}
