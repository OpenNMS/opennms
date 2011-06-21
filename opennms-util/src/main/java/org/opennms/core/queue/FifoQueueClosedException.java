/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.core.queue;

/**
 * This exception is used to represent an error condition where an attempt is
 * made to add an element to a closed {@link ClosableFifoQueue queue}.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 */
public class FifoQueueClosedException extends FifoQueueException {
    /**
     * 
     */
    private static final long serialVersionUID = -9088896767584630679L;

    /**
     * Constructs a default instance of the excpetion.
     */
    public FifoQueueClosedException() {
        super();
    }

    /**
     * Constructs a new exception with the passed explination.
     *
     * @param why
     *            The explination message.
     */
    public FifoQueueClosedException(String why) {
        super(why);
    }
}
