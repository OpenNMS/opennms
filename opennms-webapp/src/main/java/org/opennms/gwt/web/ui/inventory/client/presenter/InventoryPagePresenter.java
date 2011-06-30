/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */

package org.opennms.gwt.web.ui.inventory.client.presenter;

import org.opennms.gwt.web.ui.inventory.client.InventoryPageConstants;
import org.opennms.gwt.web.ui.inventory.client.InventoryService;
import org.opennms.gwt.web.ui.inventory.client.InventoryServiceAsync;
import org.opennms.gwt.web.ui.inventory.client.event.SavedInventoryEvent;
import org.opennms.gwt.web.ui.inventory.client.tools.fieldsets.FieldSetSuggestBox;
import org.opennms.gwt.web.ui.inventory.client.view.InventoryPageImpl;
import org.opennms.gwt.web.ui.inventory.shared.InventoryCommand;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         Presenter to mangage inventory page for displaying and editing inventorys.
 */
public class InventoryPagePresenter implements Presenter {

	/**
	 * Interface that defines the inventory page to show edit and create new inventorys.
	 * The presenter {@link InventoryPagePresenter} will work with every ui that is
	 * implementing this Display interface.
	 */
	public interface Display {

		/**
		 * Recommend by GWT MVP design. Get the ui widgets up.
		 * 
		 * @return {@link Widget}
		 */
		Widget asWidget();

		/**
		 * Cleans all ui changes, notes....
		 */
		void cleanUp();

		/**
		 * Fetches all data from the display as an {@link InventoryCommand}.
		 * 
		 * @return {@link InventoryCommand}
		 */
		InventoryCommand getData();

		/**
		 * get the reset button to add manage the related actions
		 * 
		 * @return {@link HasClickHandlers}
		 */
		HasClickHandlers getResetButton();

		/**
		 * get the save button to add manage the related actions
		 * 
		 * @return {@link HasClickHandlers}
		 */
		HasClickHandlers getSaveButton();

		/**
		 * Checks if the display ui is in a valid status. So all inputs are
		 * valid an ready for save or update.
		 * 
		 * @return boolean ui is valid ture / false
		 */
		boolean isUiValid();

		/**
		 * Puts an {@link InventoryCommand} to the display. To show all necessary
		 * content into the ui. InventoryDynaCommand contains inventory-data and additional.
		 * 
		 * @param {@link InventoryCommand}
		 */
		void setData(InventoryCommand inventory);

		/**
		 * Set the display ui in write enable or disable mode. So changing data
		 * is possible or not.
		 * 
		 * @param enabled
		 *            for edit-mode.
		 */
		void setEnable(Boolean enabled);

		/**
		 * Puts an error with description and throwable to the display ui.
		 * 
		 * @param description
		 *            of the error
		 * @param throwable
		 *            of the error
		 */
		void setError(String description, Throwable throwable);

		/**
		 * Sets status info to the display ui.
		 * 
		 * @param String
		 *            info what will be shown at the display ui.
		 */
		void setInfo(String info);
	}

	private InventoryPageConstants con = GWT.create(InventoryPageConstants.class);

	private final InventoryServiceAsync rpcService;
	private final HandlerManager eventBus;
	private final Display display;
	private InventoryCommand inventory;
	private int nodeId;

	public InventoryPagePresenter(InventoryServiceAsync rpcService, HandlerManager eventBus, InventoryPageImpl inventoryDynaPageImpl) {
		this.rpcService = rpcService;
		this.eventBus = eventBus;
		display = inventoryDynaPageImpl;

		try {
			nodeId = Integer.parseInt(Window.Location.getParameter("node"));
		} catch (NumberFormatException e) {
			GWT.log(con.nodeParamNotValidInt() + Window.Location.getParameter("node"), e);
			display.setError(con.nodeParamNotValidInt() + Window.Location.getParameter("node"), e);
		}
	}

	/**
	 * adds actions to all buttons of {@link Display}.
	 */
	public void bind() {
		display.getSaveButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (display.isUiValid()) {
					display.setInfo(con.infoInventorySaving() + nodeId);
					saveInventoryData();
				} else {
					GWT.log("isUiValid -> false; will not save.");
					display.setError(con.inventoryPageNotValidDontSave(), null);
				}
			}
		});

		display.getResetButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.setInfo(con.infoInventoryRestting() + nodeId);
				display.setData(inventory);
				display.cleanUp();
				display.setInfo(con.infoInventory() + nodeId);
			}
		});
	}

	/**
	 * Fetches inventory date from {@link InventoryService} an adds them to display.
	 */
	private void fetchInventoryData() {
		rpcService.getInventoryByNodeId(nodeId, new AsyncCallback<InventoryCommand>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log(con.errorFatchingInventoryData() + nodeId, caught);
				display.setError(con.errorFatchingInventoryData() + nodeId, caught);
			}

			@Override
			public void onSuccess(InventoryCommand result) {
				inventory = result;
				//display.setEnable(inventory.getAllowModify());
				display.setData(inventory);
				display.setInfo(con.infoInventory() + nodeId);
				display.cleanUp();
				//fetchInventorySuggData();
			}
		});
	}

	/**
	 * start up the presenter.
	 */
	@Override
	public void go(final HasWidgets container) {
		bind();
		container.clear();
		container.add(display.asWidget());
		display.setInfo(con.infoInventoryLoging() + nodeId);
		fetchInventoryData();
	}

	/**
	 * saves inventory data from display. Collects inventory data from display and
	 * stores them by {@link InventoryService} Cleans up the display. Refetches
	 * inventory data.
	 */
	private void saveInventoryData() {
		inventory = display.getData();
		GWT.log("Present get Date Result: " + inventory.toString());
		GWT.log("inventory internals 0 0 name: " + inventory.getPageModel().getSectionModelList().get(0).getContentElementList().get(0).toString());

		GWT.log("inventory internals: 0 0 value: " + inventory.getPageModel().getSectionModelList().get(0).getContentElementList().get(0).toString());
//		rpcService.saveOrUpdateInventoryDynaByNodeId(nodeId, inventory, new AsyncCallback<Boolean>() {
//			@Override
//			public void onFailure(Throwable caught) {
//				GWT.log(con.errorSavingInventoryData() + nodeId, caught);
//				display.setError(con.errorSavingInventoryData() + nodeId, caught);
//			}

//			@Override
//			public void onSuccess(Boolean result) {
//				eventBus.fireEvent(new SavedInventoryEvent(nodeId));
//				display.setInfo(con.infoInventorySaved() + nodeId);
//				display.cleanUp();
//				fetchInventoryData();
//			}
//		});
	}
}
