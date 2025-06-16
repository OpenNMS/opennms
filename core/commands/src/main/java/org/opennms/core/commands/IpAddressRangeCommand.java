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
package org.opennms.core.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.utils.InetAddressUtils;

@Command(scope = "opennms", name = "ip-addr-range", description = "Returns a list of IP addresses in a given range. Useful with closures.")
@Service
public class IpAddressRangeCommand implements Action {

    @Argument(index = 0, name = "start-address", description = "First IP address in range", required = true, multiValued = false)
    private String m_startAddress;
    
    @Argument(index = 1, name = "end-address", description = "Last IP address in range", required = true, multiValued = false)
    private String m_endAddress;

    
    @Override
    public Object execute() {
        InetAddress startAddr = null;
        InetAddress endAddr = null;
        try {
            startAddr = InetAddress.getByName(m_startAddress);
            endAddr = InetAddress.getByName(m_endAddress);
        } catch (UnknownHostException uhe) {
            System.out.println(uhe.getMessage());
            return null;
        }
        
        if (startAddr.getClass() != endAddr.getClass()) {
            System.out.println("Start and end addresses must be of same class (IPv4 / IPv6)");
            return null;
        }
        
        if (InetAddressUtils.difference(endAddr, startAddr).longValue() < 1) {
            System.out.println("Start address must be lower than end address.");
            return null;
        }

        String thisAddr = InetAddressUtils.str(startAddr);
        List<String> addresses = new ArrayList<>();
        addresses.add(thisAddr);
        for (int i = 0; i < InetAddressUtils.difference(endAddr,  startAddr).intValue(); i++) {
            try {
                thisAddr = InetAddressUtils.incr(thisAddr);
                addresses.add(thisAddr);
            } catch (UnknownHostException e) {
                // gulp
            }
        }
            
        return addresses; 
    }

}
