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
package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.HwEntityDao;
import org.opennms.netmgt.model.OnmsHwEntity;

public class MockHwEntityDao extends AbstractMockDao<OnmsHwEntity, Integer> implements HwEntityDao {

	private AtomicInteger m_id = new AtomicInteger(0);

	@Override
	protected void generateId(final OnmsHwEntity outage) {
		outage.setId(m_id.incrementAndGet());
	}

	@Override
	protected Integer getId(final OnmsHwEntity outage) {
		return outage.getId();
	}

	@Override
	public OnmsHwEntity findRootByNodeId(Integer nodeId) {
	    for (final OnmsHwEntity entity : findAll()) {
	        if (entity.getNode().getId().equals(nodeId) && entity.getParent() == null) {
	            return entity;
	        }
	    }
	    return null;
	}

	@Override
	public OnmsHwEntity findRootEntityByNodeId(Integer nodeId) {
		for (final OnmsHwEntity entity : findAll()) {
			if (entity.getNode().getId().equals(nodeId) && entity.getParent() == null) {
				return entity;
			}
		}
		return null;
	}

	@Override
	public OnmsHwEntity findEntityByIndex(Integer nodeId, Integer entPhysicalIndex) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public OnmsHwEntity findEntityByName(Integer nodeId, String entPhysicalName) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public String getAttributeValue(Integer nodeId, Integer entPhysicalIndex, String attributeName) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public String getAttributeValue(Integer nodeId, String nameSource, String attributeName) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}
}
