/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.measurements.shell;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.ResourceId;
import org.opennms.netmgt.model.RrdGraphAttribute;

@Command(scope = "opennms-measurements", name = "show-resources", description = "Displays the resource tree. Optionally filter by node or resource ID.")
@Service
public class ShowResources implements Action {

    @Reference
    SessionUtils sessionUtils;

    @Reference
    NodeDao nodeDao;

    @Reference
    ResourceDao resourceDao;

    @Option(name = "-n", aliases = "--node", description = "Node ID, or FS:FID")
    String nodeCriteria;

    @Option(name = "-r", aliases = "--resource-id", description = "Resource ID")
    String resourceId;

    @Option(name = "-c", aliases = "--no-children", description = "Don't recurse through child resources")
    boolean dontRecurse = false;

    @Override
    public Object execute() {
        sessionUtils.withReadOnlyTransaction(() -> {
            if (nodeCriteria != null) {
                final OnmsNode node = nodeDao.get(nodeCriteria);
                if (node == null) {
                    System.out.printf("No node found for: %s\n", nodeCriteria);
                    return null;
                }
                displayResourceTree(resourceDao.getResourceForNode(node));
            } else if (resourceId != null) {
                displayResourceTree(resourceDao.getResourceById(ResourceId.fromString(resourceId)));
            } else {
                displayResourceTree(resourceDao.findTopLevelResources());
            }
            return null;
        });
        return null;
    }

    private void displayResourceTree(OnmsResource resource) {
        displayResourceTree( Collections.singletonList(resource));
    }

    private void displayResourceTree(List<OnmsResource> resources) {
        for (OnmsResource resource : resources) {
            displayResource(resource);
            if (!dontRecurse) {
                displayResourceTree(resource.getChildResources());
            }
        }
    }

    private static void displayResource(OnmsResource resource) {
        System.out.println();
        System.out.println("ID:         " + resource.getId());
        System.out.println("Name:       " + resource.getName());
        System.out.println("Label:      " + resource.getLabel());
        System.out.println("Type:       " + resource.getResourceType().getLabel());
        System.out.println("Link:       " + resource.getLink());
        final OnmsResource parent = resource.getParent();
        if (parent != null) {
            System.out.println("Parent ID:  " + parent.getId());
        }

        System.out.println("Children:");
        if (resource.getChildResources() != null) {
            for (final OnmsResource childResource : resource.getChildResources()) {
                System.out.println("  " + childResource.getId());
            }
        }

        System.out.println("Attributes:");

        System.out.println("  External:");
        for (final Map.Entry<String, String> e : resource.getExternalValueAttributes().entrySet()) {
            System.out.println("    " + e.getKey() + " = '" + e.getValue() + "'");
        }

        System.out.println("  Metrics:");
        for (final Map.Entry<String, RrdGraphAttribute> e : resource.getRrdGraphAttributes().entrySet()) {
            System.out.println(
                    "    " + e.getKey() + " = '" + e.getValue().getRrdFile() + "'");
        }

        System.out.println("  Strings:");
        for (final Map.Entry<String, String> e : resource.getStringPropertyAttributes().entrySet()) {
            System.out.println("    " + e.getKey() + " = '" + e.getValue() + "'");
        }
    }
}
