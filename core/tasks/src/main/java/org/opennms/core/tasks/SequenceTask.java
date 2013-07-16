/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.tasks;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>SequenceTask class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SequenceTask extends ContainerTask<SequenceTask> {
    
    private AtomicReference<Task> m_lastChild = new AtomicReference<Task>(null);

    /**
     * <p>Constructor for SequenceTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     */
    public SequenceTask(DefaultTaskCoordinator coordinator, ContainerTask<?> parent) {
        super(coordinator, parent);
        m_lastChild.set(getTriggerTask());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void addChildDependencies(Task child) {
        super.addChildDependencies(child);
        Task last = m_lastChild.getAndSet(child);
        child.addPrerequisite(last);
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "sequenceTask";
    }
    
    


}
