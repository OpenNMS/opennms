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
import org.opennms.core.criteria.restrictions.GeRestriction;
import org.opennms.core.criteria.restrictions.LeRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfo;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpRpkiInfoDao;

public class BmpRpkiInfoImpl extends AbstractDaoHibernate<BmpRpkiInfo, Long> implements BmpRpkiInfoDao {
    public BmpRpkiInfoImpl() {
        super(BmpRpkiInfo.class);
    }

    @Override
    public BmpRpkiInfo findBmpRpkiInfoWith(String prefix, Integer prefixLenMax, Long originAsn) {
        Criteria criteria = new Criteria(BmpRpkiInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new EqRestriction("prefixLenMax", prefixLenMax));
        criteria.addRestriction(new EqRestriction("originAs", originAsn));
        List<BmpRpkiInfo> bmpRpkiInfos = findMatching(criteria);
        if (bmpRpkiInfos != null && bmpRpkiInfos.size() > 0) {
            return bmpRpkiInfos.get(0);
        }
        return null;
    }

    @Override
    public BmpRpkiInfo findMatchingRpkiInfoForGlobalRIb(String prefix, Integer prefixLen) {
        Criteria criteria = new Criteria(BmpRpkiInfo.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new LeRestriction("prefixLenMax", prefixLen));
        criteria.addRestriction(new GeRestriction("prefixLen", prefixLen));
        List<BmpRpkiInfo> bmpRpkiInfos = findMatching(criteria);
        if (bmpRpkiInfos != null && bmpRpkiInfos.size() > 0) {
            return bmpRpkiInfos.get(0);
        }
        return null;
    }
}
