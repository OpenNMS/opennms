/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ncs.persistence;

import java.util.List;

import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.ncs.NCSComponent;
import org.opennms.netmgt.model.ncs.NCSComponentRepository;
import org.springframework.dao.DataAccessException;

public class NCSComponentDao extends AbstractDaoHibernate<NCSComponent, Long> implements NCSComponentRepository {

	public NCSComponentDao() {
		super(NCSComponent.class);
	}

	@Override
	public NCSComponent findByTypeAndForeignIdentity(String type, String foreignSource, String foreignId) {
		return findUnique("from NCSComponent as ncs where ncs.type = ? and ncs.foreignSource = ? and ncs.foreignId = ?", type, foreignSource, foreignId);
	}

	@Override
	public List<NCSComponent> findComponentsThatDependOn(NCSComponent component) {
		return find("from NCSComponent as ncs where ? in elements(ncs.subcomponents)", component);
	}

	@Override
	public List<NCSComponent> findComponentsWithAttribute(String attrKey, String attrValue) {
		return find("from NCSComponent as ncs where ncs.attributes[?] = ?", attrKey, attrValue);
	}

	@Override
	public void save(NCSComponent entity) throws DataAccessException {
		super.save(entity);
	}

	@Override
	public void saveOrUpdate(NCSComponent entity) throws DataAccessException {
		super.saveOrUpdate(entity);
		
	}

	@Override
	public List<NCSComponent> findComponentsByNodeId(int nodeid) {
			return find("select distinct ncs from NCSComponent as ncs, OnmsNode as n left join fetch ncs.attributes where ncs.nodeIdentification.foreignSource = n.foreignSource and ncs.nodeIdentification.foreignId = n.foreignId and n.id = ?", nodeid);
	}
	
	
	
	
    

}
