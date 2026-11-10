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
package org.opennms.netmgt.provision.detector.smb.response;

import jcifs.netbios.NbtAddress;

/**
 * <p>NbtAddressResponse class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class NbtAddressResponse {
    
    private String m_address;
    private NbtAddress m_nbtAddress;
    
    /**
     * <p>receive</p>
     *
     * @param address a {@link java.lang.String} object.
     * @param nbtAddress a {@link jcifs.netbios.NbtAddress} object.
     */
    public void receive(String address, NbtAddress nbtAddress) {
        m_address = address;
        m_nbtAddress = nbtAddress;
    }
    
    /**
     * <p>validateAddressIsNotSame</p>
     * 
     * TODO: In ticket 1608, Antonio is asking why this validation is used.
     * Maybe the behavior needs to be changed?
     * 
     * "Something weird is here....why the address must be different?"
     * 
     * @see https://mynms.opennms.com/Ticket/Display.html?id=1608
     *
     * @return a boolean.
     */
    public boolean validateAddressIsNotSame() {
        if(m_nbtAddress.getHostName().equals(m_address)) {
           return false; 
        }else {
            return true;
        }
    }
}
