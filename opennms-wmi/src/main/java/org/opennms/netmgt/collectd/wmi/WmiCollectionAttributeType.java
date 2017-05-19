/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.config.wmi.Attrib;
import org.opennms.netmgt.config.wmi.WmiType;

/**
 * <p>WmiCollectionAttributeType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiCollectionAttributeType extends AbstractCollectionAttributeType {
        Attrib m_attribute;

        /**
         * <p>Constructor for WmiCollectionAttributeType.</p>
         *
         * @param attribute a {@link org.opennms.netmgt.config.wmi.Attrib} object.
         * @param groupType a {@link org.opennms.netmgt.collection.api.AttributeGroupType} object.
         */
        public WmiCollectionAttributeType(final Attrib attribute, final AttributeGroupType groupType) {
            super(groupType);
            m_attribute = attribute;
        }

        /** {@inheritDoc} */
        @Override
        public void storeAttribute(final CollectionAttribute attribute, final Persister persister) {
            if (m_attribute.getType() == WmiType.STRING) {
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
        @Override
        public String getName() {
            return m_attribute.getAlias();
        }

        @Override
        public String getType() {
            return m_attribute.getType().toString();
        }

}
