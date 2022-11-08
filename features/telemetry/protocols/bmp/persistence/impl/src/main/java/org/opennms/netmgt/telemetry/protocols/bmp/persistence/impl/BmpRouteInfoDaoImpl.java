/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

import java.util.List;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRouteInfoDao;

public class BmpRouteInfoDaoImpl extends AbstractDaoHibernate<BmpRouteInfo, Long> implements BmpRouteInfoDao {

    public BmpRouteInfoDaoImpl() {
        super(BmpRouteInfo.class);
    }

    @Override
    public BmpRouteInfo findByPrefixAndOriginAs(String prefix, Integer prefixLen, Long originAsn) {

        Criteria criteria = new Criteria(BmpRouteInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new EqRestriction("prefixLen", prefixLen));
        criteria.addRestriction(new EqRestriction("originAs", originAsn));
        List<BmpRouteInfo> bmpRouteInfoList = findMatching(criteria);
        if (bmpRouteInfoList != null && bmpRouteInfoList.size() > 0) {
            return bmpRouteInfoList.get(0);
        }
        return null;
    }

    @Override
    public List<BmpRouteInfo> findByPrefix(String prefix) {
        Criteria criteria = new Criteria(BmpRouteInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        return findMatching(criteria);
    }

}
