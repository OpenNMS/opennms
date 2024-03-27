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
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.OnmsMinionCollection;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.opennms.web.svclayer.api.RequisitionAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Basic Web Service using REST for {@link OnmsMinion} entity
 *
 * @author <a href="seth@opennms.org">Seth Leger</a>
 */
@Component
@Path("minions")
@Transactional
@Tag(name = "Minion", description = "Minion API")
public class MinionRestService extends AbstractDaoRestService<OnmsMinion,OnmsMinion,String,String> {

    private static final Logger LOG = LoggerFactory.getLogger(MinionRestService.class);
    private static final String PROVISIONING_FOREIGN_SOURCE_PATTERN = System.getProperty("opennms.minion.provisioning.foreignSourcePattern", "Minions");

    @Autowired
    private MinionDao m_dao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy m_eventProxy;

    @Autowired
    private RequisitionAccessService m_requisitionAccessService;

    @Override
    protected MinionDao getDao() {
        return m_dao;
    }

    @Override
    protected Class<OnmsMinion> getDaoClass() {
        return OnmsMinion.class;
    }

    @Override
    protected Class<OnmsMinion> getQueryBeanClass() {
        return OnmsMinion.class;
    }

    @Override
    protected CriteriaBuilder getCriteriaBuilder(UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsMinion.class);

        // Order by label by default
        builder.orderBy("label").desc();

        return builder;
    }

    @Override
    protected JaxbListWrapper<OnmsMinion> createListWrapper(Collection<OnmsMinion> list) {
        return new OnmsMinionCollection(list);
    }

    @Override
    protected Set<SearchProperty> getQueryProperties() {
        return SearchProperties.MINION_SERVICE_PROPERTIES;
    }

    @Override
    protected Response doUpdate(SecurityContext securityContext, UriInfo uriInfo, String key, OnmsMinion targetObject) {
        if (!key.equals(targetObject.getId())) {
            throw getException(Status.BAD_REQUEST, "The ID of the object doesn't match the ID of the path: {} != {}", targetObject.getId(), key);
        }
        getDao().saveOrUpdate(targetObject);
        return Response.noContent().build();
    }

    @Override
    protected void doDelete(SecurityContext securityContext, UriInfo uriInfo, OnmsMinion minion) {
        final String location = minion.getLocation();
        final String id = minion.getId();
        getDao().delete(minion);

        final EventBuilder eventBuilder = new EventBuilder(EventConstants.MONITORING_SYSTEM_DELETED_UEI, "ReST");
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_ID, id);
        eventBuilder.addParam(EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, location);
        try {
            m_eventProxy.send(eventBuilder.getEvent());
        } catch (final EventProxyException e) {
            LOG.warn("Failed to send Event on Minion deletion " + e.getMessage(), e);
        }

        /*
        In the heartbeat code a minion is automatically added to a requisition for monitoring. The requisition's name
        to be used is defined by a system property and the minion's location. So, we will also delete the minion from
        it's requisition here...
         */

        final String foreignSource = String.format(PROVISIONING_FOREIGN_SOURCE_PATTERN, minion.getLocation());
        m_requisitionAccessService.deleteNode(foreignSource, minion.getId());
    }

    @Override
    protected OnmsMinion doGet(UriInfo uriInfo, String id) {
        return getDao().get(id);
    }
}
