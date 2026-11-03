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
package org.opennms.netmgt.dao.hibernate;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.SituationSummary;
import org.springframework.orm.hibernate5.HibernateCallback;


/**
 * <p>AlarmDaoHibernate class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class AlarmDaoHibernate extends AbstractDaoHibernate<OnmsAlarm, Integer> implements AlarmDao {

    public AlarmDaoHibernate() {
        super(OnmsAlarm.class);
    }

    /** {@inheritDoc} */
    @Override
    public OnmsAlarm findByReductionKey(String reductionKey) {
        String hql = "from OnmsAlarm as alarms where alarms.reductionKey = ?1";
        return super.findUnique(hql, reductionKey);
    }

    /** {@inheritDoc} */
    @Override
    public List<AlarmSummary> getNodeAlarmSummariesIncludeAcknowledgedOnes(List<Integer> nodeIds) {
        if (nodeIds.isEmpty()) {
            return Collections.emptyList();
        }
        final StringBuilder sql = new StringBuilder();
        //count(*) - count(alarm.alarmAckTime) counts only the unacknowledged alarms
        sql.append("SELECT DISTINCT new org.opennms.netmgt.model.alarm.AlarmSummary( node.id, node.label, min(alarm.lastEventTime), max(alarm.severity), (count(*) - count(alarm.alarmAckTime)) ) ");
        sql.append("FROM OnmsAlarm AS alarm ");
        sql.append("LEFT JOIN alarm.node AS node ");
        sql.append("WHERE node.id IS NOT NULL AND alarm.severity != " + OnmsSeverity.CLEARED.getId());

        // optional
        if (nodeIds.size() == 1) {
            sql.append("AND node.id = " + nodeIds.get(0) + " ");
        } else {
            sql.append("AND node.id in (");
            for (int i = 0; i < nodeIds.size(); i++) {
                sql.append(nodeIds.get(i));
                if (i < nodeIds.size() - 1) {
                    sql.append(",");
                }
            }
            sql.append(") ");
        }
        sql.append("GROUP BY node.id, node.label ");
        return findObjects(AlarmSummary.class, sql.toString());
    }

    /** {@inheritDoc} */
    @Override
    public List<AlarmSummary> getNodeAlarmSummaries() {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT new org.opennms.netmgt.model.alarm.AlarmSummary(node.id, node.label, min(alarm.lastEventTime), max(alarm.severity), count(*)) ");
        sql.append("FROM OnmsAlarm AS alarm ");
        sql.append("LEFT JOIN alarm.node AS node ");
        sql.append("WHERE node.id IS NOT NULL AND alarm.severity > 3 AND alarm.alarmAckTime IS NULL ");
        sql.append("GROUP BY node.id, node.label ");
        sql.append("ORDER BY min(alarm.lastEventTime) DESC, node.label ASC");
        return findObjects(AlarmSummary.class, sql.toString());
    }

    /** {@inheritDoc} */
    @Override
    public List<SituationSummary> getSituationSummaries() {
        return getHibernateTemplate().execute(session -> (List<SituationSummary>) session.createNativeQuery(
                "SELECT " +
                        "  a1.alarmid, " +
                        "  a1.severity, " +
                        "  string_agg(DISTINCT n2.location, ', ')," +
                        "  COUNT(DISTINCT n2.nodeid) AS nodeCount, " +
                        "  COUNT(DISTINCT s1.related_alarm_id) AS alarmCount, " +
                        "  MIN(a2.lastEventTime) " +
                        "FROM " +
                        "  alarms a1 JOIN alarm_situations s1 ON a1.alarmid=s1.situation_id " +
                        "  LEFT JOIN alarms a2 ON s1.related_alarm_id = a2.alarmid " +
                        "  LEFT JOIN node n2 ON a2.nodeid = n2.nodeid " +
                        "WHERE " +
                        "  a1.alarmAckTime IS NULL AND a1.severity>3 " +
                        "GROUP BY " +
                        "  a1.alarmid " +
                        "ORDER BY " +
                        "  a1.severity DESC, " +
                        "  COUNT(DISTINCT s1.related_alarm_id) DESC")
                .setResultTransformer(new ResultTransformer() {
                    @Override
                    public Object transformTuple(Object[] tuple, String[] aliases) {
                        return new SituationSummary((Integer) tuple[0], OnmsSeverity.get((Integer) tuple[1]), (String) tuple[2], ((BigInteger) tuple[3]).longValue(), ((BigInteger) tuple[4]).longValue(), (Date) tuple[5]);
                    }

                    @SuppressWarnings("rawtypes")
                    @Override
                    public List transformList(List collection) {
                        return collection;
                    }
                }).list());
    }

    @Override
    public long getNumSituations() {
        return getHibernateTemplate().execute(s -> {
            BigInteger result = (BigInteger)s.createNativeQuery(
                    "SELECT COUNT( DISTINCT situation_id ) FROM alarm_situations").uniqueResult();
            return result != null ? result.longValue() : 0L;
        });
    }

    @Override
    public long getNumAlarmsLastHours(int hours) {

        if (hours <= 0) {
            return 0L;  // Return 0 for negative and 0 hours instead of letting SQL handle it, SQL also returns 0.
        }
        return getHibernateTemplate().execute(s -> {
            BigInteger result = (BigInteger) s.createNativeQuery(
                            "SELECT COUNT(*) FROM alarms WHERE firsteventtime >= NOW() - (:hours * INTERVAL '1 hour')")
                    .setParameter("hours", hours)
                    .uniqueResult();

            return result != null ? result.longValue() : 0L;
        });
    }

    public List<OnmsAlarm> getAlarmsForEventParameters(final Map<String, String> eventParameters) {
        final StringBuffer hqlStringBuffer = new StringBuffer("From OnmsAlarm a where ");
        for (int i = 0; i < eventParameters.size(); i++) {
            if (i > 0) {
                hqlStringBuffer.append(" and ");
            }
            hqlStringBuffer.append("exists (select p.event from OnmsEventParameter p where a.lastEvent=p.event and p.name = :name" + i + " and p.value like :value" + i + ")");
        }

        return getHibernateTemplate().execute(new HibernateCallback<List<OnmsAlarm>>() {
            @Override
            public List<OnmsAlarm> doInHibernate(Session session) throws HibernateException {
                Query q = session.createQuery(hqlStringBuffer.toString());
                int i = 0;
                for (final Map.Entry<String, String> entry : eventParameters.entrySet()) {
                    q = q.setParameter("name" + i, entry.getKey()).setParameter("value" + i, entry.getValue());
                    i++;
                }

                return q.list();
            }
        });
    }
}
