//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Aug 28, 2004: Created this file.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.vacuumd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.VacuumdConfigFactory;
import org.opennms.netmgt.config.vacuumd.Automation;
import org.opennms.netmgt.daemon.ServiceDaemon;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.Scheduler;

/**
 * Implements a daemon whose job it is to run periodic updates against the
 * database for database maintenance work.
 */
public class Vacuumd extends ServiceDaemon implements Runnable {

    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Vacuumd";

    private static Vacuumd m_singleton;

    private Thread m_thread;

    private long m_startTime;

    private boolean m_stopped = false;

    private Scheduler m_scheduler;

    public synchronized static Vacuumd getSingleton() {
        if (m_singleton == null) {
            m_singleton = new Vacuumd();
        }
        return m_singleton;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#init()
     */
    public void init() {

        ThreadCategory.setPrefix(LOG4J_CATEGORY);

        Category log = ThreadCategory.getInstance(getClass());

        try {
            log.info("Loading the configuration file.");
            VacuumdConfigFactory.reload();
        } catch (MarshalException ex) {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        log.info("Vaccumd initialization complete");
        
        createScheduler();
        scheduleAutomations();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.core.fiber.Fiber#start()
     */
    public void start() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Starting Vacuumd");

        setStatus(START_PENDING);
        m_startTime = System.currentTimeMillis();
        m_thread = new Thread(this, "Vacuumd-Thread");
        setStatus(STARTING);
        m_thread.start();
        m_scheduler.start();

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.core.fiber.Fiber#stop()
     */
    public void stop() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Stopping Vacuumd");

        setStatus(STOP_PENDING);
        m_stopped = true;
        setStatus(STOPPED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.core.fiber.PausableFiber#pause()
     */
    public void pause() {
        if (!isRunning())
            return;

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Pausing Vacuumd");

        setStatus(PAUSE_PENDING);
        m_scheduler.pause();
        m_stopped = true;
        setStatus(PAUSED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.core.fiber.PausableFiber#resume()
     */
    public void resume() {
        if (!isPaused())
            return;

        setStatus(RESUME_PENDING);
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Resuming Vacuumd");

        m_thread = new Thread(this, "Vacuumd-Thread");
        setStatus(STARTING);
        m_scheduler.resume();
        m_thread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.opennms.core.fiber.Fiber#getName()
     */
    public String getName() {
        return "OpenNMS.Vacuumd";
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Vacuumd scheduling started");
        setStatus(RUNNING);

        long now = System.currentTimeMillis();
        long period = VacuumdConfigFactory.getInstance().getPeriod();

        log.info("Vacuumd sleeping until time to execute statements period = " + period);

        long waitTime = 500L;

        while (!m_stopped) {

            try {
                now = waitPeriod(log, now, period, waitTime);

                log.info("Vacuumd beginning to execute statements");
                executeStatements();

                m_startTime = System.currentTimeMillis();

            } catch (Exception e) {
                log.error("Unexpected exception: ", e);
            }
        }
    }

    /**
     * 
     */
    private void executeStatements() {
        if (!m_stopped) {
            String[] stmts = VacuumdConfigFactory.getInstance().getStatements();
            for (int i = 0; i < stmts.length; i++) {
                runUpdate(stmts[i]);
            }

        }
    }

    /**
     * @param log
     * @param now
     * @param period
     * @param waitTime
     * @return
     */
    private long waitPeriod(Category log, long now, long period, long waitTime) {
        int count = 0;
        while (!m_stopped && ((now - m_startTime) < period)) {
            try {

                if (count % 100 == 0) {
                    log.debug("Vacuumd: " + (period - now + m_startTime) + " millis remaining to execution.");
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
    
    private ResultSet runTrigger(String sql) {
        return null;
    }
    
    private void runAction(String sql) {
        runUpdate(sql);
    }

    private void runUpdate(String sql) {
        Category log = ThreadCategory.getInstance(getClass());

        log.info("Vacuumd executing statement: " + sql);
        // update the database
        Connection dbConn = null;
        boolean commit = false;
        try {
            dbConn = DatabaseConnectionFactory.getInstance().getConnection();
            dbConn.setAutoCommit(false);

            PreparedStatement stmt = dbConn.prepareStatement(sql);
            int count = stmt.executeUpdate();
            stmt.close();

            if (log.isDebugEnabled())
                log.debug("Vacuumd: Ran update " + sql + ": this affected " + count + " rows");

            commit = true;
        } catch (SQLException ex) {
            log.error("Vacuumd:  Database error execuating statement  " + sql, ex);
        } finally {

            if (dbConn != null)
                try {
                    if (commit) {
                        dbConn.commit();
                    } else {
                        dbConn.rollback();
                    }
                } catch (SQLException ex) {
                } finally {
                    if (dbConn != null)
                        try {
                            dbConn.close();
                        } catch (Exception e) {
                        }
                }
        }

    }
    
    private void createScheduler() {
        Category log = ThreadCategory.getInstance(getClass());
        // Create a scheduler
        //
        try {
            log.debug("init: Creating Vacuumd scheduler");
            m_scheduler = new Scheduler("Vacuumd", 2);
        } catch (RuntimeException e) {
            log.fatal("init: Failed to create Vacuumd scheduler", e);
            throw e;
        }
    }
    
    public Scheduler getScheduler() {
        return m_scheduler;
    }
    
    private void scheduleAutomations() {
        
        Collection autos = VacuumdConfigFactory.getInstance().getAutomations();
        Iterator it = autos.iterator();
        
        while (it.hasNext()) {
            
            scheduleAutomation((Automation)it.next());
            
        }
    }
    
    private void scheduleAutomation(Automation auto) {
        
        AutomationProcessor ap = new AutomationProcessor();
        ap.setAutomation(auto);
        Schedule s = new Schedule(ap, new AutomationInterval(auto.getInterval()), m_scheduler);
        ap.setSchedule(s);
        s.schedule();
    }

}
