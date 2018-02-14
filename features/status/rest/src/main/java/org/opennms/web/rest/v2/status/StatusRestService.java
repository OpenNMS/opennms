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

package org.opennms.web.rest.v2.status;

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

import org.opennms.core.config.api.JaxbListWrapper;
import org.opennms.features.status.api.Query;
import org.opennms.features.status.api.SeverityFilter;
import org.opennms.features.status.api.StatusEntity;
import org.opennms.features.status.api.StatusSummary;
import org.opennms.features.status.api.application.ApplicationStatusService;
import org.opennms.features.status.api.bsm.BusinessServiceStatusService;
import org.opennms.features.status.api.node.NodeQuery;
import org.opennms.features.status.api.node.NodeStatusService;
import org.opennms.features.status.api.node.strategy.NodeStatusCalculationStrategy;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.model.OnmsApplication;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.web.rest.v2.status.model.ApplicationDTO;
import org.opennms.web.rest.v2.status.model.ApplicationDTOList;
import org.opennms.web.rest.v2.status.model.BusinessServiceDTO;
import org.opennms.web.rest.v2.status.model.BusinessServiceDTOList;
import org.opennms.web.rest.v2.status.model.NodeDTO;
import org.opennms.web.rest.v2.status.model.NodeDTOList;
import org.opennms.web.utils.QueryParameters;
import org.opennms.web.utils.QueryParametersBuilder;
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
        final NodeStatusCalculationStrategy strategy = NodeStatusCalculationStrategy.createFrom(type);
        if (strategy == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Strategy '" + type + "' not supported. Supported values are:" + Arrays.toString(NodeStatusCalculationStrategy.values()))
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
    public Response getApplications(@Context final UriInfo uriInfo) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SeverityFilter severityFilter = getSeverityFilter(uriInfo);
        final Query query = new Query(queryParameters, severityFilter);

        final List<StatusEntity<OnmsApplication>> applications = applicationStatusService.getStatus(query);
        final int totalCount = applicationStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final List<ApplicationDTO> statusEntities = applications.stream().map(a -> {
            ApplicationDTO dto = new ApplicationDTO();
            dto.setId(a.getEntity().getId());
            dto.setName(a.getEntity().getName());
            dto.setSeverity(a.getStatus());
            return dto;
        }).collect(Collectors.toList());
        final ApplicationDTOList list = new ApplicationDTOList(statusEntities);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        return createResponse(list, offset, totalCount);
    }

    @GET
    @Path("/business-services")
    public Response getBusinessServices(@Context final UriInfo uriInfo) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SeverityFilter severityFilter = getSeverityFilter(uriInfo);
        final Query query = new Query(queryParameters, severityFilter);

        final List<StatusEntity<BusinessService>> services = businessServiceStatusService.getStatus(query);
        final int totalCount = businessServiceStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final List<BusinessServiceDTO> statusEntities = services.stream().map(bs -> {
            BusinessServiceDTO statusDTO = new BusinessServiceDTO();
            statusDTO.setId(bs.getEntity().getId().intValue());
            statusDTO.setName(bs.getEntity().getName());
            statusDTO.setSeverity(bs.getStatus());
            return statusDTO;
        }).collect(Collectors.toList());
        final BusinessServiceDTOList list = new BusinessServiceDTOList(statusEntities);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        return createResponse(list, offset, totalCount);
    }

    @GET
    @Path("/nodes/{type}")
    public Response getNodes(@Context final UriInfo uriInfo, @PathParam("type") String type) {
        final NodeStatusCalculationStrategy strategy = NodeStatusCalculationStrategy.createFrom(type);
        if (strategy == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Strategy '" + type + "' not supported. Supported values are:" + Arrays.toString(NodeStatusCalculationStrategy.values()))
                    .build();
        }

        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final SeverityFilter severityFilter = getSeverityFilter(uriInfo);
        final NodeQuery query = new NodeQuery(queryParameters, severityFilter);
        query.setStatusCalculationStrategy(strategy);

        // Adjust order parameters
        if (query.getParameters().getOrder() != null && query.getParameters().getOrder().getColumn().equals("label")) {
            query.getParameters().setOrder(new QueryParameters.Order("node.nodelabel", query.getParameters().getOrder().isDesc()));
        }

        final List<StatusEntity<OnmsNode>> nodes = nodeStatusService.getStatus(query);
        final int totalCount = nodeStatusService.count(query);
        final int offset = queryParameters.getOffset();

        final List<NodeDTO> statusEntities = nodes.stream().map(node -> {
            NodeDTO nodeDTO = new NodeDTO();
            nodeDTO.setId(node.getEntity().getId());
            nodeDTO.setName(node.getEntity().getLabel());
            nodeDTO.setSeverity(node.getStatus());
            return nodeDTO;
        }).collect(Collectors.toList());
        final NodeDTOList list = new NodeDTOList(statusEntities);
        list.setOffset(queryParameters.getOffset());
        list.setTotalCount(totalCount);

        return createResponse(list, offset, totalCount);
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

    private static SeverityFilter getSeverityFilter(UriInfo uriInfo) {
        final SeverityFilter severityFilter = new SeverityFilter();
        final List<String> severityFilterList = uriInfo.getQueryParameters().get("severityFilter");
        if (severityFilterList != null) {
            for (String eachSeverity : severityFilterList) {
                OnmsSeverity severity = getSeverity(eachSeverity);
                severityFilter.add(severity);
            }
        }
        return severityFilter;
    }

    private static OnmsSeverity getSeverity(String severityString) {
        if (!Strings.isNullOrEmpty(severityString)) {
            for (OnmsSeverity eachSeverity : OnmsSeverity.values()) {
                if (eachSeverity.getLabel().equalsIgnoreCase(severityString)) {
                    return eachSeverity;
                }
            }
        }
        return null;
    }

    private static Response createResponse(JaxbListWrapper list, int offset, int totalCount) {
        if (list.isEmpty()) {
            return Response.noContent().build();
        } else {
            // Make sure that offset is set to a numeric value when setting the Content-Range header
            return Response
                    .ok(list)
                    .header("Content-Range", String.format("items %d-%d/%d", offset, offset + list.size() - 1, totalCount))
                    .build();
        }
    }
}
