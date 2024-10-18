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

package org.opennms.core.commands;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.support.table.ShellTable;
import org.opennms.netmgt.dao.api.MinionDao;
import org.opennms.netmgt.model.OnmsMinionCollection;
import org.opennms.netmgt.model.minion.OnmsMinion;

@Command(scope = "opennms", name = "show-minions", description = "Returns a list of defined Minions.")
@Service
public class ShowMinions implements Action {
    @Reference
    private MinionDao minionDao;

    @Override
    public Object execute() {
        try {
            final OnmsMinionCollection coll = new OnmsMinionCollection(minionDao.findAll());
            ShellTable table = new ShellTable();
            table.column("Minion ID");
            table.column("Location");
            table.column("Version");
            table.column("Status");
            table.column("Last Updated Time");
            for (OnmsMinion minion : coll) {
                table.addRow().addContent(minion.getId(), minion.getLocation(), minion.getVersion(), minion.getStatus(), minion.getLastUpdated());
            }
            table.print(System.out);
        } catch (Exception e) {
             System.out.println(e.getMessage());
             e.printStackTrace(System.out);
        }
        return null;
    }
}