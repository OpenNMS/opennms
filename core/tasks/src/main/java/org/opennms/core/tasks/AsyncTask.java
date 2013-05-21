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

import org.springframework.util.Assert;

/**
 * <p>AsyncTask class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AsyncTask<T> extends Task {
    
    private final Async<T> m_async;
    private final Callback<T> m_callback;

    /**
     * <p>Constructor for AsyncTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param async a {@link org.opennms.core.tasks.Async} object.
     * @param <T> a T object.
     */
    public AsyncTask(DefaultTaskCoordinator coordinator, ContainerTask<?> parent, Async<T> async) {
        this(coordinator, parent, async, null);
    }
    
    /**
     * <p>Constructor for AsyncTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param async a {@link org.opennms.core.tasks.Async} object.
     * @param callback a {@link org.opennms.core.tasks.Callback} object.
     */
    public AsyncTask(DefaultTaskCoordinator coordinator, ContainerTask<?> parent, Async<T> async, Callback<T> callback) {
        super(coordinator, parent);
        Assert.notNull(async, "async parameter must not be null");
        m_async = async;
        m_callback = callback;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return String.valueOf(m_async);
    }

    /** {@inheritDoc} */
    @Override
    protected void doSubmit() {
        Callback<T> callback = callback();
        try {
            m_async.submit(callback);
        } catch (Throwable t) {
            callback.handleException(t);
        }
    }
    
    private Callback<T> callback() {
        return new Callback<T>() {
            @Override
            public void complete(T t) {
		try {
		    if (m_callback != null) {
			m_callback.complete(t);
		    }
		} finally {
		    markTaskAsCompleted();
		}
            }
            @Override
            public void handleException(Throwable t) {
		try {
		    if (m_callback != null) {
			m_callback.handleException(t);
		    }
		} finally {
		    markTaskAsCompleted();
		}
            }
        };
    }
    
}
