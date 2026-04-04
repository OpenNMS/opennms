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
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMetaData;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMonitoredService;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Command(scope = "opennms", name = "show-requisition", description = "Display the content of a named Provisioning Requisition.")
@Service
public class ShowRequisition implements Action {
    @Reference
    private ForeignSourceRepository deployedForeignSourceRepository;

    @Option(name = "-x", aliases = "--xml", description = "Display requisition XML instead of as a table")
    private boolean asXML = false;

    @Argument(index = 0, name = "requisitionName", description = "Requisition name", multiValued = false)
    @Completion(RequisitionNameCompleter.class)
    private String requisitionName = null;

    @Override
    public Object execute() {
        try {
            if (asXML && requisitionName == null) {
                System.out.println(" -x (--xml) requires a Requisition Name");
                return null;
            }
            if (!asXML && requisitionName == null) { // show everything in a table
                final Set<Requisition> coll = deployedForeignSourceRepository.getRequisitions();
                ShellTable table = new ShellTable();
                table.column("Requisition Name");
                table.column("Last Import Date");
                table.column("Last Update Date");
                table.column("Node Count");
                for (Requisition req : coll) {
                    table.addRow().addContent(req.getForeignSource(), req.getLastImportAsDate(), req.getDate(), req.getNodeCount());
                }
                table.print(System.out);
                return null;
            }
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
