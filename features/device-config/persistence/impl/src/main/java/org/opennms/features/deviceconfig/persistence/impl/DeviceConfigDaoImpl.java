/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.persistence.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import org.hibernate.SQLQuery;
import org.hibernate.transform.ResultTransformer;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigQueryResult;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceConfigDaoImpl extends AbstractDaoHibernate<DeviceConfig, Long> implements DeviceConfigDao {
    private static final Map<String,String> ORDERBY_QUERY_PROPERTY_MAP = Map.of(
        "lastupdated", "last_updated",
        "devicename", "nodelabel",
        "lastbackup", "created_time",
        "ipaddress", "ipaddr"
    );

    private static final Logger LOG = LoggerFactory.getLogger(DeviceConfigDaoImpl.class);

    private static final int DEFAULT_LIMIT = 20;

    private static class DeviceConfigQueryCriteria {
        public String filter;
        public boolean hasFilter;
        public String searchTerm;
        public String orderBy;
        public boolean hasOrderBy;
        public String limitOffset;
    }

    public DeviceConfigDaoImpl() {
        super(DeviceConfig.class);
    }

    @Override
    public List<DeviceConfig> findConfigsForInterfaceSortedByDate(OnmsIpInterface ipInterface, String serviceName) {

        return find("from DeviceConfig dc where dc.lastUpdated is not null AND dc.ipInterface.id = ? AND serviceName = ? ORDER BY lastUpdated DESC",
                ipInterface.getId(), serviceName);
    }

    @Override
    public Optional<DeviceConfig> getLatestConfigForInterface(OnmsIpInterface ipInterface, String serviceName) {
        List<DeviceConfig> deviceConfigs =
                findObjects(DeviceConfig.class,
                        "from DeviceConfig dc WHERE dc.ipInterface.id = ? AND serviceName = ? " +
                                "ORDER BY lastUpdated DESC LIMIT 1", ipInterface.getId(), serviceName);

        if (deviceConfigs != null && !deviceConfigs.isEmpty()) {
            return Optional.of(deviceConfigs.get(0));
        }
        return Optional.empty();
    }

    /** {@inheritDoc} */
    @Override
    public List<DeviceConfigQueryResult> getLatestConfigForEachInterface(Integer limit, Integer offset, String orderBy,
        String sortOrder, String searchTerm, Set<DeviceConfigStatus> statuses) {

        var criteria = createSqlQueryCriteria(limit, offset, orderBy, sortOrder, searchTerm, statuses);

        // NOTE: '{dc.*}' and '{ip.*}' needed for Hibernate to map to entities
        // Explicit columns needed to do sort/filter/search outside the inner query since Hibernate
        // makes aliases for all the columns
        final String queryString =
            "SELECT * FROM (\n" +
            "    SELECT\n" +
            "        DISTINCT ON(dc.ipinterface_id, dc.config_type)\n" +
            "        {dc.*},\n" +
            "        {ip.*},\n" +
            "        dc.last_updated,\n" +
            "        dc.created_time,\n" +
            "        dc.status,\n" +
            "        ip.ipaddr,\n" +
            "        n.nodeid,\n" +
            "        n.nodelabel,\n" +
            "        n.operatingsystem,\n" +
            "        n.location\n" +
            "    FROM device_config dc\n" +
            "    JOIN ipinterface ip\n" +
            "        ON dc.ipinterface_id = ip.id\n" +
            "    JOIN node n\n" +
            "        ON ip.nodeid = n.nodeid\n" +
            "    ORDER BY dc.ipinterface_id, dc.config_type, dc.last_updated DESC\n" +
            ") q\n" +
            (criteria.hasFilter ? (criteria.filter + "\n") : "") +
            (criteria.hasOrderBy ? criteria.orderBy + "\n" : "") +
            criteria.limitOffset;

        LOG.debug("DeviceConfigDaoImpl.getLatestConfigs, query string:");
        LOG.debug(queryString);

        final List<DeviceConfigQueryResult> resultList = getHibernateTemplate().executeWithNativeSession(session -> {
            SQLQuery queryObject = session.createSQLQuery(queryString)
                .addEntity("dc", DeviceConfig.class)
                .addJoin("ip", "dc.ipInterface");

            if (criteria.hasFilter && !Strings.isNullOrEmpty(criteria.searchTerm)) {
                queryObject.setParameter("searchTerm", criteria.searchTerm);
            }

            List<?> queryList = queryObject.setResultTransformer(new ResultTransformer() {
                 @Override
                 public Object transformTuple(Object[] objects, String[] strings) {
                     if (objects != null && objects.length >= 2) {
                         // 'objects' is a tuple of DeviceConfig, OnmsIpInterface
                         final DeviceConfig dc = (DeviceConfig) objects[0];
                         final OnmsIpInterface ip = (OnmsIpInterface) objects[1];
                         final OnmsNode n = ip.getNode();

                         return new DeviceConfigQueryResult(dc, ip, n);
                     }

                     return null;
                 }

                 @Override
                 public List transformList(List list) {
                     return list;
                 }
            }
            ).list();

            return (List<DeviceConfigQueryResult>) queryList;
        });

        return resultList;
    }

    /**
     * Get the total device count for {@link DeviceConfigDaoImpl#getLatestConfigForEachInterface}
     * if no limit/offset were applied. Query is simplified as we do not need to do any sorting, grouping
     * or windowing functions.
     * @param searchTerm Optional search term
     * @param statuses See explanation in {@link DeviceConfigDao#getLatestConfigForEachInterface}
     */
    public int getLatestConfigCountForEachInterface(String searchTerm, Set<DeviceConfigStatus> statuses) {
        final boolean hasSearchTerm = !Strings.isNullOrEmpty(searchTerm);

        String hql =
            "SELECT COUNT (DISTINCT dc.ipInterface.id)\n" +
            "FROM DeviceConfig dc\n" +
            "INNER JOIN dc.ipInterface AS ip";

        if (hasSearchTerm) {
            hql +=
                "\n" +
                "INNER JOIN ip.node AS node\n" +
                "WHERE (node.label LIKE ? OR ip.ipAddress LIKE ?)";
        }

        if (statuses != null && !statuses.isEmpty()) {
            final String statusQuery = getStatusSubquery(statuses);

            hql +=
                "\n" +
                (hasSearchTerm ? "AND " : "WHERE ") +
                statusQuery;
        }

        final int count = hasSearchTerm
            ? this.queryInt(hql, "%" + searchTerm + "%", "%" + searchTerm + "%")
            : this.queryInt(hql);

        return count;
    }

    private DeviceConfigQueryCriteria createSqlQueryCriteria(Integer limit, Integer offset,
        String orderBy, String order, String searchTerm, Set<DeviceConfigStatus> statuses) {
        var criteria = new DeviceConfigQueryCriteria();

        // Search term filter
        if (!Strings.isNullOrEmpty(searchTerm)) {
            criteria.hasFilter = true;
            criteria.searchTerm = "%" + searchTerm + "%";
            criteria.filter = "WHERE (nodelabel LIKE :searchTerm OR ipaddr LIKE :searchTerm)";
        }

        // Status filter
        if (statuses != null && !statuses.isEmpty()) {
            criteria.hasFilter = true;
            final String statusQuery = getStatusSubquery(statuses);

            if (Strings.isNullOrEmpty(criteria.filter)) {
                criteria.filter = "WHERE " + statusQuery;
            } else {
                criteria.filter += "\nAND " + statusQuery;
            }
        }

        if (!Strings.isNullOrEmpty(orderBy) &&
            ORDERBY_QUERY_PROPERTY_MAP.containsKey(orderBy.toLowerCase(Locale.ROOT))) {
            String orderByValue = ORDERBY_QUERY_PROPERTY_MAP.get(orderBy.toLowerCase(Locale.ROOT));
            boolean isOrderDescending = !Strings.isNullOrEmpty(order) && "desc".equals(order);

            String orderByClause = String.format("ORDER BY %s%s", orderByValue, isOrderDescending ? " DESC" : "");

            criteria.hasOrderBy = true;
            criteria.orderBy = orderByClause;
        }

        int limitToUse = limit != null && limit > 0 ? limit : DEFAULT_LIMIT;
        int offsetToUse = offset != null && offset > 0 ? offset : 0;

        criteria.limitOffset = String.format("LIMIT %d OFFSET %d", limitToUse, offsetToUse);

        return criteria;
    }

    private String getStatusSubquery(Set<DeviceConfigStatus> statuses) {
        final String statusNames = statuses.stream().map(s -> "'" + s.name() + "'").collect(Collectors.joining(","));
        return "status IN (" + statusNames + ")";
    }

    @Override
    public void updateDeviceConfigContent(
            OnmsIpInterface ipInterface,
            String serviceName,
            String configType,
            String encoding,
            byte[] deviceConfigBytes,
            String fileName
    ) {
        Date currentTime = new Date();
        Optional<DeviceConfig> configOptional = getLatestConfigForInterface(ipInterface, serviceName);
        DeviceConfig lastDeviceConfig = configOptional.orElse(null);
        // Config retrieval succeeded
        if (lastDeviceConfig != null &&
            // Config didn't change, just update last updated field.
            Arrays.equals(lastDeviceConfig.getConfig(), deviceConfigBytes)) {
            lastDeviceConfig.setLastUpdated(currentTime);
            lastDeviceConfig.setLastSucceeded(currentTime);
            lastDeviceConfig.setFileName(fileName);
            lastDeviceConfig.setStatus(DeviceConfigStatus.SUCCESS);
            saveOrUpdate(lastDeviceConfig);
            LOG.debug("Device config did not change - ipInterface: {}; service: {}; type: {}", ipInterface, serviceName, configType);
        } else if (lastDeviceConfig != null
                   // last config was failure, update config now.
                   && lastDeviceConfig.getConfig() == null) {
            lastDeviceConfig.setConfig(deviceConfigBytes);
            lastDeviceConfig.setFileName(fileName);
            lastDeviceConfig.setCreatedTime(currentTime);
            lastDeviceConfig.setLastUpdated(currentTime);
            lastDeviceConfig.setLastSucceeded(currentTime);
            lastDeviceConfig.setFailureReason(null);
            lastDeviceConfig.setStatus(DeviceConfigStatus.SUCCESS);
            saveOrUpdate(lastDeviceConfig);
            LOG.info("Persisted device config - ipInterface: {}; service: {}; type: {}", ipInterface, serviceName, configType);
        } else {
            // Config changed, or there is no config for the device yet, create new entry.
            DeviceConfig deviceConfig = new DeviceConfig();
            deviceConfig.setConfig(deviceConfigBytes);
            deviceConfig.setFileName(fileName);
            deviceConfig.setCreatedTime(currentTime);
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setServiceName(serviceName);
            deviceConfig.setEncoding(encoding);
            deviceConfig.setConfigType(configType);
            deviceConfig.setLastUpdated(currentTime);
            deviceConfig.setLastSucceeded(currentTime);
            deviceConfig.setStatus(DeviceConfigStatus.SUCCESS);
            saveOrUpdate(deviceConfig);
            LOG.info("Persisted changed device config - ipInterface: {}; service: {}; type: {}", ipInterface, serviceName, configType);
        }
    }

    @Override
    public void updateDeviceConfigFailure(
            OnmsIpInterface ipInterface,
            String serviceName,
            String configType,
            String encoding,
            String reason
    ) {
        Date currentTime = new Date();
        Optional<DeviceConfig> configOptional = getLatestConfigForInterface(ipInterface, serviceName);
        DeviceConfig lastDeviceConfig = configOptional.orElse(null);
        DeviceConfig deviceConfig;
        // If there is config already, update the same entry.
        if (lastDeviceConfig != null) {
            deviceConfig = lastDeviceConfig;
        } else {
            deviceConfig = new DeviceConfig();
            deviceConfig.setIpInterface(ipInterface);
            deviceConfig.setServiceName(serviceName);
            deviceConfig.setConfigType(configType);
            deviceConfig.setEncoding(encoding);
        }
        deviceConfig.setFailureReason(reason);
        deviceConfig.setLastFailed(currentTime);
        deviceConfig.setLastUpdated(currentTime);
        deviceConfig.setStatus(DeviceConfigStatus.FAILED);
        saveOrUpdate(deviceConfig);
        LOG.warn("Persisted device config backup failure - ipInterface: {}; service: {}; type: {}; reason: {}", ipInterface, serviceName, configType, reason);
    }

    @Override
    public void createEmptyDeviceConfig(OnmsIpInterface ipInterface, String serviceName, String configType) {
        DeviceConfig deviceConfig = new DeviceConfig();
        deviceConfig.setIpInterface(ipInterface);
        deviceConfig.setServiceName(serviceName);
        deviceConfig.setConfigType(configType);
        deviceConfig.setStatus(DeviceConfigStatus.NONE);
        saveOrUpdate(deviceConfig);
    }
}
