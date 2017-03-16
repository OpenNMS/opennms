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
 * list of constants which correspond to both OpenNMS node table entries 
 * or OpenNMS asset table entries. 
 * The selected keys reference entries which could be useful in creating a graph
 * These keys are also used in the nodeInfo map as the nodeParamLabelKeys 
 * 
 * e.g. nodeInfo  Map<nodeId, Map<nodeParamLabelKey, nodeParamValue>>
 * nodeParamLabelKey a node asset parameter key (from those defined in org.opennms.plugins.graphml.asset.NodeParamLabels)
 * nodeParamValue a node asset value ( e.g. key NodeParamLabels.ASSET_RACK ('asset-rack') value: rack1
 */
public interface NodeParamLabels {
	public static final String  NODE_NODELABEL ="node-nodelabel";
	public static final String  NODE_NODEID ="node-nodeid";
	public static final String  NODE_FOREIGNSOURCE ="node-foreignsource";
	public static final String  NODE_FOREIGNID ="node-foreignid";
	public static final String  NODE_NODESYSNAME ="node-nodesysname";
	public static final String  NODE_NODESYSLOCATION ="node-nodesyslocation";
	public static final String  NODE_OPERATINGSYSTEM ="node-operatingsystem";
	public static final String  NODE_CATEGORIES ="node-categories";
	public static final String  PARENT_NODELABEL ="parent-nodelabel";
	public static final String  PARENT_NODEID ="parent-nodeid";
	public static final String  PARENT_FOREIGNSOURCE ="parent-foreignsource";
	public static final String  PARENT_FOREIGNID ="parent-foreignid";
	public static final String  ASSET_COUNTRY ="asset-country";
	public static final String  ASSET_ADDRESS1 ="asset-address1";
	public static final String  ASSET_ADDRESS2 ="asset-address2";
	public static final String  ASSET_CITY ="asset-city";
	public static final String  ASSET_ZIP ="asset-zip";
	public static final String  ASSET_STATE ="asset-state";
	public static final String  ASSET_LATITUDE ="asset-latitude";
	public static final String  ASSET_LONGITUDE ="asset-longitude";
	public static final String  ASSET_REGION ="asset-region";
	public static final String  ASSET_DIVISION ="asset-division";
	public static final String  ASSET_DEPARTMENT ="asset-department";
	public static final String  ASSET_BUILDING ="asset-building";
	public static final String  ASSET_FLOOR ="asset-floor";
	public static final String  ASSET_ROOM ="asset-room";
	public static final String  ASSET_RACK ="asset-rack";
	public static final String  ASSET_SLOT ="asset-slot";
	public static final String  ASSET_PORT ="asset-port";
	public static final String  ASSET_CIRCUITID ="asset-circuitid";
	public static final String  ASSET_CATEGORY ="asset-category";
	public static final String  ASSET_DISPLAYCATEGORY ="asset-displaycategory";
	public static final String  ASSET_NOTIFYCATEGORY ="asset-notifycategory";
	public static final String  ASSET_POLLERCATEGORY ="asset-pollercategory";
	public static final String  ASSET_THRESHOLDCATEGORY ="asset-thresholdcategory";
	public static final String  ASSET_MANAGEDOBJECTTYPE ="asset-managedobjecttype";
	public static final String  ASSET_MANAGEDOBJECTINSTANCE ="asset-managedobjectinstance";
	public static final String  ASSET_MANUFACTURER ="asset-manufacturer";
	public static final String  ASSET_VENDOR ="asset-vendor";
	public static final String  ASSET_MODELNUMBER ="asset-modelnumber";
	public static final String  ASSET_DESCRIPTION ="asset-description";
	public static final String  ASSET_OPERATINGSYSTEM ="asset-operatingsystem";

	/**
	 * static list of all the valid constant keys in this class
	 */
	public static final List<String> ALL_KEYS= Arrays.asList(NODE_NODELABEL, NODE_NODEID, NODE_FOREIGNSOURCE, 
			NODE_FOREIGNID, NODE_NODESYSNAME, NODE_NODESYSLOCATION, NODE_OPERATINGSYSTEM, NODE_CATEGORIES, 
			PARENT_NODELABEL, PARENT_NODEID, PARENT_FOREIGNSOURCE, PARENT_FOREIGNID, ASSET_COUNTRY, 
			ASSET_ADDRESS1, ASSET_ADDRESS2, ASSET_CITY, ASSET_ZIP, ASSET_STATE, ASSET_LATITUDE, ASSET_LONGITUDE, 
			ASSET_REGION, ASSET_DIVISION, ASSET_DEPARTMENT, ASSET_BUILDING, ASSET_FLOOR, ASSET_ROOM, ASSET_RACK, 
			ASSET_SLOT, ASSET_PORT, ASSET_CIRCUITID, ASSET_CATEGORY, ASSET_DISPLAYCATEGORY, ASSET_NOTIFYCATEGORY, 
			ASSET_POLLERCATEGORY, ASSET_THRESHOLDCATEGORY, ASSET_MANAGEDOBJECTTYPE, ASSET_MANAGEDOBJECTINSTANCE, 
			ASSET_MANUFACTURER, ASSET_VENDOR, ASSET_MODELNUMBER, ASSET_DESCRIPTION, ASSET_OPERATINGSYSTEM);
    
}
