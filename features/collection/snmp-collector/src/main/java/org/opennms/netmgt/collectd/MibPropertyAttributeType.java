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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.config.datacollection.MibObjProperty;

/**
 * The Class MibPropertyAttributeType.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class MibPropertyAttributeType extends SnmpAttributeType {

    /** The MIB object property. */
    private MibObjProperty property;

    /**
     * Supports type.
     *
     * @param rawType the raw type
     * @return true, if successful
     */
    public static boolean supportsType(String rawType) {
        return rawType.toLowerCase().startsWith("string");
    }

    /**
     * Instantiates a new MIB property attribute type.
     *
     * @param property the property
     * @param groupType the group type
     */
    public MibPropertyAttributeType(ResourceType resourceType, MibObjProperty property, AttributeGroupType groupType) {
        super(resourceType, null, null, groupType);
        this.property = property;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAttributeType#storeAttribute(org.opennms.netmgt.collection.api.CollectionAttribute, org.opennms.netmgt.collection.api.Persister)
     */
    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistStringAttribute(attribute);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAttributeType#getType()
     */
    @Override
    public AttributeType getType() {
        return AttributeType.STRING;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpAttributeType#getAlias()
     */
    @Override
    public String getAlias() {
        return property.getAlias();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collectd.SnmpAttributeType#getOid()
     */
    @Override
    public String getOid() {
        return "property:" + property.getAlias();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.CollectionAttributeType#getName()
     */
    @Override
    public String getName() {
        return property.getAlias();
    }

}
