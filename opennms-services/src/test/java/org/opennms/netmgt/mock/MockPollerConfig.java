/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.config.BasicScheduleUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.dao.outages.impl.OverrideablePollOutagesDaoImpl;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.PollerConfiguration;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.config.poller.outages.Interface;
import org.opennms.netmgt.config.poller.outages.Node;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Time;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.ServiceMonitorLocator;
import org.opennms.netmgt.poller.ServiceMonitorRegistry;
import org.springframework.core.io.ByteArrayResource;

import com.google.common.collect.Maps;

public class MockPollerConfig extends OverrideablePollOutagesDaoImpl implements PollerConfig {

    private final MockServiceMonitorRegistry m_serviceMonitorRegistry = new MockServiceMonitorRegistry();

    private String m_criticalSvcName;

    private Package m_currentPkg = new Package();

    private boolean m_outageProcessingEnabled = false;

    private Vector<Package> m_pkgs = new Vector<>();

    private Map<String, ServiceMonitorLocator> m_svcMonitorLocators = new TreeMap<>();

    private int m_threads = 1;

    private long m_defaultPollInterval = 7654L;

    private boolean m_pollAll = true;

    private boolean m_pathOutageEnabled = false;

    private boolean m_serviceUnresponsiveEnabled = false;

    private String m_nextOutageIdSql;

    private Service m_currentSvc;

    private MockNetwork m_network;

    private Map<Package, List<String>> m_rraLists = Maps.newHashMap();

    public MockPollerConfig(final MockNetwork network) {
        m_network = network;
        try {
            overrideConfig(new ByteArrayResource("<outages></outages>".getBytes()).getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <p>parameters</p>
     *
     * @param svc a {@link org.opennms.netmgt.config.poller.Service} object.
     * @return a {@link java.lang.Iterable} object.
     */
    @Override
    public Iterable<Parameter> parameters(final Service svc) {
        getReadLock().lock();
        try {
            return svc.getParameters();
        } finally {
            getReadLock().unlock();
        }
    }

    public void addDowntime(long interval, long begin, long end, boolean delete) {
        this.addDowntime(interval, begin, end, delete ? "true" : "false");
    }

    public void addDowntime(long interval, long begin, long end, String delete) {
        Downtime downtime = new Downtime();
        downtime.setDelete(delete);
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
    
        withWriteLock(outages -> outages.addOutage(outage));
    
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

        withWriteLock(outages -> outages.addOutage(outage));

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

        withWriteLock(outages -> outages.addOutage(outage));

        pkg.addOutageCalendar(outageName);
    }

    public void addScheduledOutage(String outageName, String dayOfWeek, String beginTime, String endTime, String ipAddr) {
        addScheduledOutage(m_currentPkg, outageName, dayOfWeek, beginTime, endTime, ipAddr);
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
        if (m_svcMonitorLocators.containsKey(name)) {
            return;
        }

        final var key = String.format("%s:%s", monitor.getClass().getCanonicalName(), name);

        m_serviceMonitorRegistry.monitors.put(key, monitor);

        m_svcMonitorLocators.put(name, new ServiceMonitorLocator() {
            @Override
            public String getServiceName() {
                return name;
            }

            @Override
            public String getServiceLocatorKey() {
                return key;
            }

            @Override
            public ServiceMonitor getServiceMonitor(final ServiceMonitorRegistry registry) {
                return registry.getMonitorByClassName(this.getServiceLocatorKey());
            }
        });
    }

    public void addService(MockService svc) {
        addService(svc.getSvcName(), m_defaultPollInterval, new MockMonitor(svc.getNetwork(), svc.getSvcName()));
        m_currentPkg.addSpecific(svc.getIpAddr());
    }

    public void clearDowntime() {
        final List<Downtime> emptyList = Collections.emptyList();
        m_currentPkg.setDowntimes(emptyList);;
    }

    public void addPackage(String name) {
        m_currentPkg = new Package();
        m_currentPkg.setName(name);

        m_pkgs.add(m_currentPkg);
    }

    @Override
    public Enumeration<Package> enumeratePackage() {
        return m_pkgs.elements();
    }

    @Override
    public List<Package> getPackages() {
        return m_pkgs;
    }

    private Service findService(Package pkg, String svcName) {
        for (Service svc : pkg.getServices()) {
            if (svcName.equals(svc.getName())) {
                return svc;
            }
        }
        return null;
    }

    @Override
    public String getCriticalService() {
        return m_criticalSvcName;
    }

    @Override
    public String getNextOutageIdSql() {
        return m_nextOutageIdSql;
    }

    @Override
    public Package getPackage(String name) {
        for (Package pkg : m_pkgs) {
            if (pkg.getName().equals(name)) {
                return pkg;
            }
        }
        return null;
    }

    public void setRRAList(Package pkg, List<String> rraList) {
        m_rraLists.put(pkg, rraList);
    }

    @Override
    public List<String> getRRAList(Package pkg) {
        return m_rraLists.get(pkg);
    }

    public ServiceMonitor getServiceMonitor(String svcName) {
        return Objects.requireNonNull(m_svcMonitorLocators.get(svcName).getServiceMonitor(m_serviceMonitorRegistry));
    }

    @Override
    public Optional<ServiceMonitorLocator> getServiceMonitorLocator(final String svcName) {
        return Optional.ofNullable(m_svcMonitorLocators.get(svcName));
    }

    @Override
    public Set<String> getServiceMonitorNames() {
        return null;
    }

    @Override
    public int getStep(Package pkg) {
        return 300;
    }

    @Override
    public int getThreads() {
        return m_threads;
    }

    /**
     * @param svcName
     * @return
     */
    public boolean hasServiceMonitor(final String svcName) {
        return getServiceMonitor(svcName) != null;
    }

    @Override
    public boolean isInterfaceInPackage(final String iface, final Package pkg) {
        for (final String ipAddr : pkg.getSpecifics()) {
            if (ipAddr.equals(iface))
                return true;
        }
        return false;
    }

    @Override
    public boolean isPolled(final String ipaddr) {
        return true;
    }

    @Override
    public boolean isPolled(final String svcName, final Package pkg) {
        return true;
    }

    @Override
    public boolean isPolled(final String ipaddr, final String svcName) {
        return true;
    }

    @Override
    public boolean isNodeOutageProcessingEnabled() {
        return m_outageProcessingEnabled;
    }

    @Override
    public boolean shouldPollAllIfNoCriticalServiceDefined() {
        // TODO Auto-generated method stub
        return m_pollAll ;
    }

    public void setPollAllIfNoCriticalServiceDefined(final boolean pollAll) {
        m_pollAll = pollAll;
    }

    @Override
    public void rebuildPackageIpListMap() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isServiceInPackageAndEnabled(final String svcName, final Package pkg) {
        for (final Service svc : pkg.getServices()) {
            if (svc.getName().equals(svcName))
                return true;
        }
        return false;
    }

    @Override
    public boolean isServiceMonitored(final String svcName) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }

    @Override
    public boolean isAsyncEngineEnabled() {
        return false;
    }

    @Override
    public int getMaxConcurrentAsyncPolls() {
        return 0;
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

    public void populatePackage(final MockNetwork network, MockService... exclude) {
        final List<MockService> servicesToExclude = Arrays.asList(exclude);
        final MockVisitor populator = new MockVisitorAdapter() {
            @Override
            public void visitService(final MockService svc) {
                if (servicesToExclude.contains(svc)) {
                    return;
                }
                addService(svc);
            }
        };
        network.visit(populator);
    }

    protected void saveXML(final String xmlString) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public Service getServiceInPackage(final String svcName, final Package pkg) {
        return findService(pkg, svcName);
    }

    @Override
    public void update() {

    }

    @Override
    public void save() {

    }

    public void addParameter(final String key, final String value) {
        final Parameter param = new Parameter();
        param.setKey(key);
        param.setValue(value);
        m_currentSvc.addParameter(param);
    }

    public void reset() {
        this.m_pkgs.clear();
    }

    @Override
    public void addPackage(final Package pkg) {
        m_pkgs.add(pkg);
    }

    @Override
    public PollerConfiguration getLocalConfiguration() {
        // FIXME: need to actually implement this
        return null;
    }

    @Override
    public List<String> getAllPackageMatches(final String ipAddr) {
        return new ArrayList<String>(0);
    }

    @Override
    public boolean isPathOutageEnabled() {
        return m_pathOutageEnabled;
    }

    public void setPathOutageEnabled(boolean pathOutageEnabled) {
        m_pathOutageEnabled = pathOutageEnabled;
    }

    @Override
    public int getDefaultCriticalPathRetries() {
        return 0;
    }

    @Override
    public InetAddress getDefaultCriticalPathIp() {
        return null;
    }

    @Override
    public int getDefaultCriticalPathTimeout() {
        return 1500;
    }

    @Override
    public List<InetAddress> getIpList(final Package pkg) {
        return Collections.emptyList();
    }

    public void saveResponseTimeData(final String locationMonitor, final OnmsMonitoredService monSvc, final double responseTime, final Package pkg) {
        throw new UnsupportedOperationException("not yet implemented");

    }

    @Override
    public Collection<ServiceMonitorLocator> getServiceMonitorLocators() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Package getFirstLocalPackageMatch(final String ipaddr) {
        throw new UnsupportedOperationException("MockPollerConfig.getFirstLocalPackageMatch is not yet implemented");
    }

    @Override
    public boolean isPolledLocally(final String ipaddr) {
        throw new UnsupportedOperationException("MockPollerConfig.isPolledLocally is not yet implemented");
    }

    @Override
    public boolean isPolledLocally(final String ipaddr, final String svcName) {
        throw new UnsupportedOperationException("MockPollerConfig.isPolledLocally is not yet implemented");
    }

    @Override
    public ServiceMonitorRegistry getServiceMonitorRegistry() {
        return m_serviceMonitorRegistry;
    }

    @Override
    public void setExternalData(final List<Package> externalPackages, final List<Monitor> externalMonitors) {
        throw new UnsupportedOperationException("MockPollerConfig.setExternalData is not yet implemented");
    }

    private  static class MockServiceMonitorRegistry implements ServiceMonitorRegistry {

        public final Map<String, ServiceMonitor> monitors = Maps.newHashMap();

        @Override
        public ServiceMonitor getMonitorByClassName(final String className) {
            return this.monitors.get(className);
        }

        @Override
        public Set<String> getMonitorClassNames() {
            return this.monitors.keySet();
        }

        @SuppressWarnings({ "rawtypes" })
        public synchronized void onBind(ServiceMonitor serviceMonitor, Map properties) { }

        @SuppressWarnings({ "rawtypes" })
        public synchronized void onUnbind(ServiceMonitor serviceMonitor, Map properties) { }
    }
}
