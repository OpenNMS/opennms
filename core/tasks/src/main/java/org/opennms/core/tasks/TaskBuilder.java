/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
