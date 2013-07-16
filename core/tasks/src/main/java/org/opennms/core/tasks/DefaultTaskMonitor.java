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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DefaultTaskMonitor
 *
 * @author brozow
 * @version $Id: $
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
    public void prerequisiteAdded(final Task monitored, final Task prerequsite) {
        LOG.trace("prerequisiteAdded({}, {})", monitored, prerequsite);
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
