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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SyncTask
 *
 * @author brozow
 * @version $Id: $
 */
public class SyncTask extends AbstractTask {
	
	private static final Logger LOG = LoggerFactory.getLogger(SyncTask.class);
    
    private final Runnable m_action;

    private String m_preferredExecutor = TaskCoordinator.DEFAULT_EXECUTOR;
    
    /**
     * <p>Constructor for SyncTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param action a {@link java.lang.Runnable} object.
     */
    public SyncTask(TaskCoordinator coordinator, ContainerTask<?> parent, Runnable action) {
        this(coordinator, parent, action, TaskCoordinator.DEFAULT_EXECUTOR);
    }


    /**
     * <p>Constructor for SyncTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param action a {@link java.lang.Runnable} object.
     * @param preferredExecutor a {@link java.lang.String} object.
     */
    public SyncTask(TaskCoordinator coordinator, ContainerTask<?> parent, Runnable action, String preferredExecutor) {
        super(coordinator, parent);
        m_action = action;
        m_preferredExecutor = preferredExecutor;
    }

    /** {@inheritDoc} */
    @Override
    protected void doSubmit() {
        getCoordinator().submitToExecutor(getPreferredExecutor(), getRunnable(), this);
    }

    /**
     * This is the run method where the 'work' related to the Task gets down.  This method can be overridden
     * or a Runnable can be passed to the task in the constructor.  The Task is not complete until this method
     * finishes
     */
    private final void run() {
        if (m_action != null) {
            m_action.run();
        }
    }

    /**
     * This method is used by the TaskCoordinator to create runnable that will run this task
     */
    final Runnable getRunnable() {
        return new Runnable() {
          @Override
          public void run() {
              try {
                  SyncTask.this.run();
              } catch (Throwable t) {
                  LOG.debug("Exception occurred executing task " + SyncTask.this, t);
              }
          }
          @Override
          public String toString() { return "Runner for "+SyncTask.this; }
        };
    }

    /**
     * <p>getPreferredExecutor</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPreferredExecutor() {
        return m_preferredExecutor;
    }

    /**
     * <p>setPreferredExecutor</p>
     *
     * @param preferredExecutor a {@link java.lang.String} object.
     */
    public void setPreferredExecutor(String preferredExecutor) {
        m_preferredExecutor = preferredExecutor;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return m_action == null ? super.toString() : m_action.toString();
    }


}
