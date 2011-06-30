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

import org.opennms.gwt.web.ui.inventory.client.presenter.InventoryPagePresenter;
import org.opennms.gwt.web.ui.inventory.client.presenter.Presenter;
import org.opennms.gwt.web.ui.inventory.client.view.InventoryPageImpl;

import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         AppController of inventory module. Following GWT MVP design.
 */
public class AppController implements Presenter {

	private final HandlerManager eventBus;
	private final InventoryServiceAsync rpcService;

	public AppController(InventoryServiceAsync rpcService, HandlerManager eventBus) {
		this.eventBus = eventBus;
		this.rpcService = rpcService;
	}

	@Override
	public void go(HasWidgets container) {
		if (RootPanel.get("opennms-inventoryNodePage") != null) {
			Presenter presenter = new InventoryPagePresenter(rpcService, eventBus, new InventoryPageImpl());
			presenter.go(container);
		}
	}
}
