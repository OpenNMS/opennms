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
package org.opennms.core.tasks;

import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>SequenceTask class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class SequenceTask extends ContainerTask<SequenceTask> {
    
    private AtomicReference<AbstractTask> m_lastChild = new AtomicReference<AbstractTask>(null);

    /**
     * <p>Constructor for SequenceTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     */
    public SequenceTask(TaskCoordinator coordinator, ContainerTask<?> parent) {
        super(coordinator, parent);
        m_lastChild.set(getTriggerTask());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void addChildDependencies(AbstractTask child) {
        super.addChildDependencies(child);
        AbstractTask last = m_lastChild.getAndSet(child);
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
