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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.netmgt.collectd.AttributeGroupType;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.Persister;
import org.opennms.netmgt.config.wmi.Attrib;

/**
 * <p>WmiCollectionAttributeType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiCollectionAttributeType implements CollectionAttributeType {
        Attrib m_attribute;
        AttributeGroupType m_groupType;

        /**
         * <p>Constructor for WmiCollectionAttributeType.</p>
         *
         * @param attribute a {@link org.opennms.netmgt.config.wmi.Attrib} object.
         * @param groupType a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
         */
        public WmiCollectionAttributeType(final Attrib attribute, final AttributeGroupType groupType) {
            m_groupType = groupType;
            m_attribute = attribute;
        }

        /**
         * <p>getGroupType</p>
         *
         * @return a {@link org.opennms.netmgt.collectd.AttributeGroupType} object.
         */
        public AttributeGroupType getGroupType() {
            return m_groupType;
        }

        /** {@inheritDoc} */
        public void storeAttribute(final CollectionAttribute attribute, final Persister persister) {
            if ("string".equalsIgnoreCase(m_attribute.getType())) {
                persister.persistStringAttribute(attribute);
            } else {
                persister.persistNumericAttribute(attribute);
            }
        }

        /**
         * <p>getName</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String getName() {
            return m_attribute.getAlias();
        }

        /**
         * <p>getType</p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String getType() {
            return m_attribute.getType();
        }
}
