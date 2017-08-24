/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.karaf.shell;

import java.util.Objects;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;

@Command(scope = "bsm", name = "delete-generated-hierarchies", description="Deletes generated hierarchies.")
public class DeleteGeneratedHierarchiesShellCommand extends OsgiCommandSupport {

    private BusinessServiceManager businessServiceManager;

    @Override
    protected Object doExecute() throws Exception {
        System.out.println("Deleting generated business services...");
        businessServiceManager.getAllBusinessServices().stream()
            .filter(b -> "true".equals(b.getAttributes().get("generated")))
            .forEach(b -> businessServiceManager.deleteBusinessService(b));
        return null;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = Objects.requireNonNull(businessServiceManager);
    }
}
