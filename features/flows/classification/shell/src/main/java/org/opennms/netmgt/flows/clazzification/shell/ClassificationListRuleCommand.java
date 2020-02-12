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

import java.util.List;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.flows.classification.ClassificationService;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;

@Command(scope="opennms-classification", name="list-rules", description = "Lists classification rules stored in the database")
@Service
public class ClassificationListRuleCommand implements Action {

    @Option(name = "-g", aliases = {"--group"}, description = "Only shows rules for this group")
    @Completion(value=GroupCompleter.class)
    private String group = "user-defined";

    @Reference
    private ClassificationService classificationService;

    @Override
    public Object execute() throws Exception {
        final Criteria criteria = new CriteriaBuilder(Rule.class)
                .alias("group", "group")
                .eq("group.name", group)
                .orderBy("position", true)
                .toCriteria();
        final List<Rule> rules = classificationService.findMatchingRules(criteria);
        final String TEMPLATE = "%4s   %-20s   %-15s   %10s   %-40s   %-10s   %-40s   %-10s   %-20s   %-15s   %s";
        if (!rules.isEmpty()) {
            System.out.println(String.format(TEMPLATE, "Pos", "Name", "Protocol", "ID", "Dest. Addr.", "Dest. Port", "Src. Addr.", "Src. Port", "Exporter Filter", "Bidirectional", "Group"));
            for (Rule rule : rules) {
                System.out.println(
                        String.format(
                                TEMPLATE,
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
                                rule.getGroup().getName()
                        ));
            }
            System.out.println();
        }
        System.out.println("=> " + rules.size() + " rule(s) defined for group '" + group + "'");
        return null;
    }
}
