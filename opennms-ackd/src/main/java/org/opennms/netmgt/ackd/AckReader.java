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
 * Created: January 7, 2009
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
package org.opennms.netmgt.ackd;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.opennms.netmgt.ackd.readers.ReaderSchedule;


/**
 * Acknowledgment reader API
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 *
 */
public interface AckReader {

    public enum AckReaderState {
        STOP_PENDING(1, "Stop Pending"),
        STOPPED(2, "Stopped"),
        START_PENDING(3, "Start Pending"),
        STARTED(4, "Started"),
        PAUSE_PENDING(5, "Pause Pending"),
        PAUSED(6, "Paused"),
        RESUME_PENDING(7, "Resume Pending"),
        RESUMED(8, "Resumed")  //might be the same as started
        ; 
        
        private int m_id;
        private String m_label;
        
        AckReaderState(int id, String label) {
            m_id = id;
            m_label = label;
        }
        
        public int getId() {
            return m_id;
        }
        
        @Override
        public String toString() {
            return m_label;
        }
        
        
    };
    
    void start(final ScheduledThreadPoolExecutor executor, final ReaderSchedule schedule);
    void pause();
    void resume(final ScheduledThreadPoolExecutor executor);
    void stop();
    //void setSchedule(final ScheduledThreadPoolExecutor executor, ReaderSchedule schedule, boolean reschedule);
    
    AckReaderState getState();
    
    String getName();
}
