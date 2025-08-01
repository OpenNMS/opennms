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

@Command(scope="opennms", name="list-classification-rules", description = "Lists classification rules stored in the database")
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
