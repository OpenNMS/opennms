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
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.web.rest.support.RedirectHelper;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.rest.v1.support.OnmsMonitoringLocationDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsMonitoringLocation} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("monitoringLocations")
@Transactional
@Tag(name = "MonitoringLocations", description = "Monitoring Locations API")
public class MonitoringLocationRestService extends AbstractDaoRestService<OnmsMonitoringLocation,OnmsMonitoringLocation,String,String> {
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringLocationRestService.class);

    @Autowired
    private MonitoringLocationDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Override
    protected MonitoringLocationDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMonitoringLocation> getDaoClass() {
        return OnmsMonitoringLocation.class;
    }

    @Override
    protected Class<OnmsMonitoringLocation> getQueryBeanClass() {
        return OnmsMonitoringLocation.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMonitoringLocation.class);

        // Order by location name by default
        builder.orderBy("locationName").asc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMonitoringLocation> createListWrapper(Collection<OnmsMonitoringLocation> list) {
        return new OnmsMonitoringLocationDefinitionList(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.LOCATION_SERVICE_PROPERTIES;
    }

    @Override
    protected OnmsMonitoringLocation doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }

    @Override
    public Response doCreate(final SecurityContext securityContext, final UriInfo uriInfo, final OnmsMonitoringLocation location) {

        final String id = getDao().save(location);

        return Response.created(RedirectHelper.getRedirectUri(uriInfo, id)).build();
    }

    @Override
    protected Response doUpdate(final SecurityContext securityContext, final UriInfo uriInfo, final String key, final OnmsMonitoringLocation targetObject) {

        if (!key.equals(targetObject.getLocationName())) {
            throw getException(Status.BAD_REQUEST, "The ID of the object doesn't match the ID of the path: {} != {}", targetObject.getLocationName(), key);
        }

        m_dao.clear();

        getDao().saveOrUpdate(targetObject);

        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMonitoringLocation location) {
        getDao().delete(location);
    }
}
