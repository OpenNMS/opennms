/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.netmgt.dao.api.HwEntityAttributeTypeDao;
import org.opennms.netmgt.model.HwEntityAttributeType;

public class MockHwEntityAttributeTypeDao extends AbstractMockDao<HwEntityAttributeType, Integer> implements HwEntityAttributeTypeDao {

	private AtomicInteger m_id = new AtomicInteger(0);

	@Override
	protected void generateId(final HwEntityAttributeType outage) {
		outage.setId(m_id.incrementAndGet());
	}

	@Override
	protected Integer getId(final HwEntityAttributeType outage) {
		return outage.getId();
	}

	@Override
	public HwEntityAttributeType get(Integer id) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public HwEntityAttributeType load(Integer id) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public HwEntityAttributeType findTypeByName(String name) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}

	@Override
	public HwEntityAttributeType findTypeByOid(String oid) {
		throw new UnsupportedOperationException("Not yet implemented!");
	}
}
