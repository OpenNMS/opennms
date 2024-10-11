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
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.ForeignSourceRepositoryException;
import org.opennms.netmgt.provision.persist.requisition.*;

import java.util.ArrayList;
import java.util.List;

@Command(scope = "opennms", name = "show-requisition", description = "Display the content of a named Provisioning Requisition.")
@Service
public class ShowRequisition implements Action {
    @Reference
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Option(name = "-x", aliases = "--xml", description = "Display requisition XML instead of as a table")
    private boolean asXML = false;

    @Argument(index = 0, name = "requisitionName", description = "Requisition name", required = true, multiValued = false)
    @Completion(RequisitionNameCompleter.class)
    private String requisitionName;

    @Override
    public Object execute() {
        try {
            if (doesRequisitionExist() && !asXML) {
                final Requisition req = deployedForeignSourceRepository.getRequisition(requisitionName);
                ShellTable reqTable = new ShellTable();
                reqTable.column("Requisition Name");
                reqTable.column("Last Import Date");
                reqTable.column("Last Update Date");
                reqTable.column("Node Count");
                reqTable.addRow().addContent(req.getForeignSource(), req.getLastImportAsDate(), req.getDate(), req.getNodeCount());
                reqTable.print(System.out);
                System.out.println();
                if (req.getNodeCount() > 0) {
                    ShellTable nodeTable = new ShellTable();
                    nodeTable.column("Node Label");
                    nodeTable.column("Foreign ID");
                    nodeTable.column("Minion Location");
                    nodeTable.column("Building");
                    nodeTable.column("City");
                    nodeTable.column("Categories");
                    nodeTable.column("Meta Data");
                    nodeTable.column("Assets");
                    nodeTable.column("Interfaces");
                    nodeTable.column("parentNodeLabel");
                    nodeTable.column("parentForeignSource");
                    nodeTable.column("parentForeignID");
                    for (RequisitionNode singleNode : req.getNodes()) {
                        //Categories
                        String categories = "";
                        for (RequisitionCategory category : singleNode.getCategories()) {
                            categories += category.getName() + "\n";
                        }
                        //Meta Data
                        String metakv = "";
                        for (RequisitionMetaData nodeMetaData : singleNode.getMetaData()) {
                            metakv += nodeMetaData.getKey() + " = " + nodeMetaData.getValue() + "\n";
                        }
                        // Assets
                        String assetkv = "";
                        for (RequisitionAsset nodeAssets : singleNode.getAssets()) {
                            assetkv += nodeAssets.getName() + " = " + nodeAssets.getValue() + "\n";
                        }
                        //Interfaces
                        String intfTable = "";
                        for (RequisitionInterface intf : singleNode.getInterfaces()) {
                            List<String> services = new ArrayList<>();
                            for (RequisitionMonitoredService srv : intf.getMonitoredServices()) {
                                services.add(srv.getServiceName());
                            }
                            intfTable += intf.getIpAddr().toString() + " : " + intf.getSnmpPrimary() + "\n";
                            for (String thisService : services) {
                                intfTable += thisService + "\n";
                            }
                            intfTable += "\n";
                        }
                        nodeTable.addRow().addContent(singleNode.getNodeLabel(), singleNode.getForeignId(), singleNode.getLocation(), singleNode.getBuilding(),
                                singleNode.getCity(), categories, metakv, assetkv, intfTable, singleNode.getParentNodeLabel(),
                                singleNode.getParentForeignSource(), singleNode.getParentForeignId());
                    }
                    nodeTable.print(System.out);
                    System.out.println();
                }
            }
            else if (doesRequisitionExist() && asXML) {
                final Requisition req = deployedForeignSourceRepository.getRequisition(requisitionName);
                System.out.println("Requisition XML:");
                System.out.println(JaxbUtils.marshal(req));
                System.out.println();
            }
            else {
                System.out.println("Requisition '" + requisitionName + "' not found.");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
        }

        return null;
    }
    private boolean doesRequisitionExist() {
        // first things first.  Does this node requisition exist?
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
}
