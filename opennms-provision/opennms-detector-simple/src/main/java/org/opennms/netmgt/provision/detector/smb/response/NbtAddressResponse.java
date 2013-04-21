/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
