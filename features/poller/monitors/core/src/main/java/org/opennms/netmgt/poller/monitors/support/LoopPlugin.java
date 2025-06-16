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
package org.opennms.netmgt.poller.monitors.support;

import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.IPLike;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;

/**
 * <p>LoopPlugin class.</p>
 *
 * @author david
 * @version $Id: $
 */
public class LoopPlugin {

    private static final String m_protocolName = "LOOP";

    /* (non-Javadoc)
     * @see org.opennms.netmgt.capsd.Plugin#getProtocolName()
     */
    /**
     * <p>getProtocolName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getProtocolName() {
        return m_protocolName;
    }

    public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, null);
    }

    public boolean isProtocolSupported(InetAddress address, Map<String, Object> qualifiers) {
        
        if (qualifiers == null) {
            return false;
        }
        
        String ipMatch = getIpMatch(qualifiers);
        if (IPLike.matches(InetAddressUtils.str(address), ipMatch)) {
            return isSupported(qualifiers);
        } else {
            return false;
        }
        
    }

    private boolean isSupported(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "is-supported", "false").equalsIgnoreCase("true");
    }

    private String getIpMatch(Map<String, Object> parameters) {
        return ParameterMap.getKeyedString(parameters, "ip-match", "*.*.*.*");
    }

}
