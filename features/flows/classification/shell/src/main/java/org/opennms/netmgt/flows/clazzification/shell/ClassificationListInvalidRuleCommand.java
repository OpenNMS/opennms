/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.clazzification.shell;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.flows.classification.ClassificationService;
import org.opennms.netmgt.flows.classification.exception.InvalidRuleException;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

@Command(scope="opennms-classification", name="list-invalid-rules", description = "Lists invalid classification rules")
@Service
public class ClassificationListInvalidRuleCommand implements Action {

    @Reference
    private ClassificationService classificationService;

    @Override
    public Object execute() throws Exception {
        final List<Rule> invalidRules = getInvalidRules();
        final String TEMPLATE = "%-20s   %4s   %-20s   %-15s   %10s   %-40s   %-10s   %-40s   %-10s   %-20s   %-15s   %s";
        if (!invalidRules.isEmpty()) {
            System.out.println(String.format(TEMPLATE, "Group", "Pos", "Name", "Protocol", "ID", "Dest. Addr.", "Dest. Port", "Src. Addr.", "Src. Port", "Exporter Filter", "Bidirectional", "Error"));
            for (Rule rule : invalidRules) {
                final String error = getErrorReason(rule);
                System.out.println(
                        String.format(
                                TEMPLATE,
                                rule.getGroup().getName(),
                                rule.getPosition(),
                                rule.getName(),
                                rule.getProtocol() == null ? "" : rule.getProtocol(),
                                rule.getId(),
                                rule.getDstAddress() == null ? "" : rule.getDstAddress(),
                                rule.getDstPort() == null ? "" : rule.getDstPort(),
                                rule.getSrcAddress() == null ? "" : rule.getSrcAddress(),
                                rule.getSrcPort() == null ? "" : rule.getSrcPort(),
                                rule.getExporterFilter() == null ? "" : rule.getExporterFilter(),
                                rule.isOmnidirectional() ? "Y" : "N",
                                error
                        ));
            }
            System.out.println();
        }
        System.out.println("=> " + invalidRules.size() + " invalid rule(s) found.");
        if (!invalidRules.isEmpty()) {
            System.out.println();
            System.out.println("Please manually fix these rules via the Flow Classification UI");
        }
        return null;
    }

    private List<Rule> getInvalidRules() {
        return classificationService.getInvalidRules().stream()
                .sorted(Comparator.comparing(Rule::getGroupPosition)
                        .thenComparing(Rule::getPosition))
                .collect(Collectors.toList());
    }

    private String getErrorReason(final Rule rule) {
        try {
            classificationService.validateRule(rule);
            return "Unknown";
        } catch (InvalidRuleException ex) {
            return ex.getMessage();
        }
    }
}
