//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 22: Be explicit about visibility and pass around the
//              Snmp4JStrategy that created us. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp.snmp4j;

import org.opennms.netmgt.snmp.SnmpConfiguration;
import org.opennms.netmgt.snmp.SnmpV3TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.snmp4j.ScopedPDU;

public class Snmp4JV3InformBuilder extends Snmp4JV2TrapBuilder implements SnmpV3TrapBuilder {
    
    protected Snmp4JV3InformBuilder(Snmp4JStrategy strategy) {
        super(strategy, new ScopedPDU(), ScopedPDU.INFORM);
    }
    
    @Override
    public SnmpValue[] sendInform(String destAddr, int destPort, int timeout, int retry, String community) throws Exception {
    	return super.sendInform(destAddr, destPort, 1000, 3, SnmpConfiguration.NOAUTH_NOPRIV, community, SnmpConfiguration.DEFAULT_AUTH_PASS_PHRASE,
    			SnmpConfiguration.DEFAULT_AUTH_PROTOCOL, SnmpConfiguration.DEFAULT_PRIV_PASS_PHRASE, SnmpConfiguration.DEFAULT_PRIV_PROTOCOL);
    }  
}
