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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class SchedulableExecutableGroup extends Schedulable {
    private static final Logger LOG = LoggerFactory.getLogger(SchedulableExecutableGroup.class);

    private final Set<Executable> m_executables = new HashSet<>();

    /**
     *  The Executor Group
     *  this will execute discovery
     */
    private final LegacyPriorityExecutor m_executor;

    /**
     * name under which are executed runnables
     */
    private final String m_name;

    /**
     * Constructs a new Group of Collection. The collection is not scheduled until the
     * <code>run</code> method is invoked.
     *
     * @param interval the time in msec between group of collections
     * @param initial the time in msec wait before performing a collection at all
     * @param executor the executor service that will perform the single collections
     * @param name a unique name that identifies this group
     */
    public SchedulableExecutableGroup(long interval, long initial, LegacyPriorityExecutor executor, String name) {
        super(interval,initial);
        Assert.notNull(executor);
        Assert.notNull(name);
        m_executor=executor;
        m_name=name;
    }

    public String getName() {
        return m_name;
    }

    public Set<Executable> getExecutables() {
        return m_executables;
    }

    public void add(Executable discovery) {
        synchronized (m_executables) {
            if (m_executables.add(discovery)) {
                LOG.info("add: {}", discovery.getInfo());
            }
        }
    }

    public void remove(Executable discovery) {
        synchronized (m_executables) {
            if (m_executables.remove(discovery)) {
                LOG.info("remove: {}", discovery.getInfo());
            }
        }
    }

    public void clear() {
        synchronized (m_executables) {
            m_executables.clear();
        }
    }

    @Override
    public void runSchedulable() {
        LOG.info("run: {}", m_name);
        m_executables.forEach(m_executor::addPriorityReadyRunnable);
    }

}
