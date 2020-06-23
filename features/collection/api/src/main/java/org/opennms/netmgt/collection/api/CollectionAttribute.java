/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.api;

import org.opennms.netmgt.collection.api.AttributeType;

/**
 * <p>CollectionAttribute interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionAttribute extends CollectionVisitable, Persistable {
    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    CollectionResource getResource();
    
    /**
     * Get the value of the attribute as a String.
     *
     * @return a String representing the attribute value
     */
    String getStringValue();
    
    /**
     * Get the numeric value of the attribute.
     *
     * @return a {@link java.lang.Number} object.
     */
    Number getNumericValue();
    
    /**
     * Gets the name of the attribute
     *
     * @return a name
     */
    String getName();
    
    /**
     * Get the metric identifier for the attribute to be used for NRTG collection
     * 
     * @return the metric identifier
     */
    String getMetricIdentifier();

    
    /**
     * Stores the attribute using the persister.  Not sure this should be here...
     *
     * @param persister a {@link org.opennms.netmgt.collectd.Persister} object.
     */
    void storeAttribute(Persister persister);
    
    /**
     * Return the attribute type for this attribute.  Not sure what an CollectionAttributeType is yet... please fill in if you do know
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAttributeType} object.
     */
    CollectionAttributeType getAttributeType();

    /**
     * <p>The type of metric that the attribute represents.</p>
     *
     * @return a {@link AttributeType} object.
     */
    AttributeType getType();
}
