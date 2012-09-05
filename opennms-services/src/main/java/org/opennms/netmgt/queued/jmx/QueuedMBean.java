/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
