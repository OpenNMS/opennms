//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
// Copyright (C) 2001 Oculan Corp.  All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
// Tab Size = 8
//
//
package org.opennms.core.queue;

import java.lang.*;

/**
 * <p>This interface is implemented by objects that need to be notified
 * when a new element is added to a queue. The notification method is
 * invoked aftet the element is added to the queue, the exact semantics
 * of which are defined by the queue.</p>
 *
 * <p>This is most often used to notify a class that an empty queue
 * has new elements that need to be processed. This allows the object
 * to perform other potentially useful work while waiting on new queue
 * elements.</p>
 *
 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 *
 */
public interface InputFifoQueueListener
{
	/**
	 * This method is invoked by a queue implementation
	 * when a new element is added its queue. The exact
	 * instance when the method is invoked is dependent
	 * upon the implementation.
	 *
	 * @param queue	The queue where the element was added.
	 */
	public void onQueueInput(NotifiableInputFifoQueue queue);
}

