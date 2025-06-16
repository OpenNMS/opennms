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
package org.opennms.core.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class InetAddressXmlAdapter extends XmlAdapter<String, InetAddress> {

    private static final Logger LOG = LoggerFactory.getLogger(InetAddressXmlAdapter.class);

    /** {@inheritDoc} */
    @Override
    public String marshal(final InetAddress inetAddr) throws Exception {
        return inetAddr == null? null : new IPAddress(inetAddr).toDbString();
    }

    /** {@inheritDoc} */
    @Override
    public InetAddress unmarshal(final String ipAddr) throws Exception {
        try {
            return (ipAddr == null || ipAddr.isEmpty()) ? null : new IPAddress(ipAddr).toInetAddress();
        }
        catch (Throwable t) {
            LOG.warn("Invalid IP Address {}", ipAddr);
            return null;
        }
    }

}
