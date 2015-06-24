/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ackd;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.opennms.netmgt.ackd.readers.AckProcessor;
import org.opennms.netmgt.ackd.readers.ReaderSchedule;


/**
 * Acknowledgment reader API
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
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
    
    /**
     * <p>start</p>
     *
     * @param executor a {@link java.util.concurrent.ScheduledThreadPoolExecutor} object.
     * @param schedule a {@link org.opennms.netmgt.ackd.readers.ReaderSchedule} object.
     * @param reloadConfig a boolean.
     */
    void start(final ScheduledThreadPoolExecutor executor, final ReaderSchedule schedule, boolean reloadConfig);
    /**
     * <p>pause</p>
     */
    void pause();
    /**
     * <p>resume</p>
     *
     * @param executor a {@link java.util.concurrent.ScheduledThreadPoolExecutor} object.
     */
    void resume(final ScheduledThreadPoolExecutor executor);
    /**
     * <p>stop</p>
     */
    void stop();
    //void setSchedule(final ScheduledThreadPoolExecutor executor, ReaderSchedule schedule, boolean reschedule);
    
    /**
     * <p>setAckProcessor</p>
     *
     * @param ackProcessor a {@link org.opennms.netmgt.ackd.readers.AckProcessor} object.
     */
    void setAckProcessor(AckProcessor ackProcessor);
    /**
     * <p>getAckProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.ackd.readers.AckProcessor} object.
     */
    AckProcessor getAckProcessor();
    
    /**
     * <p>getState</p>
     *
     * @return a {@link org.opennms.netmgt.ackd.AckReader.AckReaderState} object.
     */
    AckReaderState getState();
    
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getName();
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    void setName(String name);
}
