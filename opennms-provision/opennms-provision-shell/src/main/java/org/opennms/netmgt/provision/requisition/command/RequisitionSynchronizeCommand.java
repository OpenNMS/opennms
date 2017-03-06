/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Option;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;

import com.google.common.base.Strings;

// Command to synchronize an already existing requisition
@Command(scope = "requisition", name = "synchronize", description = "Synchronize Requisitions")
public class RequisitionSynchronizeCommand extends OsgiCommandSupport {

    @Option(name = "-a", aliases = "--all", description = "Trigger an import on all existing Requisitions", required = false, multiValued = false)
    protected boolean all = false;

    @Option(name = "-n", aliases = "--name", description = "The name of the Requisition to synchronize.", required = false, multiValued = false)
    protected String requisitionName;

    @Option(name = "-s", aliases = "--rescan", description = "Trigger a rescan on each node in the Requisition")
    protected boolean rescanExisting = false;

    private RequisitionService requisitionService;

    @Override
    protected Object doExecute() throws Exception {
        if (!all && Strings.isNullOrEmpty(requisitionName)) {
            System.out.println("You must either define a requisition to synchronize or synchronize all");
            return null;
        }
        if (all) {
            requisitionService.getRequisitions().forEach(r -> triggerImport(r.getName()));
        } else {
            triggerImport(requisitionName);
        }
        return null;
    }

    private void triggerImport(String requisitionName) {
        if (requisitionService.getRequisition(requisitionName) != null) {
            System.out.println("Could not find a requisition with name '" + requisitionName + "'. Event was not send.");
        } else {
            System.out.println("Sending synchronization event for Requisition with name '" + requisitionName + "'");
            requisitionService.triggerImport(
                    new ImportRequest(getClass().getSimpleName())
                            .withForeignSource(requisitionName)
                            .withRescanExisting(Boolean.toString(rescanExisting)));
        }
    }

    public void setRequisitionService(RequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }
}
