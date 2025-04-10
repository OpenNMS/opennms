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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.StringType;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.alarm.SituationSummary;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.google.common.collect.Lists;

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
        String hql = "from OnmsAlarm as alarms where alarms.reductionKey = ?";
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
        return getHibernateTemplate().execute(session -> (List<SituationSummary>) session.createSQLQuery(
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
    public List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, boolean processAcknowledgedAlarms, String restrictionColumn, String restrictionValue, String... groupByColumns) {

        String grouping = "";

        if (groupByColumns != null && groupByColumns.length > 0) {
            for (String groupByColumn : groupByColumns) {
                if (!"".equals(grouping)) {
                    grouping += ", ";
                }

                grouping += groupByColumn;
            }
        } else {
            grouping = entityNameColumn + ", " + entityIdColumn;
        }

        final String groupByClause = grouping;

        final String maximumSeverityQuery = (processAcknowledgedAlarms ? "max(distinct greatest(alarms.severity,3)) as maxSeverity " : "max(distinct case when alarms.alarmacktime is not null then 3 else greatest(alarms.severity,3) end) as maxSeverity ");

        return getHibernateTemplate().execute(new HibernateCallback<List<HeatMapElement>>() {
            @Override
            public List<HeatMapElement> doInHibernate(Session session) throws HibernateException, SQLException {

                // We can't use a prepared statement here as the variables are column names, and postgres
                // does not allow for parameter binding of column names.
                // Instead, we compare the values against all valid column names to validate.
                List<String> columns = new ArrayList<>(Arrays.asList(groupByColumns));
                if (entityIdColumn != null) {
                    columns.add(entityIdColumn);
                }
                columns.add(entityNameColumn);
                if (restrictionColumn != null) {
                    columns.add(restrictionColumn);
                }
                HibernateUtils.validateHibernateColumnNames(session.getSessionFactory(), Lists.newArrayList(OnmsServiceType.class, OnmsIpInterface.class, OnmsCategory.class, OnmsMonitoredService.class, OnmsAlarm.class, OnmsNode.class), true, columns.toArray(new String[0]));

                String queryStr =
                        "select coalesce(" + entityNameColumn + ",'Uncategorized'), " + (entityIdColumn != null ? entityIdColumn : "0") + ", " +
                                "count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) as servicesTotal, " +
                                "count(distinct node.nodeid) as nodeTotalCount, " +
                                maximumSeverityQuery +
                                "from node " +
                                "left join category_node using (nodeid) " +
                                "left join categories using (categoryid) " +
                                "left outer join ipinterface using (nodeid) " +
                                "left outer join ifservices on (ifservices.ipinterfaceid = ipinterface.id) " +
                                "left outer join service on (ifservices.serviceid = service.serviceid) " +
                                "left outer join alarms on (alarms.nodeid = node.nodeid and alarms.alarmtype in (1,3)) " +
                                "where nodeType <> 'D' " +
                                (restrictionColumn != null ? "and coalesce(" + restrictionColumn + ",'Uncategorized')=:restrictionValue " : "") +
                                "group by " + groupByClause + " having count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) > 0";
                
                Query query = session.createSQLQuery(queryStr);

                if (restrictionColumn != null) {
                    query.setParameter("restrictionValue",  restrictionValue, StringType.INSTANCE);
                }

                query.setResultTransformer(new ResultTransformer() {
                        private static final long serialVersionUID = 5152094813503430377L;

                        @Override
                        public Object transformTuple(Object[] tuple, String[] aliases) {
                            return new HeatMapElement((String) tuple[0], (Number) tuple[1], (Number) tuple[2], (Number) tuple[3], (Number) tuple[4]);
                        }

                        @SuppressWarnings("rawtypes")
                        @Override
                        public List transformList(List collection) {
                            return collection;
                        }
                });
                return (List<HeatMapElement>) query.list();
            }
        });
    }

    @Override
    public long getNumSituations() {
        return getHibernateTemplate().execute(s -> {
            BigInteger result = (BigInteger)s.createSQLQuery(
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
            BigInteger result = (BigInteger) s.createSQLQuery(
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

        return (List<OnmsAlarm>) getHibernateTemplate().executeFind(new HibernateCallback<List<OnmsEvent>>() {
            @Override
            public List<OnmsEvent> doInHibernate(Session session) throws HibernateException, SQLException {
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
