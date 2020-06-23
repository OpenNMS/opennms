/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

@Command(scope = "filters", name = "filter", description = "Enumerates nodes/interfaces that match a give filter")
@Service
public class FilterCommand implements Action {

    @Reference
    private FilterDao filterDao;

    @Reference
    private NodeDao nodeDao;

    @Reference
    private TransactionOperations transactionOperations;

    @Argument(description = "A filter Rule", required = true, multiValued = false)
    private String filterRule;

    @Override
    public Object execute() throws Exception {
        transactionOperations.execute((TransactionCallback<Void>) status -> {
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
