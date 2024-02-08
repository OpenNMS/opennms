/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
