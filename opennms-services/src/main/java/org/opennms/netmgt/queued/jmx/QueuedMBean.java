/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: August 31, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.queued.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>QueuedMBean interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface QueuedMBean extends BaseOnmsMBean {
	/**
	 * <p>getElapsedTime</p>
	 *
	 * @return a long.
	 */
	public long getElapsedTime();
	/**
	 * <p>getTotalOperationsPending</p>
	 *
	 * @return a long.
	 */
	public long getTotalOperationsPending();
	/**
	 * <p>getSignificantOpsCompleted</p>
	 *
	 * @return a long.
	 */
	public long getSignificantOpsCompleted();
	/**
	 * <p>getCreatesCompleted</p>
	 *
	 * @return a long.
	 */
	public long getCreatesCompleted();
	/**
	 * <p>getUpdatesCompleted</p>
	 *
	 * @return a long.
	 */
	public long getUpdatesCompleted();
	/**
	 * <p>getErrors</p>
	 *
	 * @return a long.
	 */
	public long getErrors();
	/**
	 * <p>getPromotionCount</p>
	 *
	 * @return a long.
	 */
	public long getPromotionCount();
	/**
	 * <p>getSignificantOpsEnqueued</p>
	 *
	 * @return a long.
	 */
	public long getSignificantOpsEnqueued();
	/**
	 * <p>getSignificantOpsDequeued</p>
	 *
	 * @return a long.
	 */
	public long getSignificantOpsDequeued();
	/**
	 * <p>getEnqueuedOperations</p>
	 *
	 * @return a long.
	 */
	public long getEnqueuedOperations();
	/**
	 * <p>getDequeuedOperations</p>
	 *
	 * @return a long.
	 */
	public long getDequeuedOperations();
	/**
	 * <p>getDequeuedItems</p>
	 *
	 * @return a long.
	 */
	public long getDequeuedItems();
	/**
	 * <p>getStartTime</p>
	 *
	 * @return a long.
	 */
	public long getStartTime();

}
