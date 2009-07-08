/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.core.queue;

/**
 * <p>
 * The root of all exceptions dealing with queues that implement the
 * {@link FifoQueue FifoQueue}interface. This exception is the general purpose
 * exception that is thrown when a queue error occurs.
 * </p>
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * 
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
     * Constructs a new instace of the exception with the specific message.
     * 
     * @param why
     *            The message associated with the exception
     */
    public FifoQueueException(String why) {
        super(why);
    }
}
