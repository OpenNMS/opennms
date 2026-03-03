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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.PrimitiveStatement;
import org.apache.cxf.jaxrs.ext.search.SearchBean;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.features.events.store.EventCriteria;
import org.opennms.features.events.store.EventStore;
import org.opennms.features.events.store.StoredEvent;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.mapper.v2.StoredEventMapper;
import org.opennms.web.rest.model.v2.EventCollectionDTO;
import org.opennms.web.rest.model.v2.EventDTO;
import org.opennms.web.rest.v2.api.EventRestApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.WebApplicationException;

import com.google.common.base.Strings;

/**
 * V2 REST service for events backed by {@link EventStore} (events_archive table).
 */
@Component
@Transactional
public class EventRestService implements EventRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(EventRestService.class);
    private static final int DEFAULT_LIMIT = 10;

    @Autowired
    private EventStore m_eventStore;

    @Autowired
    private EventConfDao m_eventConfDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Override
    public Response get(UriInfo uriInfo, SearchContext searchContext) {
        EventCriteria criteria = buildCriteria(uriInfo, searchContext);

        List<StoredEvent> events = m_eventStore.findByCriteria(criteria);
        if (events.isEmpty()) {
            return Response.status(Status.NO_CONTENT).build();
        }

        long totalCount = m_eventStore.count(criteria);

        StoredEventMapper mapper = getMapper();
        List<EventDTO> dtos = events.stream()
                .map(mapper::toEventDTO)
                .collect(Collectors.toList());

        EventCollectionDTO collection = new EventCollectionDTO(dtos);
        collection.setTotalCount((int) totalCount);
        collection.setOffset(criteria.getOffset());

        int offset = criteria.getOffset();
        return Response.ok(collection)
                .header("Content-Range", String.format("items %d-%d/%d",
                        offset, offset + events.size() - 1, totalCount))
                .build();
    }

    @Override
    public Response get(UriInfo uriInfo, Long id) {
        Optional<StoredEvent> event = m_eventStore.getByTsid(id);
        if (event.isEmpty()) {
            return Response.status(Status.NOT_FOUND).build();
        }
        return Response.ok(getMapper().toEventDTO(event.get())).build();
    }

    @Override
    public Response getCount(UriInfo uriInfo, SearchContext searchContext) {
        EventCriteria criteria = buildCriteria(uriInfo, searchContext);
        return Response.ok(String.valueOf(m_eventStore.count(criteria))).build();
    }

    @Override
    public Response getProperties(String query) {
        // Properties endpoint not supported for the archive store
        return Response.noContent().build();
    }

    @Override
    public Response getPropertyValues(String propertyId, String query, Integer limit) {
        // Property values endpoint not supported for the archive store
        return Response.status(Status.NOT_FOUND).build();
    }

    @Override
    public Response create(Event event) {
        if (event.getTime() == null) event.setTime(new Date());
        if (event.getSource() == null) event.setSource("ReST");

        sendEvent(event);
        return Response.noContent().build();
    }

    private EventCriteria buildCriteria(UriInfo uriInfo, SearchContext searchContext) {
        EventCriteria.Builder builder = EventCriteria.builder()
                .sortOrder(EventCriteria.SortOrder.DESC);

        // Apply FIQL search expression
        if (searchContext != null && !Strings.isNullOrEmpty(searchContext.getSearchExpression())) {
            try {
                SearchCondition<SearchBean> condition = searchContext.getCondition(SearchBean.class);
                if (condition != null) {
                    applySearchCondition(builder, condition);
                }
            } catch (Exception e) {
                LOG.warn("Error parsing FIQL search: {}", e.getMessage());
                throw new IllegalArgumentException("Error parsing FIQL search");
            }
        }

        // Apply explicit query parameters
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        applyQueryParameters(builder, params);

        return builder.build();
    }

    private void applySearchCondition(EventCriteria.Builder builder, SearchCondition<SearchBean> condition) {
        PrimitiveStatement statement = condition.getStatement();
        if (statement != null) {
            applyPrimitiveStatement(builder, statement);
        } else {
            // Compound condition — walk children
            List<SearchCondition<SearchBean>> children = condition.getSearchConditions();
            if (children != null) {
                for (SearchCondition<SearchBean> child : children) {
                    applySearchCondition(builder, child);
                }
            }
        }
    }

    private void applyPrimitiveStatement(EventCriteria.Builder builder, PrimitiveStatement statement) {
        String property = statement.getProperty();
        Object value = statement.getValue();
        if (value == null) return;

        String strValue = value.toString();

        switch (property) {
            case "node.id":
            case "event.nodeId":
            case "nodeId":
                builder.nodeId(Long.parseLong(strValue));
                break;
            case "event.eventUei":
            case "eventUei":
            case "uei":
                builder.uei(strValue);
                break;
            case "event.ipAddr":
            case "ipAddr":
                builder.ipAddress(strValue);
                break;
            case "event.eventDisplay":
            case "eventDisplay":
                builder.eventDisplayFilter(strValue);
                break;
            case "event.eventSeverity":
            case "eventSeverity":
            case "severity":
                OnmsSeverity sev = OnmsSeverity.get(strValue);
                if (sev != null) {
                    builder.severityGte(sev.getId());
                    builder.severityLte(sev.getId());
                }
                break;
            case "event.serviceName":
            case "serviceName":
                builder.serviceName(strValue);
                break;
            default:
                LOG.debug("Ignoring unsupported FIQL property: {}", property);
                break;
        }
    }

    private void applyQueryParameters(EventCriteria.Builder builder, MultivaluedMap<String, String> params) {
        String limitStr = params.getFirst("limit");
        if (limitStr != null) {
            builder.limit(Integer.parseInt(limitStr));
        } else {
            builder.limit(DEFAULT_LIMIT);
        }

        String offsetStr = params.getFirst("offset");
        if (offsetStr != null) {
            builder.offset(Integer.parseInt(offsetStr));
        }

        String order = params.getFirst("order");
        if ("asc".equalsIgnoreCase(order)) {
            builder.sortOrder(EventCriteria.SortOrder.ASC);
        }
    }

    private void sendEvent(Event event) {
        try {
            m_eventProxy.send(event);
        } catch (EventProxyException e) {
            throw getException(Status.INTERNAL_SERVER_ERROR, "Cannot send event {} : {}",
                    event.getUei(), e.getMessage());
        }
    }

    private StoredEventMapper getMapper() {
        return new StoredEventMapper(m_eventConfDao);
    }

    private static WebApplicationException getException(Status status, String msg, String... params) {
        if (params != null) msg = MessageFormatter.arrayFormat(msg, params).getMessage();
        LOG.error(msg);
        return new WebApplicationException(Response.status(status).entity(msg).build());
    }
}
