/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support;

import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract AbstractCollectionAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractCollectionAttribute implements CollectionAttribute {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCollectionAttribute.class);

    protected final CollectionAttributeType m_attribType;
    protected final CollectionResource m_resource;

    public AbstractCollectionAttribute(CollectionAttributeType attribType, CollectionResource resource) {
        m_attribType = attribType;
        m_resource = resource;
    }

    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.CollectionAttributeType} object.
     */
    @Override
    public final CollectionAttributeType getAttributeType() {
        return m_attribType;
    }

    @Override
    public final CollectionResource getResource() {
        return m_resource;
    }

    @Override
    public final String getName() {
        return m_attribType.getName();
    }

    @Override
    public final AttributeType getType() {
        return m_attribType.getType();
    }

    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.Number} object.
     */
    @Override
    public abstract Number getNumericValue();

    /**
     * <p>getStringValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getStringValue();

    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(Persister persister) {
        getAttributeType().storeAttribute(this, persister);
    }

    /** 
     * Since a {@link CollectionAttribute} is a terminal value, we just visit and
     * complete it since it doesn't have any "children".
     */
    @Override
    public final void visit(CollectionSetVisitor visitor) {
        LOG.debug("Visiting attribute {}", this);
        visitor.visitAttribute(this);
        visitor.completeAttribute(this);
    }   

}
