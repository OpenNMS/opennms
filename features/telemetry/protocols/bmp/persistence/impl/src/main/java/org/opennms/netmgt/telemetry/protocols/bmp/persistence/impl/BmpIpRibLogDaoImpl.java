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
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpIpRibLog;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.BmpIpRibLogDao;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByAsn;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByPeer;
import org.opennms.netmgt.telemetry.protocols.bmp.persistence.api.StatsByPrefix;

public class BmpIpRibLogDaoImpl extends AbstractDaoHibernate<BmpIpRibLog, Long> implements BmpIpRibLogDao {
    public BmpIpRibLogDaoImpl() {
        super(BmpIpRibLog.class);
    }

    @Override
    public List<StatsByPeer> getStatsByPeerForInterval(String interval) {

        List<StatsByPeer> statsByPeers = getHibernateTemplate().execute(session -> (List<StatsByPeer>) session.createSQLQuery(
                "SELECT to_timestamp((cast((extract(epoch from last_updated)) as bigint)/60)*60) at time zone 'utc' as IntervalTime," +
                        "peer_hash_id," +
                        "count(case WHEN bmp_ip_rib_log.is_withdrawn = true THEN 1 ELSE null END) as withdraws," +
                        "count(case WHEN bmp_ip_rib_log.is_withdrawn = false THEN 1 ELSE null END) as updates" +
                        " FROM bmp_ip_rib_log" +
                        " WHERE last_updated >= to_timestamp((cast((extract(epoch from now())) as bigint)/60)*60)  - INTERVAL '5 min' " +
                        "AND last_updated < to_timestamp((cast((extract(epoch from now())) as bigint)/60)*60)" +
                        " GROUP BY IntervalTime, peer_hash_id;"
        ).setResultTransformer(new ResultTransformer() {

            private static final long serialVersionUID = -6130733293672991513L;

            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return new StatsByPeer((Date) (tuple[0]), (String) tuple[1], (BigInteger) tuple[2], (BigInteger) tuple[3]);
            }


            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }
        }).list());

        return statsByPeers;
    }

    @Override
    public List<StatsByAsn> getStatsByAsnForInterval(String interval) {

        List<StatsByAsn> statsByAsnList = getHibernateTemplate().execute(session -> (List<StatsByAsn>) session.createSQLQuery(
                "SELECT to_timestamp((cast((extract(epoch from last_updated)) as bigint)/60)*60) at time zone 'utc' as IntervalTime," +
                        " peer_hash_id," +
                        " origin_as," +
                        " count(case WHEN bmp_ip_rib_log.is_withdrawn = true THEN 1 ELSE null END) as withdraws," +
                        " count(case WHEN bmp_ip_rib_log.is_withdrawn = false THEN 1 ELSE null END) as updates" +
                        " FROM bmp_ip_rib_log" +
                        " WHERE last_updated >= to_timestamp((cast((extract(epoch from now())) as bigint)/60)*60)  - INTERVAL '5 min' " +
                        " AND last_updated < to_timestamp((cast((extract(epoch from now())) as bigint)/60)*60)" +
                        " GROUP BY IntervalTime, peer_hash_id, origin_as;"
        ).setResultTransformer(new ResultTransformer() {

            private static final long serialVersionUID = -6130733293672991513L;

            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return new StatsByAsn((Date) (tuple[0]), (String) tuple[1], (BigInteger) tuple[2], (BigInteger) tuple[3], (BigInteger) tuple[4]);
            }


            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }
        }).list());

        return statsByAsnList;
    }

    @Override
    public List<StatsByPrefix> getStatsByPrefixForInterval(String interval) {

        List<StatsByPrefix> statsByPrefixList = getHibernateTemplate().execute(session -> (List<StatsByPrefix>) session.createSQLQuery(
                "SELECT to_timestamp((cast((extract(epoch from last_updated)) as bigint)/60)*60) at time zone 'utc' as IntervalTime," +
                        " peer_hash_id," +
                        " prefix," +
                        " prefix_len, " +
                        " count(case WHEN bmp_ip_rib_log.is_withdrawn = true THEN 1 ELSE null END) as withdraws," +
                        " count(case WHEN bmp_ip_rib_log.is_withdrawn = false THEN 1 ELSE null END) as updates" +
                        " FROM bmp_ip_rib_log" +
                        " WHERE last_updated >= to_timestamp((cast((extract(epoch from now())) as bigint)/60)*60)  - INTERVAL '5 min' " +
                        " AND last_updated < to_timestamp((cast((extract(epoch from now())) as bigint)/60)*60)" +
                        " GROUP BY IntervalTime, peer_hash_id, prefix, prefix_len;"
        ).setResultTransformer(new ResultTransformer() {

            private static final long serialVersionUID = -6130733293672991513L;

            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                return new StatsByPrefix((Date) (tuple[0]), (String) tuple[1], (String) tuple[2], (Integer) tuple[3], (BigInteger) tuple[4], (BigInteger) tuple[5]);
            }


            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }
        }).list());

        return statsByPrefixList;
    }
}
