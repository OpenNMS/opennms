/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.support.AbstractCollectionAttributeType;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class TcaCollectionAttributeType.
 * 
 * <p>It is assumed that all metrics are not counters.</p>
 * <p>The metrics are fixed:</p>
 * <ul>
 * <li>delay local-remote ~ current inbound-delay</li>
 * <li>jitter local-remote ~ current inbound-jitter</li>
 * <li>delay remote-local ~ current outbound-delay</li>
 * <li>jitter remote-local ~ current outbound-jitter-</li>
 * <li>timesync status (1 = good, time is synced, 0 = bad, out-of sync)</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class TcaCollectionAttributeType extends AbstractCollectionAttributeType {

	private final SnmpObjId m_attributeObjectId; 

	/** The m_name. */
	private final String m_name;

	/**
	 * Instantiates a new TCA collection attribute type.
	 *
	 * @param groupType the group type
	 * @param name the name
	 */
	public TcaCollectionAttributeType(AttributeGroupType groupType, SnmpObjId atributeObjectId, String name) {
		super(groupType);
		this.m_attributeObjectId = atributeObjectId;
		this.m_name = name;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.AttributeDefinition#getType()
	 */
	@Override
	public String getType() {
		return "Integer32";
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.AttributeDefinition#getName()
	 */
	@Override
	public String getName() {
		return m_name;
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.config.collector.CollectionAttributeType#storeAttribute(org.opennms.netmgt.config.collector.CollectionAttribute, org.opennms.netmgt.config.collector.Persister)
	 */
	@Override
	public void storeAttribute(CollectionAttribute attribute, Persister persister) {
		// Only numeric data comes back from this collector
		persister.persistNumericAttribute(attribute);
	}

    public SnmpObjId getAttributeObjectId() {
        return m_attributeObjectId;
    }

}
