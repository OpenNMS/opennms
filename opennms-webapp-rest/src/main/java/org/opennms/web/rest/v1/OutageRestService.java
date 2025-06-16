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
package org.opennms.web.rest.v1;

import java.util.Date;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsOutageCollection;
import org.opennms.netmgt.model.outage.OutageSummary;
import org.opennms.netmgt.model.outage.OutageSummaryCollection;
import org.opennms.web.rest.support.MultivaluedMapImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * TODO: Add functionality to getting outages by:
 * nodelabel, nodeid, foreignsource, foreignsource+foreignid, ipaddress, etc.
 * add filters for current, resolved, all
 *
 *<p>REST service to the OpenNMS Outage {@link OnmsOutage} data.</p>
 *<p>This service supports getting the list of outages or one specific outage by ID:</p>
 *<p>Example 1: Query List of outages.</p>
 *<pre>
 *curl -v -X GET -u admin:admin http://localhost:8980/opennms/rest/outages
 *</pre>
 *
 * @author <a href="mailto:cmiskell@opennms.org">Craig Miskell</a>
 * @version $Id: $
 * @since 1.8.1
 */
@Component("outageRestService")
@Path("outages")
@Tag(name = "Outages", description = "Outages API")
public class OutageRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(OutageRestService.class);

    @Autowired
    private OutageDao m_outageDao;

    /**
     * <p>getOutage</p>
     *
     * @param outageId a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsOutage} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Path("{outageId}")
    @Transactional
    public Response getOutage(@Context final UriInfo uriInfo, @PathParam("outageId") final String outageId) {
        if ("summaries".equals(outageId)) {
            final MultivaluedMap<String,String> parms = uriInfo.getQueryParameters(true);
            int limit = 10;
            if (parms.containsKey("limit")) {
                limit = Integer.parseInt(parms.getFirst("limit"));
            }
            final List<OutageSummary> collection = m_outageDao.getNodeOutageSummaries(limit);
            return collection == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(new OutageSummaryCollection(collection)).build();
        } else {
            final OnmsOutage outage = m_outageDao.get(Integer.valueOf(outageId));
            return outage == null ? Response.status(Status.NOT_FOUND).build() : Response.ok(outage).build();
        }
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("count")
    @Transactional
    public String getCount() {
        return Integer.toString(m_outageDao.countAll());
    }

    /**
     * <p>getOutages</p>
     *
     * @return a {@link org.opennms.netmgt.model.OnmsOutageCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    public OnmsOutageCollection getOutages(@Context final UriInfo uriInfo) {
        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class);
        builder.alias("monitoredService", "monitoredService", JoinType.LEFT_JOIN);
        builder.alias("monitoredService.ipInterface", "ipInterface", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.node", "node", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.node.location", "location", JoinType.LEFT_JOIN);
        builder.alias("ipInterface.snmpInterface", "snmpInterface", JoinType.LEFT_JOIN);
        builder.alias("monitoredService.serviceType", "serviceType", JoinType.LEFT_JOIN);
        builder.alias("serviceLostEvent", "serviceLostEvent", JoinType.LEFT_JOIN);
        builder.alias("serviceRegainedEvent", "serviceRegainedEvent", JoinType.LEFT_JOIN);
        builder.isNull("perspective");

        applyQueryFilters(uriInfo.getQueryParameters(), builder);

        final OnmsOutageCollection coll = new OnmsOutageCollection(m_outageDao.findMatching(builder.toCriteria()));

        //For getting totalCount
        coll.setTotalCount(m_outageDao.countMatching(builder.count().toCriteria()));

        return coll;
    }

    /**
     * <p>forNodeId</p>
     *
     * @param nodeId a int.
     * @param dateRange a long.
     * @param startTs a java.lang.Long.
     * @param endTs a java.lang.Long.
     * @return a {@link org.opennms.netmgt.model.OnmsOutageCollection} object.
     */
    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    @Transactional
    @Path("forNode/{nodeId}")
    public OnmsOutageCollection forNodeId(@Context final UriInfo uriInfo,
            @PathParam("nodeId") final int nodeId,
            @DefaultValue("604800000") @QueryParam("dateRange") final long dateRange,
            @QueryParam("start") final Long startTs,
            @QueryParam("end") final Long endTs) {

        final CriteriaBuilder builder = new CriteriaBuilder(OnmsOutage.class);
        builder.eq("node.id", nodeId);

        builder.alias("monitoredService", "monitoredService");
        builder.alias("monitoredService.ipInterface", "ipInterface");
        builder.alias("monitoredService.ipInterface.node", "node");
        builder.alias("monitoredService.serviceType", "serviceType");
        builder.isNull("perspective");

        final MultivaluedMap<String, String> params = new MultivaluedMapImpl();
        params.putAll(uriInfo.getQueryParameters());
        LOG.debug("Processing outages for node {} using {}", nodeId, params);

        if (startTs != null && endTs != null) {
            params.remove("start");
            params.remove("end");
            final Date start = new Date(startTs);
            final Date end = new Date(endTs);
            LOG.debug("Getting all outages from {} to {} for node {}", start, end, nodeId);
            builder.or(Restrictions.isNull("ifRegainedService"), Restrictions.and(Restrictions.gt("ifLostService", start), Restrictions.lt("ifLostService", end)));
        } else {
            params.remove("dateRange");
            final Date start = new Date(System.currentTimeMillis() - dateRange);
            LOG.debug("Getting all outgae from {} to current date for node {}", start, nodeId);
            builder.or(Restrictions.isNull("ifRegainedService"), Restrictions.gt("ifLostService", start));
        }

        applyQueryFilters(params, builder);

        builder.orderBy("id").desc();

        return new OnmsOutageCollection(m_outageDao.findMatching(builder.toCriteria()));
    }
}

