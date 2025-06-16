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
package org.opennms.netmgt.notifd;

import org.opennms.core.fiber.PausableFiber;

/**
 * This interface defines a handler for a Notifd queue. As notifications are
 * parsed from events they will be put on a process queue and will be handled by
 * a class implementing this interface.
 *
 * @author <a href="mailto:jason@opennms.org">Jason Johns </a>
 * @author <a href="http://www.opennms.org/>OpenNMS </a>
 * @author <a href="mailto:jason@opennms.org">Jason Johns </a>
 * @author <a href="http://www.opennms.org/>OpenNMS </a>
 * @version $Id: $
 */
public interface NotifdQueueHandler extends Runnable, PausableFiber {
    /**
     * <p>setQueueID</p>
     *
     * @param queueID a {@link java.lang.String} object.
     */
    public void setQueueID(String queueID);

    /**
     * <p>setNoticeQueue</p>
     *
     * @param queue a {@link org.opennms.netmgt.notifd.NoticeQueue} object.
     */
    public void setNoticeQueue(NoticeQueue queue);

    /**
     * <p>setInterval</p>
     *
     * @param interval a {@link java.lang.String} object.
     */
    public void setInterval(String interval);

    /**
     * <p>processQueue</p>
     */
    public void processQueue();
}
