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
package org.opennms.features.deviceconfig.persistence.impl;

import com.google.common.base.Strings;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import java.util.stream.Collectors;
import org.hibernate.SQLQuery;
import org.hibernate.transform.ResultTransformer;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.StringUtils;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfig;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigDao;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigQueryResult;
import org.opennms.features.deviceconfig.persistence.api.DeviceConfigStatus;
import org.opennms.netmgt.dao.hibernate.AbstractDaoHibernate;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

public class DeviceConfigDaoImpl extends AbstractDaoHibernate<DeviceConfig, Long> implements DeviceConfigDao {
    private static final Map<String,String> ORDERBY_QUERY_PROPERTY_MAP = Map.of(
        "lastupdated", "last_updated",
        "devicename", "nodelabel",
        "lastbackup", "created_time",
        "ipaddress", "ipaddr",
        "location", "location",
        "status", "status"
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

    /** {@inheritDoc} */
    @Override
    public List<DeviceConfig> findStaleConfigs(OnmsIpInterface ipInterface, String serviceName,
            Date staleDate, Optional<Long> excludedId) {
        var builder = new CriteriaBuilder(DeviceConfig.class);
        builder
            .isNotNull("lastUpdated")
            .isNotNull("config")
            .eq("ipInterface.id", ipInterface.getId())
            .eq("serviceName", serviceName)
            .lt("lastUpdated", staleDate);

        if (excludedId.isPresent()) {
            builder.not().eq("id", excludedId.get());
        }

        var criteria = builder.toCriteria();

        return findMatching(criteria);
    }

    @Override
    public Optional<DeviceConfig> getLatestConfigForInterface(OnmsIpInterface ipInterface, String serviceName) {
        List<DeviceConfig> deviceConfigs = new ArrayList<>();
        if (!Strings.isNullOrEmpty(serviceName)) {
            deviceConfigs =
                    findObjects(DeviceConfig.class,
                            "from DeviceConfig dc WHERE dc.ipInterface.id = ? AND serviceName = ? " +
                                    "ORDER BY lastUpdated DESC LIMIT 1", ipInterface.getId(), serviceName);
        } else {
            deviceConfigs = findObjects(DeviceConfig.class,
                    "from DeviceConfig dc WHERE dc.ipInterface.id = ? AND serviceName is NULL " +
                            "ORDER BY lastUpdated DESC LIMIT 1", ipInterface.getId());
        }

        if (!deviceConfigs.isEmpty()) {
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

    @Override
    public List<DeviceConfig> getAllDeviceConfigsWithAnInterfaceId(Integer ipInterfaceId) {
        return find("from DeviceConfig dc where dc.ipInterface.id = ? ",
                ipInterfaceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Long> getNumberOfNodesWithDeviceConfigBySysOid() {
        String query = "SELECT n.nodesysoid, count(*) FROM device_config dcb LEFT JOIN ipinterface ip ON ipinterface_id = ip.id LEFT JOIN node n ON ip.nodeid = n.nodeid GROUP BY nodesysoid";

        return getHibernateTemplate().executeWithNativeSession(session -> {
            SQLQuery queryObject = session.createSQLQuery(query);
            Map<String, Long> numberOfNodesWithDeviceConfigBySysOid = new HashMap();
            for (Object obj : queryObject.list()) {
                Object[] pair = (Object[]) obj;
                String sysOid = (String)pair[0];
                Long count = ((BigInteger)pair[1]).longValue();
                numberOfNodesWithDeviceConfigBySysOid.put(StringUtils.isEmpty(sysOid) ? "none" : sysOid, count);
            }
            return Collections.unmodifiableMap(numberOfNodesWithDeviceConfigBySysOid);
        });
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
    public Optional<Long> updateDeviceConfigContent(
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
        Optional<Long> updatedDeviceId = configOptional.map(DeviceConfig::getId);

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
            updatedDeviceId = Optional.of(deviceConfig.getId());

            LOG.info("Persisted changed device config - ipInterface: {}; service: {}; type: {}", ipInterface, serviceName, configType);
        }

        return updatedDeviceId;
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

    public void deleteDeviceConfigs(Collection<DeviceConfig> entities) throws DataAccessException{
        super.deleteAll(entities);
    }

}
