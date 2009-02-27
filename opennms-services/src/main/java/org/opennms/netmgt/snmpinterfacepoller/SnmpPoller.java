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

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

//import org.apache.log4j.Level;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterfaceConfig;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableInterface;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.config.snmpinterfacepoller.Package;


public class SnmpPoller extends AbstractServiceDaemon {
    private final static SnmpPoller m_singleton = new SnmpPoller();

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private SnmpInterfacePollerEventProcessor m_eventProcessor;

    private QueryManager m_queryManager;
    
    private SnmpInterfacePollerConfig m_pollerConfig;
    
    private EventIpcManager m_eventMgr;

    private DataSource m_dataSource;
    
    private PollableNetwork m_network;
    
    private static Map<Integer, Map<Integer, AlarmStatus>> m_nodeAlarmStatusMap;

    public final class AlarmStatus {

        boolean _hasAdminStatusDownAlarm = false;
        boolean _hasOperStatusDownAlarm = false;
        
        public AlarmStatus() {
            super();
        }
        
        public boolean hasAdminStatusDownAlarm() {
            return _hasAdminStatusDownAlarm;
        }

        public void setHasAdminStatusDownAlarm(boolean adminStatusDownAlarm) {
            _hasAdminStatusDownAlarm = adminStatusDownAlarm;
        }

        public boolean hasOperStatusDownAlarm() {
            return _hasOperStatusDownAlarm;
        }

        public void setHasOperStatusDownAlarm(boolean operStatusDownAlarm) {
            _hasOperStatusDownAlarm = operStatusDownAlarm;
        }
        
        
    }
    
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

    public SnmpInterfacePollerEventProcessor getEventProcessor() {
        return m_eventProcessor;
    }

    public void setEventProcessor(SnmpInterfacePollerEventProcessor eventProcessor) {
        m_eventProcessor = eventProcessor;
    }

    public QueryManager getQueryManager() {
        return m_queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        m_queryManager = queryManager;
    }

    public SnmpInterfacePollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    public void setPollerConfig(
            SnmpInterfacePollerConfig snmpinterfacepollerConfig) {
        m_pollerConfig = snmpinterfacepollerConfig;
    }

    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
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
        if(getEventProcessor()!=null) {
            getEventProcessor().close();
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
        
        // reset the alarm table
        //
        
        try {
            log().debug("start: select snmppoll alarms from alarm table");

            selectExistingSnmpPollAlarms();
        } catch (Exception sqlE) {
            log().error("start: Failed to delete existing snmppoll alarms", sqlE);
        }
        createScheduler();
        
        // Schedule the interfaces currently in the database
        //
        try {
            log().debug("start: Scheduling existing snmp interfaces polling");

            scheduleExistingSnmpInterface();
        } catch (Exception sqlE) {
            log().error("start: Failed to schedule existing interfaces", sqlE);
        }

        // Create an event receiver. The receiver will
        // receive events, process them, creates network
        // interfaces, and schedulers them.
        //
        try {
            log().debug("start: Creating event broadcast event processor");

            setEventProcessor(new SnmpInterfacePollerEventProcessor(this));
        } catch (Throwable t) {
            log().fatal("start: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }

        m_initialized = true;
        
    }
    protected void deleteSnmpInterface(int nodeid) {
        getNetwork().delete(getNetwork().getIp(nodeid));
    }
    
    protected void selectExistingSnmpPollAlarms() {
        m_nodeAlarmStatusMap = new HashMap<Integer, Map<Integer,AlarmStatus>>();
        String sql = "select nodeid, ifindex, eventuei from alarms where eventuei like 'uei.opennms.org/nodes/snmp/interface%'";
        Querier querier = new Querier(m_dataSource, sql) {

            public void processRow(ResultSet rs) throws SQLException {
                   setAlarmStatus(rs.getInt("nodeid"), rs.getInt("ifindex"), rs.getString("eventuei"));
            }
       
        };
       querier.execute();  
    }

    protected void setAlarmStatus(int nodeid, int ifindex, String uei) {
        Map<Integer,AlarmStatus> alarmStatusMap = new HashMap<Integer, AlarmStatus>();
        AlarmStatus alarmStatus = new AlarmStatus();
        if (m_nodeAlarmStatusMap.containsKey(nodeid)) {
            alarmStatusMap = m_nodeAlarmStatusMap.get(nodeid);
            if (alarmStatusMap.containsKey(ifindex))
                alarmStatus = alarmStatusMap.get(ifindex);
        }
        
        if (uei.equals(EventConstants.SNMP_INTERFACE_OPER_DOWN_EVENT_UEI))
            alarmStatus.setHasOperStatusDownAlarm(true);
        if (uei.equals(EventConstants.SNMP_INTERFACE_ADMIN_DOWN_EVENT_UEI))
            alarmStatus.setHasAdminStatusDownAlarm(true);        
        alarmStatusMap.put(ifindex,alarmStatus);
        m_nodeAlarmStatusMap.put(nodeid, alarmStatusMap);
    }

    protected void scheduleNewSnmpInterface(String ipaddr) {
 
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
            Package pkg = getPollerConfig().getPackageForAddress(ipaddress);
            if (pkg != null) {
                scheduleSnmpCollection(getNetwork().create(nodeid,ipaddress,pkg.getName()), pkg);
            }
        }
    }
    
    private void scheduleSnmpCollection(PollableInterface nodeGroup,Package pkg) {
        for (int j = 0; j < pkg.getInterfaceCount(); j++) {
            log().debug("found package interface with name: " + pkg.getInterface(j).getName());
            if (pkg.getInterface(j).getStatus().equals("on")) {
                log().debug("package interface: criteria: " + pkg.getInterface(j).getCriteria());
                log().debug("package interface: interval: " + pkg.getInterface(j).getInterval());
                
                
                PollableSnmpInterface node = nodeGroup.createPollableSnmpInterface(pkg.getInterface(j));
                scheduleSnmpCollection(node, pkg.getInterface(j).getCriteria(),pkg.getInterface(j).getInterval());
            } else {
                log().debug("package interface: status: " + pkg.getInterface(j).getStatus());
            }
        }
    }

    private void scheduleSnmpCollection(PollableSnmpInterface node, String criteria, long interval) {
        criteria = criteria + " and nodeid = " + node.getParent().getNodeid();
        List<OnmsSnmpInterface> snmpinterfacelist = getQueryManager().getSnmpInterfaces(criteria);
        if (snmpinterfacelist !=  null && snmpinterfacelist.size() > 0) {
            node.setSnmpinterfaces(snmpinterfacelist);
            if (m_nodeAlarmStatusMap.containsKey(node.getParent().getNodeid()))
                node.setAlarmStatus(m_nodeAlarmStatusMap.get(node.getParent().getNodeid()));
            else 
                node.setAlarmStatus(new HashMap<Integer,AlarmStatus>());
            
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
        } else {
            log().info("no interface found for node/criteria:" + node.getParent().getNodeid() + "/" + criteria);
        }
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

}
