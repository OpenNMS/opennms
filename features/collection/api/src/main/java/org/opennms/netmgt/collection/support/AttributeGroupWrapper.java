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

import java.util.Collection;

import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;

/**
 * Delegates the visit calls to the wrapped attribut group.
 * Allows individual functions to be intercepted.
 *
 * @author jwhite
 */
public class AttributeGroupWrapper extends AttributeGroup {

    private final AttributeGroup m_wrapped;

    public AttributeGroupWrapper(AttributeGroup wrapped) {
        super(wrapped.getResource(), wrapped.getGroupType());
        m_wrapped = wrapped;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return m_wrapped.getName();
    }

    /** {@inheritDoc} */
    @Override
    public CollectionResource getResource() {
        return m_wrapped.getResource();
    }

    /** {@inheritDoc} */
    @Override
    public Collection<CollectionAttribute> getAttributes() {
        return m_wrapped.getAttributes();
    }

    /** {@inheritDoc} */
    @Override
    public void addAttribute(CollectionAttribute attr) {
        m_wrapped.addAttribute(attr);
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
    public AttributeGroupType getGroupType() {
        return m_wrapped.getGroupType();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return m_wrapped.toString();
    }
}
