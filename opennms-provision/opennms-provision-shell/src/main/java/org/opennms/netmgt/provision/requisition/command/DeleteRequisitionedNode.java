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

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;

import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

import java.util.List;

@Command(scope = "opennms", name = "delete-requisitioned-node", description = "Delete a node from a Provisioning Requisition.")
@Service
public class DeleteRequisitionedNode implements Action {
    @Reference
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Option(name = "-r", aliases = "--requisition", description = "Requisition name", required = true)
    @Completion(RequisitionNameCompleter.class)
    private String requisitionName;

    @Option(name = "-f", aliases = "--foreignid", description = "One or more node foreign IDs", required = true, multiValued = true)
    private List<String> foreignIDs;

    @Override
    public Object execute() {
        try {
            if (doesRequisitionExist()) {
                Requisition req = deployedForeignSourceRepository.getRequisition(requisitionName);
                int count = 0;
                for (String fsid : foreignIDs) {
                    System.out.println("Deleting " + fsid + "...");
                    req.deleteNode(fsid);
                    count++;
                }
                System.out.println("Deleted " + count + " nodes");
            }
            else {
                System.out.println("Requisition '" + requisitionName  + "' not found.");
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }
        return null;
    }

    private boolean doesRequisitionExist() {
        boolean reqExists = true;
        Requisition someReq = null;
        try {
            someReq = deployedForeignSourceRepository.getRequisition(requisitionName);
        } catch (ForeignSourceRepositoryException e) {
            reqExists = false;
        }
        if (someReq == null) {
            reqExists = false;
        }
        return reqExists;
    }