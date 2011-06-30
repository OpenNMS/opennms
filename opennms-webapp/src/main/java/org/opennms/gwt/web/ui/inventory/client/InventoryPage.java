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


package org.opennms.gwt.web.ui.inventory.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.RootPanel;
/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 * EntryPoint of asset module. Following GWT MVP design.
 */
public class InventoryPage implements EntryPoint {

	private InventoryServiceAsync m_inventoryServiceAsync;

	public void onModuleLoad() {
		InventoryServiceAsync rpcService = getInventoryService();
		HandlerManager eventBus = new HandlerManager(null);
		AppController appViewer = new AppController(rpcService, eventBus);
		
		if (RootPanel.get("opennms-inventoryNodePage") != null) {
			appViewer.go(RootPanel.get("opennms-inventoryNodePage"));
		}
	}

	private InventoryServiceAsync getInventoryService() {
		if (m_inventoryServiceAsync == null) {
			String serviceEntryPoint = GWT.getHostPageBaseURL() + "inventoryService.gwt";

			// define the service you want to call
			final InventoryServiceAsync svc = (InventoryServiceAsync) GWT.create(InventoryService.class);
			ServiceDefTarget endpoint = (ServiceDefTarget) svc;
			endpoint.setServiceEntryPoint(serviceEntryPoint);
			m_inventoryServiceAsync = svc;
		}
		return m_inventoryServiceAsync;
	}
}