/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.gwt.web.ui.inventory.server;

import org.opennms.gwt.web.ui.inventory.client.InventoryService;
import org.opennms.gwt.web.ui.inventory.shared.InventoryCommand;
import org.opennms.gwt.web.ui.inventory.shared.FieldSetModel;
import org.opennms.gwt.web.ui.inventory.shared.PageModel;
import org.opennms.gwt.web.ui.inventory.shared.SectionModel;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * 
 */

public class InventoryServiceImpl extends RemoteServiceServlet implements
		InventoryService {

	/**
	 * generated serial
	 */
	private static final long serialVersionUID = 3847574684959207209L;

	/** {@inheritDoc} */
	@Override
	public Boolean saveOrUpdateInventoryByNodeId(int nodeId,
			InventoryCommand inventoryCommand) {
		return false;
	}

	@Override
	public InventoryCommand getInventoryByNodeId(int nodeId) throws Exception {
		PageModel pm = new PageModel();

		SectionModel sm1 = new SectionModel("Lokation");
		FieldSetModel fsm1a = new FieldSetModel("Stadt", "Fulda", "Stadt halt");
		FieldSetModel fsm2a = new FieldSetModel("Land", "Deutschland",
				"Land halt");
		FieldSetModel fsm3a = new FieldSetModel("Fluss", "Main", "Fluss halt");
		FieldSetModel fsm4a = new FieldSetModel("Punkte", "7", "Punkte halt");
		pm.add(sm1);
		sm1.add(fsm1a);
		sm1.add(fsm2a);

		SectionModel smA = new SectionModel("Eingenistet");
		FieldSetModel fsm1A = new FieldSetModel("Begrüßung", "Hallo",
				"Stadt halt");
		FieldSetModel fsm2A = new FieldSetModel("Name", "Indigo", "Land halt");
		FieldSetModel fsm3A = new FieldSetModel("Formel", "alles Gute",
				"Fluss halt");
		smA.add(fsm1A);
		smA.add(fsm2A);
		smA.add(fsm3A);

		sm1.add(smA);
		sm1.add(fsm3a);
		sm1.add(fsm4a);

		SectionModel sm2 = new SectionModel("Spiel");
		FieldSetModel fsm1b = new FieldSetModel("Sching", "Schere", "A");
		FieldSetModel fsm2b = new FieldSetModel("Schang", "Stein", "B");
		FieldSetModel fsm3b = new FieldSetModel("Schong", "Papier", "C");
		pm.add(sm2);
		sm2.add(fsm1b);
		sm2.add(fsm2b);
		sm2.add(fsm3b);

		SectionModel sm3 = new SectionModel("Shooter");
		FieldSetModel fsm1c = new FieldSetModel("Quake", "Best",
				"ID Make My Day again");
		FieldSetModel fsm2c = new FieldSetModel("Unreal", "FOO",
				"Don't do that");
		FieldSetModel fsm3c = new FieldSetModel("Doom", "Like Car-Race",
				"ID Make My Day");
		pm.add(sm3);
		sm3.add(fsm1c);
		sm3.add(fsm2c);
		sm3.add(fsm3c);

		InventoryCommand inventory = new InventoryCommand();
		inventory.setPageModel(pm);
		return inventory;
	}
}
