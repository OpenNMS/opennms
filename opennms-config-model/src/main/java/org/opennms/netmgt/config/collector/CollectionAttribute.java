/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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


package org.opennms.netmgt.config.collector;

/**
 * <p>CollectionAttribute interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CollectionAttribute {
    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    public CollectionResource getResource();
    
    /**
     * Get the value of the attribute as a String.
     *
     * @return a String representing the attribute value
     */
    public String getStringValue();
    
    /**
     * Get the numeric value of the attribute, as a String.  Assumes the underlying value is actually numeric, and will
     * return null if it is not parseable.
     *
     * @return a string representation of the numeric value of this attribute
     */
    public String getNumericValue();
    
    /**
     * Gets the name of the attribute
     *
     * @return a name
     */
    public String getName();

    
    /**
     * Stores the attribute using the persister.  Not sure this should be here...
     *
     * @param persister a {@link org.opennms.netmgt.collectd.Persister} object.
     */
    void storeAttribute(Persister persister);
    
    /**
     * Determines whether the attribute should be persisted.
     *
     * @param params a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
     * @return a boolean.
     */
    public boolean shouldPersist(ServiceParameters params);
    
    /**
     * Return the attribute type for this attribute.  Not sure what an CollectionAttributeType is yet... please fill in if you do know
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionAttributeType} object.
     */
    public CollectionAttributeType getAttributeType();
    
    /**
     * Visit this attribute
     *
     * @param visitor a {@link org.opennms.netmgt.config.collector.CollectionSetVisitor} object.
     */
    public void visit(CollectionSetVisitor visitor);
    
    /**
     * Returns type of value (typically one of "counter", "gauge", "timeticks", "integer", "octetstring" - see NumericAttributeType)
     *
     * @return type of value stored in this attribute (SNMP semantics)
     */
    public String getType();
}
