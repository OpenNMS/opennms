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

import org.springframework.util.Assert;

/**
 * <p>AsyncTask class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AsyncTask<T> extends AbstractTask {
    
    private final Async<T> m_async;
    private final Callback<T> m_callback;

    /**
     * <p>Constructor for AsyncTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param async a {@link org.opennms.core.tasks.Async} object.
     * @param <T> a T object.
     */
    public AsyncTask(TaskCoordinator coordinator, ContainerTask<?> parent, Async<T> async) {
        this(coordinator, parent, async, null);
    }
    
    /**
     * <p>Constructor for AsyncTask.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     * @param parent a {@link org.opennms.core.tasks.ContainerTask} object.
     * @param async a {@link org.opennms.core.tasks.Async} object.
     * @param callback a {@link org.opennms.core.tasks.Callback} object.
     */
    public AsyncTask(TaskCoordinator coordinator, ContainerTask<?> parent, Async<T> async, Callback<T> callback) {
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
            m_async.supplyAsyncThenAccept(callback);
        } catch (Throwable t) {
            callback.handleException(t);
        }
    }

    /**
     * <p>markTaskAsCompleted</p>
     */
    private final void markTaskAsCompleted() {
        getCoordinator().markTaskAsCompleted(this);
    }

    private Callback<T> callback() {
        return new Callback<T>() {
            @Override
            public void accept(T t) {
                try {
                    if (m_callback != null) {
                        m_callback.accept(t);
                    }
                } finally {
                    markTaskAsCompleted();
                }
            }
            @Override
            public T apply(Throwable t) {
                try {
                    if (m_callback != null) {
                        m_callback.handleException(t);
                    }
                } finally {
                    markTaskAsCompleted();
                }
                return null;
            }
        };
    }
    
}
