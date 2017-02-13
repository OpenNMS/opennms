/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jetty;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.jetty.server.handler.ContextHandler.AliasCheck;
import org.eclipse.jetty.util.resource.Resource;

/**
 * In certain Spring MVC pages, duplicate slashes are added to the URLs, leading to
 * aliases of the form:
 *
 *   /opt/opennms/jetty-webapps/opennms/WEB-INF/jsp/support/index.jsp
 * 
 * with resources of the form:
 *
 *   /opt/opennms/jetty-webapps/opennms/WEB-INF/jsp//support/index.jsp
 *
 * This {@link AliasCheck} will approve paths, if the resource points
 * to a file, and the absolute path of the file matches the alias.
 *
 * @author jwhite
 */
public class ApproveAbsolutePathAliases implements AliasCheck {

    @Override
    public boolean check(String path, Resource resource) {
        if (path == null || resource == null) {
            return false;
        }

        try {
            // Only match file resources
            final File file = resource.getFile();
            if (file == null) {
                return false;
            }

            // Only match resources with aliases
            final URI resourceAlias = resource.getAlias();
            if (resourceAlias == null) {
                return false;
            }

            try {
                // Compare the absolute path of the file and the alias
                return new File(resourceAlias).getAbsolutePath().equals(file.getAbsolutePath());
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

}
