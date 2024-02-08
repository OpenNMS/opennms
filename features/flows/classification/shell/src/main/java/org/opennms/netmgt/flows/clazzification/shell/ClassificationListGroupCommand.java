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
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.flows.classification.ClassificationService;
import org.opennms.netmgt.flows.classification.persistence.api.Group;

@Command(scope="opennms", name="list-classification-groups", description = "Lists all classification groups stored in the database")
@Service
public class ClassificationListGroupCommand implements Action {

    @Reference
    private ClassificationService classificationService;

    @Override
    public Object execute() throws Exception {
        final Criteria criteria = new CriteriaBuilder(Group.class).orderBy("position", true).toCriteria();
        final List<Group> groups = classificationService.findMatchingGroups(criteria);
        final String TEMPLATE = "%4s   %4s   %-20s   %-50s   %-8s   %-8s";
        if (!groups.isEmpty()) {
            System.out.println(String.format(TEMPLATE, "ID", "Pos", "Name", "Description", "Editable", "Read-only"));
            for (Group group : groups) {
                System.out.println(
                    String.format(
                            TEMPLATE,
                            group.getId(),
                            group.getPosition(),
                            group.getName(),
                            group.getDescription(),
                            group.isEnabled() ? "Y" : "N",
                            group.isReadOnly() ? "Y" : "N"
                    ));
            }
            System.out.println();
        }
        System.out.println("=> " + groups.size() + " group(s) defined");
        return null;
    }
}

