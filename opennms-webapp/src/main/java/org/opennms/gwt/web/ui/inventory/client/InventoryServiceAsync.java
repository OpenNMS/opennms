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

import org.opennms.gwt.web.ui.inventory.shared.InventoryCommand;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         </br> Client side GWT Interface for asynchronous remote procedure
 *         call (RPC) at asset services. Asynchronous version of
 *         {@link InventoryService}. GWT requires this asynchronous version in
 *         addition to {@link InventoryService}. All method signatures have an
 *         additional AsyncCallBack parameter. {@link InventoryService} and
 *         AssetServiceAsync have to be in sync.
 */
public interface InventoryServiceAsync {
	void saveOrUpdateInventoryByNodeId(int nodeId, InventoryCommand inventory, AsyncCallback<Boolean> callback);

	void getInventoryByNodeId(int nodeId, AsyncCallback<InventoryCommand> callback);
}
