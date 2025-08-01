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

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ServiceCollector;

/**
 * This is a simple {@link CollectionSet} that is used to encapsulate the collection
 * of a single resource. The status is initially set to 
 * {@link ServiceCollector#COLLECTION_FAILED}. It is up to the collector to set it
 * to a different value upon collection completion.
 */
public class SingleResourceCollectionSet extends AbstractCollectionSet {
    private CollectionStatus m_status = CollectionStatus.FAILED;
	private final CollectionResource m_collectionResource;
	private final Date m_timestamp;

	public SingleResourceCollectionSet(CollectionResource resource, Date timestamp) {
		m_collectionResource = resource;
		m_timestamp = timestamp;
	}

	@Override
	public final CollectionStatus getStatus() {
		return m_status;
	}

	public final void setStatus(CollectionStatus status) {
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
