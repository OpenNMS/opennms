/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.OnmsServiceType;

public class ServiceTypeDaoHibernate extends AbstractCachingDaoHibernate<OnmsServiceType, Integer, String> implements ServiceTypeDao {

    /**
     * <p>Constructor for ServiceTypeDaoHibernate.</p>
     */
    public ServiceTypeDaoHibernate() {
		super(OnmsServiceType.class, false);
	}
    

    /** {@inheritDoc} */
    @Override
    protected String getKey(OnmsServiceType serviceType) {
        return serviceType.getName();
    }



    /** {@inheritDoc} */
    @Override
    public OnmsServiceType findByName(final String name) {
        return findByCacheKey("from OnmsServiceType as svcType where svcType.name = ?", name);
    }
    
    
}
