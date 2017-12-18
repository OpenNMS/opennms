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

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.CsvRuleParser;
import org.opennms.netmgt.flows.classification.persistence.api.ClassificationRuleDao;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.rest.classification.ClassificationDTO;
import org.opennms.netmgt.flows.rest.classification.ClassificationRequestDTO;
import org.opennms.netmgt.flows.rest.classification.ClassificationRestService;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Strings;

public class ClassificationRestServiceImpl implements ClassificationRestService {

    private ClassificationRuleDao classificationDao;

    private ClassificationEngine classificationEngine;

    private CsvRuleParser csvRuleParser;

    private TransactionOperations transactionTemplate;

    public ClassificationRestServiceImpl(ClassificationRuleDao classificationDao,
                                         ClassificationEngine classificationEngine,
                                         CsvRuleParser csvRuleParser,
                                         TransactionOperations transactionOperations) {
        this.classificationDao = Objects.requireNonNull(classificationDao);
        this.classificationEngine = Objects.requireNonNull(classificationEngine);
        this.csvRuleParser = Objects.requireNonNull(csvRuleParser);
        this.transactionTemplate = Objects.requireNonNull(transactionOperations);
    }

    @Override
    public Response getClassifications() {
        final List<Rule> rules = classificationDao.findAll();
        if (rules.isEmpty()) {
            return Response.noContent().build();
        }
        final List<ClassificationDTO> dtos = rules.stream()
                .map(r -> convert(r))
                .collect(Collectors.toList());
        return Response.ok().entity(dtos).build();
    }

    @Override
    public Response getClassification(int id) {
        final Rule rule = classificationDao.get(id);
        if (rule == null) return Response.status(NOT_FOUND).build();
        return Response.ok(convert(rule)).build();
    }

    @Override
    public Response saveClassification(ClassificationDTO classificationDTO) {
        final Rule rule = convert(classificationDTO);
        if (!rule.isValid()) return createInvalidRuleResponse(rule);

        return runInTransaction((status) -> {
            final Integer flowId = classificationDao.save(rule);
            final UriBuilder builder = UriBuilder.fromResource(ClassificationRestService.class);
            final URI uri = builder.path(ClassificationRestService.class, "getClassification").build(flowId);
            return Response.created(uri).build();
        });
    }

    @Override
    public Response importClassifications(InputStream inputStream) {
        try {
            final List<Rule> rules = csvRuleParser.parse(inputStream);
            return runInTransaction(status -> {
                rules.forEach(rule -> classificationDao.save(rule));
                return Response.ok().build();
            });
        } catch (IOException e) {
            return Response.status(500)
                    .type(TEXT_PLAIN)
                    .entity("Error while importing rule(s): " + e.getMessage())
                    .build();
        }
    }

    @Override
    public Response deleteClassifications() {
        return runInTransaction(status -> {
            classificationDao.findAll().forEach(r -> classificationDao.delete(r));
            return Response.ok().build();
        });
    }

    @Override
    public Response deleteClassification(int id) {
        return runInTransaction(status -> {
            final Rule rule = classificationDao.get(id);
            if (rule == null) return Response.status(NOT_FOUND).build();
            return runInTransaction(transactionStatus -> {
                classificationDao.delete(rule);
                return Response.ok().build();
            });
        });
    }

    @Override
    public Response updateClassification(int id, ClassificationDTO newValue) {
        Rule rule = classificationDao.get(id);
        if (rule == null) return Response.status(NOT_FOUND).build();
        rule.setProtocol(newValue.getProtocol());
        rule.setPort(newValue.getPort());
        rule.setIpAddress(newValue.getIpAddress());
        rule.setName(newValue.getName());
        if (!rule.isValid()) return createInvalidRuleResponse(rule);

        // Persist
        return runInTransaction(status -> {
            classificationDao.save(rule);
            return Response.ok(convert(rule)).build();
        });
    }

    @Override
    public Response classify(ClassificationRequestDTO classificationRequestDTO) {
        final ClassificationRequest classificationRequest = new ClassificationRequest(null,
                classificationRequestDTO.getPort(),
                classificationRequestDTO.getIpAddress(),
                Protocols.getProtocol(classificationRequestDTO.getProtocol()));
        final String classify = classificationEngine.classify(classificationRequest);
        if (Strings.isNullOrEmpty(classify)) return Response.noContent().build();
        return Response.ok(classify).type(MediaType.TEXT_PLAIN).build();
    }

    @Override
    public Response getProtocols() {
        return Response.ok(Protocols.getProtocols()).build();
    }

    private Response runInTransaction(TransactionCallback<Response> callback) {
        return transactionTemplate.execute(callback);
    }

    private Response createInvalidRuleResponse(Rule input) {
        return Response.status(BAD_REQUEST).type(TEXT_PLAIN).entity(getInvalidMessage(input)).build();
    }

    private String getInvalidMessage(Rule rule) {
        if (Strings.isNullOrEmpty(rule.getName())) {
            return "A rule must contain a name";
        }
        return "A rule must contain a definition. No port, protocol or ipAddress provided.";
    }

    private static Rule convert(ClassificationDTO classificationDTO) {
        if (classificationDTO == null) return null;
        final Rule rule = new Rule();
        rule.setName(classificationDTO.getName());
        rule.setIpAddress(classificationDTO.getIpAddress());
        rule.setPort(classificationDTO.getPort());
        rule.setProtocol(classificationDTO.getProtocol());
        return rule;
    }

    private static ClassificationDTO convert(Rule rule) {
        if (rule == null) return null;
        final ClassificationDTO classification = new ClassificationDTO();
        classification.setName(rule.getName());
        classification.setIpAddress(rule.getIpAddress());
        classification.setProtocol(rule.getProtocol());
        classification.setPort(rule.getPort());
        return classification;
    }
}
