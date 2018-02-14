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

package org.opennms.netmgt.poller.monitors;

import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.SocketWrapper;
import org.opennms.core.utils.SslSocketWrapper;
import org.opennms.netmgt.poller.Distributable;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the IMAPS service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 *
 */
@Distributable
final public class ImapsMonitor extends ImapMonitor {

    /**
     * Default IMAPS ports.
     */
    private static final int DEFAULT_IMAPS_PORT = 993;

    @Override
    protected int determinePort(Map<String, Object> parameters) {
        return ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_IMAPS_PORT);
    }

    /** {@inheritDoc} */
    @Override
    protected SocketWrapper getSocketWrapper() {
        return new SslSocketWrapper();
    }

}
