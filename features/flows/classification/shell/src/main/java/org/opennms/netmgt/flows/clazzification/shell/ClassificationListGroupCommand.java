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
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.flows.classification.ClassificationService;
import org.opennms.netmgt.flows.classification.persistence.api.Group;

@Command(scope="opennms-classification", name="list-groups", description = "Lists all classification groups stored in the database")
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

