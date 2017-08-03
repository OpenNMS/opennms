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

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;

/**
 */
public abstract class MultiResourceCollectionSet<T extends CollectionResource> extends AbstractCollectionSet {
	private CollectionStatus m_status = CollectionStatus.FAILED;
	private final Set<T> m_collectionResources = new LinkedHashSet<>();
	private Date m_timestamp;

	@Override
	public final CollectionStatus getStatus() {
		return m_status;
	}

	public final void setStatus(CollectionStatus status) {
		m_status = status;
	}

	public final Set<T> getCollectionResources() {
		return m_collectionResources;
	}

	public final void setCollectionResources(Set<T> collectionResources) {
		m_collectionResources.clear();
		m_collectionResources.addAll(collectionResources);
	}

	@Override
	public final void visit(CollectionSetVisitor visitor) {
		visitor.visitCollectionSet(this);

		for(T resource : getCollectionResources()) {
			resource.visit(visitor);
		}

		visitor.completeCollectionSet(this);
	}

	@Override
	public final Date getCollectionTimestamp() {
		return m_timestamp;
	}
	public final void setCollectionTimestamp(Date timestamp) {
		this.m_timestamp = timestamp;
	}

}
