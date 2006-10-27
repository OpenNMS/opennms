//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.utils.ParameterMap;
/**
 * This class uses the Java 5 isReachable method to determine up/down and is
 * currently considered "experimental".  Please give it a try and let us
 * know.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@Distributable
public class AvailabilityMonitor extends IPv4Monitor {

    private static final int DEFAULT_RETRY = 3;
    private static final int DEFAULT_TIMEOUT = 3000;

    public void initialize(Map parameters) {
    }

    public void initialize(MonitoredService svc) {
    }

    public PollStatus poll(MonitoredService svc, Map parameters) {
        int serviceStatus = PollStatus.SERVICE_UNAVAILABLE;
        long responseTime = -1;
        String reason = null;
        int retries = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        boolean reachable = false;
        try {
            for (int i = 0; i < retries; i++) {
                long begin = System.nanoTime();
                reachable = svc.getAddress().isReachable(ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT));
                long end = System.nanoTime();
                responseTime =  end - begin;
                if (reachable) break;
            }
            reason = (reachable ? null : "Unreachable");
            serviceStatus = (reachable ? PollStatus.SERVICE_AVAILABLE : PollStatus.SERVICE_UNAVAILABLE);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return PollStatus.get(serviceStatus, reason, responseTime);
    }

    public void release() {
    }

    public void release(MonitoredService svc) {
    }

}
