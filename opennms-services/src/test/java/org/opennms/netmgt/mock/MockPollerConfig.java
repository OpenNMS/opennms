/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.BasicScheduleUtils;
import org.opennms.netmgt.config.PollOutagesConfigManager;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Interface;
import org.opennms.netmgt.config.poller.Node;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.config.poller.Time;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.springframework.core.io.ByteArrayResource;

public class MockPollerConfig extends PollOutagesConfigManager implements PollerConfig {

    private String m_criticalSvcName;

    private Package m_currentPkg = new Package();

    private boolean m_outageProcessingEnabled = false;

    private Vector<Package> m_pkgs = new Vector<Package>();

    private Map<String, ServiceMonitor> m_svcMonitors = new TreeMap<String, ServiceMonitor>();

    private int m_threads = 1;

    private long m_defaultPollInterval = 7654L;

    private boolean m_pollAll = true;

    private boolean m_serviceUnresponsiveEnabled = false;

    private String m_nextOutageIdSql;

    private Service m_currentSvc;

    private MockNetwork m_network;

    public MockPollerConfig(final MockNetwork network) {
        m_network = network;
        setConfigResource(new ByteArrayResource("<outages></outages>".getBytes()));
        afterPropertiesSet();
    }

    /**
     * <p>parameters</p>
     *
     * @param svc a {@link org.opennms.netmgt.config.poller.Service} object.
     * @return a {@link java.lang.Iterable} object.
     */
    public Iterable<Parameter> parameters(final Service svc) {
        getReadLock().lock();
        try {
            return svc.getParameterCollection();
        } finally {
            getReadLock().unlock();
        }
    }

    public void addDowntime(long interval, long begin, long end, boolean delete) {
        Downtime downtime = new Downtime();
        downtime.setDelete(delete ? "true" : "false");
        downtime.setBegin(begin);
        downtime.setInterval(interval);
        if (end >= 0)
            downtime.setEnd(end);
        m_currentPkg.addDowntime(downtime);
    }
    
    /**
     * Adds a scehduled outage to pkg from begin to end, for the nodeid
     * @param pkg - the package to which  
     * @param outageName - a name, arbitrary
     * @param begin - time, in seconds since epoch, when the outage starts
     * @param end - time, in seconds since the epoch, when the outage ends
     * @param nodeid - the node the outage applies to
     */
    public void addScheduledOutage(Package pkg, String outageName, long begin, long end, int nodeid) {

        Outage outage = new Outage();
        outage.setName(outageName);
    
        Node node=new Node();
        node.setId(nodeid);
        outage.addNode(node);
    
        Time time = new Time();
        Date beginDate = new Date(begin);
        Date endDate = new Date(end);
        time.setBegins(new SimpleDateFormat(BasicScheduleUtils.FORMAT1).format(beginDate));
        time.setEnds(new SimpleDateFormat(BasicScheduleUtils.FORMAT1).format(endDate));
    
        outage.addTime(time);
    
        getConfig().addOutage(outage);
    
        pkg.addOutageCalendar(outageName);
    }
    
    /**
     * Adds a scehduled outage from begin to end, for the nodeid 
     * @param outageName - a name, arbitrary
     * @param begin - time, in seconds since epoch, when the outage starts
     * @param end - time, in seconds since the epoch, when the outage ends
     * @param nodeid - the node the outage applies to
     */
    public void addScheduledOutage(String outageName, long begin, long end, int nodeid) {
        addScheduledOutage(m_currentPkg, outageName, begin, end, nodeid);
    }
    
    
    public void addScheduledOutage(Package pkg, String outageName, long begin, long end, String ipAddr) {
        Outage outage = new Outage();
        outage.setName(outageName);

        Interface iface = new Interface();
        iface.setAddress(ipAddr);

        outage.addInterface(iface);

        Time time = new Time();
        Date beginDate = new Date(begin);
        Date endDate = new Date(end);
        time.setBegins(new SimpleDateFormat(BasicScheduleUtils.FORMAT1).format(beginDate));
        time.setEnds(new SimpleDateFormat(BasicScheduleUtils.FORMAT1).format(endDate));

        outage.addTime(time);

        getConfig().addOutage(outage);

        pkg.addOutageCalendar(outageName);

    }

    public void addScheduledOutage(String outageName, long begin, long end, String ipAddr) {
        addScheduledOutage(m_currentPkg, outageName, begin, end, ipAddr);
    }

    public void addScheduledOutage(Package pkg, String outageName, String dayOfWeek, String beginTime, String endTime, String ipAddr) {
        Outage outage = new Outage();
        outage.setName(outageName);
        outage.setType("weekly");

        Interface iface = new Interface();
        iface.setAddress(ipAddr);

        outage.addInterface(iface);

        Time time = new Time();
        time.setDay(dayOfWeek);
        time.setBegins(beginTime);
        time.setEnds(endTime);

        outage.addTime(time);

        getConfig().addOutage(outage);

        pkg.addOutageCalendar(outageName);
    }

    public void addScheduledOutage(String outageName, String dayOfWeek, String beginTime, String endTime, String ipAddr) {
        addScheduledOutage(m_currentPkg, outageName, dayOfWeek, beginTime, endTime, ipAddr);
    }


    public void addService(String name, ServiceMonitor monitor) {
        addService(name, m_defaultPollInterval, monitor);
    }

    public void addService(String name, long interval, ServiceMonitor monitor) {
        Service service = findService(m_currentPkg, name);
        if (service == null) {
            service = new Service();
            service.setName(name);
            service.setInterval(interval);
            m_currentPkg.addService(service);
            m_currentSvc = service;
        }
        addServiceMonitor(name, monitor);
    }

    private void addServiceMonitor(String name, ServiceMonitor monitor) {
        if (!hasServiceMonitor(name))
            m_svcMonitors.put(name, monitor);
    }

    public void addService(MockService svc) {
        addService(svc.getSvcName(), m_defaultPollInterval, new MockMonitor(svc.getNetwork(), svc.getSvcName()));
        m_currentPkg.addSpecific(svc.getIpAddr());
    }

    public void clearDowntime() {
        m_currentPkg.removeAllDowntime();
    }

    public void addPackage(String name) {
        m_currentPkg = new Package();
        m_currentPkg.setName(name);

        m_pkgs.add(m_currentPkg);
    }

    public void addMonitor(String svcName, String className) {
        addServiceMonitor(svcName, new MockMonitor(m_network, svcName));

    }

    public Enumeration<Package> enumeratePackage() {
        return m_pkgs.elements();
    }

    private Service findService(Package pkg, String svcName) {
        for (Service svc : pkg.getServiceCollection()) {
            if (svcName.equals(svc.getName())) {
                return svc;
            }
        }
        return null;
    }

    public String getCriticalService() {
        return m_criticalSvcName;
    }

    public Package getFirstPackageMatch(String ipaddr) {
        return null;
    }

    public String getNextOutageIdSql() {
        return m_nextOutageIdSql;
    }

    public Package getPackage(String name) {
        for (Package pkg : m_pkgs) {
            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }

    public List<String> getRRAList(Package pkg) {
        return null;
    }

    public ServiceMonitor getServiceMonitor(String svcName) {
        return getServiceMonitors().get(svcName);
    }

    public Map<String, ServiceMonitor> getServiceMonitors() {
        return m_svcMonitors;
    }

    public int getStep(Package pkg) {
        return 0;
    }

    public int getThreads() {
        return m_threads;
    }

    public boolean shouldNotifyXmlrpc() {
        return false;
    }

    /**
     * @param svcName
     * @return
     */
    public boolean hasServiceMonitor(final String svcName) {
        return getServiceMonitor(svcName) != null;
    }

    public boolean isInterfaceInPackage(final String iface, final Package pkg) {
        for (final String ipAddr : pkg.getSpecificCollection()) {
            if (ipAddr.equals(iface))
                return true;
        }
        return false;
    }

    public boolean isPolled(final String ipaddr) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPolled(final String svcName, final Package pkg) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPolled(final String ipaddr, final String svcName) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isNodeOutageProcessingEnabled() {
        return m_outageProcessingEnabled;
    }

    public boolean shouldPollAllIfNoCriticalServiceDefined() {
        // TODO Auto-generated method stub
        return m_pollAll ;
    }

    public void setPollAllIfNoCriticalServiceDefined(final boolean pollAll) {
        m_pollAll = pollAll;
    }

    public void rebuildPackageIpListMap() {
        // TODO Auto-generated method stub

    }

    public boolean isServiceInPackageAndEnabled(final String svcName, final Package pkg) {
        for (final Service svc : pkg.getServiceCollection()) {
            if (svc.getName().equals(svcName))
                return true;
        }
        return false;
    }

    public boolean isServiceMonitored(final String svcName) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }

    public void setNextOutageIdSql(final String nextOutageIdSql) {
        m_nextOutageIdSql = nextOutageIdSql;
    }

    public void setServiceUnresponsiveEnabled(final boolean serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    public void setCriticalService(final String criticalSvcName) {
        m_criticalSvcName = criticalSvcName;
    }

    public void setInterfaceMatch(final String matchRegexp) {
        m_currentPkg.addIncludeUrl(matchRegexp);
    }


    public void setNodeOutageProcessingEnabled(final boolean outageProcessingEnabled) {
        m_outageProcessingEnabled = outageProcessingEnabled;
    }

    public void setPollInterval(final String svcName, final long interval) {
        setPollInterval(m_currentPkg, svcName, interval);
    }

    public void setPollInterval(final Package pkg, final String svcName, final long interval) {
        final Service svc = findService(pkg, svcName);
        if (svc == null)
            throw new IllegalArgumentException("No service named: "+svcName+" in package "+pkg);

        svc.setInterval(interval);
    }

    public void setPollerThreads(final int threads) {
        m_threads = threads;
    }

    public void setDefaultPollInterval(final long defaultPollInterval) {
        m_defaultPollInterval = defaultPollInterval;
    }

    public void populatePackage(final MockNetwork network) {
        final MockVisitor populator = new MockVisitorAdapter() {
            public void visitService(final MockService svc) {
                addService(svc);
            }
        };
        network.visit(populator);
    }

    protected void saveXML(final String xmlString) throws IOException, MarshalException, ValidationException {
        // TODO Auto-generated method stub

    }

    public Service getServiceInPackage(final String svcName, final Package pkg) {
        return findService(pkg, svcName);
    }

    public void update() {

    }

    public void save() {

    }

    public void addParameter(final String key, final String value) {
        final Parameter param = new Parameter();
        param.setKey(key);
        param.setValue(value);
        m_currentSvc.addParameter(param);
    }

    public void addPackage(final Package pkg) {
        m_pkgs.add(pkg);
    }

    public PollerConfiguration getConfiguration() {
        // FIXME: need to actually implement this
        return null;
    }

    public List<String> getAllPackageMatches(final String ipAddr) {
        return new ArrayList<String>(0);
    }

    public boolean isPathOutageEnabled() {
        return false;
    }

    public void releaseAllServiceMonitors() {
    }

    public List<InetAddress> getIpList(final Package pkg) {
        return Collections.emptyList();
    }

    public ServiceSelector getServiceSelectorForPackage(final Package pkg) {
        return null;
    }

    public void saveResponseTimeData(final String locationMonitor, final OnmsMonitoredService monSvc, final double responseTime, final Package pkg) {
        throw new UnsupportedOperationException("not yet implemented");

    }

    public Collection<ServiceMonitorLocator> getServiceMonitorLocators(final DistributionContext context) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public Package getFirstLocalPackageMatch(final String ipaddr) {
        throw new UnsupportedOperationException("MockPollerConfig.getFirstLocalPackageMatch is not yet implemented");
    }

    public boolean isPolledLocally(final String ipaddr) {
        throw new UnsupportedOperationException("MockPollerConfig.isPolledLocally is not yet implemented");
    }

    public boolean isPolledLocally(final String ipaddr, final String svcName) {
        throw new UnsupportedOperationException("MockPollerConfig.isPolledLocally is not yet implemented");
    }

 


}
