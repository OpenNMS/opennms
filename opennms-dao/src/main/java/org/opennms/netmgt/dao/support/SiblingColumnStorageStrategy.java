//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.support;

import java.util.List;

import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */
public class SiblingColumnStorageStrategy extends IndexStorageStrategy {
    private static final String PARAM_SIBLING_COLUMN_OID = "sibling-column-oid";
    private String m_siblingColumnOid;
        
    @Override
    public String getResourceNameFromIndex(String resourceParent, String resourceIndex) {
        SnmpObjId oid = SnmpObjId.get(m_siblingColumnOid + "." + resourceIndex);
        SnmpValue snmpValue = SnmpUtils.get(m_storageStrategyService.getAgentConfig(), oid);
        String value = (snmpValue != null ? snmpValue.toString() : resourceIndex);

        /*
         * 1. Special-case a bare "/" to come out as "_slash_"
         * 2. Eliminate non-US-ASCII characters
         * 3. Strip Windows hrStorageDescr crud if present
         * 4. Eliminate tabs and spaces
         * 5. Replace slash and backslash characters with "-"
         * 6. Replace colons and semicolons with "_"
         * 7. Remove leading dot(s)
         */
        if ("/".equals(value))
            return "_slash_";
        
        String name = value.replaceAll("[^\\x00-\\x7F]", "").replaceAll("Label:.*?\\s+Serial Number [0-9A-Fa-f]+$", "").replaceAll("\\s", "").replaceAll("[/\\\\]", "-").replaceAll("[:;]", "_").replaceAll("^\\.+", "");
        
        if ("".equals(name)) return resourceIndex;
        return ("".equals(name) ? resourceIndex : name);
    }
    
    @Override
    public void setParameters(List<Parameter> parameterCollection) {
        if (parameterCollection == null) {
            log().fatal("Got a null parameter list, but need one containing a '" + PARAM_SIBLING_COLUMN_OID + "' parameter.");
            throw new RuntimeException("Got a null parameter list, but need one containing a '" + PARAM_SIBLING_COLUMN_OID + "' parameter.");
        }
        
        for (Parameter param : parameterCollection) {
            if (PARAM_SIBLING_COLUMN_OID.equals(param.getKey())) {
                m_siblingColumnOid = param.getValue();
            }
        }
        
        if (m_siblingColumnOid == null) {
            log().error("The provided parameter list must contain a '" + PARAM_SIBLING_COLUMN_OID + "' parameter.");
            throw new RuntimeException("The provided parameter list must contain a '" + PARAM_SIBLING_COLUMN_OID + "' parameter.");
        }
        
        if (! m_siblingColumnOid.matches("^\\.[0-9.]+$")) {
            log().error("The value '" + m_siblingColumnOid + "' provided for parameter '" + PARAM_SIBLING_COLUMN_OID + "' is not a valid SNMP object identifier.");
            throw new RuntimeException("The value '" + m_siblingColumnOid + "' provided for parameter '" + PARAM_SIBLING_COLUMN_OID + "' is not a valid SNMP object identifier.");
        }
    }
}