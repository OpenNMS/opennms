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

package org.opennms.core.queue;

/**
 * <p>
 * The root of all exceptions dealing with queues that implement the
 * {@link FifoQueue FifoQueue} interface. This exception is the general purpose
 * exception that is thrown when a queue error occurs.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class FifoQueueException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = 4596596920225763462L;

    /**
     * Constructs a default instance of the exception with no message.
     */
    public FifoQueueException() {
        super();
    }

    /**
     * Constructs a new instance of the exception with the specific message.
     *
     * @param why
     *            The message associated with the exception
     */
    public FifoQueueException(String why) {
        super(why);
    }
}
