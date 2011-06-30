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
// original asset layout
//	@Override
//	public InventoryCommand getInventoryByNodeId(int nodeId) throws Exception {
//		PageModel pm = new PageModel();
//
//		SectionModel sm1 = new SectionModel("SNMP Info");
//		FieldSetModel fsm1a = new FieldSetModel("System Id",
//				".1.3.6.1.4.1.3854.1", "");
//		FieldSetModel fsm2a = new FieldSetModel("System Name",
//				"securityProbe 5E", "");
//		FieldSetModel fsm3a = new FieldSetModel("System Location",
//				"Indigo's Hackerspace", "");
//		FieldSetModel fsm4a = new FieldSetModel("System Contact",
//				"ronny@opennms.org", "");
//		FieldSetModel fsm5a = new FieldSetModel("System Description",
//				"securityProbe 5E SEC-PXAV402p Apr 22 2011 09:50:23", "");
//
//		pm.add(sm1);
//		sm1.add(fsm1a);
//		sm1.add(fsm2a);
//		sm1.add(fsm3a);
//		sm1.add(fsm4a);
//		sm1.add(fsm5a);
//
//		SectionModel sm2 = new SectionModel("Configuration Categories");
//		FieldSetModel fsm1b = new FieldSetModel("Display Category", "", "");
//		FieldSetModel fsm2b = new FieldSetModel("Notification Category", "", "");
//		FieldSetModel fsm3b = new FieldSetModel("Poller Category", "", "");
//		FieldSetModel fsm4b = new FieldSetModel("Threshold Category", "", "");
//
//		pm.add(sm2);
//		sm2.add(fsm1b);
//		sm2.add(fsm2b);
//		sm2.add(fsm3b);
//		sm2.add(fsm4b);
//
//		SectionModel sm3 = new SectionModel("Identification");
//		FieldSetModel fsm1c = new FieldSetModel("Description", "", "");
//		FieldSetModel fsm2c = new FieldSetModel("Category", "", "");
//		FieldSetModel fsm3c = new FieldSetModel("Manufacturer", "", "");
//		FieldSetModel fsm4c = new FieldSetModel("Model Number", "", "");
//		FieldSetModel fsm5c = new FieldSetModel("Serial Number", "", "");
//		FieldSetModel fsm6c = new FieldSetModel("Asset Number", "", "");
//		FieldSetModel fsm7c = new FieldSetModel("Date Installed", "", "");
//		FieldSetModel fsm8c = new FieldSetModel("Operationg System", "", "");
//
//		pm.add(sm3);
//		sm3.add(fsm1c);
//		sm3.add(fsm2c);
//		sm3.add(fsm3c);
//		sm3.add(fsm4c);
//		sm3.add(fsm5c);
//		sm3.add(fsm6c);
//		sm3.add(fsm7c);
//		sm3.add(fsm8c);
//
//		SectionModel sm4 = new SectionModel("Location");
//		FieldSetModel fsm1d = new FieldSetModel("State", "", "");
//		FieldSetModel fsm2d = new FieldSetModel("Region", "", "");
//		FieldSetModel fsm3d = new FieldSetModel("Address 1", "", "");
//		FieldSetModel fsm4d = new FieldSetModel("Address 2", "", "");
//		FieldSetModel fsm5d = new FieldSetModel("City", "", "");
//		FieldSetModel fsm6d = new FieldSetModel("ZIP", "", "");
//		FieldSetModel fsm7d = new FieldSetModel("Division", "", "");
//		FieldSetModel fsm8d = new FieldSetModel("Department", "", "");
//		FieldSetModel fsm9d = new FieldSetModel("Building", "", "");
//		FieldSetModel fsm10d = new FieldSetModel("Floor", "", "");
//		FieldSetModel fsm11d = new FieldSetModel("Room", "", "");
//		FieldSetModel fsm12d = new FieldSetModel("Rack", "", "");
//		FieldSetModel fsm13d = new FieldSetModel("Rack unit height", "", "");
//		FieldSetModel fsm14d = new FieldSetModel("Slot", "", "");
//		FieldSetModel fsm15d = new FieldSetModel("Port", "", "");
//		FieldSetModel fsm16d = new FieldSetModel("CircuitId", "", "");
//		FieldSetModel fsm17d = new FieldSetModel("Admin", "", "");
//
//		pm.add(sm4);
//		sm4.add(fsm1d);
//		sm4.add(fsm2d);
//		sm4.add(fsm3d);
//		sm4.add(fsm4d);
//		sm4.add(fsm5d);
//		sm4.add(fsm6d);
//		sm4.add(fsm7d);
//		sm4.add(fsm8d);
//		sm4.add(fsm9d);
//		sm4.add(fsm10d);
//		sm4.add(fsm11d);
//		sm4.add(fsm12d);
//		sm4.add(fsm13d);
//		sm4.add(fsm14d);
//		sm4.add(fsm15d);
//		sm4.add(fsm16d);
//		sm4.add(fsm17d);
//
//		SectionModel sm5 = new SectionModel("Vendor");
//		FieldSetModel fsm1e = new FieldSetModel("Name", "AKCP", "");
//		FieldSetModel fsm2e = new FieldSetModel("Phone", "", "");
//		FieldSetModel fsm3e = new FieldSetModel("Fax", "", "");
//		FieldSetModel fsm4e = new FieldSetModel("Lease", "", "");
//		FieldSetModel fsm5e = new FieldSetModel("Lease Expires", "", "");
//		FieldSetModel fsm6e = new FieldSetModel("Vendor Asset", "", "");
//		FieldSetModel fsm7e = new FieldSetModel("Maint Contract Number",
//				"Didactum 8x5x9 NBD", "");
//		FieldSetModel fsm8e = new FieldSetModel("Contract Expires",
//				"2011-06-25", "");
//		FieldSetModel fsm9e = new FieldSetModel("Maint Phone", "", "");
//
//		pm.add(sm5);
//		sm5.add(fsm1e);
//		sm5.add(fsm2e);
//		sm5.add(fsm3e);
//		sm5.add(fsm4e);
//		sm5.add(fsm5e);
//		sm5.add(fsm6e);
//		sm5.add(fsm7e);
//		sm5.add(fsm8e);
//		sm5.add(fsm9e);
//
//		SectionModel sm6 = new SectionModel("Authentication");
//		FieldSetModel fsm1f = new FieldSetModel("Username", "", "");
//		FieldSetModel fsm2f = new FieldSetModel("Password", "", "");
//		FieldSetModel fsm3f = new FieldSetModel("Enable Password", "", "");
//		FieldSetModel fsm4f = new FieldSetModel("Connection", "", "");
//		FieldSetModel fsm5f = new FieldSetModel("AutoEnable", "", "");
//		FieldSetModel fsm6f = new FieldSetModel("SNMP community", "", "");
//
//		pm.add(sm6);
//		sm6.add(fsm1f);
//		sm6.add(fsm2f);
//		sm6.add(fsm3f);
//		sm6.add(fsm4f);
//		sm6.add(fsm5f);
//		sm6.add(fsm6f);
//
//		SectionModel sm7 = new SectionModel("Hardware");
//		FieldSetModel fsm1g = new FieldSetModel("Cpu", "", "");
//		FieldSetModel fsm2g = new FieldSetModel("Ram", "", "");
//		FieldSetModel fsm3g = new FieldSetModel("Additional hardware", "", "");
//		FieldSetModel fsm4g = new FieldSetModel("Number of power supplies", "",
//				"");
//		FieldSetModel fsm5g = new FieldSetModel("Inputpower", "", "");
//		FieldSetModel fsm6g = new FieldSetModel("Storage Controller", "", "");
//		FieldSetModel fsm7g = new FieldSetModel("HDD 1", "", "");
//		FieldSetModel fsm8g = new FieldSetModel("HDD 2", "", "");
//		FieldSetModel fsm9g = new FieldSetModel("HDD 3", "", "");
//		FieldSetModel fsm10g = new FieldSetModel("HDD 4", "", "");
//		FieldSetModel fsm11g = new FieldSetModel("HDD 5", "", "");
//		FieldSetModel fsm12g = new FieldSetModel("HDD 6", "", "");
//
//		pm.add(sm7);
//		sm7.add(fsm1g);
//		sm7.add(fsm2g);
//		sm7.add(fsm3g);
//		sm7.add(fsm4g);
//		sm7.add(fsm5g);
//		sm7.add(fsm6g);
//		sm7.add(fsm7g);
//		sm7.add(fsm8g);
//		sm7.add(fsm9g);
//		sm7.add(fsm10g);
//		sm7.add(fsm11g);
//		sm7.add(fsm12g);
//
//		SectionModel sm8 = new SectionModel("Comments");
//		FieldSetModel fsm1h = new FieldSetModel("Comment", "", "");
//
//		pm.add(sm8);
//		sm8.add(fsm1h);
//
//		InventoryCommand inventory = new InventoryCommand();
//		inventory.setPageModel(pm);
//		return inventory;
//	}
}
