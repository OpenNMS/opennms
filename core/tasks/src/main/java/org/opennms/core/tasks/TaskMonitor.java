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
