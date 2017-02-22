/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

import java.nio.file.Path;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.TimeKeeper;

/**
 * Delegates the visit calls to the wrapped resource.
 * Allows individual functions to be intercepted.
 *
 * @author jwhite
 */
public class CollectionResourceWrapper implements CollectionResource {

    private final CollectionResource m_wrapped;

    public CollectionResourceWrapper(CollectionResource wrapped) {
        m_wrapped = wrapped;
    }

    /** {@inheritDoc} */
    @Override
    public String getOwnerName() {
        return m_wrapped.getOwnerName();
    }

    /** {@inheritDoc} */
    @Override
    public Path getPath() {
        return m_wrapped.getPath();
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
    public boolean rescanNeeded() {
        return m_wrapped.rescanNeeded();
    }

    /** {@inheritDoc} */
    @Override
    public String getResourceTypeName() {
        return m_wrapped.getResourceTypeName();
    }

    /** {@inheritDoc} */
    @Override
    public String getParent() {
        return m_wrapped.getParent();
    }

    /** {@inheritDoc} */
    @Override
    public String getInstance() {
        return m_wrapped.getInstance();
    }

    /** {@inheritDoc} */
    @Override
    public String getInterfaceLabel() {
        return m_wrapped.getInterfaceLabel();
    }

    /** {@inheritDoc} */
    @Override
    public TimeKeeper getTimeKeeper() {
        return m_wrapped.getTimeKeeper();
    }
}
