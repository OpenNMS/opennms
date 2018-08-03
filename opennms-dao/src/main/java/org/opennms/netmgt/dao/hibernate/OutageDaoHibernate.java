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

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.ResultTransformer;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.HeatMapElement;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateCallback;

public class OutageDaoHibernate extends AbstractDaoHibernate<OnmsOutage, Integer> implements OutageDao {
    private static final Comparator<OnmsOutage> OUTAGE_COMPARATOR = Comparator
            .nullsLast(Comparator
                       .comparing(OnmsOutage::getIfLostService)
                       .thenComparing(OnmsOutage::getIfRegainedService));

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
        return queryInt("select count(*) from OnmsOutage as o where o.ifRegainedService is null");
    }

    /**
     * <p>currentOutages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    @Override
    public Collection<OnmsOutage> currentOutages() {
        return find("from OnmsOutage as o where o.ifRegainedService is null");
    }

    @Override
    public OnmsOutage currentOutageForService(OnmsMonitoredService service) {
        return findUnique("from OnmsOutage as o where o.monitoredService = ? and o.ifRegainedService is null", service);
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

    private Stream<OnmsOutage> initializeOutage(Stream<OnmsOutage> outages) {
        return outages
                .map(outage -> {
                    Hibernate.initialize(outage.getForeignId());
                    Hibernate.initialize(outage.getMonitoredService());
                    Hibernate.initialize(outage.getMonitoredService().getIpInterface());
                    Hibernate.initialize(outage.getMonitoredService().getIpInterface().getNode());
                    Hibernate.initialize(outage.getMonitoredService().getIpInterface().getNode());
                    Hibernate.initialize(outage.getMonitoredService().getServiceType());
                    return outage;
                })
                .sorted(OUTAGE_COMPARATOR);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> matchingOutages(final ServiceSelector selector) {
        return getHibernateTemplate().execute(new HibernateCallback<List<OnmsOutage>>() {
            @Override
            public List<OnmsOutage> doInHibernate(Session session) throws HibernateException, SQLException {
                Transaction tx = null;
                try {
                    tx = session.beginTransaction();
                    final Set<InetAddress> matchingAddrs = new HashSet<InetAddress>(m_filterDao.getIPAddressList(selector.getFilterRule()));
                    @SuppressWarnings("unchecked")
                    final Collection<OnmsOutage> outages = (Collection<OnmsOutage>)session.createQuery("FROM OnmsOutage AS o WHERE o.monitoredService.serviceType.name IN (:services)").setParameterList("services", selector.getServiceNames()).list();

                    return initializeOutage(
                                            outages.parallelStream()
                                            .filter(outage -> matchingAddrs.contains(outage.getMonitoredService().getIpAddress()))
                            ).collect(Collectors.toList());
                } finally {
                    if (tx != null) {
                        tx.commit();
                    }
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public Collection<OnmsOutage> matchingLatestOutages(final ServiceSelector selector) {
        final Set<String> matchingAddrs = m_filterDao.getIPAddressList(selector.getFilterRule()).stream().map(InetAddressUtils::str).collect(Collectors.toSet());
        final List<Integer> outageIds = getHibernateTemplate().execute(new HibernateCallback<List<Integer>>() {
            @Override
            @SuppressWarnings("unchecked")
            public List<Integer> doInHibernate(Session session) throws HibernateException, SQLException {
                return (List<Integer>) session.createSQLQuery(
                    "SELECT DISTINCT MAX(outageID) AS outageID\n" +
                    "  FROM outages\n" +
                    "  GROUP BY ifServiceID\n"
                ).list();
            }
        });

        if (outageIds.size() == 0) {
            return Collections.emptyList();
        }

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class)
                .join("monitoredService.ipInterface", "iface")
                .in("id", outageIds)
                .orderBy("id");
        if (selector.getServiceNames().size() > 0) {
            builder
                .join("monitoredService.serviceType", "serviceType")
                .in("serviceType.name", selector.getServiceNames());
        }
        return findMatching(builder.toCriteria()).stream().filter(outage -> {
            return matchingAddrs.contains(InetAddressUtils.str(outage.getIpAddress()));
        }).collect(Collectors.toList());
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
                return (List<HeatMapElement>) session.createSQLQuery(
                        "select coalesce(" + entityNameColumn + ",'Uncategorized'), " + entityIdColumn + ", " +
                                "count(distinct case when outages.outageid is not null and ifservices.status <> 'D' then ifservices.id else null end) as servicesDown, " +
                                "count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) as servicesTotal, " +
                                "count(distinct case when outages.outageid is null and ifservices.status <> 'D' then node.nodeid else null end) as nodesUp, " +
                                "count(distinct node.nodeid) as nodeTotalCount " +
                                "from node left " +
                                "join category_node using (nodeid) left join categories using (categoryid) " +
                                "left outer join ipinterface using (nodeid) " +
                                "left outer join ifservices on (ifservices.ipinterfaceid = ipinterface.id) " +
                                "left outer join service on (ifservices.serviceid = service.serviceid) " +
                                "left outer join outages on (outages.ifserviceid = ifservices.id and outages.ifregainedservice is null) " +
                                "where nodeType <> 'D' " +
                                (restrictionColumn != null ? "and coalesce(" + restrictionColumn + ",'Uncategorized')='" + restrictionValue + "' " : "") +
                                "group by " + groupByClause + " having count(distinct case when ifservices.status <> 'D' then ifservices.id else null end) > 0")
                        .setResultTransformer(new ResultTransformer() {
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
                        }).list();
            }
        });
    }
}
