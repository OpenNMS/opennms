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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.dhcp.detector;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.PollerConfigManager;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.DetectRequestImpl;
import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import org.opennms.netmgt.config.poller.Package;

@Component
public class DhcpDetectorFactory extends GenericServiceDetectorFactory<DhcpDetector> {
    final Set<String> PARAMETER_KEYS = Sets.newHashSet("retry", "timeout", "macAddress", "relayMode", "extendedMode", "myAddress", "requestIpAddress");

    public DhcpDetectorFactory() {
        super(DhcpDetector.class);
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes) {
        Map<String, String> runtimeAttributes = Collections.emptyMap();

        final String serviceName = attributes.get("serviceName");

        if (!Strings.isNullOrEmpty(serviceName)) {
            final Package pkg = PollerConfigFactory.getInstance().getFirstPackageMatch(InetAddressUtils.str(address));

            if (pkg != null) {
                final Service service = pkg.getService(serviceName);
                runtimeAttributes = service.getParameters().stream()
                        .filter(p -> PARAMETER_KEYS.contains(p.getKey()))
                        .collect(Collectors.toMap(Parameter::getKey, Parameter::getValue));
            }
        }
        return new DetectRequestImpl(address, port, runtimeAttributes);
    }
}
