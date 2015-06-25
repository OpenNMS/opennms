/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;
import org.springframework.orm.hibernate3.HibernateCallback;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

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
        if (nodeIds.size() < 1) {
            return Collections.emptyList();
        }
        StringBuilder sql = new StringBuilder();
        //count(*) - count(alarm.alarmAckTime) counts only the unacknowledged alarms
        sql.append("SELECT DISTINCT new org.opennms.netmgt.model.alarm.AlarmSummary( node.id, node.label, min(alarm.lastEventTime), max(alarm.severity), (count(*) - count(alarm.alarmAckTime)) ) ");
        sql.append("FROM OnmsAlarm AS alarm ");
        sql.append("LEFT JOIN alarm.node AS node ");
        sql.append("WHERE node.id IS NOT NULL AND alarm.severity > 3 ");

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

    @Override
    public List<EdgeAlarmStatusSummary> getLldpEdgeAlarmSummaries(List<Integer> lldpLinkIds) {
        if (lldpLinkIds.size() < 1) {
            return Collections.emptyList();
        }

        /*StringBuilder sql = new StringBuilder();
        sql.append("SELECT new org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary( LEAST(s.id, t.id), GREATEST(s.id, t.id), alarm.uei)\n");
        sql.append("FROM LldpLink as s\n");
        sql.append("LEFT JOIN org.opennms.netmgt.model.LldpLink as t\n");
        sql.append("with s.lldpRemPortDescr = t.lldpPortDescr AND\n");
        sql.append(" s.lldpRemPortIdSubtype = t.lldpPortIdSubtype AND\n");
        sql.append(" s.lldpRemPortId = t.lldpPortId\n");
        sql.append("LEFT JOIN\n");
        sql.append("  OnmsAlarm as alarm\n");
        sql.append("with\n");
        sql.append(" alarm.node.id = s.node.id AND\n");
        sql.append(" s.lldpPortIfindex = alarm.ifindex\n");
        sql.append("GROUP BY\n");
        sql.append(" s.id,\n");
        sql.append(" t.id,\n");
        sql.append(" s.nodeId,\n");
        sql.append(" t.nodeId,\n");
        sql.append(" alarm.uei,\n");
        sql.append(" alarm.lastEventTime\n");
        sql.append("ORDER BY\n");
        sql.append(" alarm.lastEventTime DESC limit 1");*/
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT new org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary( LEAST(s.id, t.id), GREATEST(s.id, t.id), alarm.uei)\n");
        sql.append("FROM LldpLink as s\n");
        sql.append("LEFT JOIN org.opennms.netmgt.model.LldpLink as t\n");
        sql.append("LEFT JOIN\n");
        sql.append("  OnmsAlarm as alarm\n");
        sql.append("with\n");
        sql.append(" alarm.node.id = s.node.id AND\n");
        sql.append(" s.lldpPortIfindex = alarm.ifindex\n");
        sql.append("GROUP BY\n");
        sql.append(" s.id,\n");
        sql.append(" t.id,\n");
        sql.append(" s.node.id,\n");
        sql.append(" t.node.id,\n");
        sql.append(" alarm.uei,\n");
        sql.append(" alarm.lastEventTime\n");
        sql.append("ORDER BY\n");
        sql.append(" alarm.lastEventTime DESC limit 1");


        return findObjects(EdgeAlarmStatusSummary.class, sql.toString());
    }

    /** {@inheritDoc} */
    @Override
    public List<AlarmSummary> getNodeAlarmSummaries() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT new org.opennms.netmgt.model.alarm.AlarmSummary(node.id, node.label, min(alarm.lastEventTime), max(alarm.severity), count(*)) ");
        sql.append("FROM OnmsAlarm AS alarm ");
        sql.append("LEFT JOIN alarm.node AS node ");
        sql.append("WHERE node.id IS NOT NULL AND alarm.severity > 3 AND alarm.alarmAckTime IS NULL ");
        sql.append("GROUP BY node.id, node.label ");
        sql.append("ORDER BY min(alarm.lastEventTime) DESC, node.label ASC");
        return findObjects(AlarmSummary.class, sql.toString());
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
                return (List<HeatMapElement>) session.createSQLQuery(
                        "select coalesce(" + entityNameColumn + ",'Uncategorized'), " + entityIdColumn + ", " +
                                "count(distinct case when ifservices.status = 'A' then ifservices.id else null end) as servicesTotal, " +
                                "count(distinct node.nodeid) as nodeTotalCount, " +
                                maximumSeverityQuery +
                                "from node " +
                                "left join category_node using (nodeid) " +
                                "left join categories using (categoryid) " +
                                "left outer join ipinterface using (nodeid) " +
                                "left outer join ifservices on (ifservices.ipinterfaceid = ipinterface.id) " +
                                "left outer join alarms on (alarms.nodeid = node.nodeid) " +
                                "where nodeType <> 'D' " +
                                (restrictionColumn != null ? "and coalesce(" + restrictionColumn + ",'Uncategorized')='" + restrictionValue + "' " : "") +
                                "group by " + groupByClause)
                        .setResultTransformer(new ResultTransformer() {
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
                        }).list();
            }
        });
    }
}
