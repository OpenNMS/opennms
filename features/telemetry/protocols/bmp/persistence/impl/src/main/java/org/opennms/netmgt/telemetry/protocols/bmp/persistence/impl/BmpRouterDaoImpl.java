/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.persistence.impl;

import java.util.ArrayList;
import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouter;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouterDao;

import com.google.common.base.Strings;

public class BmpRouterDaoImpl extends AbstractDaoHibernate<BmpRouter, Long> implements BmpRouterDao {
    public BmpRouterDaoImpl() {
        super(BmpRouter.class);
    }

    @Override
    public BmpRouter findByRouterHashId(String hashId) {
        if (Strings.isNullOrEmpty(hashId)) {
            return null;
        }
        Criteria criteria = new Criteria(BmpRouter.class);
        criteria.addRestriction(new EqRestriction("hashId", hashId));
        List<BmpRouter> bmpRouters = findMatching(criteria);
        if (bmpRouters != null && bmpRouters.size() > 0) {
            return bmpRouters.get(0);
        }
        return null;
    }

    @Override
    public List<BmpRouter> findRoutersByCollectorHashId(String collectorHashId) {
        if (Strings.isNullOrEmpty(collectorHashId)) {
            return new ArrayList<>();
        }
        Criteria criteria = new Criteria(BmpRouter.class);
        criteria.addRestriction(new EqRestriction("collectorHashId", collectorHashId));
        return findMatching(criteria);
    }
}
