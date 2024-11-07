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

package org.opennms.netmgt.provision.requisition.command;

import org.apache.karaf.shell.api.action.*;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import java.util.ArrayList;
import java.util.List;

@Command(scope = "opennms", name = "delete-requisition", description = "Delete a named Provisioning Requisition.")
@Service
public class DeleteRequisition implements Action {
    @Reference
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Option(name = "-e", aliases = "--empty", description = "Empty the requisition of all nodes / delete all nodes from the requisition.  Send a 'uei.opennms.org/internal/importer/reloadImport' even to then synchronize the requisition and delete all the nodes.")
    boolean Empty = false;

    @Option(name = "--yes-i-really-mean-it", description = "Yes, I understand what I am doing, really delete things.")
    boolean yesreally = false;

    @Argument(index = 0, name = "requisitionName", description = "Requisition name", required = true, multiValued = false)
    @Completion(RequisitionNameCompleter.class)
    private String requisitionName;

    @Override
    public Object execute() {
        try {
            final Requisition req = deployedForeignSourceRepository.getRequisition(requisitionName);
            if (Empty && yesreally) {
                int nodeCount = 0;
                System.out.println("Requisition contains " + req.getNodeCount() + " nodes.");
                List<RequisitionNode> nodeList = new ArrayList<>(req.getNodes()); // to avoid ConcurrentModificationException
                for (RequisitionNode thisNode : nodeList ) {
                    req.deleteNode(thisNode);
                    nodeCount++;
                }
                System.out.println("Deleted " + nodeCount + " nodes.");
                req.validate();
                deployedForeignSourceRepository.save(req);
            }
            else if (yesreally && req.getNodeCount() == 0) {
                deployedForeignSourceRepository.delete(req);
                System.out.println("Deleted '" + requisitionName + "'.");
            }
            else if (yesreally && req.getNodeCount() > 0) {
                System.out.println("Requisition " + requisitionName + " is not empty.  Empty it first with '--empty', and optionally send an appropriate 'uei.opennms.org/internal/importer/reloadImport' event to synchronize it to the database.");
            }
            else {
                System.out.println("You must confirm you really want to perform this action.  see --help");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
    }

}
