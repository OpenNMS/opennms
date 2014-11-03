/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.gwt.web.ui.asset.client;

import org.opennms.gwt.web.ui.asset.client.tools.fieldsets.FieldSetSuggestBox;
import org.opennms.gwt.web.ui.asset.shared.AssetCommand;
import org.opennms.gwt.web.ui.asset.shared.AssetSuggCommand;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * @author <a href="mailto:MarkusNeumannMarkus@gmail.com">Markus Neumann</a>
 *         </br> Client side GWT Interface for remote procedure calls (RPC) at
 *         asset services.
 */
@RemoteServiceRelativePath("asset")
public interface AssetService extends RemoteService {

	/**
	 * Calling this method will return a {@link AssetCommand} with contains all
	 * asset data and additional data for GWT asset ui. If no OnmsNode is found
	 * by given nodeId a exception will occur.
	 * 
	 * @param nodeId
	 *            related to OnmsNode.
	 * @return {@link AssetCommand} by the given nodeId
	 * @throws Exception
	 *             , used asset service can throw all types of exception
	 */
	AssetCommand getAssetByNodeId(int nodeId) throws Exception;

	/**
	 * Calling this method will return an {@link AssetSuggCommand} that contains
	 * all suggestions for all {@link FieldSetSuggestBox}es at the GWT asset ui.
	 * 
	 * @return {@link AssetSuggCommand}
	 * @throws Exception
	 *             , used asset service can throw all types of exception
	 */
	AssetSuggCommand getAssetSuggestions() throws Exception;

	/**
	 * Calling this method will save or update a OnmsAsset by the given
	 * {@link AssetCommand}. Problems will be thrown as exceptions.
	 * 
	 * @param nodeId
	 *            related to OnmsNode.
	 * @param {@link AssetCommand}
	 * @return If the operation went well result will be true, if not false.
	 * @throws Exception
	 *             , used asset service can throw all types of exception
	 */
	Boolean saveOrUpdateAssetByNodeId(int nodeId, AssetCommand asset) throws Exception;
}
