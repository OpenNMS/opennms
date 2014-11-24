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
