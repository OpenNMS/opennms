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
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.hibernate.transform.ResultTransformer;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.core.criteria.restrictions.LtRestriction;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRib;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpGlobalIpRibDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsIpOrigins;

public class BmpGlobalIpRibDaoImpl extends AbstractDaoHibernate<BmpGlobalIpRib, Long> implements BmpGlobalIpRibDao {

    public BmpGlobalIpRibDaoImpl() {
        super(BmpGlobalIpRib.class);
    }

    @Override
    public BmpGlobalIpRib findByPrefixAndAS(String prefix, Long originAS) {

        Criteria criteria = new Criteria(BmpGlobalIpRib.class);
        criteria.addRestriction(new EqRestriction("prefix", prefix));
        criteria.addRestriction(new EqRestriction("recvOriginAs", originAS));
        List<BmpGlobalIpRib> globalIpRibs = findMatching(criteria);
        if (globalIpRibs != null && globalIpRibs.size() > 0) {
            return globalIpRibs.get(0);
        }
        return null;
    }

    @Override
    public List<StatsIpOrigins> getStatsIpOrigins() {
        return getHibernateTemplate().execute(session -> (List<StatsIpOrigins>) session.createSQLQuery(
                "SELECT to_timestamp((cast((extract(epoch from now())) as bigint)/900)*900)," +
                        " recv_origin_as," +
                        " sum(case when family(inet(prefix)) = 4 THEN 1 ELSE 0 END) as v4_prefixes, " +
                        " sum(case when family(inet(prefix)) = 6 THEN 1 ELSE 0 END) as v6_prefixes, " +
                        " sum(case when rpki_origin_as > 0 and family(inet(prefix)) = 4 THEN 1 ELSE 0 END) as v4_with_rpki," +
                        " sum(case when rpki_origin_as > 0 and family(inet(prefix)) = 6 THEN 1 ELSE 0 END) as v6_with_rpki," +
                        " sum(case when irr_origin_as > 0 and family(inet(prefix)) = 4 THEN 1 ELSE 0 END) as v4_with_irr," +
                        " sum(case when irr_origin_as > 0 and family(inet(prefix)) = 6 THEN 1 ELSE 0 END) as v6_with_irr" +
                        " FROM bmp_global_ip_ribs " +
                        " GROUP BY recv_origin_as "
        ).setResultTransformer(new ResultTransformer() {
            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return new StatsIpOrigins((Date) tuple[0], (BigInteger) tuple[1],
                        (BigInteger) tuple[2], (BigInteger) tuple[3], (BigInteger) tuple[4], (BigInteger) tuple[5], (BigInteger) tuple[6], (BigInteger) tuple[7]);
            }

            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }
        }).list());
    }

    @Override
    public List<BmpGlobalIpRib> findGlobalRibsBeforeGivenTime(long timeInSecs) {
        Criteria criteria = new Criteria(BmpGlobalIpRib.class);
        Instant instantForGivenTime = Instant.now().minusSeconds(timeInSecs);
        criteria.addRestriction(new EqRestriction("shouldDelete", true));
        criteria.addRestriction(new LtRestriction("timeStamp", Date.from(instantForGivenTime)));
        return findMatching(criteria);

    }

    @Override
    public int deleteGlobalRibsBeforeGivenTime(long timeInSecs) {
        Instant instantForGivenTime = Instant.now().minusSeconds(timeInSecs);
        Date givenTime = Date.from(instantForGivenTime);
        String hql = "DELETE FROM BmpGlobalIpRib where shouldDelete = true and timeStamp < ?1";
        Object[] values = {givenTime};
        return bulkDelete(hql, values);
    }

    @Override
    public List<BigInteger> getAsnsNotExistInAsnInfo() {

        return  getHibernateTemplate().execute(session -> (List<BigInteger>) session.createSQLQuery(
                "SELECT DISTINCT recv_origin_as FROM bmp_global_ip_ribs r" +
                " LEFT JOIN bmp_asn_info asnInfo ON asnInfo.asn = r.recv_origin_as" +
                        " WHERE asnInfo.asn is null")
                .setResultTransformer(new ResultTransformer() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        return tuple[0];
                    }

                    @SuppressWarnings("rawtypes")
                    @Override
                    public List transformList(List collection) {
                        return collection;
                    }
                }).list());
    }

}
