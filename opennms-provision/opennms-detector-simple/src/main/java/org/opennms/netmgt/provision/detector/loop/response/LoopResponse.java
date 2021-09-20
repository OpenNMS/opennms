/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.loop.response;

import org.opennms.core.utils.IPLike;
import org.opennms.netmgt.provision.detector.simple.response.LineOrientedResponse;

/**
 * <p>LoopResponse class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class LoopResponse extends LineOrientedResponse {
    
    private String m_address;
    private boolean m_isSupported;
    
    /**
     * <p>Constructor for LoopResponse.</p>
     */
    public LoopResponse() {
        super("");
    }

    /**
     * <p>receive</p>
     *
     * @param address a {@link java.lang.String} object.
     * @param isSupported a boolean.
     */
    public void receive(String address, boolean isSupported) {
        m_address = address;
        m_isSupported = isSupported;
    }
    
    /**
     * <p>validateIPMatch</p>
     *
     * @param ip a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean validateIPMatch(String ip){
      if(IPLike.matches(m_address, ip)){
          return m_isSupported;
      }else{
        return false;  
      }
        
    }

}
