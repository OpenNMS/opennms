/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 2007 May 06: Moved database synchronization out of CapsdConfigFactory,
 *              use Java 5 generics, eliminate warnings, cleanup logging,
 *              and do some code formatting. - dj@opennms.org
 *              
 * Created: July 13, 2005
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.netmgt.xmlrpcd;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.CapsdDbSyncer;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Filter;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class OpenNMSProvisioner implements Provisioner {
    
    private static final String JDBC_MONITOR = "org.opennms.netmgt.poller.monitors.JDBCMonitor";
    private static final String HTTPS_MONITOR = "org.opennms.netmgt.poller.monitors.HttpsMonitor";
    private static final String HTTP_MONITOR = "org.opennms.netmgt.poller.monitors.HttpMonitor";
    private static final String TCP_MONITOR = "org.opennms.netmgt.poller.monitors.TcpMonitor";
    private static final String DNS_MONITOR = "org.opennms.netmgt.poller.monitors.DnsMonitor";
    private static final String ICMP_MONITOR = "org.opennms.netmgt.poller.monitors.IcmpMonitor";

    private static final String JDBC_PLUGIN = "org.opennms.netmgt.capsd.plugins.JDBCPlugin";
    private static final String HTTPS_PLUGIN = "org.opennms.netmgt.capsd.plugins.HttpsPlugin";
    private static final String HTTP_PLUGIN = "org.opennms.netmgt.capsd.plugins.HttpPlugin";
    private static final String TCP_PLUGIN = "org.opennms.netmgt.capsd.plugins.TcpPlugin";
    private static final String DNS_PLUGIN = "org.opennms.netmgt.capsd.plugins.DnsPlugin";
    private static final String ICMP_PLUGIN = "org.opennms.netmgt.capsd.plugins.IcmpPlugin";


    private static class Parm {
        String m_key;
        String m_val;
        Parm(String key, String val) { m_key = key; m_val = val; }
        Parm(String key, int val) { m_key = key; m_val = ""+val; }
        String getKey() { return m_key; }
        String getVal() { return m_val; }

    }

    private CapsdConfig m_capsdConfig;
    private PollerConfig m_pollerConfig;
    private EventIpcManager m_eventManager;
    private CapsdDbSyncer m_capsdDbSyncer;



//    def _checkRetries(self, retries):
//        # Check if the retries value is valid
//        if not isinstance(retries, int):
//            self.Fault(FAULT_DATA_INVALID)
//        if retries < 0: #or retries > 3:
//            self.Fault(FAULT_DATA_INVALID)
    
    private void checkRetries(int retries) {
        if (retries < 0)
            throw new IllegalArgumentException("Illegal retries "+retries+". Must be >= 0");
        
        
    }
//    def _checkTimeout(self, timeout):
//        # Check if the timeout value is valid
//        if not isinstance(timeout, int):
//            self.Fault(FAULT_DATA_INVALID)
//        if timeout < sectomilis(1): #or timeout > mintomilis(60):
//            self.Fault(FAULT_DATA_INVALID)
    
    private void checkTimeout(int timeout) {
        if (timeout <= 0)
            throw new IllegalArgumentException("Illegal timeout "+timeout+". Must be > 0");
    }
    
//    def _checkInterval(self, interval):
//        # Check if the interval value is valid
//        if not isinstance(interval, int):
//            self.Fault(FAULT_DATA_INVALID)
//        if interval < mintomilis(1): #or interval > mintomilis(60):
//            self.Fault(FAULT_DATA_INVALID)
    
    private void checkInterval(int interval) {
        if (interval <= 0)
            throw new IllegalArgumentException("Illegal interval "+interval+". Must be > 0");
    }
    
//    def _checkDowntimeInterval(self, interval):
//        self._checkInterval(interval)
    
    private void checkDowntimeInterval(int interval) {
        checkInterval(interval);
    }
    
//    def _checkDowntimeDuration(self, duration):
//        self._checkInterval(duration)

    private void checkDowntimeDuration(int duration) {
        checkInterval(duration);
    }
    
//    def _checkPort(self, port):
//        if not isinstance(port, int):
//            self.Fault(FAULT_DATA_INVALID)
//        if port < 1 or port > 65535:
//            self.Fault(FAULT_DATA_INVALID)
    
    private void checkPort(int port) {
        if (port < 1 || port > 65535) 
            throw new IllegalArgumentException("Illegal port "+port+". Must be between 1 and 65535 (inclusive)");
        
    }
    
//    def _checkHostname(self, hostname):
//        if not isinstance(hostname, basestring):
//            self.Fault(FAULT_DATA_INVALID)
//        if len(hostname) > 512:
//            self.Fault(FAULT_DATA_INVALID)

    private void checkHostname(String hostname) {
        if (hostname == null)
            throw new NullPointerException("hostname must not be null");

        if (hostname.length() > 512)
            throw new IllegalArgumentException("Illegal hostname "+hostname+". Hostnames must not be longer than 512 characters");
    }
    
//    def _checkURL(self, url):
//        if not isinstance(url, basestring):
//            self.Fault(FAULT_DATA_INVALID)
//        if len(url) > 512:
//            self.Fault(FAULT_DATA_INVALID)
//        if len(url) == 0:
//            self.Fault(FAULT_DATA_INVALID)
//        # Check for a protocol
//        if (len(url) > 0) and (url.find('://') < 1): # No protocol given
//            self.Fault(FAULT_URL_INVALID)

    private void checkUrl(String url) {
        if (url == null)
            throw new NullPointerException("url must not be null");
        
        if (url.length() == 0)
            throw new IllegalArgumentException("Illegal url \'\'.  Must not be zero length");
        
        if (url.length() > 512)
            throw new IllegalArgumentException("Illegal url "+url+". Must be less than 512 chars");
    }
    
//    def _checkContentCheck(self, check):
//        if not isinstance(check, basestring):
//            self.Fault(FAULT_DATA_INVALID)
//        if len(check) > 128:
//            self.Fault(FAULT_DATA_INVALID)

    private void checkContentCheck(String check) {
        if (check != null && check.length() > 128)
            throw new IllegalArgumentException("Illegal contentCheck "+check+". Must be less than 128 chars.");
    }
    
//    def _checkResponseCode(self, code):
//        if not isinstance(code, int):
//            self.Fault(FAULT_DATA_INVALID)
//        if code < 100 or code > 599:
//            self.Fault(FAULT_DATA_INVALID)
    
    private void checkResponseRange(String response) {
        if (response == null || response.equals(""))
            return;
        
        if (response.indexOf('-') < 0) {
        	checkResponseCode(response);
        }
        else {
        	int dash = response.indexOf('-');
        	String startCode = response.substring(0, dash).trim();
        	if ("".equals(startCode))
        		throw new IllegalArgumentException("Illegal Start code. Expected range format is <startCode>-<endCode>.");
        	checkResponseCode(startCode);

        	if (dash+1 > response.length()-1)
        		throw new IllegalArgumentException("Illegal End code. Expected range format is <startcode>-<endcode>");
        	String endCode = response.substring(dash+1);
        	checkResponseCode(endCode);
        }
    }
    
    private void checkResponseCode(String response) {
        if (response == null || response.equals(""))
            return;
            
        int code = Integer.parseInt(response);
        if (code < 100 || code > 599)
            throw new IllegalArgumentException("Illegal response code "+code+". Must be between 100 and 599");
    }
//    def _checkUsername(self, name):
//        if not isinstance(name, basestring):
//            self.Fault(FAULT_DATA_INVALID)
//        if len(name) > 64:
//            self.Fault(FAULT_DATA_INVALID)
    
    private void checkUsername(String username) {
        if (username == null)
            throw new NullPointerException("username is null");
        if (username.length() > 64)
            throw new IllegalArgumentException("Illegal username "+username+". username must be less than 64 characters");
    }
    
//    def _checkPassword(self, pw):
//        if not isinstance(pw, basestring):
//            self.Fault(FAULT_DATA_INVALID)
//        if len(pw) > 64:
//            self.Fault(FAULT_DATA_INVALID)
        
    private void checkPassword(String pw) {
        if (pw == null)
            throw new NullPointerException("password is null");
        if (pw.length() > 64)
            throw new IllegalArgumentException("Illegal password "+pw+". password must be less than 64 charachters");
    }
    
//    def _checkDriver(self, driver):
//        if not isinstance(driver, basestring):
//            self.Fault(FAULT_DATA_INVALID)
//        if len(driver) > 128:
//            self.Fault(FAULT_DATA_INVALID)
//        if driver == '':
//            self.Fault(FAULT_DATA_INVALID)

    private void checkDriver(String driver) {
        if (driver == null)
            throw new NullPointerException("driver is null");
        if (driver.length() == 0)
            throw new IllegalArgumentException("Illegal driver.  Must not be zero length");
        if (driver.length() > 128)
            throw new IllegalArgumentException("Illegal driver "+driver+". Driver length must be less than 128 characters");
            
    }
    
//    def _validateSchedule(self, retries, timeout, interval,
//                          downtime_interval, downtime_duration):
//        self._checkRetries(retries)
//        self._checkTimeout(timeout)
//        self._checkInterval(interval)
//        self._checkDowntimeInterval(downtime_interval)
//        self._checkDowntimeDuration(downtime_duration)
    
    private void validateSchedule(int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration) {
        checkRetries(retries);
        checkTimeout(timeout);
        checkInterval(interval);
        checkDowntimeInterval(downTimeInterval);
        checkDowntimeDuration(downTimeDuration);
    }
        


    public boolean addServiceICMP(String serviceId, int retry, int timeout, int interval, int downTimeInterval, int downTimeDuration) {
//        System.err.println("Called OpenNMSProvisioner.addServiceICMP("+
//                           serviceId+", "+
//                           retries+", "+
//                           timeout+", "+
//                           interval+", "+
//                           downTimeInterval+", "+
//                           downTimeDuration+
//                           ")");
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, ICMP_MONITOR, ICMP_PLUGIN);
    }
    
    private boolean addService(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, String monitor, String plugin) {
        return addService(serviceId, retries, timeout, interval, downTimeInterval, downTimeDuration, monitor, plugin, new Parm[0]);
    }
    
    private boolean addService(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, String monitor, String plugin, Parm[] entries) {
        String pkgName = serviceId;

        Package pkg = getPackage(pkgName, interval, downTimeInterval, downTimeDuration);

        Properties parms = new Properties();
        parms.setProperty("retry", ""+retries);
        parms.setProperty("timeout", ""+timeout);
        
        for(int i = 0; i < entries.length; i++) {
            parms.setProperty(entries[i].getKey(), entries[i].getVal());
        }

        addServiceToPackage(pkg, serviceId, interval, parms);

        if (m_pollerConfig.getPackage(pkg.getName()) == null)
            m_pollerConfig.addPackage(pkg);
        
        if (m_pollerConfig.getServiceMonitor(serviceId) == null) {
            log().debug("Adding a new monitor for "+serviceId);
            m_pollerConfig.addMonitor(serviceId, monitor);
        } else {
            log().debug("No need to add a new monitor for "+serviceId);
        }
        
        if (m_capsdConfig.getProtocolPlugin(serviceId) == null) {
            ProtocolPlugin pPlugin = new ProtocolPlugin();
            pPlugin.setProtocol(serviceId);
            pPlugin.setClassName(plugin);
            pPlugin.setScan("off");
            m_capsdConfig.addProtocolPlugin(pPlugin);
        }
        
        saveConfigs();
        return true;
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    private void saveConfigs() {
        try {
            m_capsdConfig.save();
            syncServices();
            m_pollerConfig.save();

            Event event = new Event();
            event.setUei(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI);
            event.setCreationTime(EventConstants.formatToString(new Date()));
            event.setTime(EventConstants.formatToString(new Date()));
            event.setSource("OpenNMSProvisioner");
            m_eventManager.sendNow(event);
            
        } catch (Exception e) {
            throw new RuntimeException("Error saving poller or capsd configuration: " + e, e);
        }
    }
    
    private void syncServices() {
        getCapsdDbSyncer().syncServicesTable();
    }
    
    private void addServiceToPackage(Package pkg, String serviceId, int interval, Properties parms) {
        Service svc = m_pollerConfig.getServiceInPackage(serviceId, pkg);
        if (svc == null) {
            svc = new Service();
            svc.setName(serviceId);
            pkg.addService(svc);
        }
        svc.setInterval(interval);
        Enumeration<?> keys = parms.propertyNames();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            String value = parms.getProperty(key);
            setParameter(svc, key, value);
        }
    }
    private void setParameter(Service svc, String key, String value) {
        Parameter parm = findParamterWithKey(svc, key);
        if (parm == null) {
            parm = new Parameter();
            svc.addParameter(parm);
        }
        parm.setKey(key);
        parm.setValue(value);
    }
    
    public Parameter findParamterWithKey(Service svc, String key) {
        Enumeration<Parameter> e = svc.enumerateParameter();
        while(e.hasMoreElements()) {
            Parameter parameter = (Parameter)e.nextElement();
            if (key.equals(parameter.getKey())) {
                return parameter;
            }
        }
        return null;
    }
    private Package getPackage(String pkgName, int interval, int downTimeInterval, int downTimeDuration) {
        Package pkg = m_pollerConfig.getPackage(pkgName);
        if (pkg == null) {
            pkg = new Package();
            pkg.setName(pkgName);
            
            Filter filter = new Filter();
            filter.setContent("IPADDR IPLIKE *.*.*.*");
            pkg.setFilter(filter);
            
            Rrd rrd = new Rrd();
            rrd.setStep(300);
            rrd.addRra("RRA:AVERAGE:0.5:1:2016");
            rrd.addRra("RRA:AVERAGE:0.5:12:4464");
            rrd.addRra("RRA:MIN:0.5:12:4464");
            rrd.addRra("RRA:MAX:0.5:12:4464");
            pkg.setRrd(rrd);
            
        }
        Downtime dt = new Downtime();
        dt.setBegin(0);
        dt.setEnd(downTimeDuration);
        dt.setInterval(downTimeInterval);
        Downtime dt2 = new Downtime();
        dt2.setBegin(downTimeDuration);
        dt2.setInterval(interval);
        pkg.setDowntime(new Downtime[] { dt, dt2 });
        return pkg;
    }

    public boolean addServiceDNS(String serviceId, int retry, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String lookup) {
//        System.err.println("Called OpenNMSProvisioner.addServiceDNS("+
//                           serviceId+", "+
//                           retries+", "+
//                           timeout+", "+
//                           interval+", "+
//                           downTimeInterval+", "+
//                           downTimeDuration+", "+
//                           port+", "+
//                           hostname+
//                           ")");
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkPort(port);
        checkHostname(lookup);

        Parm[] parm = new Parm[] {
                new Parm("port", port),
                new Parm("lookup", lookup),
        };
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, DNS_MONITOR, DNS_PLUGIN, parm);
    }

    public boolean addServiceTCP(String serviceId, int retry, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String banner) {
//        System.err.println("Called OpenNMSProvisioner.addServiceTCP("+
//                           serviceId+", "+
//                           retries+", "+
//                           timeout+", "+
//                           interval+", "+
//                           downTimeInterval+", "+
//                           downTimeDuration+", "+
//                           port+", "+
//                           contentCheck+
//                           ")");
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkPort(port);
        checkContentCheck(banner);
        
        Parm[] parm = new Parm[] {
                new Parm("port", port),
                new Parm("banner", banner),
        };
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, TCP_MONITOR, TCP_PLUGIN, parm);
    }

    public boolean addServiceHTTP(String serviceId, int retry, int timeout, int interval, int downTimeInterval, int downTimeDuration, String hostName, int port, String response, String responseText, String url, String user, String passwd, String agent) throws MalformedURLException {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkHostname(hostName);
        checkUsername(user);
        checkPassword(passwd);
        checkPort(port);
        checkResponseRange(response);
        checkContentCheck(responseText);
        checkUrl(url);
        
        List<Parm> parmList = new ArrayList<Parm>();
        
        if ("".equals(response)) { 
            response = null;
        }
        
        parmList.add(new Parm("port", port));
        if (response != null) { 
            parmList.add(new Parm("response", response));
        }
        parmList.add(new Parm("response text", responseText));
        parmList.add(new Parm("url", url));
        if (hostName != null) {
            parmList.add(new Parm("host-name", hostName));
        }
        if (user != null) { 
            parmList.add(new Parm("user", user));
        }
        if (passwd != null) { 
            parmList.add(new Parm("password", passwd));
        }
        if (agent != null) { 
            parmList.add(new Parm("user-agent", agent));
        }
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, HTTP_MONITOR, HTTP_PLUGIN, parmList.toArray(new Parm[parmList.size()]));
    }

    public boolean addServiceHTTPS(String serviceId, int retry, int timeout, int interval, int downTimeInterval, int downTimeDuration, String hostName, int port, String response, String responseText, String url, String user, String passwd, String agent) throws MalformedURLException {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkHostname(hostName);
        checkUsername(user);
        checkPassword(passwd);
        checkPort(port);
        checkResponseRange(response);
        checkContentCheck(responseText);
        checkUrl(url);
        
        if ("".equals(response)) { 
            response = null;
        }

        List<Parm> parmList = new ArrayList<Parm>();
        parmList.add(new Parm("port", port));
        if (response != null) { 
            parmList.add(new Parm("response", response));
        }
        parmList.add(new Parm("response text", responseText));
        parmList.add(new Parm("url", url));
        if (hostName != null) { 
            parmList.add(new Parm("host-name", hostName));
        }
        if (user != null) { 
            parmList.add(new Parm("user", user));
        }
        if (passwd != null) {
            parmList.add(new Parm("password", passwd));
        }
        if (agent != null) {
            parmList.add(new Parm("user-agent", agent));
        }
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, HTTPS_MONITOR, HTTPS_PLUGIN, parmList.toArray(new Parm[parmList.size()]));
    }

    public boolean addServiceDatabase(String serviceId, int retry, int timeout, int interval, int downTimeInterval, int downTimeDuration, String user, String password, String driver, String url)   {
//        System.err.println("Called OpenNMSProvisioner.addServiceDatabase("+
//                           serviceId+", "+
//                           retries+", "+
//                           timeout+", "+
//                           interval+", "+
//                           downTimeInterval+", "+
//                           downTimeDuration+", "+
//                           username+", "+
//                           password+", "+
//                           driver+", "+
//                           url+
//                           ")");
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkUsername(user);
        checkPassword(password);
        checkDriver(driver);
        checkUrl(url);
        
        Parm[] parm = new Parm[] {
                new Parm("driver", driver),
                new Parm("url", url),
                new Parm("user", user),
                new Parm("password", password),
        };
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, JDBC_MONITOR, JDBC_PLUGIN, parm);
    }
    
    public Map<String, Object> getServiceConfiguration(String pkgName, String serviceId) {
        if (pkgName == null) {
            throw new NullPointerException("pkgName is null");
        }
        if (serviceId == null) {
            throw new NullPointerException("serviceId is null");
        }

        Package pkg = m_pollerConfig.getPackage(pkgName);
        if (pkg == null) {
            throw new IllegalArgumentException(pkgName+" is not a valid poller package name");
        }
        
        Service svc = m_pollerConfig.getServiceInPackage(serviceId, pkg);
        if (svc == null) {
            throw new IllegalArgumentException("Could not find service "+serviceId+" in package "+pkgName);
        }
        
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("serviceid", serviceId);
        m.put("interval", new Integer((int)svc.getInterval()));
        
        for(int i = 0; i < svc.getParameterCount(); i++) {
            Parameter param = svc.getParameter(i);
            String key = param.getKey();
            String valStr = param.getValue();
            Object val = valStr;
            if ("retry".equals(key)) {
                key = "retries";
                val = Integer.decode(valStr);
            } else if ("timeout".equals(key)) {
                val = Integer.decode(valStr);
            } else if ("port".equals(key)) {
                val = Integer.decode(valStr);
            } else if ("response".equals(key)) {
                val = valStr;
            } else if ("response text".equals(key)) {
                key = "response_text";
            } else if ("response-text".equals(key)) {
                key = "response_text";
            } else if ("host-name".equals(key)) {
                key = "hostname";
            } else if ("host name".equals(key)) {
                key = "hostname";
            } else if ("user-agent".equals(key)) {
                key = "agent";
            } else if ("basic-authentication".equals(key)) {
                int colon = valStr.indexOf(':');
                if (colon < 0) {
                    continue;
                }
                String user = valStr.substring(0, colon);
                String passwd = valStr.substring(colon+1);
                m.put("user", user);
                m.put("password", passwd);
                continue;
            }
            
            m.put(key, val);
        }
        
        for(int i = 0; i < pkg.getDowntimeCount(); i++) {
            Downtime dt = pkg.getDowntime(i);
            String suffix = (i == 0 ? "" : ""+i);
            if ((dt.hasEnd()) || (dt.getDelete() != null && !"false".equals(dt.getDelete()))) {
                m.put("downtime_interval"+suffix, new Integer((int)dt.getInterval()));
                int duration = (!dt.hasEnd() ? Integer.MAX_VALUE : (int)(dt.getEnd() - dt.getBegin()));
                m.put("downtime_duration"+suffix, new Integer(duration));
            }   
        }
        
        return m;
    }
    
    public void setCapsdConfig(CapsdConfig capsdConfig) {
        m_capsdConfig = capsdConfig;
    }
    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }
    
    private CapsdDbSyncer getCapsdDbSyncer() {
        return m_capsdDbSyncer;
    }

    public void setCapsdDbSyncer(CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

}
