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
 * This exception is used to represent an error condition where an 
 * attempt is made to add an element to a closed
 * {@link ClosableFifoQueue queue}.
 *
 * @author <a href="mailto:weave@opennms.org">Brian Weaver</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 *
 */
public class FifoQueueClosedException
	extends FifoQueueException
{
	/**
	 * Constructs a default instance of the
	 * excpetion.
	 */
	public FifoQueueClosedException()
	{
		super();
	}

	/**
	 * Constructs a new exception with the passed
	 * explination.
	 *
	 * @param why	The explination message.
	 */
	public FifoQueueClosedException(String why)
	{
		super(why);
	}
}
