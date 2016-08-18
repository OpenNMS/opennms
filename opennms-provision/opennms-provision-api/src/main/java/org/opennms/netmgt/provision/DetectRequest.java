/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import java.net.InetAddress;
import java.util.Map;

/**
 * Groups all of the parameters required for making calls to {@link SyncServiceDetector#detect}
 * and {@link AsyncServiceDetector#detect}.
 *
 * The runtime attributes here differ from the properties and attributes that are configured
 * on the detector i.e. port, ipMatch, etc... These are used to store additional attributes
 * which pertain to the system's state and/or agent specific attributes i.e. the SNMP read community
 * of the agent (which is not defined the the detector's configuration).
 *
 * These requests should be created by calls to {@link ServiceDetectorFactory#buildRequest}.
 *
 * @author jwhite
 */
public interface DetectRequest {

    /**
     * @return the address of the host against with the detector should be invoked.
     */
    InetAddress getAddress();

    /**
     * @return additional attributes stored outside of the detector's configuration that
     * may be required when running the detector.
     */
    Map<String, String> getRuntimeAttributes();
}
