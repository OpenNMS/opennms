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
