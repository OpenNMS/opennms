/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.opennms.core.criteria.Criteria;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsMonitoringSystem;

/**
 * This {@link DistPollerDao} wraps the single instance that represents the
 * current Minion device.
 * 
 * @author Seth
 */
public class DistPollerDaoMinion implements DistPollerDao {

	private final OnmsDistPoller m_distPoller;

	public DistPollerDaoMinion(OnmsDistPoller distPoller) {
		m_distPoller = Objects.requireNonNull(distPoller);
	}

	public DistPollerDaoMinion(MinionIdentity identity) {
		Objects.requireNonNull(identity);
		m_distPoller = new OnmsDistPoller();
		m_distPoller.setId(identity.getId());
		m_distPoller.setLabel(identity.getId());
		m_distPoller.setLastUpdated(new Date());
		m_distPoller.setLocation(identity.getLocation());
		m_distPoller.setType(OnmsMonitoringSystem.TYPE_MINION);
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
