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

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * This class uses the Java 5 isReachable method to determine up/down and is
 * currently considered "experimental".  Please give it a try and let us
 * know.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@Distributable
public class AvailabilityMonitor extends AbstractServiceMonitor {
    
    
    public static final Logger LOG = LoggerFactory.getLogger(AvailabilityMonitor.class);

    private static final int DEFAULT_RETRY = 3;
    private static final int DEFAULT_TIMEOUT = 3000;

    /** {@inheritDoc} */
    @Override
    public void initialize(Map<String, Object> parameters) {
    }

    /**
     * <p>initialize</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    @Override
    public void initialize(MonitoredService svc) {
    }

    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        
        TimeoutTracker timeoutTracker = new TimeoutTracker(parameters, DEFAULT_RETRY, DEFAULT_TIMEOUT);
        
        for(timeoutTracker.reset(); timeoutTracker.shouldRetry(); timeoutTracker.nextAttempt()) {
            try {
                timeoutTracker.startAttempt();
                if (svc.getAddress().isReachable(timeoutTracker.getSoTimeout())) {
                    return PollStatus.available(timeoutTracker.elapsedTimeInMillis());
                }
            } catch (IOException e) {
                LOG.debug("Unable to contact {}", svc.getIpAddr(), e);
            }
        }
        String reason = svc+" failed to respond";
        
        LOG.debug(reason);
        return PollStatus.unavailable(reason);
    }

    /**
     * <p>release</p>
     */
    @Override
    public void release() {
    }

    /** {@inheritDoc} */
    @Override
    public void release(MonitoredService svc) {
    }

}
