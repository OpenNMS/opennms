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

package org.opennms.netmgt.poller;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>LatencyCollectionAttribute class.</p>
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 * @version $Id: $
 */
public class LatencyCollectionAttribute implements CollectionAttribute {

    private static final Logger LOG = LoggerFactory.getLogger(LatencyCollectionAttribute.class);

    private LatencyCollectionResource m_resource;
    private LatencyCollectionAttributeType m_type;
    private Double m_value;
    private String m_name;

    /**
     * <p>Constructor for LatencyCollectionAttribute.</p>
     *
     * @param resource a {@link org.opennms.netmgt.poller.LatencyCollectionResource} object.
     * @param type a {@link org.opennms.netmgt.poller.LatencyCollectionAttributeType} object.
     * @param name a {@link java.lang.String} object.
     * @param value a {@link java.lang.Double} object.
     */
    public LatencyCollectionAttribute(LatencyCollectionResource resource, LatencyCollectionAttributeType type, String name, Double value) {
        super();
        m_resource = resource;
        m_type = type;
        m_name = name;
        m_value = value;
    }

    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.poller.LatencyCollectionAttributeType} object.
     */
    @Override
    public LatencyCollectionAttributeType getAttributeType() {
        return m_type;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public Double getNumericValue() {
        return m_value;
    }

    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionResource} object.
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
        return null;
    }

    /**
     * <p>getType</p>
     *
     * @return a {@link AttributeType} object.
     */
    @Override
    public AttributeType getType() {
        return AttributeType.GAUGE;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        LOG.debug("Visiting attribute {}", this);
        visitor.visitAttribute(this);
        visitor.completeAttribute(this);
    }

    @Override
    public String getMetricIdentifier() {
        return String.format("%s/%s", m_resource.getServiceName(), m_resource.getIpAddress());
    }

    @Override
    public String toString() {
        return String.format("%s: %f", m_name, m_value);
    }
}
