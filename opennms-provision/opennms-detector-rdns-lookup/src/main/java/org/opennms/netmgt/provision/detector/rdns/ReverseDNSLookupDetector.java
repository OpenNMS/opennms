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
package org.opennms.netmgt.provision.detector.rdns;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.provision.support.SyncAbstractDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbill.DNS.Address;

/**
 * Reverse DNS Lookup Detector will detect if there is a valid PTR record for a given IP Address.
 */
public class ReverseDNSLookupDetector extends SyncAbstractDetector {

    private static final Logger LOG = LoggerFactory.getLogger(ReverseDNSLookupDetector.class);

    private static final String SERVICE_NAME = "Reverse-DNS-Lookup";

    public ReverseDNSLookupDetector() {
        super(SERVICE_NAME, -1);
    }

    @Override
    public boolean isServiceDetected(InetAddress address) {
        String hostName = address.getCanonicalHostName();
        if (InetAddressUtils.str(address).equals(hostName)) {
            try {
                hostName = Address.getHostName(address);
                //Check again.
                if (!InetAddressUtils.str(address).equals(hostName)) {
                    return true;
                }
            } catch (UnknownHostException e) {
                LOG.warn("Failed to retrieve domain/hostname for {}.", address);
            } catch (Exception e) {
                LOG.warn("Unknown exception while retrieving domain/hostname for {}.", address);
            }
        } else {
            return true;
        }
        return false;
    }

    @Override
    protected void onInit() {
        //pass
    }


    @Override
    public void dispose() {
        //pass
    }
}
