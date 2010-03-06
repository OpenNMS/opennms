/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.snmpinterfacepoller;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterfaceConfig;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableInterface;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;

@EventListener(name="snmpPoller")
public class SnmpPoller extends AbstractServiceDaemon {
    private final static SnmpPoller m_singleton = new SnmpPoller();

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private SnmpInterfacePollerConfig m_pollerConfig;
    
    private DataSource m_dataSource;
    
    private PollableNetwork m_network;
        
    public PollableNetwork getNetwork() {
        return m_network;
    }

    public void setNetwork(
            PollableNetwork pollableNetwork) {
        m_network = pollableNetwork;
    }

    public boolean isInitialized() {
        return m_initialized;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    public Scheduler getScheduler() {
        return m_scheduler;
    }

    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    public SnmpInterfacePollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    public void setPollerConfig(
            SnmpInterfacePollerConfig snmpinterfacepollerConfig) {
        m_pollerConfig = snmpinterfacepollerConfig;
    }

    protected void onStart() {
        // get the category logger
        // start the scheduler
        //
        try {
            log().debug("start: Starting Snmp Interface Poller scheduler");

            getScheduler().start();
        } catch (RuntimeException e) {
            log().fatal("start: Failed to start scheduler", e);
            throw e;
        }

    }

    protected void onStop() {
        if(getScheduler()!=null) {
            getScheduler().stop();
        }

        setScheduler(null);
    }

    protected void onPause() {
        getScheduler().pause();
    }

    protected void onResume() {
        getScheduler().resume();
    }

    public static SnmpPoller getInstance() {
        return m_singleton;
    }
    
    public SnmpPoller() {
        super("OpenNMS.SnmpPoller");
    }

    @Override
    protected void onInit() {
        
        createScheduler();
        
        // Schedule the interfaces currently in the database
        //
        try {
            log().debug("start: Scheduling existing snmp interfaces polling");
            scheduleExistingSnmpInterface();
        } catch (Exception sqlE) {
            log().error("start: Failed to schedule existing interfaces", sqlE);
        }

        m_initialized = true;
        
    }
    
    
    protected void scheduleNewSnmpInterface(String ipaddr) {
        getNetwork().getContext().setServiceName(getPollerConfig().getService());
 
        String sql = "SELECT nodeid, ipaddr from ipinterface where issnmpprimary = 'P' and ismanaged = 'M' and ipaddr = '" + ipaddr + "'";
        
        Querier querier = new Querier(m_dataSource, sql) {

            public void processRow(ResultSet rs) throws SQLException {
                
                   schedulePollableInterface(rs.getInt("nodeid"), rs.getString("ipaddr"));
            }
       
        };
       querier.execute();  
    }
    
    protected void scheduleExistingSnmpInterface() {
        String sql = "SELECT nodeid, ipaddr from ipinterface where issnmpprimary = 'P' and ismanaged='M'";
        
        Querier querier = new Querier(m_dataSource, sql) {

            public void processRow(ResultSet rs) throws SQLException {
                
                   schedulePollableInterface(rs.getInt("nodeid"), rs.getString("ipaddr"));
            }
       
        };
       querier.execute();  
    }   

    protected void schedulePollableInterface(int nodeid, String ipaddress) {
        
        if (ipaddress != null && !ipaddress.equals("0.0.0.0")) {
            String pkgName = getPollerConfig().getPackageName(ipaddress);
            if (pkgName != null) {
                log().debug("Scheduling snmppolling for node: " + nodeid +" ip address: " + ipaddress + " - Found package interface with name: " + pkgName);
                scheduleSnmpCollection(getNetwork().create(nodeid,ipaddress,pkgName), pkgName);
            } else {
                log().debug("No snmp Poll Package found for node: " + nodeid +" ip address: " + ipaddress + ".");                
            }
        }
    }
    
    private void scheduleSnmpCollection(PollableInterface nodeGroup,String pkgName) {
        for (String pkgInterfaceName: getPollerConfig().getInterfaceOnPackage(pkgName)) {
            log().debug("found package interface with name: " +pkgInterfaceName);
            if (getPollerConfig().getStatus(pkgName, pkgInterfaceName)){
                
                String criteria = getPollerConfig().getCriteria(pkgName, pkgInterfaceName);
                log().debug("package interface: criteria: " + criteria);
                
                long interval = getPollerConfig().getInterval(pkgName, pkgInterfaceName);
                log().debug("package interface: interval: " + interval);

                boolean hasPort = getPollerConfig().hasPort(pkgName, pkgInterfaceName);
                int port = -1;
                if (hasPort) port = getPollerConfig().getPort(pkgName, pkgInterfaceName);
                
                boolean hasTimeout = getPollerConfig().hasTimeout(pkgName, pkgInterfaceName);
                int timeout = -1;
                if (hasTimeout) timeout = getPollerConfig().getTimeout(pkgName, pkgInterfaceName);
                
                boolean hasRetries = getPollerConfig().hasRetries(pkgName, pkgInterfaceName);
                int retries = -1;
                if (hasRetries) retries = getPollerConfig().getRetries(pkgName, pkgInterfaceName);

                boolean hasMaxVarsPerPdu = getPollerConfig().hasMaxVarsPerPdu(pkgName, pkgInterfaceName);
                int maxVarsPerPdu = -1;
                if (hasMaxVarsPerPdu) maxVarsPerPdu = getPollerConfig().getMaxVarsPerPdu(pkgName, pkgInterfaceName);

                PollableSnmpInterface node = nodeGroup.createPollableSnmpInterface(pkgInterfaceName, criteria, 
                   hasPort, port, hasTimeout, timeout, hasRetries, retries, hasMaxVarsPerPdu, maxVarsPerPdu);

                scheduleSnmpCollection(node, criteria,interval);
            } else {
                log().debug("package interface status: Off");
            }
        }
    }

    private void scheduleSnmpCollection(PollableSnmpInterface node, String criteria, long interval) {
        criteria = criteria + " and nodeid = " + node.getParent().getNodeid();
        node = getNetwork().getContext().refresh(node);
        
        PollableSnmpInterfaceConfig nodeconfig = new PollableSnmpInterfaceConfig(getScheduler(),interval);

        node.setSnmppollableconfig(nodeconfig);

        synchronized(node) {
            if (node.getSchedule() == null) {
                log().debug("Scheduling node: " + node.getParent().getIpaddress());
                Schedule schedule = new Schedule(node, nodeconfig, getScheduler());
                node.setSchedule(schedule);
            }
        }
        
            node.schedule();
    }
    
private void createScheduler() {

        // Create a scheduler
        //
        try {
            log().debug("init: Creating Snmp Interface Poller scheduler");

            setScheduler(new LegacyScheduler("Snmpinterfacepoller", getPollerConfig().getThreads()));
        } catch (RuntimeException e) {
            log().fatal("init: Failed to create snmp interface poller scheduler", e);
            throw e;
        }
    }
    
    @EventHandler(uei = EventConstants.SNMPPOLLERCONFIG_CHANGED_EVENT_UEI)
    public void reloadConfig(Event event) {
        try {
            getPollerConfig().update();
            
            getNetwork().deleteAll();
            getPollerConfig().rebuildPackageIpListMap();
            scheduleExistingSnmpInterface();
        } catch (MarshalException e) {
            log().error("Update SnmpPoller configuration file failed",e);
        } catch (ValidationException e) {
            log().error("Update SnmpPoller configuration file failed",e);
        } catch (IOException e) {
            log().error("Update SnmpPoller configuration file failed",e);
        }
    }

    @EventHandler(uei = EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI)
    public void primarychangeHandler(Event event) {
        nodeDeletedHandler(event);
        
        for (Parm parm : event.getParms().getParmCollection()){
            if (parm.isValid() && parm.getParmName().equals("newPrimarySnmpAddress")) {
                getPollerConfig().rebuildPackageIpListMap();
                scheduleNewSnmpInterface(parm.getValue().getContent());
                return;
            }
        }
    }
    
    @EventHandler(uei = EventConstants.DELETE_INTERFACE_EVENT_UEI)
    public void deleteInterfaceHaldler(Event event){
        refreshInterface(event.getNodeid());
    }

    @EventHandler(uei = EventConstants.PROVISION_SCAN_COMPLETE_UEI)
    public void scanCompletedHaldler(Event event){
        refreshInterface(event.getNodeid());
    }

    @EventHandler(uei = EventConstants.RESCAN_COMPLETED_EVENT_UEI)
    public void rescanCompletedHaldler(Event event){
        refreshInterface(event.getNodeid());
    }

    public void refreshInterface(long nodeid) {
        Long nodeidlong = new Long(nodeid);
        getNetwork().refresh(nodeidlong.intValue());
        
    }

    @EventHandler(uei = EventConstants.NODE_DELETED_EVENT_UEI)
    public void nodeDeletedHandler(Event event) {
        long nodeid  = event.getNodeid();
        Long nodeidlong = new Long(nodeid);
        getNetwork().delete(getNetwork().getIp(nodeidlong.intValue()));
    }

    @EventHandler(uei = EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)
    public void serviceGainedHandler(Event event) {
        if (event.getService().equals(getPollerConfig().getService())) {
            getPollerConfig().rebuildPackageIpListMap();
            scheduleNewSnmpInterface(event.getInterface());
        }
    }

    @EventHandler(uei = EventConstants.NODE_LOST_SERVICE_EVENT_UEI)
    public void serviceDownHandler(Event event) {
        String service = event.getService();
        String[] criticalServices = getPollerConfig().getCriticalServiceIds();
        for (int i = 0; i< criticalServices.length ; i++) {
            if (criticalServices[i].equals(service)) {
                getNetwork().suspend(event.getInterface());
            }
        }
    }

    @EventHandler(uei = EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI)
    public void serviceUpHandler(Event event) {
        String service = event.getService();
        String[] criticalServices = getPollerConfig().getCriticalServiceIds();
        for (int i = 0; i< criticalServices.length ; i++) {
            if (criticalServices[i].equals(service)) {
                getNetwork().activate(event.getInterface());
            }
        }
        
    }

    @EventHandler(uei = EventConstants.INTERFACE_UP_EVENT_UEI)
    public void interfaceUpHandler(Event event) {
        getNetwork().activate(event.getInterface());
    }

    @EventHandler(uei = EventConstants.INTERFACE_DOWN_EVENT_UEI)
    public void interfaceDownHandler(Event event) {
        getNetwork().suspend(event.getInterface());
    }

    @EventHandler(uei = EventConstants.NODE_UP_EVENT_UEI)
    public void nodeUpHandler(Event event) {
        long nodeid  = event.getNodeid();
        Long nodeidlong = new Long(nodeid);
        String ipprimary = getNetwork().getIp(nodeidlong.intValue());
        if (ipprimary != null) getNetwork().activate(ipprimary);
    }

    @EventHandler(uei = EventConstants.NODE_DOWN_EVENT_UEI)
    public void nodeDownHandler(Event event) {
        long nodeid  = event.getNodeid();
        Long nodeidlong = new Long(nodeid);
        String ipprimary = getNetwork().getIp(nodeidlong.intValue());
        if (ipprimary != null) getNetwork().suspend(ipprimary);
    }

}
