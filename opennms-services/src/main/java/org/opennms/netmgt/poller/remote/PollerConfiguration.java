/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote;

import java.util.Date;

/**
 * <p>PollerConfiguration interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollerConfiguration {
    
    /**
     * This is the time on the server just before it is sent to the configuration 
     * to the client in millis since the epoch UTC. 
     * (Same as returned by System.currentTimeMillis())
     */
    long getServerTime();
    
    /**
     * <p>getConfigurationTimestamp</p>
     *
     * @return a {@link java.util.Date} object.
     */
    Date getConfigurationTimestamp();
	
    /**
     * <p>getPolledServices</p>
     *
     * @return an array of {@link org.opennms.netmgt.poller.remote.PolledService} objects.
     */
    PolledService[] getPolledServices();
}
