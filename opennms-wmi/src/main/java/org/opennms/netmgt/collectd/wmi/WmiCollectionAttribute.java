/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.support.AbstractCollectionAttribute;

/**
 * <p>WmiCollectionAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiCollectionAttribute extends AbstractCollectionAttribute {
        private final String m_value;

        /**
         * <p>Constructor for WmiCollectionAttribute.</p>
         *
         * @param resource a {@link org.opennms.netmgt.collectd.wmi.WmiCollectionResource} object.
         * @param attribType a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
         * @param alias a {@link java.lang.String} object.
         * @param value a {@link java.lang.String} object.
         */
        public WmiCollectionAttribute(final WmiCollectionResource resource, final CollectionAttributeType attribType, final String value) {
            super(attribType, resource);
            m_value = value;
        }

        /**
         * <p>getNumericValue</p>
         *
         * @return a {@link java.lang.String} object.
         */
        @Override
        public String getNumericValue() {
            return m_value;
        }

        /**
         * <p>getStringValue</p>
         *
         * @return a {@link java.lang.String} object.
         */
        @Override
        public String getStringValue() {
            return m_value; //Should this be null instead?
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        @Override
        public String toString() {
            return "WmiCollectionAttribute " + getName() + "=" + m_value;
        }

        @Override
        public String getMetricIdentifier() {
            return "Not supported yet._" + "WMI_" + getName();
        }
}
