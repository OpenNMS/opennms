//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 15: Fix a spelling mistake in a log message. - dj@opennms.org
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

package org.opennms.netmgt.collectd;


import org.opennms.netmgt.config.MibObject;

/**
 * <p>NumericAttributeType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class NumericAttributeType extends SnmpAttributeType {
    
    private static String[] s_supportedTypes = new String[] { "counter", "gauge", "timeticks", "integer", "octetstring" };
    
    /**
     * <p>supportsType</p>
     *
     * @param rawType a {@link java.lang.String} object.
     * @return a boolean.
     */
    public static boolean supportsType(String rawType) {
        String type = rawType.toLowerCase();
        for (int i = 0; i < s_supportedTypes.length; i++) {
            String supportedType = s_supportedTypes[i];
            if (type.startsWith(supportedType))
                return true;
        }
        return false;
    }



    static final String DST_COUNTER = "COUNTER";
    /**
     * <p>Constructor for NumericAttributeType.</p>
     *
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @param collectionName a {@link java.lang.String} object.
     * @param mibObj a {@link org.opennms.netmgt.config.MibObject} object.
     * @param groupType a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
     */
    public NumericAttributeType(ResourceType resourceType, String collectionName, MibObject mibObj, AttributeGroupType groupType) {
        super(resourceType, collectionName, mibObj, groupType);
        
            // Assign the data source object identifier and instance
            if (log().isDebugEnabled()) {
                log().debug(
                        "buildDataSourceList: ds_name: "+ getName()
                        + " ds_oid: " + getOid()
                        + "." + getInstance());
            }
            
            String alias = getAlias();
            if (alias.length() > PersistOperationBuilder.MAX_DS_NAME_LENGTH) {
                logNameTooLong();
            }


    }
    
    /** {@inheritDoc} */
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistNumericAttribute(attribute);
    }

    void logNameTooLong() {
        log().warn(
                "buildDataSourceList: Mib object name/alias '"
                + getAlias()
                + "' exceeds 19 char maximum for RRD data source names, truncating.");
   }


}
