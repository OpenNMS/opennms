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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.mapper.v2.EventMapper;
import org.opennms.web.rest.model.v2.EventCollectionDTO;
import org.opennms.web.rest.model.v2.EventDTO;
import org.opennms.web.rest.support.Aliases;
import org.opennms.web.rest.support.CriteriaBehavior;
import org.opennms.web.rest.support.CriteriaBehaviors;
import org.opennms.web.rest.support.IpLikeCriteriaBehavior;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v2.api.EventRestApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsEvent} entity.
 *
 * @author <a href="agalue@opennms.org">Alejandro Galue</a>
 */
@Component
@Transactional
public class EventRestService extends AbstractDaoRestServiceWithDTO<OnmsEvent,EventDTO,SearchBean,Long,Long> implements EventRestApi {

    @Autowired
    private EventDao m_dao;

    @Autowired
    private EventMapper m_eventMapper;

    @Override
    protected EventDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsEvent> getDaoClass() {
        return OnmsEvent.class;
    }

    @Override
    protected Class<SearchBean> getQueryBeanClass() {
        return SearchBean.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(getDaoClass(), Aliases.event.toString());

        // 1st level JOINs
        builder.alias("alarm", Aliases.alarm.toString(), JoinType.LEFT_JOIN);
        builder.alias("distPoller", Aliases.distPoller.toString(), JoinType.LEFT_JOIN);
        builder.alias("node", Aliases.node.toString(), JoinType.LEFT_JOIN);
        // TODO: Only add this alias when filtering by category so that we can specify a join condition
        builder.alias("serviceType", Aliases.serviceType.toString(), JoinType.LEFT_JOIN);

        // 2nd level JOINs
        builder.alias(Aliases.node.prop("assetRecord"), Aliases.assetRecord.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("ipInterfaces"), Aliases.ipInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.ipInterface.prop("ipAddress"), Aliases.event.prop("ipAddr")), Restrictions.isNull(Aliases.ipInterface.prop("ipAddress"))));
        builder.alias(Aliases.node.prop("location"), Aliases.location.toString(), JoinType.LEFT_JOIN);
        // Left joins on a toMany relationship need a join condition so that only one row is returned
        builder.alias(Aliases.node.prop("snmpInterfaces"), Aliases.snmpInterface.toString(), JoinType.LEFT_JOIN, Restrictions.or(Restrictions.eqProperty(Aliases.snmpInterface.prop("ifIndex"), Aliases.event.prop("ifIndex")), Restrictions.isNull(Aliases.snmpInterface.prop("ifIndex"))));

        builder.orderBy("eventTime").desc(); // order by event time by default

        return builder;
    }

    @Override
    protected JaxbListWrapper<EventDTO> createListWrapper(Collection<EventDTO> list) {
        return new EventCollectionDTO(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.EVENT_SERVICE_PROPERTIES;
    }

    @Override
    protected Map<String, String> getSearchBeanPropertyMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("event.uei", "event.eventUei");
        return map;
    }

    @Override
    protected Map<String,CriteriaBehavior<?>> getCriteriaBehaviors() {
        final Map<String,CriteriaBehavior<?>> map = new HashMap<>();

        // Root alias
        map.putAll(CriteriaBehaviors.EVENT_BEHAVIORS);
        // Allow iplike queries on ipAddr
        map.put("ipAddr", new IpLikeCriteriaBehavior("ipAddr"));

        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.event, CriteriaBehaviors.EVENT_BEHAVIORS));
        // Allow iplike queries on event.ipAddr
        map.put(Aliases.event.prop("ipAddr"), new IpLikeCriteriaBehavior("ipAddr"));

        // 1st level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.alarm, CriteriaBehaviors.ALARM_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.distPoller, CriteriaBehaviors.DIST_POLLER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.eventParameter, CriteriaBehaviors.EVENT_PARAMETER_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.node, CriteriaBehaviors.NODE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.serviceType, CriteriaBehaviors.SERVICE_TYPE_BEHAVIORS));

        // 2nd level JOINs
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.assetRecord, CriteriaBehaviors.ASSET_RECORD_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.ipInterface, CriteriaBehaviors.IP_INTERFACE_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.location, CriteriaBehaviors.MONITORING_LOCATION_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.category, CriteriaBehaviors.NODE_CATEGORY_BEHAVIORS));
        map.putAll(CriteriaBehaviors.withAliasPrefix(Aliases.snmpInterface, CriteriaBehaviors.SNMP_INTERFACE_BEHAVIORS));

        return map;
    }

    @Override
    protected OnmsEvent doGet(UriInfo uriInfo, Long id) {
        return getDao().get(id);
    }

    @Override
    public Response get(UriInfo uriInfo, SearchContext searchContext) {
        return super.get(uriInfo, searchContext);
    }

    @Override
    public Response get(UriInfo uriInfo, Long id) {
        return super.get(uriInfo, id);
    }

    @Override
    public Response getCount(UriInfo uriInfo, SearchContext searchContext) {
        return super.getCount(uriInfo, searchContext);
    }

    @Override
    public Response getProperties(String query) {
        return super.getProperties(query);
    }

    @Override
    public Response getPropertyValues(String propertyId, String query, Integer limit) {
        return super.getPropertyValues(propertyId, query, limit);
    }

    /**
     * NOTE: This method defines an unused parameter of 0 length in the @Path annotation
     * in order to get CXF to prioritize this method definition instead of the create method
     * defined in the parent class.
     *
     * We cannot simply override the parent method, since the class types are different:
     * we want to receive a {@link org.opennms.netmgt.xml.event.Event} whereas the parent class
     * receives a {@link  org.opennms.netmgt.model.OnmsEvent}.
     *
     * @param event the event to forward
     * @return a response containing "no content" (204) when the event was succesfully forwarded
     */
    @Override
    public Response create(Event event) {
        if (event.getTime() == null) event.setTime(new Date());
        if (event.getSource() == null) event.setSource("ReST");

        sendEvent(event);
        return Response.noContent().build();
    }

    @Override
    public EventDTO mapEntityToDTO(OnmsEvent entity) {
        return m_eventMapper.eventToEventDTO(entity);
    }

    @Override
    public OnmsEvent mapDTOToEntity(EventDTO dto) {
        return m_eventMapper.eventDTOToEvent(dto);
    }

}
