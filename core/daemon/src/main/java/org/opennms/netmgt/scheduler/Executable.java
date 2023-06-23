/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Executable implements PriorityReadyRunnable, Comparable<Executable> {

    private static final Logger LOG = LoggerFactory.getLogger(Executable.class);

    private boolean m_suspend = false;

    private Integer m_priority = 0;

    public Integer getPriority() {
          return m_priority;
    }

    public void setPriority(Integer priority) {
        m_priority=priority;
    }

    public Executable() {
    }

    public Executable(int priority) {
        m_priority = priority;
    }

    public abstract String getName();
    public abstract void runExecutable();
    
    // run is called by a Thread for the runnable
    // execute is where you got the stuff made
    public void run() {
        //if collection is suspended then
        // schedule the collection
        if (m_suspend) {
            LOG.info( "run: suspended {}", 
                      getInfo());
            return;
        }
        LOG.info( "run: running {}", 
                      getInfo());
        runExecutable();
    }

    /**
     * <p>
     * suspend
     * </p>
     */
    public void suspend() {
        m_suspend = true;
    }

    /**
     * <p>
     * wakeUp
     * </p>
     */
    public void wakeUp() {
        m_suspend = false;
    }

    /**
     * <p>
     * getInfo
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getInfo() {
        return  getName() + ": Priority: " + m_priority;
    }

    @Override
    public int compareTo(Executable o) {
        return m_priority-o.getPriority();
    }
}
