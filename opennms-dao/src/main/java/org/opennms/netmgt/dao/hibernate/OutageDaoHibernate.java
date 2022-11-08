/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.StringType;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.model.outage.CurrentOutageDetails;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.google.common.collect.Lists;

public class OutageDaoHibernate extends AbstractDaoHibernate<OnmsOutage, Integer> implements OutageDao {
    @Autowired
    private FilterDao m_filterDao;

    /**
     * <p>Constructor for OutageDaoHibernate.</p>
     */
    public OutageDaoHibernate() {
        super(OnmsOutage.class);
    }

    /**
     * <p>currentOutageCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Override
    public Integer currentOutageCount() {
        return queryInt("select count(*) from OnmsOutage as o where o.perspective is null and o.ifRegainedService is null");
    }

    /**
     * <p>currentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> currentOutages() {
        return find("from OnmsOutage as o where o.perspective is null and o.ifRegainedService is null");
    }

    @Override
    public Map<Integer, Set<OnmsOutage>> currentOutagesByServiceId() {
        // Retrieve open outages and the associated service id
        final List<Object[]> serviceOutageTuples = getHibernateTemplate().execute((HibernateCallback<List<Object[]>>) session ->
                session.createQuery("select o.monitoredService.id, o from OnmsOutage as o where o.perspective is null and o.ifRegainedService is null")
                        .list());
        // Group the results
        Map<Integer, Set<OnmsOutage>> outagesByServiceId = new HashMap<>();
        for (Object[] tuple : serviceOutageTuples) {
            Integer serviceId = (Integer)tuple[0];
            OnmsOutage outage = (OnmsOutage)tuple[1];
            outagesByServiceId.compute(serviceId, (k, v) -> {
                if (v == null) {
                    v = new HashSet<>();
                }
                v.add(outage);
                return v;
            });
        }
        return outagesByServiceId;
    }

    @Override
    public OnmsOutage currentOutageForService(OnmsMonitoredService service) {
        return findUnique("from OnmsOutage as o where o.perspective is null and o.monitoredService = ? and o.ifRegainedService is null", service);
    }

    @Override
    public OnmsOutage currentOutageForServiceFromPerspective(final OnmsMonitoredService service, final OnmsMonitoringLocation perspective) {
        return findUnique("from OnmsOutage as o where o.monitoredService = ? and o.perspective = ? and o.ifRegainedService is null", service, perspective);
    }

    @Override
    public Collection<OnmsOutage> currentOutagesForServiceFromPerspectivePoller(OnmsMonitoredService service) {
        return find("from OnmsOutage as o where o.monitoredService = ?  and o.perspective is not null and o.ifRegainedService is null", service);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> findAll(final Integer offset, final Integer limit) {
        return (Collection<OnmsOutage>) getHibernateTemplate().execute(new HibernateCallback<Collection<OnmsOutage>>() {

            @SuppressWarnings("unchecked")
            @Override
            public Collection<OnmsOutage> doInHibernate(final Session session) throws HibernateException, SQLException {
                return session.createCriteria(OnmsOutage.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .list();
            }

        });
    }

    /** {@inheritDoc} */
    @Override
    public Collection<CurrentOutageDetails> newestCurrentOutages(final List<String> serviceNames) {
        return getHibernateTemplate().execute(new HibernateCallback<List<CurrentOutageDetails>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<CurrentOutageDetails> doInHibernate(Session session) throws HibernateException, SQLException {
                final StringBuilder query = new StringBuilder()
                        .append("SELECT DISTINCT\n")
                        .append("        outages.outageId,\n")
                        .append("        outages.ifServiceId AS monitoredServiceId,\n")
                        .append("        service.serviceName AS serviceName,\n")
                        .append("        outages.ifLostService,\n")
                        .append("        node.nodeId,\n")
                        .append("        node.foreignSource,\n")
                        .append("        node.foreignId,\n")
                        .append("        node.location\n")
                        .append("FROM outages\n")
                        .append("        LEFT JOIN ifServices ON outages.ifServiceId = ifServices.id\n")
                        .append("        LEFT JOIN service ON ifServices.serviceId = service.serviceId\n")
                        .append("        LEFT JOIN ipInterface ON ifServices.ipInterfaceId = ipInterface.id\n")
                        .append("        LEFT JOIN node ON ipInterface.nodeId = node.nodeId\n")
                        .append("WHERE\n")
                        .append("        outages.ifRegainedService IS NULL AND outages.perspective IS NULL\n");
                if (serviceNames.size() > 0) {
                    query.append("        AND service.serviceName IN ( :serviceNames )\n");
                }
                query.append("ORDER BY outages.outageId\n")
                .append(";\n");

                Query sqlQuery = session.createSQLQuery( query.toString() );
                if (serviceNames.size() > 0) {
                    sqlQuery = sqlQuery.setParameterList("serviceNames", serviceNames);
                }

                return (List<CurrentOutageDetails>) sqlQuery.setResultTransformer(new ResultTransformer() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public Object transformTuple(Object[] tuple, String[] aliases) {
                                return new CurrentOutageDetails(
                                                         (Integer)tuple[0],
                                                         (Integer)tuple[1],
                                                         (String)tuple[2],
                                                         (Date)tuple[3],
                                                         (Integer)tuple[4],
                                                         (String)tuple[5],
                                                         (String)tuple[6],
                                                         (String)tuple[7]);
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

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> matchingCurrentOutages(final ServiceSelector selector) {
        final Set<InetAddress> matchingAddrs = new HashSet<InetAddress>(m_filterDao.getIPAddressList(selector.getFilterRule()));
        final Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());

        final List<OnmsOutage> matchingOutages = new LinkedList<>();
        final Collection<OnmsOutage> outages = currentOutages();
        for (final OnmsOutage outage : outages) {
            final OnmsMonitoredService svc = outage.getMonitoredService();
            if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) && matchingAddrs.contains(svc.getIpAddress())) {
                matchingOutages.add(outage);
            }

        }

        return matchingOutages;
    }

    /** {@inheritDoc} */
    @Override
    public int countOutagesByNode() {
        return getNodeOutageSummaries(0).size();
    }

    // final int nodeId, final String nodeLabel, final Date timeDown, final Date timeUp, final Date timeNow

    /** {@inheritDoc} */
    @Override
    public List<OutageSummary> getNodeOutageSummaries(final int rows) {
        final List<OutageSummary> outages = findObjects(
                OutageSummary.class,
                "SELECT DISTINCT new org.opennms.netmgt.model.outage.OutageSummary(node.id, node.label, max(outage.ifLostService)) " +
                        "FROM OnmsOutage AS outage " +
                        "LEFT JOIN outage.monitoredService AS monitoredService " +
                        "LEFT JOIN monitoredService.ipInterface AS ipInterface " +
                        "LEFT JOIN ipInterface.node AS node " +
                        "WHERE outage.ifRegainedService IS NULL " +
                        "AND outage.perspective IS NULL " +
                        "GROUP BY node.id, node.label " +
                        "ORDER BY max(outage.ifLostService) DESC, node.label ASC, node.id ASC"
        );
        if (rows == 0 || outages.size() < rows) {
            return outages;
        } else {
            return outages.subList(0, rows);
        }
    }

    @Override
    public List<HeatMapElement> getHeatMapItemsForEntity(String entityNameColumn, String entityIdColumn, String restrictionColumn, String restrictionValue, String... groupByColumns) {

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

        return getHibernateTemplate().execute(new HibernateCallback<List<HeatMapElement>>() {
            @Override
            @SuppressWarnings("unchecked")
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
                HibernateUtils.validateHibernateColumnNames(session.getSessionFactory(), Lists.newArrayList(OnmsServiceType.class, OnmsIpInterface.class, OnmsCategory.class, OnmsMonitoredService.class, OnmsOutage.class, OnmsNode.class), true, columns.toArray(new String[0]));

                // NOW, this is safe
                String queryStr = "select coalesce(" + entityNameColumn + ",'Uncategorized'), " + (entityIdColumn != null ? entityIdColumn : "0") + ", " +
                        "count(distinct case when outages.outageid is not null and ifservices.status <> 'D' then ifservices.id else null end) as servicesDown, " +
                        "count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) as servicesTotal, " +
                        "count(distinct case when outages.outageid is null and ifservices.status <> 'D' then node.nodeid else null end) as nodesUp, " +
                        "count(distinct node.nodeid) as nodeTotalCount " +
                        "from node left " +
                        "join category_node using (nodeid) left join categories using (categoryid) " +
                        "left outer join ipinterface using (nodeid) " +
                        "left outer join ifservices on (ifservices.ipinterfaceid = ipinterface.id) " +
                        "left outer join service on (ifservices.serviceid = service.serviceid) " +
                        "left outer join outages on (outages.ifserviceid = ifservices.id and outages.perspective is null and outages.ifregainedservice is null) " +
                        "where nodeType <> 'D' " +
                        (restrictionColumn != null ? "and coalesce(" + restrictionColumn + ",'Uncategorized')=:restrictionValue " : "") +
                        "group by " + groupByClause + " having count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) > 0";


                Query query = session.createSQLQuery(queryStr);
                if (restrictionColumn != null) {
                    query.setParameter("restrictionValue", restrictionValue, StringType.INSTANCE);
                }

                query.setResultTransformer(
                    new ResultTransformer() {
                        private static final long serialVersionUID = 5152094813503430377L;

                        @Override
                        public Object transformTuple(Object[] tuple, String[] aliases) {
                            return new HeatMapElement((String) tuple[0], (Number) tuple[1], (Number) tuple[2], (Number) tuple[3], (Number) tuple[4], (Number) tuple[5]);
                        }

                        @SuppressWarnings("rawtypes")
                        @Override
                        public List transformList(List collection) {
                            return collection;
                        }
                    }
                );
                return (List<HeatMapElement>) query.list();
                
            };
        });
    }

    @Override
    public Collection<OnmsOutage> getStatusChangesForApplicationIdBetween(final Date startDate, final Date endDate, final Integer applicationId) {
        return find("SELECT DISTINCT o FROM OnmsOutage o " +
                        "WHERE o.perspective IS NOT NULL AND " +
                        "o.monitoredService.id IN (SELECT m.id FROM OnmsApplication a LEFT JOIN a.monitoredServices m WHERE a.id = ?) AND " +
                        "o.perspective.id IN (SELECT p.id FROM OnmsApplication a LEFT JOIN a.perspectiveLocations p WHERE a.id = ?) AND " +
                        "((o.ifRegainedService >= ? AND o.ifLostService <= ?) OR (o.ifLostService <= ? AND o.ifRegainedService IS NULL))", applicationId, applicationId, startDate, endDate, endDate);
    }
}
