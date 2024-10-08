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
package org.opennms.web.rest.v2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsOutageCollection;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsOutage} entity.
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("outages")
@Transactional
@Tag(name = "Outages", description = "Outages API")
public class OutageRestService extends AbstractDaoRestService<OnmsOutage,SearchBean,Integer,Integer> {

    @Autowired
    private OutageDao m_dao;

    @Override
    protected OutageDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsOutage> getDaoClass() {
        return OnmsOutage.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class, Aliases.outage.toString());
        // 1st level JOINs
        builder.alias("monitoredService", "monitoredService", JoinType.LEFT_JOIN);
        builder.alias("serviceLostEvent", "serviceLostEvent", JoinType.LEFT_JOIN);
        builder.alias("serviceRegainedEvent", "serviceRegainedEvent", JoinType.LEFT_JOIN);
        builder.alias("perspective", "perspective", JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias("monitoredService.ipInterface", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("monitoredService.serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceLostEvent.distPoller", Aliases.distPoller.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        builder.alias(Aliases.ipInterface.prop("node"), Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.ipInterface.prop("snmpInterface"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 4th level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        //builder.alias(Aliases.node.prop("categories"), Aliases.category.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);

        // NOTE: Left joins on a toMany relationship need a join condition so that only one row is returned

        // Order by ID by default
        builder.orderBy("id").desc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsOutage> createListWrapper(Collection<OnmsOutage> list) {
        return new OnmsOutageCollection(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.OUTAGE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.OUTAGE_BEHAVIORS);
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.outage, CriteriaBehaviors.OUTAGE_BEHAVIORS));

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.monitoredService, CriteriaBehaviors.MONITORED_SERVICE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix("serviceLostEvent", CriteriaBehaviors.EVENT_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix("serviceRegainedEvent", CriteriaBehaviors.EVENT_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix("perspective", CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.distPoller, CriteriaBehaviors.DIST_POLLER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 3rd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        // 4th level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        //map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));

        return map;
    }

    @Override
    protected OnmsOutage doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

}
