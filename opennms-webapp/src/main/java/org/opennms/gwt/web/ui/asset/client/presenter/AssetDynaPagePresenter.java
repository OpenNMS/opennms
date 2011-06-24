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

package org.opennms.gwt.web.ui.asset.client.presenter;

import org.opennms.gwt.web.ui.asset.client.AssetPageConstants;
import org.opennms.gwt.web.ui.asset.client.AssetService;
import org.opennms.gwt.web.ui.asset.client.AssetServiceAsync;
import org.opennms.gwt.web.ui.asset.client.event.SavedAssetEvent;
import org.opennms.gwt.web.ui.asset.client.tools.fieldsets.FieldSetSuggestBox;
import org.opennms.gwt.web.ui.asset.client.view.AssetDynaPageImpl;
import org.opennms.gwt.web.ui.asset.shared.AssetDynaCommand;
import org.opennms.gwt.web.ui.asset.shared.AssetSuggCommand;

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
 *         Presenter to mangage asset page for displaying and editing assets.
 */
public class AssetDynaPagePresenter implements Presenter {

	/**
	 * Interface that defines the asset page to show edit and create new assets.
	 * The presenter {@link AssetDynaPagePresenter} will work with every ui that is
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
		 * Fetches all data from the display as an {@link AssetDynaCommand}.
		 * 
		 * @return {@link AssetDynaCommand}
		 */
		AssetDynaCommand getData();

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
		 * Puts an {@link AssetDynaCommand} to the display. To show all necessary
		 * content into the ui. AssetDynaCommand contains asset-data and additional.
		 * 
		 * @param {@link AssetDynaCommand}
		 */
		void setData(AssetDynaCommand asset);

		/**
		 * Puts an {@link AssetSuggCommand} to the display. That contains all
		 * suggestions for all {@link FieldSetSuggestBox}es at Display.
		 */
		void setDataSugg(AssetSuggCommand assetSugg);

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

	private AssetPageConstants con = GWT.create(AssetPageConstants.class);

	private final AssetServiceAsync rpcService;
	private final HandlerManager eventBus;
	private final Display display;
	private AssetDynaCommand asset;
	private int nodeId;

	public AssetDynaPagePresenter(AssetServiceAsync rpcService, HandlerManager eventBus, AssetDynaPageImpl assetDynaPageImpl) {
		this.rpcService = rpcService;
		this.eventBus = eventBus;
		display = assetDynaPageImpl;

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
					display.setInfo(con.infoAssetSaving() + nodeId);
					saveAssetData();
				} else {
					GWT.log("isUiValid -> false; will not save.");
					display.setError(con.assetPageNotValidDontSave(), null);
				}
			}
		});

		display.getResetButton().addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				display.setInfo(con.infoAssetRestting() + nodeId);
				display.setData(asset);
				display.cleanUp();
				display.setInfo(con.infoAsset() + nodeId);
			}
		});
	}

	/**
	 * Fetches asset date from {@link AssetService} an adds them to display.
	 */
	private void fetchAssetData() {
		rpcService.getAssetDynaByNodeId(nodeId, new AsyncCallback<AssetDynaCommand>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log(con.errorFatchingAssetData() + nodeId, caught);
				display.setError(con.errorFatchingAssetData() + nodeId, caught);
			}

			@Override
			public void onSuccess(AssetDynaCommand result) {
				asset = result;
				//display.setEnable(asset.getAllowModify());
				display.setData(asset);
				display.setInfo(con.infoAsset() + nodeId);
				display.cleanUp();
				//fetchAssetSuggData();
			}
		});
	}

	/**
	 * Fetches suggestions for all {@link FieldSetSuggestBox}es and add them to
	 * the display.
	 */
	private void fetchAssetSuggData() {
		rpcService.getAssetSuggestions(new AsyncCallback<AssetSuggCommand>() {
			@Override
			public void onFailure(Throwable caught) {
				GWT.log(con.errorFetchingAssetSuggData() + nodeId, caught);
				display.setError(con.errorFetchingAssetSuggData() + nodeId, caught);
			}

			@Override
			public void onSuccess(AssetSuggCommand result) {
//				assetSugg = result;
//				display.setDataSugg(assetSugg);
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
		display.setInfo(con.infoAssetLoging() + nodeId);
		fetchAssetData();
	}

	/**
	 * saves asset data from display. Collects asset data from display and
	 * stores them by {@link AssetService} Cleans up the display. Refetches
	 * asset data.
	 */
	private void saveAssetData() {
		asset = display.getData();
		GWT.log("Present get Date Result: " + asset.toString());
		GWT.log("asset internals 0 0 name: " + asset.getPageModel().getSectionModelList().get(0).getContentElementList().get(0).toString());

		GWT.log("asset internals: 0 0 value: " + asset.getPageModel().getSectionModelList().get(0).getContentElementList().get(0).toString());
//		rpcService.saveOrUpdateAssetDynaByNodeId(nodeId, asset, new AsyncCallback<Boolean>() {
//			@Override
//			public void onFailure(Throwable caught) {
//				GWT.log(con.errorSavingAssetData() + nodeId, caught);
//				display.setError(con.errorSavingAssetData() + nodeId, caught);
//			}

//			@Override
//			public void onSuccess(Boolean result) {
//				eventBus.fireEvent(new SavedAssetEvent(nodeId));
//				display.setInfo(con.infoAssetSaved() + nodeId);
//				display.cleanUp();
//				fetchAssetData();
//			}
//		});
	}
}
