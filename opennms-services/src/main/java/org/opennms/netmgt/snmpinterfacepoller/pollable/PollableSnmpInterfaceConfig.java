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
package org.opennms.netmgt.snmpinterfacepoller.pollable;

import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;

/**
 * Represents a PollableSnmpInterfaceConfig
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class PollableSnmpInterfaceConfig implements ScheduleInterval {

    private Timer m_timer;
    private long interval;
    
    /**
     * <p>Getter for the field <code>interval</code>.</p>
     *
     * @return a long.
     */
    @Override
    public long getInterval() {
        return interval;
    }

    /**
     * <p>scheduledSuspension</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean scheduledSuspension() {
        return false;
    }

    /**
     * <p>getCurrentTime</p>
     *
     * @return a long.
     */
    public long getCurrentTime() {
        return m_timer.getCurrentTime();
    }

    /**
     * <p>Constructor for PollableSnmpInterfaceConfig.</p>
     *
     * @param timer a {@link org.opennms.netmgt.scheduler.Timer} object.
     * @param interval a long.
     */
    public PollableSnmpInterfaceConfig(Timer timer, long interval) {
        super();
        m_timer = timer;
        this.interval = interval;
    }

}
