//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.mock;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Interface;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.config.poller.Time;
import org.opennms.netmgt.poller.monitors.ServiceMonitor;

public class MockPollerConfig implements PollerConfig, PollOutagesConfig {

    private String m_criticalSvcName;

    private Package m_currentPkg = new Package();

    private boolean m_outageProcessingEnabled = false;

    private List m_outages = new ArrayList();

    private Vector m_pkgs = new Vector();

    private Map m_svcMonitors = new TreeMap();

    private int m_threads = 1;

    private long m_defaultPollInterval = 7654L;

    private boolean m_pollAll = true;

    private boolean m_serviceUnresponsiveEnabled = false;

    private String m_nextOutageIdSql;

    public void addDowntime(long interval, long begin, long end, boolean delete) {
        Downtime downtime = new Downtime();
        downtime.setDelete(delete ? "true" : "false");
        downtime.setBegin(begin);
        downtime.setInterval(interval);
        if (end >= 0)
            downtime.setEnd(end);
        m_currentPkg.addDowntime(downtime);
    }

    public void addScheduledOutage(Package pkg, String outageName, long begin, long end, String ipAddr) {
        Outage outage = new Outage();
        outage.setName(outageName);

        Interface iface = new Interface();
        iface.setAddress(ipAddr);

        outage.addInterface(iface);

        Time time = new Time();
        time.setBegins(String.valueOf(begin));
        time.setEnds(String.valueOf(end));

        outage.addTime(time);

        m_outages.add(outage);

        pkg.addOutageCalendar(outageName);

    }
    
    public void addScheduledOutage(String outageName, long begin, long end, String ipAddr) {
        addScheduledOutage(m_currentPkg, outageName, begin, end, ipAddr);
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
        }
        addServiceMonitor(name, monitor);
    }
    
    private void addServiceMonitor(String name, ServiceMonitor monitor) {
        if (!hasServiceMonitor(name))
            m_svcMonitors.put(name, monitor);
    }

    public void addService(MockService svc) {
        addService(svc.getName(), m_defaultPollInterval, new MockMonitor(svc.getNetwork(), svc.getName()));
        m_currentPkg.addSpecific(svc.getIpAddr());
    }

    public void clearDowntime() {
        m_currentPkg.clearDowntime();
    }

    public void addPackage(String name) {
        m_currentPkg = new Package();
        m_currentPkg.setName(name);

        m_pkgs.add(m_currentPkg);
    }

    public Enumeration enumeratePackage() {
        return m_pkgs.elements();
    }

    private Outage findOutage(String name) {
        Iterator it = m_outages.iterator();
        while (it.hasNext()) {
            Outage outage = (Outage) it.next();
            if (outage.getName().equals(name)) {
                return outage;
            }
        }
        return null;
    }

    private Service findService(Package pkg, String svcName) {
        Enumeration svcs = pkg.enumerateService();
        while (svcs.hasMoreElements()) {
            Service svc = (Service) svcs.nextElement();
            if (svcName.equals(svc.getName()))
                return svc;
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
        for (int i = 0; i < m_pkgs.size(); i++) {
            Package pkg = (Package) m_pkgs.get(i);
            if (pkg.getName().equals(name))
                return pkg;
        }
        return null;

    }

    public List getRRAList(Package pkg) {
        return null;
    }

    public ServiceMonitor getServiceMonitor(String svcName) {
        return (ServiceMonitor) getServiceMonitors().get(svcName);
    }

    public Map getServiceMonitors() {
        return m_svcMonitors;
    }

    public int getStep(Package pkg) {
        return 0;
    }

    public int getThreads() {
        return m_threads;
    }

    public boolean getXmlrpc() {
        return false;
    }

    /**
     * @param svcName
     * @return
     */
    public boolean hasServiceMonitor(String svcName) {
        return getServiceMonitor(svcName) != null;
    }

    public boolean interfaceInPackage(String iface, Package pkg) {
        Enumeration en = pkg.enumerateSpecific();
        while (en.hasMoreElements()) {
            String ipAddr = (String)en.nextElement();
            if (ipAddr.equals(iface))
                return true;
        }
        return false;
    }

    public boolean isCurTimeInOutage(String outName) {
        Outage outage = findOutage(outName);
        if (outage == null)
            return false;

        long now = System.currentTimeMillis();
        Iterator it = outage.getTimeCollection().iterator();
        while (it.hasNext()) {
            Time time = (Time) it.next();
            long begin = Long.parseLong(time.getBegins());
            long end = Long.parseLong(time.getEnds());

            if (begin < now && now < end) {
                return true;
            }
        }

        return false;
    }

    public boolean isInterfaceInOutage(String ipAddr, String outName) {
        Outage outage = findOutage(outName);
        if (outage == null)
            return false;

        Iterator it = outage.getInterfaceCollection().iterator();
        while (it.hasNext()) {
            Interface iface = (Interface) it.next();
            if (iface.getAddress().equals(ipAddr)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPolled(String ipaddr) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPolled(String svcName, Package pkg) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isPolled(String ipaddr, String svcName) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean nodeOutageProcessingEnabled() {
        return m_outageProcessingEnabled;
    }

    public boolean pollAllIfNoCriticalServiceDefined() {
        // TODO Auto-generated method stub
        return m_pollAll ;
    }
    
    public void setPollAllIfNoCriticalServiceDefined(boolean pollAll) {
        m_pollAll = pollAll;
    }

    public void rebuildPackageIpListMap() {
        // TODO Auto-generated method stub

    }

    public boolean serviceInPackageAndEnabled(String svcName, Package pkg) {
        Enumeration en = pkg.enumerateService();
        while(en.hasMoreElements()) {
            Service svc = (Service)en.nextElement();
            if (svc.getName().equals(svcName))
                return true;
        }
        return false;
    }

    public boolean serviceMonitored(String svcName) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean serviceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }
    
    public void setNextOutageIdSql(String nextOutageIdSql) {
        m_nextOutageIdSql = nextOutageIdSql;
    }
    
    public void setServiceUnresponsiveEnabled(boolean serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    public void setCriticalService(String criticalSvcName) {
        m_criticalSvcName = criticalSvcName;
    }

    public void setInterfaceMatch(String matchRegexp) {
        m_currentPkg.addIncludeUrl(matchRegexp);
    }
    

    public void setNodeOutageProcessingEnabled(boolean outageProcessingEnabled) {
        m_outageProcessingEnabled = outageProcessingEnabled;
    }

    public void setPollInterval(String svcName, long interval) {
        Service svc = findService(m_currentPkg, svcName);
        if (svc == null)
            throw new IllegalArgumentException("No service named: "+svcName);
            
        svc.setInterval(interval);
    }

    public void setPollerThreads(int threads) {
        m_threads = threads;
    }

    public void setDefaultPollInterval(long defaultPollInterval) {
        m_defaultPollInterval = defaultPollInterval;
    }

    public void populatePackage(final MockNetwork network) {
        MockVisitor populator = new MockVisitorAdapter() {
            public void visitService(MockService svc) {
                addService(svc);
            }
        };
        network.visit(populator);
    }

}
