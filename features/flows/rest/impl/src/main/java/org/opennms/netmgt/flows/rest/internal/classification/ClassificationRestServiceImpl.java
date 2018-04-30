/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.rest.internal.classification;

import static org.opennms.netmgt.flows.rest.internal.classification.ClassificationRequestDTOValidator.validate;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.criteria.restrictions.Restrictions;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRequestBuilder;
import org.opennms.netmgt.flows.classification.ClassificationService;
import org.opennms.netmgt.flows.classification.persistence.api.Group;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.rest.classification.ClassificationRequestDTO;
import org.opennms.netmgt.flows.rest.classification.ClassificationResponseDTO;
import org.opennms.netmgt.flows.rest.classification.ClassificationRestService;
import org.opennms.netmgt.flows.rest.classification.GroupDTO;
import org.opennms.netmgt.flows.rest.classification.RuleDTO;
import org.opennms.web.utils.CriteriaBuilderUtils;
import org.opennms.web.utils.QueryParameters;
import org.opennms.web.utils.QueryParametersBuilder;
import org.opennms.web.utils.ResponseUtils;
import org.opennms.web.utils.UriInfoUtils;

import com.google.common.base.Strings;

public class ClassificationRestServiceImpl implements ClassificationRestService {

    private final ClassificationService classificationService;

    public ClassificationRestServiceImpl(ClassificationService classificationService) {
        this.classificationService = Objects.requireNonNull(classificationService);
    }

    @Override
    public Response getRules(UriInfo uriInfo) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final CriteriaBuilder criteriaBuilder = CriteriaBuilderUtils.buildFrom(Rule.class, queryParameters);

        // Apply group Filter
        criteriaBuilder.alias("group", "group");
        final Set<Integer> groupFilter = UriInfoUtils.getValues(uriInfo, "groupFilter", Collections.emptyList())
                .stream().map(g -> g != null ? g.trim() : g)
                .filter(g -> g != null)
                .map(g -> Integer.valueOf(g))
                .collect(Collectors.toSet());
        if (!groupFilter.isEmpty()) {
            criteriaBuilder.in("group.id", groupFilter);
        }

        // Apply query filter
        final String rawQuery = UriInfoUtils.getValue(uriInfo, "query", null);
        if (rawQuery != null && !rawQuery.trim().isEmpty()) {
            final String query = "%" + rawQuery + "%";
            criteriaBuilder.or(
                    Restrictions.iplike("src_address", rawQuery), // use column for iplike and not the entity property
                    Restrictions.like("srcAddress", query),
                    Restrictions.ilike("srcPort", query),
                    Restrictions.iplike("dst_address", rawQuery), // use column for iplike and not the entity property
                    Restrictions.like("dstAddress", query),
                    Restrictions.ilike("dstPort", query),
                    Restrictions.ilike("name", query),
                    Restrictions.ilike("exporterFilter", query),
                    Restrictions.ilike("protocol", query)).toCriteria();
        }

        // Apply group priority sorting as well, if ordering is position
        final QueryParameters.Order order = queryParameters.getOrder();
        if (order != null && order.getColumn() != null && order.getColumn().equalsIgnoreCase("position")) {
            criteriaBuilder.clearOrder();
            criteriaBuilder.orderBy("group.priority", false);
            criteriaBuilder.orderBy(order.getColumn(), queryParameters.getOrder().isAsc());
        }

        // Apply filter to only fetch rules for enabled groups
        criteriaBuilder.eq("group.enabled", true);
        return createResponse(criteriaBuilder,
                (criteria) -> classificationService.findMatchingRules(criteria),
                (criteria) -> classificationService.countMatchingRules(criteria),
                rules -> rules.stream().map(rule -> convert(rule)).collect(Collectors.toList()));
    }

    @Override
    public Response getRule(int id) {
        final Rule rule = classificationService.getRule(id);
        return Response.ok(convert(rule)).build();
    }

    @Override
    public Response saveRule(RuleDTO ruleDTO) {
        final Rule rule = convert(ruleDTO);
        rule.setId(null);

        final int ruleId = classificationService.saveRule(rule);
        final UriBuilder builder = UriBuilder.fromResource(ClassificationRestService.class);
        final URI uri = builder.path(ClassificationRestService.class, "getRule").build(ruleId);
        return Response.created(uri).build();
    }

    @Override
    public Response importRules(UriInfo uriInfo, InputStream inputStream) {
        boolean skipHeader = Boolean.valueOf(UriInfoUtils.getValue(uriInfo, "hasHeader", "true"));
        boolean deleteExistingRules = Boolean.valueOf(UriInfoUtils.getValue(uriInfo, "deleteExistingRules", "true"));
        classificationService.importRules(inputStream, skipHeader, deleteExistingRules);
        return Response.noContent().build();
    }

    @Override
    public Response deleteRules(UriInfo uriInfo) {
        String groupId = UriInfoUtils.getValue(uriInfo, "groupId", null);
        if (groupId != null) {
            classificationService.deleteRules(Integer.parseInt(groupId));
            return Response.noContent().build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @Override
    public Response deleteRule(int id) {
        classificationService.deleteRule(id);
        return Response.noContent().build();
    }

    @Override
    public Response updateRule(int id, RuleDTO newValue) {
        // Update
        final Rule rule = classificationService.getRule(id);
        final Rule newRule = convert(newValue);
        rule.setProtocol(newRule.getProtocol());
        rule.setDstPort(newRule.getDstPort());
        rule.setDstAddress(newRule.getDstAddress());
        rule.setSrcPort(newRule.getSrcPort());
        rule.setSrcAddress(newRule.getSrcAddress());
        rule.setName(newRule.getName());
        rule.setExporterFilter(newValue.getExporterFilter());

        // Persist
        classificationService.updateRule(rule);
        return Response.ok(convert(rule)).build();
    }

    @Override
    public Response classify(ClassificationRequestDTO classificationRequestDTO) {
        validate(classificationRequestDTO);

        final ClassificationRequest classificationRequest = new ClassificationRequestBuilder()
                .withLocation(null)
                .withSrcAddress(classificationRequestDTO.getSrcAddress())
                .withSrcPort(Integer.parseInt(classificationRequestDTO.getSrcPort()))
                .withDstAddress(classificationRequestDTO.getDstAddress())
                .withDstPort(Integer.parseInt(classificationRequestDTO.getDstPort()))
                .withProtocol(Protocols.getProtocol(classificationRequestDTO.getProtocol()))
                .withExporterAddress(classificationRequestDTO.getExporterAddress())
                .build();
        final String classification = classificationService.classify(classificationRequest);
        if (Strings.isNullOrEmpty(classification)) return Response.noContent().build();
        return Response.ok(new ClassificationResponseDTO(classification)).build();
    }

    @Override
    public Response getGroups(UriInfo uriInfo) {
        final QueryParameters queryParameters = QueryParametersBuilder.buildFrom(uriInfo);
        final CriteriaBuilder criteriaBuilder = CriteriaBuilderUtils.buildFrom(Group.class, queryParameters);
        return createResponse(
                criteriaBuilder,
                (criteria) -> classificationService.findMatchingGroups(criteria),
                (criteria) -> classificationService.countMatchingGroups(criteria),
                (groups) -> groups.stream().map(group -> convert(group)).collect(Collectors.toList()));
    }

    @Override
    public Response getGroup(int groupId, String format, String requestedFilename, String acceptHeader) {
        boolean isCsvRequested = (acceptHeader != null && acceptHeader.contains("text/comma-separated-values"))
                || "csv".equalsIgnoreCase(format);

        if (isCsvRequested
                && requestedFilename != null // this means filename parameter was present
                && !new FilenameHelper().isValidFileName(requestedFilename)) {
          return Response.status(Response.Status.BAD_REQUEST)
                  .entity("parameter filename should follow this regex pattern: " + FilenameHelper.REGEX_ALLOWED_CHAR)
                  .build();
        } else if(isCsvRequested) {
            return getGroupAsCsv(groupId, requestedFilename);
        } else {
            return getGroupAsJson(groupId);
        }
    }

    private Response getGroupAsCsv(int groupId, String requestedFilename){
        final String csvContent = classificationService.exportRules(groupId);
        final String filename = new FilenameHelper().createFilenameForGroupExport(groupId, requestedFilename);
        return Response.ok()
                .header("Content-Disposition", "attachment; filename=\""+filename+"\"")
                .header("Content-Type", "text/comma-separated-values")
                .entity(csvContent)
                .build();
    }

    private Response getGroupAsJson(int groupId){
        final Group group = classificationService.getGroup(groupId);
        return Response.ok(convert(group))
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .build();
    }

    @Override
    public Response deleteGroup(int groupId) {
        classificationService.deleteGroup(groupId);
        return Response.noContent().build();
    }

    @Override
    public Response updateGroup(int id, GroupDTO newValue) {
        final Group group = classificationService.getGroup(id);

        // At the moment only togging the enabled state is supported
        group.setEnabled(newValue.isEnabled());

        classificationService.updateGroup(group);
        return Response.ok(convert(group)).build();
    }

    @Override
    public Response getProtocols() {
        return Response.ok(Protocols.getProtocols()).build();
    }

    private static Rule convert(RuleDTO ruleDTO) {
        if (ruleDTO == null) return null;
        final Rule rule = new Rule();
        if (!Strings.isNullOrEmpty(ruleDTO.getName())) {
            rule.setName(ruleDTO.getName());
        }
        if (!Strings.isNullOrEmpty(ruleDTO.getDstAddress())) {
            rule.setDstAddress(ruleDTO.getDstAddress());
        }
        if (!Strings.isNullOrEmpty(ruleDTO.getDstPort())) {
            rule.setDstPort(ruleDTO.getDstPort());
        }
        if (!Strings.isNullOrEmpty(ruleDTO.getSrcAddress())) {
            rule.setSrcAddress(ruleDTO.getSrcAddress());
        }
        if (!Strings.isNullOrEmpty(ruleDTO.getSrcPort())) {
            rule.setSrcPort(ruleDTO.getSrcPort());
        }
        if (!Strings.isNullOrEmpty(ruleDTO.getExporterFilter())) {
            rule.setExporterFilter(ruleDTO.getExporterFilter());
        }
        rule.setProtocol(ruleDTO.getProtocols().stream().collect(Collectors.joining(",")));
        return rule;
    }

    private static RuleDTO convert(Rule rule) {
        if (rule == null) return null;
        final RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setId(rule.getId());
        ruleDTO.setName(rule.getName());
        ruleDTO.setDstAddress(rule.getDstAddress());
        ruleDTO.setProtocol(rule.getProtocol());
        ruleDTO.setDstPort(rule.getDstPort());
        ruleDTO.setSrcAddress(rule.getSrcAddress());
        ruleDTO.setSrcPort(rule.getSrcPort());
        ruleDTO.setGroup(convert(rule.getGroup()));
        ruleDTO.setPosition(rule.getPosition());
        ruleDTO.setExporterFilter(rule.getExporterFilter());
        return ruleDTO;
    }

    private static GroupDTO convert(Group group) {
        if (group == null) return null;
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(group.getId());
        groupDTO.setName(group.getName());
        groupDTO.setDescription(group.getDescription());
        groupDTO.setPriority(group.getPriority());
        groupDTO.setEnabled(group.isEnabled());
        groupDTO.setReadOnly(group.isReadOnly());
        groupDTO.setRuleCount(group.getRules().size());
        return groupDTO;
    }

    private static <T, X> Response createResponse(CriteriaBuilder criteriaBuilder,
                                                  MatchingDelegate<T> matchingDelegate,
                                                  CountDelegate countDelegate,
                                                  Function<List<T>, List<X>> transform) {
        Objects.requireNonNull(criteriaBuilder);
        Objects.requireNonNull(matchingDelegate);
        Objects.requireNonNull(countDelegate);
        Objects.requireNonNull(transform);

        final Criteria criteria = criteriaBuilder.toCriteria();
        final List<T> entities = matchingDelegate.findMatching(criteria);
        if (entities.isEmpty()) {
            return Response.noContent().build();
        }
        // Reset any offset/limits and orders in order to count properly
        criteria.setOrders(new ArrayList<>());
        criteria.setOffset(null);
        criteria.setLimit(null);

        // build response
        final List responseBody = transform.apply(entities);
        final int offset = (criteria.getOffset() == null ? 0 : criteria.getOffset());
        final long totalCount = countDelegate.countMatching(criteria);
        return ResponseUtils.createResponse(responseBody, offset, totalCount);
    }

    @FunctionalInterface
    private interface MatchingDelegate<T> {
        List<T> findMatching(Criteria criteria);
    }

    @FunctionalInterface
    private interface CountDelegate {
        long countMatching(Criteria criteria);
    }
}
