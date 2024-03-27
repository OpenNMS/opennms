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

@Command(scope="opennms", name="list-classification-invalid-rules", description = "Lists invalid classification rules")
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
