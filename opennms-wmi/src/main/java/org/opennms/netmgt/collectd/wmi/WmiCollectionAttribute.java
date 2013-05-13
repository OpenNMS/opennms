/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;

/**
 * <p>WmiCollectionAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class WmiCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {
    String m_alias;
        String m_value;
        WmiCollectionResource m_resource;
        CollectionAttributeType m_attribType;

        /**
         * <p>Constructor for WmiCollectionAttribute.</p>
         *
         * @param resource a {@link org.opennms.netmgt.collectd.wmi.WmiCollectionResource} object.
         * @param attribType a {@link org.opennms.netmgt.config.collector.CollectionAttributeType} object.
         * @param alias a {@link java.lang.String} object.
         * @param value a {@link java.lang.String} object.
         */
        public WmiCollectionAttribute(final WmiCollectionResource resource, final CollectionAttributeType attribType, final String alias, final String value) {
            m_resource=resource;
            m_attribType=attribType;
            m_alias = alias;
            m_value = value;
        }

        /**
         * <p>getAttributeType</p>
         *
         * @return a {@link org.opennms.netmgt.config.collector.CollectionAttributeType} object.
         */
    @Override
        public CollectionAttributeType getAttributeType() {
            return m_attribType;
        }

        /**
         * <p>getName</p>
         *
         * @return a {@link java.lang.String} object.
         */
    @Override
        public String getName() {
            return m_alias;
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
         * <p>getResource</p>
         *
         * @return a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
         */
    @Override
        public CollectionResource getResource() {
            return m_resource;
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

        /** {@inheritDoc} */
    @Override
        public boolean shouldPersist(final ServiceParameters params) {
            return true;
        }

        /**
         * <p>getType</p>
         *
         * @return a {@link java.lang.String} object.
         */
    @Override
        public String getType() {
            return m_attribType.getType();
        }

        /**
         * <p>toString</p>
         *
         * @return a {@link java.lang.String} object.
         */
    @Override
        public String toString() {
            return "WmiCollectionAttribute " + m_alias+"=" + m_value;
        }

        @Override
        public String getMetricIdentifier() {
            return "Not supported yet._" + "WMI_" + getName();
        }
}
