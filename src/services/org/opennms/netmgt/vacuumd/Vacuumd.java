//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2004 The OpenNMS Group, Inc.  All rights reserved.
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
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.VacuumdConfigFactory;

/**
 * Implements a daemon whose job it is to run periodic updates against the database
 * for database maintenance work.
 */
public class Vacuumd implements PausableFiber, Runnable {
    
    /** 
     * The log4j category used to log debug messsages
     * and statements.
     */
    private static final String LOG4J_CATEGORY  = "OpenNMS.Vacuumd";

    private static Vacuumd m_singleton;
    
    private Thread m_thread;
    
    private long m_startTime;
    
    private boolean m_stopped = false;
    
    private int m_status = START_PENDING;
    
    public synchronized static Vacuumd getSingleton() {
        if (m_singleton == null) {
            m_singleton = new Vacuumd();
        }
        return m_singleton;
    }

    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.vacuumd.jmx.VacuumdMBean#init()
     */
    public void init() {
        
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        
        Category log = ThreadCategory.getInstance(getClass());
        
        try {
            log.info("Loading the configuration file.");
            VacuumdConfigFactory.reload();
        }
        catch(MarshalException ex)
        {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
        catch(ValidationException ex)
        {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
        catch(IOException ex)
        {
            log.error("Failed to load outage configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }
        
        log.info("Vaccumd initialization complete");
        
    }
    /* (non-Javadoc)
     * @see org.opennms.core.fiber.Fiber#start()
     */
    public void start() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Starting Vacuumd");
        
        m_status = STARTING;
        m_startTime = System.currentTimeMillis();
        m_thread = new Thread(this, "Vacuumd-Thread");
        m_thread.start();
        m_status = RUNNING;

    }
    /* (non-Javadoc)
     * @see org.opennms.core.fiber.Fiber#stop()
     */
    public void stop() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Stopping Vacuumd");

        m_status = STOP_PENDING;
        m_stopped = true;
        m_status = STOPPED;
    }

    /* (non-Javadoc)
     * @see org.opennms.core.fiber.PausableFiber#pause()
     */
    public void pause() {
        if (m_status != RUNNING)
            return;

        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Pausing Vacuumd");

        m_status = PAUSE_PENDING;
        m_stopped = true;
        m_status = PAUSED;
    }
    /* (non-Javadoc)
     * @see org.opennms.core.fiber.PausableFiber#resume()
     */
    public void resume() {
         if (m_status != PAUSED)
            return;
        
         ThreadCategory.setPrefix(LOG4J_CATEGORY);
         Category log = ThreadCategory.getInstance(getClass());
         log.info("Resuming Vacuumd");

         m_thread = new Thread(this, "Vacuumd-Thread");
        m_thread.start();
    }
    /* (non-Javadoc)
     * @see org.opennms.core.fiber.Fiber#getName()
     */
    public String getName() {
        return "OpenNMS.Vacuumd";
    }
    /* (non-Javadoc)
     * @see org.opennms.core.fiber.Fiber#getStatus()
     */
    public int getStatus() {
        return m_status;
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        Category log = ThreadCategory.getInstance(getClass());
        log.info("Vacuumd scheduling started");

        long now = System.currentTimeMillis();
        long period = VacuumdConfigFactory.getInstance().getPeriod();
        
        log.info("Vacuumd sleeping until time to execute statements");
        
        long waitTime = Math.max(500L, period/10);
        
        while(!m_stopped && ((now - m_startTime) < period)) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                // FIXME: what do I do here?
            }
        }
        log.info("Vacuumd beginning to execute statements");
        
        if (!m_stopped) {
            String[] stmts = VacuumdConfigFactory.getInstance().getStatements();
            for(int i = 0; i < stmts.length; i++) {
                runUpdate(stmts[i]);
            }
            
        }
    }

    public void runUpdate(String sql) {
        Category log = ThreadCategory.getInstance(getClass());

        log.info("Vacuumd executing statement: "+sql);
        // update the database
        Connection dbConn = null;
        boolean commit = false;
        try {
            dbConn = DatabaseConnectionFactory.getInstance().getConnection();
            dbConn.setAutoCommit(false);
            
            PreparedStatement stmt = dbConn.prepareStatement(sql);
            int count = stmt.executeUpdate();
            stmt.close();

            if (log.isDebugEnabled()) log.debug("Vacuumd: Ran update "+sql+": this affected "+count+" rows");

            commit = true;
        } catch (SQLException ex) {
            log.error("Vacuumd:  Database error execuating statement  " + sql, ex);
        } finally {

            if (dbConn != null) try {
                if (commit) {
                    dbConn.commit();
                } else {
                    dbConn.rollback();
                }
            } catch (SQLException ex) {
            } finally {
                if (dbConn != null) try { dbConn.close(); } catch (Exception e) {}                
            }
        }

    }


}
