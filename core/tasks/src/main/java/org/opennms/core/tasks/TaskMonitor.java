/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.tasks;

/**
 * This listener interface defines methods that are fired
 * during lifecycle events of a {@link Task}.
 *
 * @author brozow
 */
public interface TaskMonitor {
    
    /**
     * <p>prerequisiteAdded</p>
     *
     * @param monitored a {@link org.opennms.core.tasks.Task} object.
     * @param prerequsite a {@link org.opennms.core.tasks.Task} object.
     */
    public void prerequisiteAdded(Task monitored, Task prerequsite);
    
    /**
     * <p>prerequisiteCompleted</p>
     *
     * @param monitored a {@link org.opennms.core.tasks.Task} object.
     * @param prerequisite a {@link org.opennms.core.tasks.Task} object.
     */
    public void prerequisiteCompleted(Task monitored, Task prerequisite);
    
    /**
     * <p>scheduled</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     */
    public void scheduled(Task task);
    
    /**
     * <p>submitted</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     */
    public void submitted(Task task);
    
    /**
     * <p>started</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     */
    public void started(Task task);
    
    /**
     * <p>completed</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     */
    public void completed(Task task);
    
    /**
     * <p>getChildTaskMonitor</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     * @param child a {@link org.opennms.core.tasks.Task} object.
     * @return a {@link org.opennms.core.tasks.TaskMonitor} object.
     */
    public TaskMonitor getChildTaskMonitor(Task task, Task child);
    
    /**
     * This is called if an exception occurs while calling a monitor method
     *
     * @param t a {@link java.lang.Throwable} object.
     */
    public void monitorException(Throwable t);

}
