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

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import org.hibernate.transform.ResultTransformer;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefix;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpUnicastPrefixDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.PrefixByAS;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsPeerRib;

import com.google.common.base.Strings;

public class BmpUnicastPrefixDaoImpl extends AbstractDaoHibernate<BmpUnicastPrefix, Long> implements BmpUnicastPrefixDao {

    public BmpUnicastPrefixDaoImpl() {
        super(BmpUnicastPrefix.class);
    }

    @Override
    public BmpUnicastPrefix findByHashId(String hashId) {
        if (Strings.isNullOrEmpty(hashId)) {
            return null;
        }
        Criteria criteria = new Criteria(BmpUnicastPrefix.class);
        criteria.addRestriction(new EqRestriction("hashId", hashId));
        List<BmpUnicastPrefix> bmpUnicastPrefixes = findMatching(criteria);
        if (bmpUnicastPrefixes != null && bmpUnicastPrefixes.size() > 0) {
            return bmpUnicastPrefixes.get(0);
        }
        return null;
    }

    @Override
    public List<BmpUnicastPrefix> getUnicastPrefixesAfterDate(String hashId, Date time) {
        CriteriaBuilder criteriaBuilder = new CriteriaBuilder(BmpUnicastPrefix.class);
        criteriaBuilder.alias("bmpPeer", "bmpPeer")
                .and(Restrictions.eq("bmpPeer.hashId", hashId))
                .and(Restrictions.lt("bmpPeer.timeStamp", time));
        return findMatching(criteriaBuilder.toCriteria());
    }

    @Override
    public List<PrefixByAS> getPrefixesGroupedByAS() {

        String query = "SELECT DISTINCT new org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.PrefixByAS( " +
                "prefix.prefix, prefix.prefixLen, prefix.originAs, max(prefix.timestamp), count(prefix.bmpPeer)) " +
                "FROM BmpUnicastPrefix AS prefix " +
                "WHERE prefix.originAs != 0 AND prefix.originAs !=23456 AND prefix.isWithDrawn = false " +
                "GROUP BY prefix.prefix,  prefix.prefixLen, prefix.originAs";

        return findObjects(PrefixByAS.class, query);
    }

    @Override
    public List<StatsPeerRib> getPeerRibCountsByPeer() {
        return getHibernateTemplate().execute(session -> (List<StatsPeerRib>) session.createSQLQuery(
                "SELECT to_timestamp((cast((extract(epoch from now())) as bigint)/900)*900)," +
                        " peer_hash_id," +
                        " sum(CASE WHEN is_ipv4 = true THEN 1 ELSE 0 END) AS v4_prefixes, " +
                        " sum(CASE WHEN is_ipv4 = false THEN 1 ELSE 0 END) as v6_prefixes " +
                        " FROM bmp_ip_ribs " +
                        " WHERE is_withdrawn = false" +
                        " GROUP BY peer_hash_id"
        ).setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return new StatsPeerRib((Date) tuple[0], (String) tuple[1], (BigInteger) tuple[2], (BigInteger) tuple[3]);
            }

            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }
        }).list());
    }
}
