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

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.FilterFavoriteDao;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.netmgt.model.OnmsFilterFavorite.Page;

public class MockFilterFavoriteDao extends AbstractMockDao<OnmsFilterFavorite, Integer> implements FilterFavoriteDao {

	private AtomicInteger m_id = new AtomicInteger(0);

	@Override
	protected void generateId(final OnmsFilterFavorite outage) {
		outage.setId(m_id.incrementAndGet());
	}

	@Override
	protected Integer getId(final OnmsFilterFavorite outage) {
		return outage.getId();
	}

	@Override
	public OnmsFilterFavorite findBy(String userName, String filterName) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public List<OnmsFilterFavorite> findBy(String userName, Page page) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public boolean existsFilter(String userName, String filterName, Page page) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}
}
