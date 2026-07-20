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
package org.opennms.netmgt.surveillance.views.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.Order;
import org.opennms.core.criteria.restrictions.Restriction;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.core.criteria.restrictions.SqlRestriction.Type;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.CategoryDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.NotificationDao;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.SurveillanceStatus;
import org.opennms.netmgt.surveillance.views.SurveillanceView;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDataService;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDef;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Port of the aggregation logic from the retired Vaadin
 * {@code DefaultSurveillanceViewService}: the category-intersection SQL, the
 * notification severity bucketing, and the 24-hour RTC math are carried over
 * unchanged. Everything runs inside {@link TransactionOperations} and returns
 * detached value objects.
 */
public class DefaultSurveillanceViewDataService implements SurveillanceViewDataService {

    private NodeDao m_nodeDao;
    private CategoryDao m_categoryDao;
    private AlarmDao m_alarmDao;
    private NotificationDao m_notificationDao;
    private OutageDao m_outageDao;
    private MonitoredServiceDao m_monitoredServiceDao;
    private TransactionOperations m_transactionOperations;

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setCategoryDao(CategoryDao categoryDao) {
        m_categoryDao = categoryDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public void setNotificationDao(NotificationDao notificationDao) {
        m_notificationDao = notificationDao;
    }

    public void setOutageDao(OutageDao outageDao) {
        m_outageDao = outageDao;
    }

    public void setMonitoredServiceDao(MonitoredServiceDao monitoredServiceDao) {
        m_monitoredServiceDao = monitoredServiceDao;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        m_transactionOperations = transactionOperations;
    }

    @Override
    public SurveillanceStatus[][] calculateCellStatus(final SurveillanceView view) {
        return m_transactionOperations.execute(status -> {
            final List<Collection<OnmsCategory>> rowCategories = resolveDefs(view, view.getRows());
            final List<Collection<OnmsCategory>> columnCategories = resolveDefs(view, view.getColumns());

            final SurveillanceStatus[][] cellStatus = new SurveillanceStatus[rowCategories.size()][columnCategories.size()];
            for (int rowIndex = 0; rowIndex < rowCategories.size(); rowIndex++) {
                for (int colIndex = 0; colIndex < columnCategories.size(); colIndex++) {
                    cellStatus[rowIndex][colIndex] = m_nodeDao.findSurveillanceStatusByCategoryLists(rowCategories.get(rowIndex), columnCategories.get(colIndex));
                }
            }
            return cellStatus;
        });
    }

    private List<Collection<OnmsCategory>> resolveDefs(final SurveillanceView view, final List<SurveillanceViewDef> defs) {
        final List<Collection<OnmsCategory>> resolved = new ArrayList<>(defs.size());
        for (final SurveillanceViewDef def : defs) {
            final List<OnmsCategory> categories = new ArrayList<>(def.getCategories().size());
            for (final String categoryName : def.getCategories()) {
                final OnmsCategory category = m_categoryDao.findByName(categoryName);
                if (category == null) {
                    throw new IllegalArgumentException("Surveillance view '" + view.getName() + "' references the unknown category '" + categoryName + "'");
                }
                categories.add(category);
            }
            resolved.add(categories);
        }
        return resolved;
    }

    @Override
    public List<SurveillanceAlarm> getAlarmsForCategories(final Set<String> rowCategories, final Set<String> columnCategories) {
        if (rowCategories.isEmpty() || columnCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return m_transactionOperations.execute(status -> {
            final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsAlarm.class);

            criteriaBuilder.alias("node", "node");
            criteriaBuilder.ne("node.type", "D");
            criteriaBuilder.isNull("alarmAckUser");

            criteriaBuilder.limit(100);
            criteriaBuilder.distinct();

            addCategoryIntersectionRestriction(criteriaBuilder, "{alias}.nodeId", rowCategories, columnCategories);

            final List<SurveillanceAlarm> alarms = new ArrayList<>();
            for (final OnmsAlarm alarm : m_alarmDao.findMatching(criteriaBuilder.toCriteria())) {
                final OnmsNode node = alarm.getNode();
                alarms.add(new SurveillanceAlarm(
                        alarm.getId(),
                        alarm.getUei(),
                        alarm.getSeverity() != null ? alarm.getSeverity().getLabel() : null,
                        node != null ? node.getId() : null,
                        node != null ? node.getLabel() : null,
                        alarm.getLogMsg(),
                        alarm.getLastEventTime(),
                        alarm.getCounter()));
            }
            return alarms;
        });
    }

    @Override
    public List<SurveillanceNotification> getNotificationsForCategories(final Set<String> rowCategories, final Set<String> columnCategories) {
        if (rowCategories.isEmpty() || columnCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return m_transactionOperations.execute(status -> {
            final Date fifteenMinutesAgo = new Date(System.currentTimeMillis() - (15 * 60 * 1000));
            final Date oneWeekAgo = new Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000));

            final List<SurveillanceNotification> notifications = new ArrayList<>();
            notifications.addAll(getNotificationsWithCriterias(rowCategories, columnCategories, "Critical", Restrictions.isNull("respondTime"), Restrictions.le("pageTime", fifteenMinutesAgo)));
            notifications.addAll(getNotificationsWithCriterias(rowCategories, columnCategories, "Minor", Restrictions.isNull("respondTime"), Restrictions.gt("pageTime", fifteenMinutesAgo)));
            notifications.addAll(getNotificationsWithCriterias(rowCategories, columnCategories, "Normal", Restrictions.isNotNull("respondTime"), Restrictions.gt("pageTime", oneWeekAgo)));
            return notifications;
        });
    }

    @Override
    public List<NodeRtc> getNodeRtcsForCategories(final Set<String> rowCategories, final Set<String> columnCategories) {
        if (rowCategories.isEmpty() || columnCategories.isEmpty()) {
            return Collections.emptyList();
        }
        return m_transactionOperations.execute(status -> {
            final CriteriaBuilder outageCriteriaBuilder = new CriteriaBuilder(OnmsOutage.class);

            outageCriteriaBuilder.isNull("perspective");
            outageCriteriaBuilder.alias("monitoredService", "monitoredService", Alias.JoinType.INNER_JOIN);
            outageCriteriaBuilder.alias("monitoredService.ipInterface", "ipInterface", Alias.JoinType.INNER_JOIN);
            outageCriteriaBuilder.alias("monitoredService.ipInterface.node", "node", Alias.JoinType.INNER_JOIN);
            outageCriteriaBuilder.eq("monitoredService.status", "A");
            outageCriteriaBuilder.ne("ipInterface.isManaged", "D");
            outageCriteriaBuilder.ne("node.type", "D");

            final CriteriaBuilder serviceCriteriaBuilder = new CriteriaBuilder(OnmsMonitoredService.class);

            serviceCriteriaBuilder.alias("ipInterface", "ipInterface", Alias.JoinType.INNER_JOIN);
            serviceCriteriaBuilder.alias("ipInterface.node", "node", Alias.JoinType.INNER_JOIN);
            serviceCriteriaBuilder.alias("serviceType", "serviceType", Alias.JoinType.INNER_JOIN);
            serviceCriteriaBuilder.alias("currentOutages", "currentOutages", Alias.JoinType.LEFT_JOIN);
            serviceCriteriaBuilder.eq("status", "A");
            serviceCriteriaBuilder.ne("ipInterface.isManaged", "D");
            serviceCriteriaBuilder.ne("node.type", "D");

            // HACK (carried over from the Vaadin service): Hibernate aliases
            // 'node' as 'node2_' for this entity graph, so the raw SQL
            // restriction must use that alias.
            addCategoryIntersectionRestriction(serviceCriteriaBuilder, "node2_.nodeId", rowCategories, columnCategories);

            return getNodeRtcsForCriteria(serviceCriteriaBuilder.toCriteria(), outageCriteriaBuilder.toCriteria());
        });
    }

    /**
     * Restricts a query to nodes that are members of at least one row category
     * AND at least one column category (the surveillance-view cell semantics).
     */
    private static void addCategoryIntersectionRestriction(final CriteriaBuilder criteriaBuilder, final String nodeIdProperty, final Set<String> rowCategories, final Set<String> columnCategories) {
        final List<String> parameters = new ArrayList<>(rowCategories);
        parameters.addAll(columnCategories);

        final Type[] types = new Type[parameters.size()];
        Arrays.fill(types, Type.STRING);

        criteriaBuilder.sql(
                createQuery(nodeIdProperty, rowCategories.size(), columnCategories.size()),
                parameters.toArray(new String[parameters.size()]),
                types);
    }

    private static String createQuery(final String nodeIdProperty, final int rowCategoryCount, final int columnCategoryCount) {
        final StringBuilder stringBuffer = new StringBuilder();

        stringBuffer.append(nodeIdProperty + " in (select distinct cn.nodeId from category_node cn join categories c on cn.categoryId = c.categoryId where c.categoryName in (");

        String[] questionMarks = new String[rowCategoryCount];
        Arrays.fill(questionMarks, "?");
        stringBuffer.append(String.join(",", questionMarks));

        stringBuffer.append("))");

        stringBuffer.append("and " + nodeIdProperty + " in (select distinct cn.nodeId from category_node cn join categories c on cn.categoryId = c.categoryId where c.categoryName in (");

        questionMarks = new String[columnCategoryCount];
        Arrays.fill(questionMarks, "?");
        stringBuffer.append(String.join(",", questionMarks));

        stringBuffer.append("))");

        return stringBuffer.toString();
    }

    private List<SurveillanceNotification> getNotificationsWithCriterias(final Set<String> rowCategories, final Set<String> columnCategories, final String severity, final Restriction... criterias) {
        final CriteriaBuilder criteriaBuilder = new CriteriaBuilder(OnmsNotification.class);

        criteriaBuilder.alias("node", "node");

        addCategoryIntersectionRestriction(criteriaBuilder, "{alias}.nodeId", rowCategories, columnCategories);

        criteriaBuilder.ne("node.type", "D");
        criteriaBuilder.orderBy("pageTime", false);

        final Criteria criteria = criteriaBuilder.toCriteria();

        for (final Restriction restriction : criterias) {
            criteria.addRestriction(restriction);
        }

        final List<SurveillanceNotification> notifications = new ArrayList<>();
        for (final OnmsNotification notification : m_notificationDao.findMatching(criteria)) {
            final OnmsNode node = notification.getNode();
            notifications.add(new SurveillanceNotification(
                    notification.getNotifyId(),
                    node != null ? node.getId() : null,
                    node != null ? node.getLabel() : null,
                    notification.getServiceType() != null ? notification.getServiceType().getName() : null,
                    notification.getTextMsg(),
                    notification.getPageTime(),
                    notification.getRespondTime(),
                    notification.getAnsweredBy(),
                    severity));
        }
        return notifications;
    }

    private static double calculateAvailability(final long serviceCount, final long downMillisCount) {
        final long upMillis = (serviceCount * (24L * 60L * 60L * 1000L)) - downMillisCount;

        return ((double) upMillis / (double) (serviceCount * (24 * 60 * 60 * 1000)));
    }

    private static Map<OnmsMonitoredService, Long> calculateServiceDownTime(final Date periodEnd, final Date periodStart, final List<OnmsOutage> outages) {
        final Map<OnmsMonitoredService, Long> map = new HashMap<>();
        for (final OnmsOutage outage : outages) {
            if (map.get(outage.getMonitoredService()) == null) {
                map.put(outage.getMonitoredService(), Long.valueOf(0));
            }

            final Date begin;
            if (outage.getIfLostService().before(periodStart)) {
                begin = periodStart;
            } else {
                begin = outage.getIfLostService();
            }

            final Date end;
            if (outage.getIfRegainedService() == null || !outage.getIfRegainedService().before(periodEnd)) {
                end = periodEnd;
            } else {
                end = outage.getIfRegainedService();
            }

            Long count = map.get(outage.getMonitoredService());
            count += (end.getTime() - begin.getTime());
            map.put(outage.getMonitoredService(), count);
        }
        return map;
    }

    private List<NodeRtc> getNodeRtcsForCriteria(final Criteria serviceCriteria, final Criteria outageCriteria) {
        final List<Order> ordersService = new ArrayList<>();
        ordersService.add(Order.asc("node.label"));
        ordersService.add(Order.asc("node.id"));
        ordersService.add(Order.asc("ipInterface.ipAddress"));
        ordersService.add(Order.asc("serviceType.name"));
        serviceCriteria.setOrders(ordersService);

        final Date periodEnd = new Date(System.currentTimeMillis());
        final Date periodStart = new Date(periodEnd.getTime() - (24 * 60 * 60 * 1000));

        outageCriteria.addRestriction(Restrictions.any(Restrictions.isNull("ifRegainedService"), Restrictions.ge("ifLostService", periodStart), Restrictions.ge("ifRegainedService", periodStart)));
        final List<Order> ordersOutage = new ArrayList<>();
        ordersOutage.add(Order.asc("monitoredService"));
        ordersOutage.add(Order.asc("ifLostService"));
        outageCriteria.setOrders(ordersOutage);

        final List<OnmsMonitoredService> services = m_monitoredServiceDao.findMatching(serviceCriteria);
        final List<OnmsOutage> outages = m_outageDao.findMatching(outageCriteria);

        final Map<OnmsMonitoredService, Long> serviceDownTime = calculateServiceDownTime(periodEnd, periodStart, outages);

        final List<NodeRtc> model = new ArrayList<>();

        OnmsNode lastNode = null;
        int serviceCount = 0;
        int serviceDownCount = 0;
        long downMillisCount = 0;
        for (final OnmsMonitoredService service : services) {
            if (!service.getIpInterface().getNode().equals(lastNode) && lastNode != null) {
                model.add(new NodeRtc(lastNode.getId(), lastNode.getLabel(), serviceCount, serviceDownCount, calculateAvailability(serviceCount, downMillisCount)));

                serviceCount = 0;
                serviceDownCount = 0;
                downMillisCount = 0;
            }

            serviceCount++;
            if (service.isDown()) {
                serviceDownCount++;
            }

            final Long downMillis = serviceDownTime.get(service);
            if (downMillis != null) {
                downMillisCount += downMillis;
            }

            lastNode = service.getIpInterface().getNode();
        }
        if (lastNode != null) {
            model.add(new NodeRtc(lastNode.getId(), lastNode.getLabel(), serviceCount, serviceDownCount, calculateAvailability(serviceCount, downMillisCount)));
        }

        return model;
    }
}
