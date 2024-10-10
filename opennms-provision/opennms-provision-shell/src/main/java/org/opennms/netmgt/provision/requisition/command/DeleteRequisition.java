/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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
