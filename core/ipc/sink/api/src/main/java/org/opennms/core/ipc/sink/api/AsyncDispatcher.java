/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.api;

import java.util.concurrent.CompletableFuture;

/**
 * Used to asynchronously dispatch messages.
 *
 * Instances of these should be created by the {@link MessageDispatcherFactory}.
 *
 * @author jwhite
 */
public interface AsyncDispatcher<S extends Message> extends AutoCloseable {

    /**
     * Asynchronously send the given message and return a future
     * that is resolved once the message was successfully dispatched.
     *
     * @param message the message to send
     * @return a future that is resolved once the message was dispatched
     */
    CompletableFuture<S> send(S message);

    /**
     * Returns the number of messages that are currently queued
     * awaiting for dispatch.
     *
     * @return current queue size
     */
    int getQueueSize();

}
