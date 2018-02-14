/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.passive.PassiveStatusKeeper;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;

 /**
 * <p>PassiveServiceMonitor class.</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
// this retrieves data from the deamon so it is not Distributable
@Distributable(DistributionContext.DAEMON)
public class PassiveServiceMonitor extends AbstractServiceMonitor {

    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
    	return PassiveStatusKeeper.getInstance().getStatus(svc.getNodeLabel(), svc.getIpAddr(), svc.getSvcName());
    }

    @Override
    public String getEffectiveLocation(String location) {
        // Always run the PSK in the same JVM
        return MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID;
    }
}
