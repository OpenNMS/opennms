//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2010 Jan 06: Created file.  -jeffg@opennms.org
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
package org.opennms.netmgt.dao.support;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.ReplaceAllOperation;
import org.opennms.core.utils.ReplaceFirstOperation;
import org.opennms.core.utils.StringReplaceOperation;
import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <p>SiblingColumnStorageStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public class SiblingColumnStorageStrategy extends IndexStorageStrategy {
    private static final String PARAM_SIBLING_COLUMN_OID = "sibling-column-oid";
    private String m_siblingColumnOid;

    private static final String PARAM_REPLACE_FIRST = "replace-first";
    private static final String PARAM_REPLACE_ALL = "replace-all";
    private List<StringReplaceOperation> m_replaceOps;

    /**
     * <p>Constructor for SiblingColumnStorageStrategy.</p>
     */
    public SiblingColumnStorageStrategy() {
        super();
        m_replaceOps = new ArrayList<StringReplaceOperation>();
    }
    
    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(String resourceParent, String resourceIndex) {
        SnmpObjId oid = SnmpObjId.get(m_siblingColumnOid + "." + resourceIndex);
        SnmpValue snmpValue = SnmpUtils.get(m_storageStrategyService.getAgentConfig(), oid);
        String value = (snmpValue != null ? snmpValue.toString() : resourceIndex);
        
        // First remove all non-US-ASCII characters and turn all forward slashes into dashes 
        String name = value.replaceAll("[^\\x00-\\x7F]", "").replaceAll("/", "-");
        
        // Then perform all replacement operations specified in the parameters
        for (StringReplaceOperation op : m_replaceOps) {
            log().debug("Doing string replacement on instance name '" + name + "' using " + op);
            name = op.replace(name);
        }

        log().debug("Inbound instance name was '" + resourceIndex + "', outbound was '" + ("".equals(name) ? resourceIndex : name) + "'");
        return ("".equals(name) ? resourceIndex : name);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParameters(List<Parameter> parameterCollection) {
        if (parameterCollection == null) {
            log().fatal("Got a null parameter list, but need one containing a '" + PARAM_SIBLING_COLUMN_OID + "' parameter.");
            throw new RuntimeException("Got a null parameter list, but need one containing a '" + PARAM_SIBLING_COLUMN_OID + "' parameter.");
        }
        
        for (Parameter param : parameterCollection) {
            if (PARAM_SIBLING_COLUMN_OID.equals(param.getKey())) {
                m_siblingColumnOid = param.getValue();
            } else if (PARAM_REPLACE_FIRST.equals(param.getKey())) {
                m_replaceOps.add(new ReplaceFirstOperation(param.getValue()));
            } else if (PARAM_REPLACE_ALL.equals(param.getKey())) {
                m_replaceOps.add(new ReplaceAllOperation(param.getValue()));
            } else {
                log().warn("Encountered unsupported parameter key=\"" + param.getKey() + "\". Can accept: " + PARAM_SIBLING_COLUMN_OID + ", " + PARAM_REPLACE_FIRST + ", " + PARAM_REPLACE_ALL);
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
