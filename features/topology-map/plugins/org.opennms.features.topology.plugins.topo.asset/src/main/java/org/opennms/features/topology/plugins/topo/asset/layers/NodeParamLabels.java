/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.asset.layers;

import java.util.Arrays;
import java.util.List;

/**
 * List of constants which correspond to both OpenNMS node table entries
 * or OpenNMS asset table entries. 
 * The selected keys reference entries which can be used in creating a graph
 */
public interface NodeParamLabels {
	String NODE_NODELABEL ="node-nodelabel";
	String NODE_NODEID ="node-nodeid";
	String NODE_FOREIGNSOURCE ="node-foreignsource";
	String NODE_FOREIGNID ="node-foreignid";
	String NODE_NODESYSNAME ="node-nodesysname";
	String NODE_NODESYSLOCATION ="node-nodesyslocation";
	String NODE_OPERATINGSYSTEM ="node-operatingsystem";
	String NODE_CATEGORIES ="node-categories";
	String PARENT_NODELABEL ="parent-nodelabel";
	String PARENT_NODEID ="parent-nodeid";
	String PARENT_FOREIGNSOURCE ="parent-foreignsource";
	String PARENT_FOREIGNID ="parent-foreignid";
	String ASSET_COUNTRY ="asset-country";
	String ASSET_ADDRESS1 ="asset-address1";
	String ASSET_ADDRESS2 ="asset-address2";
	String ASSET_CITY ="asset-city";
	String ASSET_ZIP ="asset-zip";
	String ASSET_STATE ="asset-state";
	String ASSET_LATITUDE ="asset-latitude";
	String ASSET_LONGITUDE ="asset-longitude";
	String ASSET_REGION ="asset-region";
	String ASSET_DIVISION ="asset-division";
	String ASSET_DEPARTMENT ="asset-department";
	String ASSET_BUILDING ="asset-building";
	String ASSET_FLOOR ="asset-floor";
	String ASSET_ROOM ="asset-room";
	String ASSET_RACK ="asset-rack";
	String ASSET_SLOT ="asset-slot";
	String ASSET_PORT ="asset-port";
	String ASSET_CIRCUITID ="asset-circuitid";
	String ASSET_CATEGORY ="asset-category";
	String ASSET_DISPLAYCATEGORY ="asset-displaycategory";
	String ASSET_NOTIFYCATEGORY ="asset-notifycategory";
	String ASSET_POLLERCATEGORY ="asset-pollercategory";
	String ASSET_THRESHOLDCATEGORY ="asset-thresholdcategory";
	String ASSET_MANAGEDOBJECTTYPE ="asset-managedobjecttype";
	String ASSET_MANAGEDOBJECTINSTANCE ="asset-managedobjectinstance";
	String ASSET_MANUFACTURER ="asset-manufacturer";
	String ASSET_VENDOR ="asset-vendor";
	String ASSET_MODELNUMBER ="asset-modelnumber";
	String ASSET_DESCRIPTION ="asset-description";
	String ASSET_OPERATINGSYSTEM ="asset-operatingsystem";

	/**
	 * static list of all the valid constant keys in this class
	 */
	List<String> ALL_KEYS= Arrays.asList(NODE_NODELABEL, NODE_NODEID, NODE_FOREIGNSOURCE, 
			NODE_FOREIGNID, NODE_NODESYSNAME, NODE_NODESYSLOCATION, NODE_OPERATINGSYSTEM, NODE_CATEGORIES, 
			PARENT_NODELABEL, PARENT_NODEID, PARENT_FOREIGNSOURCE, PARENT_FOREIGNID, ASSET_COUNTRY, 
			ASSET_ADDRESS1, ASSET_ADDRESS2, ASSET_CITY, ASSET_ZIP, ASSET_STATE, ASSET_LATITUDE, ASSET_LONGITUDE, 
			ASSET_REGION, ASSET_DIVISION, ASSET_DEPARTMENT, ASSET_BUILDING, ASSET_FLOOR, ASSET_ROOM, ASSET_RACK, 
			ASSET_SLOT, ASSET_PORT, ASSET_CIRCUITID, ASSET_CATEGORY, ASSET_DISPLAYCATEGORY, ASSET_NOTIFYCATEGORY, 
			ASSET_POLLERCATEGORY, ASSET_THRESHOLDCATEGORY, ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE, 
			ASSET_MANUFACTURER, ASSET_VENDOR, ASSET_MODELNUMBER, ASSET_DESCRIPTION, ASSET_OPERATINGSYSTEM);
    
}
