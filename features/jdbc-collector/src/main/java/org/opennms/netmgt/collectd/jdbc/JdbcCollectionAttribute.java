/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.jdbc;

import org.opennms.netmgt.collectd.AbstractCollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;

public class JdbcCollectionAttribute extends AbstractCollectionAttribute implements CollectionAttribute {
    String m_alias;
    String m_value;
    JdbcCollectionResource m_resource;
    CollectionAttributeType m_attribType;
    
    public JdbcCollectionAttribute(JdbcCollectionResource resource, CollectionAttributeType attribType, String alias, String value) {
        m_resource=resource;
        m_attribType=attribType;
        m_alias = alias;
        m_value = value;
    }
    
    @Override
    public CollectionAttributeType getAttributeType() {
        return m_attribType;
    }
    
    @Override
    public String getName() {
        return m_alias;
    }
    
    @Override
    public String getNumericValue() {
        return m_value;
    }
    
    @Override
    public CollectionResource getResource() {
        return m_resource;
    }
    
    @Override
    public String getStringValue() {
        return m_value; //Should this be null instead?
    }
    
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }
    
    @Override
    public String getType() {
        return m_attribType.getType();
    }
    
    @Override
    public String toString() {
        return "JdbcCollectionAttribute " + m_alias+"=" + m_value;
    }

    @Override
    public String getMetricIdentifier() {
        return "Not supported yet._" + "JDBC_" + getName();
    }

}
