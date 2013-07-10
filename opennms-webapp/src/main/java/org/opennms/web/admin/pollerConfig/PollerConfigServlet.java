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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet that handles managing or unmanaging interfaces and services on a
 * node
 *
 * @author <A HREF="mailto:jacinta@opennms.org">Jacinta Remedios </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class PollerConfigServlet extends HttpServlet {
	
	private static final Logger LOG = LoggerFactory.getLogger(PollerConfigServlet.class);

    /**
     * 
     */
    private static final long serialVersionUID = -4273370522387271772L;

    private PollerConfiguration m_pollerConfig = null;

    private CapsdConfiguration m_capsdConfig = null;

    protected String m_redirectSuccess;

    private Map<String, Service> m_pollerServices = new HashMap<String, Service>();

    private Map<String, ProtocolPlugin> m_capsdProtocols = new HashMap<String, ProtocolPlugin>();

    private List<ProtocolPlugin> m_capsdColl = new ArrayList<ProtocolPlugin>();

    private org.opennms.netmgt.config.poller.Package m_pkg = null;

    private List<ProtocolPlugin> m_pluginColl = null;

    private Properties m_props = new Properties();

    private PollerConfig m_pollerFactory = null;

    private CapsdConfig m_capsdFactory = null;

    /**
     * <p>init</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    @Override
    public void init() throws ServletException {
        getInitParameters();

        loadPollerConfProperties();
        
        initPollerConfigFactory();
        
        initCapsdConfigFactory();

        initPollerServices();
        initCapsdProtocols();
    }

    private void getInitParameters() throws ServletException {
        ServletConfig config = getServletConfig();
        m_redirectSuccess = config.getInitParameter("redirect.success");
        if (m_redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }
    }

    private void initCapsdConfigFactory() throws ServletException {
        try {
            CapsdConfigFactory.init();
        } catch (Throwable e) {
            throw new ServletException(e);
        }
        m_capsdFactory = CapsdConfigFactory.getInstance();
        m_capsdConfig = m_capsdFactory.getConfiguration();
        if (m_capsdConfig == null) {
            throw new ServletException("Capsd Configuration file is empty");
        }
    }

    private void initPollerConfigFactory() throws ServletException {
        try {
            PollerConfigFactory.init();
        } catch (Throwable e) {
            throw new ServletException(e);
        }
        m_pollerFactory = PollerConfigFactory.getInstance();
        m_pollerConfig = m_pollerFactory.getConfiguration();
        if (m_pollerConfig == null) {
            throw new ServletException("Poller Configuration file is empty");
        }
    }

    private void loadPollerConfProperties() throws ServletException {
        try {
            m_props.load(new FileInputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)));
        } catch (Throwable e) {
            throw new ServletException(e);
        }
        
        //String[] protocols = BundleLists.parseBundleList(m_props.getProperty("services"));
    }

    /**
     * <p>reloadFiles</p>
     *
     * @throws javax.servlet.ServletException if any.
     */
    public void reloadFiles() throws ServletException {
        ServletConfig config = getServletConfig();
        try {
            loadPollerConfProperties();
            initPollerConfigFactory();
            initCapsdConfigFactory();
        } catch (Throwable e) {
            throw new ServletException(e);
        }
        initPollerServices();
        initCapsdProtocols();
        m_redirectSuccess = config.getInitParameter("redirect.success");
        if (m_redirectSuccess == null) {
            throw new ServletException("Missing required init parameter: redirect.success");
        }
    }

    /**
     * <p>initCapsdProtocols</p>
     */
    public void initCapsdProtocols() {
        m_pluginColl = getCapsdProtocolPlugins();
        if (m_pluginColl != null) {
            for (ProtocolPlugin plugin : m_pluginColl) {
                m_capsdColl.add(plugin);
                m_capsdProtocols.put(plugin.getProtocol(), plugin);
            }
        }
    }

    private List<ProtocolPlugin> getCapsdProtocolPlugins() {
        return (List<ProtocolPlugin>) m_capsdConfig.getProtocolPluginCollection();
    }

    /**
     * <p>initPollerServices</p>
     */
    public void initPollerServices() {
        Collection<org.opennms.netmgt.config.poller.Package> packageColl = m_pollerConfig.getPackageCollection();
        if (packageColl != null) {
            Iterator<Package> pkgiter = packageColl.iterator();
            if (pkgiter.hasNext()) {
                m_pkg = pkgiter.next();
                Collection<Service> svcColl = m_pkg.getServiceCollection();
                for (Service svcProp : svcColl) {
                    m_pollerServices.put(svcProp.getName(), svcProp);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        reloadFiles();

        List<String> checkedList = new ArrayList<String>();
        List<String> deleteList = new ArrayList<String>();

        m_props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null);

        String[] requestActivate = request.getParameterValues("activate");
        String[] requestDelete = request.getParameterValues("delete");

        if (requestActivate != null) {
            for (int i = 0; i < requestActivate.length; i++) {
                modifyPollerInfo("on", requestActivate[i]);
                checkedList.add(requestActivate[i]);
            }
        }

        if (requestDelete != null) {
            for (int j = 0; j < requestDelete.length; j++) {
                deleteList.add(requestDelete[j]);
            }
        }
        adjustNonChecked(checkedList);
        deleteThese(deleteList);

        Writer poller_fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONFIG_FILE_NAME)), "UTF-8");
        Writer capsd_fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.CAPSD_CONFIG_FILE_NAME)), "UTF-8");
        try {
            Marshaller.marshal(m_pollerConfig, poller_fileWriter);
            Marshaller.marshal(m_capsdConfig, capsd_fileWriter);
        } catch (MarshalException e) {
            LOG.error("Could not marshal config object when writing config file: {}", e, e);
            throw new ServletException(e);
        } catch (ValidationException e) {
            LOG.error("Could not validate config object when writing config file: {}", e, e);
            throw new ServletException(e);
        }

        String redirectPage = request.getParameter("redirect");
        if (redirectPage == null) {
            redirectPage = m_redirectSuccess;
        }
        response.sendRedirect(redirectPage);
    }

    /**
     * <p>deleteCapsdInfo</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void deleteCapsdInfo(String name) {
        if (m_capsdProtocols.get(name) != null) {
            ProtocolPlugin tmpproto = m_capsdProtocols.get(name);
            m_capsdProtocols.remove(name);
            m_pluginColl = new ArrayList<ProtocolPlugin>(m_capsdProtocols.values());
            m_capsdColl.remove(tmpproto);
            m_capsdConfig.setProtocolPlugin(m_pluginColl.toArray(new ProtocolPlugin[0]));
        }
    }

    /**
     * <p>adjustNonChecked</p>
     *
     * @param checkedList a {@link java.util.List} object.
     */
    public void adjustNonChecked(List<String> checkedList) {
        if (m_pkg != null) {
            Collection<Service> svcColl = m_pkg.getServiceCollection();
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
    public void deleteThese(List<String> deleteServices) throws IOException {
        for (String svcname : deleteServices) {
            if (m_pkg != null) {
                Collection<Service> svcColl = m_pkg.getServiceCollection();
                if (svcColl != null) {
                    for (Service svc : svcColl) {
                        if (svc != null) {
                            if (svc.getName().equals(svcname)) {
                                m_pkg.removeService(svc);
                                LOG.info("Package removed {}", svc.getName());
                                removeMonitor(svc.getName());
                                deleteCapsdInfo(svc.getName());
                                m_props.remove("service." + svc.getName() + ".protocol");
                                m_props.store(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.POLLER_CONF_FILE_NAME)), null);
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
        Collection<Monitor> monitorColl = m_pollerConfig.getMonitorCollection();
        Monitor newMonitor = new Monitor();
        if (monitorColl != null) {
            for (Monitor mon : monitorColl) {
                if (mon != null) {
                    if (mon.getService().equals(service)) {
                        newMonitor.setService(service);
                        newMonitor.setClassName(mon.getClassName());
                        newMonitor.setParameter(mon.getParameterCollection());
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
        if (m_pkg != null) {
            Collection<Service> svcColl = m_pkg.getServiceCollection();
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

    /**
     * @return logger for this servlet
     */
    
}
