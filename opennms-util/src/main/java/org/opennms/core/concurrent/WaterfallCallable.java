/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaterfallCallable implements Runnable {

    private static Logger LOG = LoggerFactory.getLogger(WaterfallCallable.class);

    private final Executor m_service;
    private final Callable<Callable<?>> m_callable;

    public WaterfallCallable(Executor service, Callable<Callable<?>> callable) {
        m_service = service;
        m_callable = callable;
    }

    /**
     * This method will execute the given {@link Callable} and if it returns a subsequent
     * {@link Callable}, it will enqueue the subsequent {@link Callable} on the same
     * {@link ExecutorO}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void run() {
        try {
            Callable next = m_callable.call();
            if (next != null) {
                m_service.execute(new WaterfallCallable(m_service, next));
            }
        } catch (Throwable e) {
            LOG.warn("Exception while executing callable: " + e.getMessage(), e);
        }
    }
}
