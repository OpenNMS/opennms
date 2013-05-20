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

package org.opennms.netmgt.poller.remote.support;

import java.io.Serializable;
import java.util.Date;

import org.opennms.netmgt.poller.remote.PolledService;
import org.opennms.netmgt.poller.remote.PollerConfiguration;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
class EmptyPollerConfiguration implements PollerConfiguration, Serializable {

    private static final long serialVersionUID = 6908427719063336610L;

    /**
     * <p>getConfigurationTimestamp</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Override
    public Date getConfigurationTimestamp() {
        return new Date(0);
    }

    /**
     * <p>getPolledServices</p>
     *
     * @return an array of {@link org.opennms.netmgt.poller.remote.PolledService} objects.
     */
    @Override
    public PolledService[] getPolledServices() {
        return new PolledService[0];
    }

    @Override
    public long getServerTime() {
        return 0;
    }
    
}
