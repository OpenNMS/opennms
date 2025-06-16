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
