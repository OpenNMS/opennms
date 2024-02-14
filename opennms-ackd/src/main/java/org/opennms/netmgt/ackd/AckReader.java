/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
