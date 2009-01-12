package org.opennms.netmgt.snmpinterfacepoller;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

//import org.apache.log4j.Level;

import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableNetwork;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterfaceConfig;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableInterface;
import org.opennms.netmgt.utils.Querier;
import org.opennms.netmgt.utils.Updater;
import org.opennms.netmgt.config.SnmpInterfacePollerConfig;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.config.snmpinterfacepoller.Package;


public class Poller extends AbstractServiceDaemon {
    private final static Poller m_singleton = new Poller();

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private SnmpInterfacePollerEventProcessor m_eventProcessor;

    private QueryManager m_queryManager;
    
    private SnmpInterfacePollerConfig m_pollerConfig;
    
    private EventIpcManager m_eventMgr;

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

    public static Poller getInstance() {
        return m_singleton;
    }
    
    public Poller() {
        super("OpenNMS.SnmpPoller");
    }

    @Override
    protected void onInit() {
        
        // reset the alarm table
        //
        try {
            log().debug("start: Deleting snmppoll alarms from alarm table");

            deleteExistingSnmpPollAlarms();
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
    
    protected void deleteExistingSnmpPollAlarms() {
        String sql = "delete from alarms where eventuei like 'uei.opennms.org/nodes/snmp/interface%'";
        Updater updater = new Updater(m_dataSource, sql);
        updater.execute();
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
        node.setSnmpinterfaces(getQueryManager().getSnmpInterfaces(criteria));
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

}
