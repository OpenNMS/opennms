//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
// 
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//
// Tab Size = 8
//
//

package org.opennms.core.queue;

/**
 * <p>
 * This interface is implemented by FIFO queue implementations that can notify
 * interested listener when elements are added to the queue. This is useful for
 * listeners that may block or preform other work while a queue is empty,
 * instead of using polling.
 * </p>
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org">OpenNMS </a>
 * @version $Id: $
 */
public interface NotifiableInputFifoQueue<T> extends FifoQueue<T> {
    /**
     * Adds a new listener to the notifiable queue. If the listener already
     * exists then it is up to the implementor to determine behavior. When a new
     * element is added to the queue the listener will have its
     * {@link InputFifoQueueListener#onQueueInput callback}method invoked.
     *
     * @param listener
     *            The instance to be notified on queue additions.
     */
    public void addInputListener(InputFifoQueueListener listener);

    /**
     * Removes an already registered listener. If the listener was not already
     * registered then no action is performed.
     *
     * @param listener
     *            The listener to remove from the queue.
     */
    public void removeInputListener(InputFifoQueueListener listener);
}
