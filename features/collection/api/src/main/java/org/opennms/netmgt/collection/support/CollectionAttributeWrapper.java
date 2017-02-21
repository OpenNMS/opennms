/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionAttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.ServiceParameters;

/**
 * Delegates the visit calls to the wrapped attribute.
 * Allows individual functions to be intercepted.
 *
 * @author jwhite
 */
public class CollectionAttributeWrapper implements CollectionAttribute {

    private final CollectionAttribute m_wrapped;

    public CollectionAttributeWrapper(CollectionAttribute wrapped) {
        m_wrapped = wrapped;
    }

    /** {@inheritDoc} */
    @Override
    public void visit(CollectionSetVisitor visitor) {
        m_wrapped.visit(visitor);
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldPersist(ServiceParameters params) {
        return m_wrapped.shouldPersist(params);
    }

    /** {@inheritDoc} */
    @Override
    public CollectionResource getResource() {
        return m_wrapped.getResource();
    }

    /** {@inheritDoc} */
    @Override
    public String getStringValue() {
        return m_wrapped.getStringValue();
    }

    /** {@inheritDoc} */
    @Override
    public Number getNumericValue() {
        return m_wrapped.getNumericValue();
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return m_wrapped.getName();
    }

    /** {@inheritDoc} */
    @Override
    public String getMetricIdentifier() {
        return m_wrapped.getMetricIdentifier();
    }

    /** {@inheritDoc} */
    @Override
    public void storeAttribute(Persister persister) {
        m_wrapped.storeAttribute(persister);
    }

    /** {@inheritDoc} */
    @Override
    public CollectionAttributeType getAttributeType() {
        return m_wrapped.getAttributeType();
    }

    /** {@inheritDoc} */
    @Override
    public String getType() {
        return m_wrapped.getType();
    }
}
