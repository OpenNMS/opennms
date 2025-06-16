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
package org.opennms.netmgt.graph.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerCache;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.shell.completer.ContainerIdCompleter;

@Service
@Command(scope = "opennms", name = "graph-force-reload", description="Invalidates the cache for the given container, forcing it to reload when next request occurs")
public class GraphReloadCommand implements Action {

    @Reference
    private GraphContainerCache graphContainerCache;

    @Reference
    private GraphService graphService;

    @Completion(ContainerIdCompleter.class)
    @Option(name="--container", description="The id of the container", required=true)
    private String containerId;

    @Override
    public Object execute() throws Exception {
        final GraphContainerInfo graphContainerInfo = graphService.getGraphContainerInfo(containerId);
        if (graphContainerInfo == null) {
            System.out.println("No graph in container with id '" + containerId + "' found");
        } else {
            graphContainerCache.invalidate(containerId);
            System.out.println("Container invalidated. On next request, the container is reloaded");
        }
        return null;
    }
}
