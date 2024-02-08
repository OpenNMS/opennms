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
package org.opennms.netmgt.filters.shell;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;

@Command(scope = "opennms", name = "filter", description = "Enumerates nodes/interfaces that match a given filter")
@Service
public class FilterCommand implements Action {

    @Reference
    private FilterDao filterDao;

    @Reference
    private NodeDao nodeDao;

    @Reference
    private SessionUtils sessionUtils;

    @Argument(description = "A filter Rule", required = true, multiValued = false)
    private String filterRule;

    @Override
    public Object execute() throws Exception {
        sessionUtils.withReadOnlyTransaction(() -> {
            boolean matching = false;
            SortedMap<Integer, String> nodeMap = null;
            List<InetAddress> matchingInetAddressList = null;
            try {
                matching = filterDao.isRuleMatching(filterRule);
                if (matching) {
                    nodeMap = filterDao.getNodeMap(filterRule);
                    matchingInetAddressList = filterDao.getActiveIPAddressList(filterRule);
                }
            } catch (Exception e) {
                // pass
            }
            if (!matching) {
                System.out.printf("No matching nodes/interfaces for this rule.\n");
                return null;
            }
            List<OnmsNode> nodes = new ArrayList<>();
            if (nodeMap != null && !nodeMap.isEmpty()) {
                nodeMap.keySet().forEach(nodeId -> nodes.add(nodeDao.get(nodeId)));
            }
            for (OnmsNode node : nodes) {

                String foreignId = (node.getForeignId() != null) ? "" : "foreignId=" + node.getForeignId();
                String foreignSource = (node.getForeignSource() != null) ? ""
                        : "foreignSource=" + node.getForeignSource();
                String location = nodeDao.getLocationForId(node.getId());
                System.out.printf("\nnodeId=%d nodeLabel=%s location=%s %s %s \n", node.getId(), node.getLabel(), location, foreignId, foreignSource);

                List<String> categoryNames = new ArrayList<>();
                node.getCategories().forEach(category -> categoryNames.add(category.getName()));
                if (!node.getCategories().isEmpty()) {
                    System.out.printf("\tcategories: \n");
                    categoryNames.stream().forEach(name -> System.out.printf("\t\t%s", name));
                }

                List<String> matchingIpAddresses = new ArrayList<>();
                matchingInetAddressList.stream().forEach(ipAddress -> matchingIpAddresses.add(ipAddress.getHostAddress()));
                System.out.printf("\tIpAddresses: \n");
                List<String> interfacesOnNode = new ArrayList<>();
                node.getIpInterfaces().forEach(ipInterface -> interfacesOnNode.add(ipInterface.getIpAddress().getHostAddress()));
                interfacesOnNode.stream().filter(matchingIpAddresses::contains).collect(Collectors.toList())
                        .forEach(ipAddress -> System.out.printf("\t\t%s \n", ipAddress));

            }
            System.out.flush();
            return null;
        });
        return null;
    }

}
