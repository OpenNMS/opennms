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

/**
 * TaskBuilder
 *
 * @author brozow
 * @version $Id: $
 */
public class TaskBuilder<T extends ContainerTask<?>> {
    
    private T m_task;
    
    /**
     * <p>Constructor for TaskBuilder.</p>
     *
     * @param task a T object.
     * @param <T> a T object.
     */
    public TaskBuilder(T task) {
        m_task = task;
    }
    
    /**
     * <p>createSequence</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<SequenceTask> createSequence() {
        return m_task.getCoordinator().createSequence(m_task);
    }
    
    /**
     * <p>createBatch</p>
     *
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<BatchTask> createBatch() {
        return m_task.getCoordinator().createBatch(m_task);
    }
    
    /**
     * <p>setParent</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> setParent(ContainerTask<?> parent) {
        parent.add(m_task);
        return this;
    }
    
    /**
     * <p>addSequence</p>
     *
     * @param runnables a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> addSequence(Runnable... runnables) {
        createSequence().add(runnables).setParent(m_task);
        return this;
    }
    
    /**
     * <p>addSequence</p>
     *
     * @param runIns a {@link org.opennms.core.tasks.RunInBatch} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> addSequence(RunInBatch... runIns) {
        createSequence().add(runIns).setParent(m_task);
        return this;
    }
    
    /**
     * <p>addBatch</p>
     *
     * @param runnables a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> addBatch(Runnable... runnables) {
        createBatch().add(runnables).setParent(m_task);
        return this;
    }

    /**
     * <p>addBatch</p>
     *
     * @param runIns a {@link org.opennms.core.tasks.RunInBatch} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> addBatch(RunInBatch... runIns) {
        createBatch().add(runIns).setParent(m_task);
        return this;
    }

    
    /**
     * <p>add</p>
     *
     * @param runnables a {@link java.lang.Runnable} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> add(Runnable... runnables) {
        for(Runnable r : runnables) {
            m_task.add(r);
        }
        return this;
    }
    
    /**
     * <p>add</p>
     *
     * @param runIns a {@link org.opennms.core.tasks.RunInBatch} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> add(RunInBatch... runIns) {
        for(final RunInBatch runIn : runIns) {
            final TaskBuilder<BatchTask> bldr = createBatch();
            bldr.add(new Runnable() {
                @Override
                public void run() {
                    runIn.run(bldr.get());
                }
            }).setParent(m_task);
        }
        return this;
    }
    
    /**
     * <p>add</p>
     *
     * @param needers a {@link org.opennms.core.tasks.NeedsContainer} object.
     * @return a {@link org.opennms.core.tasks.TaskBuilder} object.
     */
    public TaskBuilder<T> add(NeedsContainer... needers) {
        for(final NeedsContainer needer : needers) {
            add(new Runnable() {
                @Override
                public void run() {
                    needer.run(m_task);
                }
            });
        }
        return this;
    }
    
    /**
     * <p>get</p>
     *
     * @return a T object.
     */
    public T get() {
        return m_task;
    }

    /**
     * <p>get</p>
     *
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @return a T object.
     */
    public T get(ContainerTask<?> parent) {
        return setParent(parent).get();
    }
    

}
