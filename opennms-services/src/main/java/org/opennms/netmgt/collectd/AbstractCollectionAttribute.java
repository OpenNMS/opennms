/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionAttributeType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.CollectionSetVisitor;
import org.opennms.netmgt.config.collector.Persister;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Abstract AbstractCollectionAttribute class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class AbstractCollectionAttribute implements  CollectionAttribute {
    
    private static final Logger LOG = LoggerFactory.getLogger(AbstractCollectionAttribute.class);

    /**
     * <p>getAttributeType</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.CollectionAttributeType} object.
     */
    @Override
    public abstract CollectionAttributeType getAttributeType();

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getName();

    /**
     * <p>getNumericValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getNumericValue();

    /**
     * <p>getResource</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.CollectionResource} object.
     */
    @Override
    public abstract CollectionResource getResource();

    /**
     * <p>getStringValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String getStringValue();

    /** {@inheritDoc} */
    @Override
    public abstract boolean shouldPersist(ServiceParameters params);

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

}
