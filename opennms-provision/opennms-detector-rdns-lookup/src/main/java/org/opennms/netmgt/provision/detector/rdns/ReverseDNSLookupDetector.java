/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.rdns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.support.DetectResultsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

/**
 * Reverse DNS Lookup Detector will detect if there is FQDN match for a given IP Address.
 */
public class ReverseDNSLookupDetector implements SyncServiceDetector {

    private static final Logger LOG = LoggerFactory.getLogger(ReverseDNSLookupDetector.class);

    private static final String SERVICE_NAME = "reverse-DNS-lookup";

    @Override
    public DetectResults detect(DetectRequest request) {
        InetAddress address = request.getAddress();
        String hostName = address.getCanonicalHostName();
        if (InetAddressUtils.str(address).equals(hostName)) {
            try {
                hostName = Address.getHostName(address);
                //Check again.
                if (!InetAddressUtils.str(address).equals(hostName)) {
                    return new DetectResultsImpl(true);
                }
            } catch (UnknownHostException e) {
                LOG.warn("Failed to retrieve the fully qualified domain name for {}.", address);
            } catch (Exception e) {
                LOG.warn("Unknown exception while retrieving domain name for {}.", address);
            }
        } else {
            return new DetectResultsImpl(true);
        }
        return new DetectResultsImpl(false);
    }

    @Override
    public void init() {

    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void setServiceName(String serviceName) {

    }

    @Override
    public int getPort() {
        return 0;
    }

    @Override
    public void setPort(int port) {

    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public void setTimeout(int timeout) {

    }

    @Override
    public String getIpMatch() {
        return null;
    }

    @Override
    public void setIpMatch(String ipMatch) {

    }

    @Override
    public void dispose() {

    }
}
