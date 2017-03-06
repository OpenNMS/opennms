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

import java.io.File;

import javax.xml.bind.JAXB;

import org.apache.felix.gogo.commands.Command;
import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMerger;
import org.opennms.netmgt.provision.persist.RequisitionService;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

@Command(scope = "requisition", name = "import", description = "Import Requisitions")
public class RequisitionImportCommand extends AbstractImportCommand<Requisition> {

    private RequisitionService requisitionService;

    private RequisitionMerger requisitionMerger;

    @Override
    protected void doImport(ImportItem<Requisition> itemToImport) {
        final Requisition requisition = itemToImport.getItem();
        final RequisitionEntity requisitionEntity = requisitionMerger.mergeOrCreate(requisition);
        requisitionService.saveOrUpdateRequisition(requisitionEntity);
    }

    @Override
    protected ImportItem<Requisition> getItemToImport(File file) {
        final Requisition requisition = JAXB.unmarshal(file, Requisition.class);
        return new ImportItem<Requisition>() {
            @Override
            public String getName() {
                return requisition.getForeignSource();
            }

            @Override
            public String getType() {
                return "Requisition";
            }

            @Override
            public Requisition getItem() {
                return requisition;
            }

            @Override
            public boolean alreadyExists() {
                return requisitionService.getRequisition(requisition.getForeignSource()) != null;
            }
        };
    }

    public void setRequisitionService(RequisitionService requisitionService) {
        this.requisitionService = requisitionService;
    }

    public void setRequisitionMerger(RequisitionMerger requisitionMerger) {
        this.requisitionMerger = requisitionMerger;
    }
}
