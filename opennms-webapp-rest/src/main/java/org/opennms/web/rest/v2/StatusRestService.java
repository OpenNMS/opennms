/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v2;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.support.QueryParameters;
import org.opennms.web.rest.support.QueryParametersBuilder;
import org.opennms.web.rest.v2.status.SeverityFilter;
import org.opennms.web.rest.v2.status.StatusSummary;
import org.opennms.web.rest.v2.status.application.ApplicationDTO;
import org.opennms.web.rest.v2.status.application.ApplicationDTOList;
import org.opennms.web.rest.v2.status.application.ApplicationStatusService;
import org.opennms.web.rest.v2.status.application.Query;
import org.opennms.web.rest.v2.status.bsm.BusinessServiceDTO;
import org.opennms.web.rest.v2.status.bsm.BusinessServiceDTOList;
import org.opennms.web.rest.v2.status.bsm.BusinessServiceStatusService;
import org.opennms.web.rest.v2.status.node.NodeDTO;
import org.opennms.web.rest.v2.status.node.NodeDTOList;
import org.opennms.web.rest.v2.status.node.NodeQuery;
import org.opennms.web.rest.v2.status.node.NodeStatusService;
import org.opennms.web.rest.v2.status.node.strategy.StatusCalculationStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component("statusRestService")
@Path("status")
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
public class StatusRestService {

    @Autowired
    private BusinessServiceStatusService businessServiceStatusService;

    @Autowired
    private ApplicationStatusService applicationStatusService;

    @Autowired
    private NodeStatusService nodeStatusService;

    @GET
    @Path("/summary/nodes/{type}")
    public Response getNodeStatus(@PathParam("type") String type) {
        final StatusCalculationStrategy strategy = StatusCalculationStrategy.createFrom(type);
        if (strategy == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Strategy '" + type + "' not supported. Supported values are:" + Arrays.toString(StatusCalculationStrategy.values()))
                    .build();
        }
        final StatusSummary summary = nodeStatusService.getSummary(strategy);
        return Response.ok().entity(convert(summary)).build();
    }

    @GET
    @Path("/summary/applications")
    public List<Object[]> getApplicationStatus() {
        StatusSummary summary = applicationStatusService.getSummary();
        return convert(summary);
    }

    @GET
    @Path("/summary/business-services")
    public List<Object[]> getBusinessServiceStatus() {
        StatusSummary summary = businessServiceStatusService.getSummary();
        return convert(summary);
    }

    @GET
    @Path("/applications")
    public Response getApplications(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SearchCondition<SeverityFilter> searchCondition = getSearchCondition(searchContext);
        final Query query = new Query(queryParameters, searchCondition);

        final List<ApplicationDTO> applications = applicationStatusService.getStatus(query);
        final int totalCount = applicationStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final ApplicationDTOList list = new ApplicationDTOList(applications);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        // Make sure that offset is set to a numeric value when setting the Content-Range header
        return Response
                .ok(list)
                .header("Content-Range", String.format("items %d-%d/%d", offset, offset + list.size() - 1, totalCount))
                .build();
    }

    @GET
    @Path("/business-services")
    public Response getBusinessServices(@Context final UriInfo uriInfo, @Context final SearchContext searchContext) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SearchCondition<SeverityFilter> searchCondition = getSearchCondition(searchContext);
        final Query query = new Query(queryParameters, searchCondition);

        final List<BusinessServiceDTO> services = businessServiceStatusService.getStatus(query);
        final int totalCount = businessServiceStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final BusinessServiceDTOList list = new BusinessServiceDTOList(services);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        // Make sure that offset is set to a numeric value when setting the Content-Range header
        return Response
                .ok(list)
                .header("Content-Range", String.format("items %d-%d/%d", offset, offset + list.size() - 1, totalCount))
                .build();
    }

    @GET
    @Path("/nodes/{type}")
    public Response getNodes(@Context final UriInfo uriInfo, @Context final SearchContext searchContext, @PathParam("type") String type) {
        final StatusCalculationStrategy strategy = StatusCalculationStrategy.createFrom(type);
        if (strategy == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Strategy '" + type + "' not supported. Supported values are:" + Arrays.toString(StatusCalculationStrategy.values()))
                    .build();
        }

        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SearchCondition<SeverityFilter> searchCondition = getSearchCondition(searchContext);
        final NodeQuery query = new NodeQuery(queryParameters, searchCondition);
        query.setStatusCalculationStrategy(strategy);

        final List<NodeDTO> nodes = nodeStatusService.getStatus(query);
        final int totalCount = nodeStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final NodeDTOList list = new NodeDTOList(nodes);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        // Make sure that offset is set to a numeric value when setting the Content-Range header
        return Response
                .ok(list)
                .header("Content-Range", String.format("items %d-%d/%d", offset, offset + list.size() - 1, totalCount))
                .build();
    }

    private static List<Object[]> convert(StatusSummary statusSummary) {
        return convert(statusSummary.getSeverityMap());
    }

    private static List<Object[]> convert(Map<OnmsSeverity, Long> input) {
        return input.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> new Object[]{e.getKey().getLabel(), e.getValue()})
                .collect(Collectors.toList());
    }

    private static SearchCondition<SeverityFilter> getSearchCondition(SearchContext searchContext) {
        if (!Strings.isNullOrEmpty(searchContext.getSearchExpression())) {
            return searchContext.getCondition(SeverityFilter.class);
        }
        return null;
    }
}
