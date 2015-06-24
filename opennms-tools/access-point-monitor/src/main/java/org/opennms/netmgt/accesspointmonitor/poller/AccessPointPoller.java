/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.accesspointmonitor.poller;

import java.util.Map;
import java.util.concurrent.Callable;

import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.opennms.netmgt.model.OnmsIpInterface;

public interface AccessPointPoller extends Callable<OnmsAccessPointCollection> {

    public void setAccessPointDao(AccessPointDao accessPointDao);

    public AccessPointDao getAccessPointDao();

    public void setInterfaceToPoll(OnmsIpInterface interfaceToPoll);

    public OnmsIpInterface getInterfaceToPoll();

    public void setPropertyMap(Map<String, String> parameters);

    public Map<String, String> getPropertyMap();

    public void setPackage(Package pkg);

    public Package getPackage();

}
