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

import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_ID;
import static org.opennms.netmgt.events.api.EventConstants.PARM_APPLICATION_NAME;

import java.util.Collection;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v1.support.OnmsApplicationList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsApplication} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("applications")
@Transactional
@Tag(name = "Applications", description = "Applications API")
public class ApplicationRestService extends AbstractDaoRestService<OnmsApplication,OnmsApplication,Integer,Integer> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationRestService.class);

    private final ApplicationDao m_dao;

    private final EventProxy m_eventProxy;

    @Autowired
    public ApplicationRestService(final ApplicationDao dao, final EventProxy eventProxy) {
        this.m_dao = dao;
        this.m_eventProxy = eventProxy;
    }

    @Override
    protected ApplicationDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsApplication> getDaoClass() {
        return OnmsApplication.class;
    }

    @Override
    protected Class<OnmsApplication> getQueryBeanClass() {
        return OnmsApplication.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsApplication.class);

        // Order by application name by default
        builder.orderBy("name").asc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsApplication> createListWrapper(Collection<OnmsApplication> list) {
        return new OnmsApplicationList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.APPLICATION_SERVICE_PROPERTIES;
    }

    @Override
    protected OnmsApplication doGet(UriInfo uriInfo, Integer id) {
        return getDao().get(id);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsApplication object) {
        final Integer id = getDao().save(object);
        sendEvent(object, EventConstants.APPLICATION_CREATED_EVENT_UEI);
        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsApplication object) {
        getDao().delete(object);
        sendEvent(object, EventConstants.APPLICATION_DELETED_EVENT_UEI);
    }

    private void sendEvent(final OnmsApplication application, final String uei) {
        final Event event = new EventBuilder(uei, "Web UI")
                .addParam(PARM_APPLICATION_ID, application.getId())
                .addParam(PARM_APPLICATION_NAME, application.getName())
                .getEvent();
        try {
            m_eventProxy.send(event);
        } catch (final EventProxyException e) {
            LOG.warn("Failed to send event {}: {}", event.getUei(), e.getMessage(), e);
        }
    }
}
