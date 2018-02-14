/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.support;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;


/**
 * Used to persist distributed latency statistics collected by the remote poller.
 *
 * @author jwhite
 */
public class DistributedLatencyCollectionAttributeType extends AbstractCollectionAttributeType {

    private final String m_name;

    public DistributedLatencyCollectionAttributeType(String groupTypeName, String attributeTypeName) {
        super(new AttributeGroupType(groupTypeName, AttributeGroupType.IF_TYPE_ALL));
        m_name = attributeTypeName;
    }

    @Override
    public AttributeType getType() {
        return AttributeType.GAUGE;
    }

    @Override
    public String getName() {
        return m_name;
    }

    @Override
    public void storeAttribute(CollectionAttribute attribute, Persister persister) {
        persister.persistNumericAttribute(attribute);
    }
}
