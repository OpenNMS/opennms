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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.web.api.RestUtils;
import org.opennms.web.rest.model.v2.MonitoredServiceCollectionDTO;
import org.opennms.web.rest.model.v2.MonitoredServiceDTO;
import org.opennms.web.rest.model.v2.ServiceTypeDTO;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

/**
 * Basic Web Service using REST for {@link OnmsMonitoredService} entity.
 * 
 * <p>This end-point exist to retrieve and update a set of monitored services at once,
 * based on a given criteria.</p>
 * <p>This facilitates moving services to maintenance mode (and restore the services to be online).</p>
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Path("ifservices")
@Transactional
public class IfServiceRestService extends AbstractDaoRestServiceWithDTO<OnmsMonitoredService,MonitoredServiceDTO,SearchBean,Integer,String> {
    @Autowired
    private MonitoredServiceDao m_dao;

    @Autowired
    private MonitoredServicesComponent m_component;

    @Override
    protected MonitoredServiceDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoredService> getDaoClass() {
        return OnmsMonitoredService.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass());
        // 1st level JOINs
        builder.alias("ipInterface", Aliases.ipInterface.toString(), JoinType.LEFT_JOIN);
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.ipInterface.prop("node"), Aliases.node.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.ipInterface.prop("snmpInterface"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN);

        // 3rd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);

        // TODO: Only add this alias when filtering so that we can specify a join condition
        //builder.alias("node.categories", Aliases.category.toString(), JoinType.LEFT_JOIN);

        builder.orderBy("id");

        return builder;
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.IF_SERVICE_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.MONITORED_SERVICE_BEHAVIORS);

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        // 3rd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        //map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));

        return map;
    }

    @Override
    protected JaxbListWrapper<MonitoredServiceDTO> createListWrapper(Collection<MonitoredServiceDTO> list) {
        return new MonitoredServiceCollectionDTO(list);
    }

    @Override
    public MonitoredServiceDTO mapEntityToDTO(OnmsMonitoredService entity) {
        final var dto = new MonitoredServiceDTO();
        dto.setId(entity.getId());
        dto.setDown(entity.isDown());
        dto.setNotify(entity.getNotify());
        dto.setStatus(entity.getStatus());
        dto.setSource(entity.getSource());

        final var serviceType = new ServiceTypeDTO();
        serviceType.setId(entity.getServiceId());
        serviceType.setName(entity.getServiceName());
        dto.setServiceType(serviceType);

        dto.setQualifier(entity.getQualifier());
        dto.setLastFail(entity.getLastFail());
        dto.setLastGood(entity.getLastGood());
        dto.setStatusLong(entity.getStatusLong());
        dto.setIpInterfaceId(entity.getIpInterfaceId());
        dto.setIpAddress(entity.getIpAddress().getHostAddress());
        dto.setNodeId(entity.getNodeId());
        dto.setNodeLabel(entity.getNodeLabel());

        return dto;
    }

    @Override
    public OnmsMonitoredService mapDTOToEntity(MonitoredServiceDTO dto) {
        // currently unused, providing a basic but incomplete mapping of some top-level items
        final var service = new OnmsMonitoredService();
        service.setId(dto.getId());
        service.setNotify(dto.getNotify());
        service.setStatus(dto.getStatus());
        service.setSource(dto.getSource());

        final var serviceType = new OnmsServiceType();
        serviceType.setId(dto.getServiceType().getId());
        serviceType.setName(dto.getServiceType().getName());
        service.setServiceType(serviceType);

        service.setQualifier(dto.getQualifier());
        service.setLastFail(dto.getLastFail());
        service.setLastGood(dto.getLastGood());

        return service;
    }

    @Override
    protected Response doUpdateProperties(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoredService targetObject, MultivaluedMapImpl params) {
        final String previousStatus = targetObject.getStatus();
        final Set<OnmsApplication> applicationsOriginal = new HashSet<>(); // unfortunately applications set is not immutable, let's make a copy.

        if (targetObject.getApplications() != null) {
            applicationsOriginal.addAll(targetObject.getApplications());
        }

        RestUtils.setBeanProperties(targetObject, params);
        getDao().update(targetObject);

        Set<OnmsApplication> changedApplications = Sets.symmetricDifference(applicationsOriginal, targetObject.getApplications());
        ApplicationEventUtil.getApplicationChangedEvents(changedApplications).forEach(this::sendEvent);

        boolean changed = m_component.hasStatusChanged(previousStatus, targetObject);
        return changed ? Response.noContent().build() : Response.notModified().build();
    }

    @Override
    protected OnmsMonitoredService doGet(UriInfo uriInfo, String serviceName) {
        throw new WebApplicationException(Response.status(Status.NOT_IMPLEMENTED).build());
    }
}
