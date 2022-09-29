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
     * priority for executing runnables
     */
    private Integer m_priority;

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
     * @param priority the priority for executables
     * @param name a unique name that identifies this group
     */
    public SchedulableExecutableGroup(long interval, long initial, LegacyPriorityExecutor executor, int priority, String name) {
        super(interval,initial);
        Assert.notNull(executor);
        Assert.notNull(name);
        m_executor=executor;
        m_priority=priority;
        m_name=name;
    }

    public String getName() {
        return m_name;
    }

    public Integer getPriority() {
        return m_priority;
    }

    public void setPriority(Integer priority) {
        m_priority = priority;
    }


    public Set<Executable> getExecutables() {
        return m_executables;
    }


    public void add(Executable discovery, Integer priority) {
        synchronized (m_executables) {
            if (m_executables.add(discovery)) {
                discovery.setPriority(m_priority + priority);
                LOG.info("add: {}", discovery.getInfo());
            }
        }
    }

    public void add(Executable discovery) {
        synchronized (m_executables) {
            if (m_executables.add(discovery)) {
                discovery.setPriority(discovery.getPriority() + m_priority);
                LOG.info("add: {}", discovery.getInfo());
            }
        }
    }

    public void remove(Executable discovery) {
        synchronized (m_executables) {
            if (m_executables.remove(discovery)) {
                LOG.info("remove: {}", discovery.getInfo());
                discovery.setPriority(null);
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
