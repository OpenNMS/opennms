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

package org.opennms.netmgt.notifd.jmx;

import org.opennms.netmgt.daemon.BaseOnmsMBean;

/**
 * <p>NotifdMBean interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface NotifdMBean extends BaseOnmsMBean {

    /**
     * @return The total number of notification tasks enqueued since Notifd
     *         was last started
     */
    public long getNotificationTasksQueued();

    /**
     * @return The total number of binary notifications attempted since Notifd
     *         was last started
     */
    public long getBinaryNoticesAttempted();

    /**
     * @return The total number of Java notifications attempted since Notifd
     *         was last started
     */
    public long getJavaNoticesAttempted();

    /**
     * @return The total number of attempted binary notifications that
     *         succeeded since Notifd was last started
     */
    public long getBinaryNoticesSucceeded();

    /**
     * @return The total number of attempted Java notifications that succeeded
     *         since Notifd was last started
     */
    public long getJavaNoticesSucceeded();

    /**
     * @return The total number of attempted binary notifications that failed
     *         (returned a non-zero exit code) since Notifd was last started
     */
    public long getBinaryNoticesFailed();

    /**
     * @return The total number of attempted Java notifications that failed
     *         (returned a non-zero value) since Notifd was last started
     */
    public long getJavaNoticesFailed();

    /**
     * @return The total number of attempted binary notifications that were
     *         interrupted (threw an exception) since Notifd was last started.
     */
    public long getBinaryNoticesInterrupted();

    /**
     * @return The total number of attempted Java notifications that were
     *         interrupted (threw an exception) since Notifd was last started
     */
    public long getJavaNoticesInterrupted();

    /**
     * @return The total number of unknown notifications that were interrupted
     *         (threw an exception) since Notifd was last started.
     */
    public long getUnknownNoticesInterrupted();
}
