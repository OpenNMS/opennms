/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.admin.config;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.PollerConfigFactory;
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

    protected String redirectSuccess;

    Map<String, Service> pollerServices = new HashMap<String, Service>();

    org.opennms.netmgt.config.poller.Package pkg = null;

    PollerConfig pollerFactory = null;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        ServletConfig config = this.getServletConfig();
        try {
            PollerConfigFactory.init();
            pollerFactory = PollerConfigFactory.getInstance();
            pollerConfig = pollerFactory.getConfiguration();

            if (pollerConfig == null) {
                throw new ServletException("Poller Configuration file is empty");
            }

        } catch (Throwable e) {
            throw new ServletException(e.getMessage());
        }
        initPollerServices();
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
            PollerConfigFactory.init();
            pollerFactory = PollerConfigFactory.getInstance();
            pollerConfig = pollerFactory.getConfiguration();

            if (pollerConfig == null) {
                throw new ServletException("Poller Configuration file is empty");
            }

        } catch (Throwable e) {
            throw new ServletException(e.getMessage());
        }
        initPollerServices();
        this.redirectSuccess = config.getInitParameter("redirect.success");
        if (this.redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }
    }

    /**
     * <p>initPollerServices</p>
     */
    public void initPollerServices() {
        Collection<org.opennms.netmgt.config.poller.Package> packageColl = pollerConfig.getPackages();
        if (packageColl != null) {
            Iterator<org.opennms.netmgt.config.poller.Package> pkgiter = packageColl.iterator();
            if (pkgiter.hasNext()) {
                pkg = pkgiter.next();
                Collection<Service> svcColl = pkg.getServices();
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
            java.util.List<String> checkedList = new ArrayList<>();
            java.util.List<String> deleteList = new ArrayList<>();

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

            try(Writer poller_fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME)), StandardCharsets.UTF_8)) {
                JaxbUtils.marshal(pollerConfig, poller_fileWriter);
            }
        }

        response.sendRedirect(this.redirectSuccess);
    }

    /**
     * <p>adjustNonChecked</p>
     *
     * @param checkedList a {@link java.util.List} object.
     */
    public void adjustNonChecked(java.util.List<String> checkedList) {
        if (pkg != null) {
            Collection<Service> svcColl = pkg.getServices();
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
                Collection<Service> svcColl = pkg.getServices();
                if (svcColl != null) {
                    for (Service svc : svcColl) {
                        if (svc != null) {
                            if (svc.getName().equals(svcname)) {
                                pkg.removeService(svc);
                                removeMonitor(svc.getName());
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
     * FIXME: I think that this should be using Iterator.remove()
     *
     * @param service a {@link java.lang.String} object.
     */
    public void removeMonitor(String service) {
        // Add the new monitor with the protocol.
        Collection<Monitor> monitorColl = pollerConfig.getMonitors();
        Monitor newMonitor = new Monitor();
        if (monitorColl != null) {
            for (Monitor mon : monitorColl) {
                if (mon != null) {
                    if (mon.getService().equals(service)) {
                        newMonitor.setService(service);
                        newMonitor.setClassName(mon.getClassName());
                        newMonitor.setParameters(mon.getParameters());
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
            Collection<Service> svcColl = pkg.getServices();
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
