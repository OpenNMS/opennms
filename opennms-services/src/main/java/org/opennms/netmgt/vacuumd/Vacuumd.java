/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 2007 Dec 27: Add non-transactional support for statements so that the PostgreSQL vacuum command can be called
 * 2007 May 21: Java 5 generics and loops, format code. - dj@opennms.org
 * 2007 Mar 13: Call VacuumdConfigFactory.init(), not reload(). - dj@opennms.org
 * 2004 Aug 28: Created this file.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.vacuumd;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Action;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.config.vacuumd.Statement;
import org.opennms.netmgt.config.vacuumd.Trigger;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

/**
 * Implements a daemon whose job it is to run periodic updates against the
 * database for database maintenance work.
 * 
 * @author <a href=mailto:brozow@opennms.org>Mathew Brozowski</a>
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @author <a href=mailto:dj@opennms.org>DJ Gregor</a>
 */
public class Vacuumd extends AbstractServiceDaemon implements Runnable, EventListener {

    private static volatile Vacuumd m_singleton;

    private volatile Thread m_thread;

    private volatile long m_startTime;

    private volatile boolean m_stopped = false;

    private volatile LegacyScheduler m_scheduler;

    private volatile EventIpcManager m_eventMgr;

    public synchronized static Vacuumd getSingleton() {
        if (m_singleton == null) {
            m_singleton = new Vacuumd();
        }
        return m_singleton;
    }

    public Vacuumd() {
        super("OpenNMS.Vacuumd");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#init()
     */
    @Override
    protected void onInit() {
        try {
            log().info("Loading the configuration file.");
            VacuumdConfigFactory.init();
            getEventManager().addEventListener(this, EventConstants.RELOAD_VACUUMD_CONFIG_UEI);

            initializeDataSources();
        } catch (Exception ex) {
            log().error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        log().info("Vaccumd initialization complete");

        createScheduler();
        scheduleAutomations();
    }

    private void initializeDataSources() throws MarshalException, ValidationException, IOException, ClassNotFoundException, PropertyVetoException, SQLException {
        for (Trigger trigger : getVacuumdConfig().getTriggers()) {
            DataSourceFactory.init(trigger.getDataSource());
        }

        for (Action action : getVacuumdConfig().getActions()) {
            DataSourceFactory.init(action.getDataSource());
        }
    }

    @Override
    protected void onStart() {
        m_startTime = System.currentTimeMillis();
        m_thread = new Thread(this, "Vacuumd-Thread");
        m_thread.start();
        m_scheduler.start();
    }

    @Override
    protected void onStop() {
        m_stopped = true;
        m_scheduler.stop();
    }

    @Override
    protected void onPause() {
        m_scheduler.pause();
        m_stopped = true;
    }

    @Override
    protected void onResume() {
        m_thread = new Thread(this, "Vacuumd-Thread");
        m_thread.start();
        m_scheduler.resume();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ThreadCategory.setPrefix(getName());
        log().info("Vacuumd scheduling started");

        long now = System.currentTimeMillis();
        long period = getVacuumdConfig().getPeriod();

        log().info("Vacuumd sleeping until time to execute statements period = " + period);

        long waitTime = 500L;

        while (!m_stopped) {
            try {
                now = waitPeriod(now, period, waitTime);

                log().info("Vacuumd beginning to execute statements");
                executeStatements();

                m_startTime = System.currentTimeMillis();

            } catch (Exception e) {
                log().error("Unexpected exception: ", e);
            }
        }
    }

    /**
     * 
     */
    protected void executeStatements() {
        if (!m_stopped) {
            List<Statement> statements = getVacuumdConfig().getStatements();
            for (Statement statement : statements) {
				runUpdate(statement.getContent(), statement.getTransactional());
			}
        }
    }

    /**
     * @param now
     * @param period
     * @param waitTime
     * @return
     */
    private long waitPeriod(long now, long period, long waitTime) {
        int count = 0;
        while (!m_stopped && ((now - m_startTime) < period)) {
            try {
                if (count % 100 == 0) {
                    log().debug("Vacuumd: " + (period - now + m_startTime) + "ms remaining to execution.");
                }
                Thread.sleep(waitTime);
                now = System.currentTimeMillis();
                count++;
            } catch (InterruptedException e) {
                // FIXME: what do I do here?
            }
        }
        return now;
    }

    private void runUpdate(String sql, boolean transactional) {
        log().info("Vacuumd executing statement: " + sql);
        // update the database
        Connection dbConn = null;
        
        //initially set doCommit to avoid doing a commit in the finally
        //if an exception is thrown.        
        boolean commitRequired = false;
        boolean autoCommitFlag = !transactional;
        try {
            dbConn = getDataSourceFactory().getConnection();
            dbConn.setAutoCommit(autoCommitFlag);

            PreparedStatement stmt = dbConn.prepareStatement(sql);
            int count = stmt.executeUpdate();
            stmt.close();

            if (log().isDebugEnabled()) {
                log().debug("Vacuumd: Ran update " + sql + ": this affected " + count + " rows");
            }

            commitRequired = transactional;
        } catch (SQLException ex) {
            log().error("Vacuumd:  Database error execuating statement  " + sql, ex);
        } finally {
            if (dbConn != null) {
                try {
                    if (commitRequired) {
                        dbConn.commit();
                    } else if (transactional) {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                } finally {
                    if (dbConn != null) {
                        try {
                            dbConn.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    private void createScheduler() {
        try {
            log().debug("init: Creating Vacuumd scheduler");
            m_scheduler = new LegacyScheduler("Vacuumd", 2);
        } catch (RuntimeException e) {
            log().fatal("init: Failed to create Vacuumd scheduler: " + e, e);
            throw e;
        }
    }

    public Scheduler getScheduler() {
        return m_scheduler;
    }

    private void scheduleAutomations() {
        for (Automation auto : getVacuumdConfig().getAutomations()) {
            scheduleAutomation(auto);
        }
    }

    private void scheduleAutomation(Automation auto) {
        if (auto.getActive()) {
            AutomationProcessor ap = new AutomationProcessor(auto);
            Schedule s = new Schedule(ap, new AutomationInterval(auto.getInterval()), m_scheduler);
            ap.setSchedule(s);
            s.schedule();
        }
    }

    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    public void onEvent(Event event) {
        if (isReloadConfigEvent(event)) {
            try {
                log().info("onEvent: reloading configuration.");
                log().debug("onEvent: Number of elements in schedule:"+m_scheduler.getScheduled());
                log().debug("onEvent: calling stop on scheduler.");

                stop();
                while (m_scheduler.getRunner().getStatus() != STOPPED || m_scheduler.getStatus() != STOPPED) {
                    log().debug("onEvent: waiting for scheduler to stop." +
                            " Current status of scheduler: "+m_scheduler.getStatus()+"; Current status of runner: "+m_scheduler.getRunner().getStatus());
                    Thread.sleep(500);
                }
                log().debug("onEvent: Current status of scheduler: "+m_scheduler.getStatus()+"; Current status of runner: "+m_scheduler.getRunner().getStatus());
                log().debug("onEvent: Number of elements in schedule:"+m_scheduler.getScheduled());
                log().debug("onEvent: reloading vacuumd configuration.");

                VacuumdConfigFactory.reload();
                log().debug("onEvent: creating new schedule and rescheduling automations.");

                init();
                log().debug("onEvent: restarting vacuumd and scheduler.");

                start();
                log().debug("onEvent: Number of elements in schedule:"+m_scheduler.getScheduled());
            } catch (MarshalException e) {
                log().error("onEvent: problem marshaling vacuumd configuration: " + e, e);
            } catch (ValidationException e) {
                log().error("onEvent: problem validating vacuumd configuration: " + e, e);
            } catch (IOException e) {
                log().error("onEvent: IO problem reading vacuumd configuration: " + e, e);
            } catch (InterruptedException e) {
                log().error("onEvent: Problem interrupting current Vacuumd Thread: " + e, e);
            }
            log().info("onEvent: completed configuration reload.");
        }
    }

    private boolean isReloadConfigEvent(Event event) {
        boolean isTarget = false;
        
        if (EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(event.getUei())) {
            List<Parm> parmCollection = event.getParms().getParmCollection();
            
            for (Parm parm : parmCollection) {
                if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && "Vacuumd".equalsIgnoreCase(parm.getValue().getContent())) {
                    isTarget = true;
                    break;
                }
            }
        
        //Depreciating this one...
        } else if (EventConstants.RELOAD_VACUUMD_CONFIG_UEI.equals(event.getUei())) {
            isTarget = true;
        }
        
        return isTarget;
    }
    
    private VacuumdConfigFactory getVacuumdConfig() {
        return VacuumdConfigFactory.getInstance();
    }
    
    private DataSource getDataSourceFactory() {
        return DataSourceFactory.getInstance();
    }
}
