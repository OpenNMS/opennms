/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

public class ValueMappingAttributeType extends SnmpAttributeType {
    public static class Wrapper extends SnmpAttribute {
        private Wrapper(final SnmpAttribute attribute) {
            super(attribute.getResource(), (SnmpAttributeType) attribute.getAttributeType(), attribute.getValue());
        }

        @Override
        public String getMetricIdentifier() {
            return null;
        }
    }

    private final MibObjProperty property;
    private final AttributeType attributeType;

    public ValueMappingAttributeType(final ResourceType resourceType, final MibObjProperty property, final AttributeGroupType groupType, final AttributeType attributeType) {
        super(resourceType, null, null, groupType);
        this.property = property;
        this.attributeType = attributeType;
    }

    @Override
    public void storeAttribute(final CollectionAttribute attribute, final Persister persister) {
        if (attributeType.isNumeric()) {
            persister.persistNumericAttribute(new Wrapper((SnmpAttribute) attribute));
        } else {
            persister.persistStringAttribute(new Wrapper((SnmpAttribute) attribute));
        }
    }

    @Override
    public AttributeType getType() {
        return attributeType;
    }

    @Override
    public String getAlias() {
        return property.getAlias();
    }

    @Override
    public String getOid() {
        return "property:" + property.getAlias();
    }

    @Override
    public String getName() {
        return property.getAlias();
    }
}
