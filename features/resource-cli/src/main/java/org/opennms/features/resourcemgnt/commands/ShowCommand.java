/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.resourcemgnt.commands;

import java.util.Map;

import org.kohsuke.args4j.Argument;
import org.opennms.features.resourcemgnt.ResourceCli;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.resource.ResourceDTO;

public class ShowCommand extends AbstractCommand {

    @Argument(required = true,
              metaVar = "resource",
              usage = "the resource to show")
    private String resource = "";

    @Override
    public void execute(final ResourceCli resourceCli) throws Exception {
        // Request and print the data
        final ResourceDTO resource = connect(resourceCli, this.resource)
                .header("Accept", "application/xml")
                .get(ResourceDTO.class);

        System.out.println("ID:         " + resource.getId());
        System.out.println("Name:       " + resource.getName());
        System.out.println("Label:      " + resource.getLabel());
        System.out.println("Type:       " + resource.getTypeLabel());
        System.out.println("Link:       " + resource.getLink());
        System.out.println("Parent ID:  " + resource.getParentId());

        System.out.println("Children:");
        if (resource.getChildren() != null) {
            for (final ResourceDTO childResource : resource.getChildren().getObjects()) {
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
