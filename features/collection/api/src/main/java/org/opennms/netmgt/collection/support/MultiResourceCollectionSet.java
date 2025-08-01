/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
