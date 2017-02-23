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

package org.opennms.netmgt.provision.support;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.provision.DetectRequest;

public class DetectRequestImpl implements DetectRequest {

    private final InetAddress address;
    private final Integer port;
    private final Map<String, String> runtimeAttributes;

    public DetectRequestImpl(InetAddress address, Integer port) {
        this(address, port, Collections.emptyMap());
    }

    public DetectRequestImpl(InetAddress address, Integer port, Map<String, String> runtimeAttributes) {
        this.address = Objects.requireNonNull(address);
        this.port = port;
        this.runtimeAttributes = Objects.requireNonNull(runtimeAttributes);
    }

    public InetAddress getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public Map<String, String> getRuntimeAttributes() {
        return runtimeAttributes;
    }

}
