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

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceCollector;

/**
 * This is a simple {@link CollectionSet} that is used to encapsulate the collection
 * of a single resource. The status is initially set to 
 * {@link ServiceCollector#COLLECTION_FAILED}. It is up to the collector to set it
 * to a different value upon collection completion.
 */
public class SingleResourceCollectionSet extends AbstractCollectionSet {
	private int m_status = ServiceCollector.COLLECTION_FAILED;
	private final CollectionResource m_collectionResource;
	private final Date m_timestamp;

	public SingleResourceCollectionSet(CollectionResource resource, Date timestamp) {
		m_collectionResource = resource;
		m_timestamp = timestamp;
	}

	@Override
	public final int getStatus() {
		return m_status;
	}

	public final void setStatus(int status) {
		m_status = status;
	}

	public final CollectionResource getCollectionResource() {
		return m_collectionResource;
	}

	@Override
	public final void visit(CollectionSetVisitor visitor) {
		visitor.visitCollectionSet(this);
		m_collectionResource.visit(visitor);
		visitor.completeCollectionSet(this);
	}

	@Override
	public final Date getCollectionTimestamp() {
		return m_timestamp;
	}
}
