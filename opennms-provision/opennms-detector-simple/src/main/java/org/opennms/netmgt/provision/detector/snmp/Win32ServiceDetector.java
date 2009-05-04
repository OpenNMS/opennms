/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.snmp;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class Win32ServiceDetector extends SnmpDetector {
    
    private static final String SV_SVC_OPERATING_STATE_OID = ".1.3.6.1.4.1.77.1.2.3.1.3";
    private static final String DEFAULT_SERVICE_NAME = "Service";
    
    public Win32ServiceDetector(){
        setServiceName(DEFAULT_SERVICE_NAME);
        setOid(generateOid());
        setVbvalue("1");
    }

    private String generateOid() {
        int snLength = getServiceName().length();
        
        StringBuffer serviceOidBuf = new StringBuffer(SV_SVC_OPERATING_STATE_OID);
        serviceOidBuf.append(".").append(Integer.toString(snLength));
        for (byte thisByte : getServiceName().getBytes()) {
            serviceOidBuf.append(".").append(Byte.toString(thisByte));
        }
        
        return serviceOidBuf.toString();
    }
}
