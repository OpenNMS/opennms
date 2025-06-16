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
package org.opennms.netmgt.vacuumd;

import org.opennms.netmgt.scheduler.ScheduleInterval;
/**
 * <p>AutomationInterval class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class AutomationInterval implements ScheduleInterval {

    /* (non-Javadoc)
     * @see org.opennms.netmgt.scheduler.ScheduleInterval#getInterval()
     */
    
    private long m_interval;
    
    /**
     * <p>Constructor for AutomationInterval.</p>
     *
     * @param interval a long.
     */
    public AutomationInterval(long interval) {
        setInterval(interval);
    }
    
    /**
     * <p>getInterval</p>
     *
     * @return a long.
     */
    @Override
    public long getInterval() {
        return m_interval;
    }
    
    /**
     * <p>setInterval</p>
     *
     * @param interval a long.
     */
    public void setInterval(long interval) {
        m_interval = interval;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.scheduler.ScheduleInterval#scheduledSuspension()
     */
    /**
     * <p>scheduledSuspension</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean scheduledSuspension() {
        // TODO Auto-generated method stub
        return false;
    }

}
