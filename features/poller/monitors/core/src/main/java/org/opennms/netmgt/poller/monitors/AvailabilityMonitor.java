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
package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.util.Map;

import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
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
public class AvailabilityMonitor extends AbstractServiceMonitor {
    
    
    public static final Logger LOG = LoggerFactory.getLogger(AvailabilityMonitor.class);

    private static final int DEFAULT_RETRY = 3;
    private static final int DEFAULT_TIMEOUT = 3000;

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

}
