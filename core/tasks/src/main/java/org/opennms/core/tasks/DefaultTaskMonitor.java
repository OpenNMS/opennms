/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.core.tasks;

import org.opennms.core.utils.LogUtils;

/**
 * DefaultTaskMonitor
 *
 * @author brozow
 * @version $Id: $
 */
public class DefaultTaskMonitor implements TaskMonitor {

    /**
     * <p>Constructor for DefaultTaskMonitor.</p>
     *
     * @param task a {@link org.opennms.core.tasks.Task} object.
     */
    public DefaultTaskMonitor(final Task task) {
    }

    /** {@inheritDoc} */
    public void completed(final Task task) {
        log("completed(%s)", task);
    }

    /** {@inheritDoc} */
    public void prerequisiteAdded(final Task monitored, final Task prerequsite) {
        log("prerequisiteAdded(%s, %s)", monitored, prerequsite);
    }

    /** {@inheritDoc} */
    public void prerequisiteCompleted(final Task monitored, final Task prerequisite) {
        log("prerequisiteCompleted(%s, %s)", monitored, prerequisite);
    }

    /** {@inheritDoc} */
    public void scheduled(final Task task) {
        log("scheduled(%s)", task);
    }

    /** {@inheritDoc} */
    public void started(final Task task) {
        log("started(%s)", task);
    }

    /** {@inheritDoc} */
    public void submitted(final Task task) {
        log("submitted(%s)", task);
    }

    /** {@inheritDoc} */
    public void monitorException(final Throwable t) {
        log(t, "monitorException(%s)", t);
    }
    
    /** {@inheritDoc} */
    public TaskMonitor getChildTaskMonitor(final Task task, final Task child) {
        return this;
    }

    private void log(final String format, final Object... args) {
        if (LogUtils.isTraceEnabled(this)) {
            LogUtils.tracef(this, format, args);
        }
    }
    
    private void log(final Throwable t, final String format, final Object... args) {
        if (LogUtils.isTraceEnabled(this)) {
            LogUtils.tracef(this, t, format, args);
        }
    }

}
