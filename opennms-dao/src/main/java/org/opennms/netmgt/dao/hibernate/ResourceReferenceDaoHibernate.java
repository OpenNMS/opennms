/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import org.opennms.netmgt.dao.api.ResourceReferenceDao;
import org.opennms.netmgt.model.ResourceReference;

/**
 * DAO implementation for accessing ResourceReference objects.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see ResourceReference
 * @version $Id: $
 */
public class ResourceReferenceDaoHibernate extends AbstractDaoHibernate<ResourceReference, Integer> implements ResourceReferenceDao {

	/**
	 * <p>Constructor for ResourceReferenceDaoHibernate.</p>
	 */
	public ResourceReferenceDaoHibernate() {
		super(ResourceReference.class);
	}

    /** {@inheritDoc} */
        @Override
    public ResourceReference getByResourceId(String resourceId) {
        return findUnique("from ResourceReference where resourceId = ?", resourceId);
    }

}
