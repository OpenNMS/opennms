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
package org.opennms.netmgt.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;

/**
 * This {@link DistPollerDao} wraps the single instance that represents the
 * current Minion device.
 * 
 * @author Seth
 */
public class DistPollerDaoDistributed implements DistPollerDao {

	private final OnmsDistPoller m_distPoller;

	public DistPollerDaoDistributed(OnmsDistPoller distPoller) {
		m_distPoller = Objects.requireNonNull(distPoller);
	}

	public DistPollerDaoDistributed(Identity identity) {
		Objects.requireNonNull(identity);
		m_distPoller = new OnmsDistPoller();
		m_distPoller.setId(identity.getId());
		m_distPoller.setLabel(identity.getId());
		m_distPoller.setLastUpdated(new Date());
		m_distPoller.setLocation(identity.getLocation());
		m_distPoller.setType(identity.getType());
	}

	@Override
	public void lock() {
	}

	@Override
	public void initialize(Object obj) {
	}

	@Override
	public void flush() {
	}

	@Override
	public void clear() {
	}

	@Override
	public int countAll() {
		return 1;
	}

	@Override
	public void delete(OnmsDistPoller entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete(String key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<OnmsDistPoller> findAll() {
		return Collections.singletonList(whoami());
	}

	@Override
	public List<OnmsDistPoller> findMatching(Criteria criteria) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int countMatching(Criteria onmsCrit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OnmsDistPoller get(String id) {
		if (m_distPoller.getId().equals(id)) {
			return m_distPoller;
		} else {
			return null;
		}
	}

	@Override
	public OnmsDistPoller load(String id) {
		return get(id);
	}

	@Override
	public String save(OnmsDistPoller entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void saveOrUpdate(OnmsDistPoller entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void update(OnmsDistPoller entity) {
		throw new UnsupportedOperationException();
	}

	@Override
	public OnmsDistPoller whoami() {
		return m_distPoller;
	}
}
