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
 * This {@link TaskMonitor} is used to log {@link Task} lifecycle events.
 *
 * @author brozow
 */
public class DefaultTaskMonitor implements TaskMonitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(DefaultTaskMonitor.class);

    /**
     * <p>Constructor for DefaultTaskMonitor.</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     */
    public DefaultTaskMonitor(final Task task) {
    }

    /** {@inheritDoc} */
    @Override
    public void completed(final Task task) {
    	LOG.trace("completed({})", task);
    }

    /** {@inheritDoc} */
    @Override
    public void prerequisiteAdded(final Task monitored, final Task prerequisite) {
        LOG.trace("prerequisiteAdded({}, {})", monitored, prerequisite);
    }

    /** {@inheritDoc} */
    @Override
    public void prerequisiteCompleted(final Task monitored, final Task prerequisite) {
    	LOG.trace("prerequisiteCompleted({}, {})", monitored, prerequisite);
    }

    /** {@inheritDoc} */
    @Override
    public void scheduled(final Task task) {
        LOG.trace("scheduled({})", task);
    }

    /** {@inheritDoc} */
    @Override
    public void started(final Task task) {
        LOG.trace("started({})", task);
    }

    /** {@inheritDoc} */
    @Override
    public void submitted(final Task task) {
        LOG.trace("submitted({})", task);
    }

    /** {@inheritDoc} */
    @Override
    public void monitorException(final Throwable t) {
    	LOG.trace("monitorException({})", t);
    }
    
    /** {@inheritDoc} */
    @Override
    public TaskMonitor getChildTaskMonitor(final Task task, final Task child) {
        return this;
    }

}
