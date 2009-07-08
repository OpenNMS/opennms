/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.collectd.CollectionAttribute;
import org.opennms.netmgt.collectd.CollectionAttributeType;
import org.opennms.netmgt.collectd.CollectionResource;
import org.opennms.netmgt.collectd.ServiceParameters;

public class WmiCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {
    String m_alias;
        String m_value;
        WmiCollectionResource m_resource;
        CollectionAttributeType m_attribType;

        public WmiCollectionAttribute(WmiCollectionResource resource, CollectionAttributeType attribType, String alias, String value) {
            m_resource=resource;
            m_attribType=attribType;
            m_alias = alias;
            m_value = value;
        }

        public CollectionAttributeType getAttributeType() {
            return m_attribType;
        }

        public String getName() {
            return m_alias;
        }

        public String getNumericValue() {
            return m_value;
        }

        public CollectionResource getResource() {
            return m_resource;
        }

        public String getStringValue() {
            return m_value; //Should this be null instead?
        }

        public boolean shouldPersist(ServiceParameters params) {
            return true;
        }

        public String getType() {
            return m_attribType.getType();
        }

        public String toString() {
            return "WmiCollectionAttribute " + m_alias+"=" + m_value;
        }
}
