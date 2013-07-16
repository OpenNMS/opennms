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

package org.opennms.netmgt.xmlrpcd;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;

/**
 * <p>OpenNMSProvisioner class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
public class OpenNMSProvisioner implements Provisioner {
    private static final Logger LOG = LoggerFactory.getLogger(OpenNMSProvisioner.class);
    
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


    private void checkRetries(final int retries) {
        if (retries < 0) throw new IllegalArgumentException("Illegal retries "+retries+". Must be >= 0");
    }

    private void checkTimeout(final int timeout) {
        if (timeout <= 0) throw new IllegalArgumentException("Illegal timeout "+timeout+". Must be > 0");
    }
    
    private void checkInterval(final int interval) {
        if (interval <= 0) throw new IllegalArgumentException("Illegal interval "+interval+". Must be > 0");
    }
    
    private void checkDowntimeInterval(final int interval) {
        checkInterval(interval);
    }
    
    private void checkDowntimeDuration(final int duration) {
        checkInterval(duration);
    }
    
    private void checkPort(final int port) {
        if (port < 1 || port > 65535) throw new IllegalArgumentException("Illegal port "+port+". Must be between 1 and 65535 (inclusive)");
    }
    
    private void checkHostname(final String hostname) {
        if (hostname == null) throw new NullPointerException("hostname must not be null");
        if (hostname.length() > 512) throw new IllegalArgumentException("Illegal hostname "+hostname+". Hostnames must not be longer than 512 characters");
    }
    
    private void checkUrl(final String url) {
        if (url == null) throw new NullPointerException("url must not be null");
        if (url.length() == 0) throw new IllegalArgumentException("Illegal url \'\'.  Must not be zero length");
        if (url.length() > 512) throw new IllegalArgumentException("Illegal url "+url+". Must be no more than 512 chars");
    }
    
    private void checkContentCheck(final String check) {
        if (check != null && check.length() > 128) throw new IllegalArgumentException("Illegal contentCheck "+check+". Must be no more than 128 chars.");
    }
    
    private void checkResponseRange(final String response) {
        if (response == null || response.equals("")) return;
        
        if (response.indexOf('-') < 0) {
        	checkResponseCode(response);
        } else {
        	final int dash = response.indexOf('-');
        	final String startCode = response.substring(0, dash).trim();
        	if ("".equals(startCode)) throw new IllegalArgumentException("Illegal Start code. Expected range format is <startCode>-<endCode>.");
        	checkResponseCode(startCode);

        	if (dash+1 > response.length()-1) throw new IllegalArgumentException("Illegal End code. Expected range format is <startcode>-<endcode>");
        	final String endCode = response.substring(dash+1);
        	checkResponseCode(endCode);
        }
    }
    
    private void checkResponseCode(final String response) {
        if (response == null || response.equals("")) return;
            
        final int code = Integer.parseInt(response);
        if (code < 100 || code > 599) throw new IllegalArgumentException("Illegal response code "+code+". Must be between 100 and 599");
    }
    
    private void checkUsername(final String username) {
        if (username == null) throw new NullPointerException("username is null");
        if (username.length() > 64) throw new IllegalArgumentException("Illegal username "+username+". username must be no more than than 64 characters");
    }
    
    private void checkPassword(final String pw) {
        if (pw == null) throw new NullPointerException("password is null");
        if (pw.length() > 64) throw new IllegalArgumentException("Illegal password "+pw+". password must be no more than than 64 charachters");
    }
    
    private void checkDriver(final String driver) {
        if (driver == null) throw new NullPointerException("driver is null");
        if (driver.length() == 0) throw new IllegalArgumentException("Illegal driver.  Must not be zero length");
        if (driver.length() > 128) throw new IllegalArgumentException("Illegal driver "+driver+". Driver length must be no more than than 128 characters");
            
    }
    
    private void validateSchedule(final int retries, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration) {
        checkRetries(retries);
        checkTimeout(timeout);
        checkInterval(interval);
        checkDowntimeInterval(downTimeInterval);
        checkDowntimeDuration(downTimeDuration);
    }
        


    /** {@inheritDoc} */
    @Override
    public boolean addServiceICMP(final String serviceId, final int retry, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration) {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, ICMP_MONITOR, ICMP_PLUGIN);
    }
    
    private boolean addService(final String serviceId, final int retries, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration, final String monitor, final String plugin) {
        return addService(serviceId, retries, timeout, interval, downTimeInterval, downTimeDuration, monitor, plugin, new Parm[0]);
    }
    
    private boolean addService(final String serviceId, final int retries, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration, final String monitor, final String plugin, final Parm[] entries) {
    	final String pkgName = serviceId;
    	final Package pkg = getPackage(pkgName, interval, downTimeInterval, downTimeDuration);

    	final Properties parms = new Properties();
        parms.setProperty("retry", ""+retries);
        parms.setProperty("timeout", ""+timeout);
        
        for(int i = 0; i < entries.length; i++) {
            parms.setProperty(entries[i].getKey(), entries[i].getVal());
        }

        addServiceToPackage(pkg, serviceId, interval, parms);

        if (m_pollerConfig.getPackage(pkg.getName()) == null) {
            m_pollerConfig.addPackage(pkg);
        }
        
        if (m_pollerConfig.getServiceMonitor(serviceId) == null) {
            LOG.debug("Adding a new monitor for {}", serviceId);
            m_pollerConfig.addMonitor(serviceId, monitor);
        } else {
            LOG.debug("No need to add a new monitor for {}", serviceId);
        }
        
        if (m_capsdConfig.getProtocolPlugin(serviceId) == null) {
        	final ProtocolPlugin pPlugin = new ProtocolPlugin();
            pPlugin.setProtocol(serviceId);
            pPlugin.setClassName(plugin);
            pPlugin.setScan("off");
            m_capsdConfig.addProtocolPlugin(pPlugin);
        }
        
        saveConfigs();
        return true;
    }
    private void saveConfigs() {
        try {
            m_capsdConfig.save();
            syncServices();
            m_pollerConfig.save();

            m_eventManager.sendNow(new EventBuilder(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI, "OpenNMSProvisioner").getEvent());
        } catch (final Throwable e) {
            throw new RuntimeException("Error saving poller or capsd configuration: " + e, e);
        }
    }
    
    private void syncServices() {
        getCapsdDbSyncer().syncServicesTable();
    }
    
    private void addServiceToPackage(final Package pkg, final String serviceId, final int interval, final Properties parms) {
    	Service svc = m_pollerConfig.getServiceInPackage(serviceId, pkg);
        if (svc == null) {
            svc = new Service();
            svc.setName(serviceId);
            pkg.addService(svc);
        }
        svc.setInterval(interval);
        final Enumeration<?> keys = parms.propertyNames();
        while(keys.hasMoreElements()) {
            final String key = (String)keys.nextElement();
            final String value = parms.getProperty(key);
            setParameter(svc, key, value);
        }
    }
    private void setParameter(final Service svc, final String key, final String value) {
    	Parameter parm = findParamterWithKey(svc, key);
        if (parm == null) {
            parm = new Parameter();
            svc.addParameter(parm);
        }
        parm.setKey(key);
        parm.setValue(value);
    }
    
    /**
     * <p>findParamterWithKey</p>
     *
     * @param svc a {@link org.opennms.netmgt.config.poller.Service} object.
     * @param key a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.poller.Parameter} object.
     */
    public Parameter findParamterWithKey(final Service svc, final String key) {
    	final Enumeration<Parameter> e = svc.enumerateParameter();
        while(e.hasMoreElements()) {
            final Parameter parameter = (Parameter)e.nextElement();
            if (key.equals(parameter.getKey())) {
                return parameter;
            }
        }
        return null;
    }
    private Package getPackage(final String pkgName, final int interval, final int downTimeInterval, final int downTimeDuration) {
    	Package pkg = m_pollerConfig.getPackage(pkgName);
        if (pkg == null) {
            pkg = new Package();
            pkg.setName(pkgName);
            
            final Filter filter = new Filter();
            filter.setContent("IPADDR IPLIKE *.*.*.*");
            pkg.setFilter(filter);
            
            final Rrd rrd = new Rrd();
            rrd.setStep(300);
            rrd.addRra("RRA:AVERAGE:0.5:1:2016");
            rrd.addRra("RRA:AVERAGE:0.5:12:4464");
            rrd.addRra("RRA:MIN:0.5:12:4464");
            rrd.addRra("RRA:MAX:0.5:12:4464");
            pkg.setRrd(rrd);
            
        }

        final Downtime dt = new Downtime();
        dt.setBegin(0);
        dt.setEnd(downTimeDuration);
        dt.setInterval(downTimeInterval);

        final Downtime dt2 = new Downtime();
        dt2.setBegin(downTimeDuration);
        dt2.setInterval(interval);
        pkg.setDowntime(new Downtime[] { dt, dt2 });
        return pkg;
    }

    /** {@inheritDoc} */
    @Override
    public boolean addServiceDNS(final String serviceId, final int retry, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration, final int port, final String lookup) {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkPort(port);
        checkHostname(lookup);

        final Parm[] parm = new Parm[] {
                new Parm("port", port),
                new Parm("lookup", lookup),
        };
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, DNS_MONITOR, DNS_PLUGIN, parm);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addServiceTCP(final String serviceId, final int retry, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration, final int port, final String banner) {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkPort(port);
        checkContentCheck(banner);
        
        final Parm[] parm = new Parm[] {
                new Parm("port", port),
                new Parm("banner", banner),
        };
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, TCP_MONITOR, TCP_PLUGIN, parm);
    }

    /** {@inheritDoc} */
    @Override
    public boolean addServiceHTTP(final String serviceId, final int retry, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration, final String hostName, final int port, final String response, final String responseText, final String url, final String user, final String passwd, final String agent) throws MalformedURLException {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkHostname(hostName);
        checkUsername(user);
        checkPassword(passwd);
        checkPort(port);
        checkResponseRange(response);
        checkContentCheck(responseText);
        checkUrl(url);
        
        final List<Parm> parmList = new ArrayList<Parm>();
        final String responseString = "".equals(response)? null : response;
        
        parmList.add(new Parm("port", port));
        if (responseString != null) { 
            parmList.add(new Parm("response", responseString));
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

    /** {@inheritDoc} */
    @Override
    public boolean addServiceHTTPS(final String serviceId, final int retry, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration, final String hostName, final int port, final String response, final String responseText, final String url, final String user, final String passwd, final String agent) throws MalformedURLException {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkHostname(hostName);
        checkUsername(user);
        checkPassword(passwd);
        checkPort(port);
        checkResponseRange(response);
        checkContentCheck(responseText);
        checkUrl(url);
        
        final List<Parm> parmList = new ArrayList<Parm>();
        final String responseString = "".equals(response)? null : response;

        parmList.add(new Parm("port", port));
        if (responseString != null) { 
            parmList.add(new Parm("response", responseString));
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

    /** {@inheritDoc} */
    @Override
    public boolean addServiceDatabase(final String serviceId, final int retry, final int timeout, final int interval, final int downTimeInterval, final int downTimeDuration, final String user, final String password, final String driver, final String url)   {
        validateSchedule(retry, timeout, interval, downTimeInterval, downTimeDuration);
        checkUsername(user);
        checkPassword(password);
        checkDriver(driver);
        checkUrl(url);
        
        final Parm[] parm = new Parm[] {
                new Parm("driver", driver),
                new Parm("url", url),
                new Parm("user", user),
                new Parm("password", password),
        };
        
        return addService(serviceId, retry, timeout, interval, downTimeInterval, downTimeDuration, JDBC_MONITOR, JDBC_PLUGIN, parm);
    }
    
    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getServiceConfiguration(final String pkgName, final String serviceId) {
        if (pkgName == null) {
            throw new NullPointerException("pkgName is null");
        }
        if (serviceId == null) {
            throw new NullPointerException("serviceId is null");
        }

        final Package pkg = m_pollerConfig.getPackage(pkgName);
        if (pkg == null) {
            throw new IllegalArgumentException(pkgName+" is not a valid poller package name");
        }
        
        final Service svc = m_pollerConfig.getServiceInPackage(serviceId, pkg);
        if (svc == null) {
            throw new IllegalArgumentException("Could not find service "+serviceId+" in package "+pkgName);
        }
        
        final Map<String, Object> m = new HashMap<String, Object>();
        m.put("serviceid", serviceId);
        m.put("interval", Integer.valueOf((int)svc.getInterval()));
        
        for(int i = 0; i < svc.getParameterCount(); i++) {
        	final Parameter param = svc.getParameter(i);
            String key = param.getKey();
            final String valStr = param.getValue();
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
                final String user = valStr.substring(0, colon);
                final String passwd = valStr.substring(colon+1);
                m.put("user", user);
                m.put("password", passwd);
                continue;
            }
            
            m.put(key, val);
        }
        
        for(int i = 0; i < pkg.getDowntimeCount(); i++) {
            final Downtime dt = pkg.getDowntime(i);
            final String suffix = (i == 0 ? "" : ""+i);
            if ((dt.hasEnd()) || (dt.getDelete() != null && !"false".equals(dt.getDelete()))) {
                m.put("downtime_interval"+suffix, Integer.valueOf((int)dt.getInterval()));
                int duration = (!dt.hasEnd() ? Integer.MAX_VALUE : (int)(dt.getEnd() - dt.getBegin()));
                m.put("downtime_duration"+suffix, Integer.valueOf(duration));
            }   
        }
        
        return m;
    }
    
    /**
     * <p>setCapsdConfig</p>
     *
     * @param capsdConfig a {@link org.opennms.netmgt.config.CapsdConfig} object.
     */
    public void setCapsdConfig(final CapsdConfig capsdConfig) {
        m_capsdConfig = capsdConfig;
    }
    /**
     * <p>setPollerConfig</p>
     *
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public void setPollerConfig(final PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }
    /**
     * <p>setEventManager</p>
     *
     * @param eventManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventManager(final EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }
    
    private CapsdDbSyncer getCapsdDbSyncer() {
        return m_capsdDbSyncer;
    }

    /**
     * <p>setCapsdDbSyncer</p>
     *
     * @param capsdDbSyncer a {@link org.opennms.netmgt.capsd.CapsdDbSyncer} object.
     */
    public void setCapsdDbSyncer(final CapsdDbSyncer capsdDbSyncer) {
        m_capsdDbSyncer = capsdDbSyncer;
    }

}
