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
