/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.daemon;

/**
 * <p>BaseOnmsMBean interface.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public interface BaseOnmsMBean {

    /**
     * Initialization invoked prior to start.
     */
    void init();

    /**
     * Starts the current managed bean.
     */
    void start();

    /**
     * Starts the current managed bean.
     */
    void stop();

    /**
     * The current status of the managed bean. This is a representation of the
     * managed bean's run state as defined by the Fiber
     * interface.
     *
     * @return a int.
     */
    int getStatus();

    /**
     * <p>status</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String status();

    /**
     * <p>getStatusText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getStatusText();

}
