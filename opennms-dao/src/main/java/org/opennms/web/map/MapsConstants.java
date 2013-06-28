/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.map;

/**
 * <p>MapsConstants class.</p>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author maumig
 * This class contains some constants for Maps application
 * @version $Id: $
 * @since 1.8.1
 */
public final class MapsConstants {

    /** Constant <code>LOG4J_CATEGORY="OpenNMS.Map"</code> */
    public static final String LOG4J_CATEGORY = "map";
	
    // map default ids
	/** Constant <code>MAP_NOT_OPENED=-1</code> */
	public static final int MAP_NOT_OPENED = -1;
	
	/** Constant <code>NEW_MAP=-2</code> */
	public static final int NEW_MAP = -2;
	
    /** Constant <code>SEARCH_MAP=-3</code> */
    public static final int SEARCH_MAP = -3;

    /** Constant <code>NEW_MAP_NAME="NewMap"</code> */
    public static final String NEW_MAP_NAME = "NewMap";
    
    /** Constant <code>SEARCH_MAP_NAME="SearchMap"</code> */
    public static final String SEARCH_MAP_NAME = "SearchMap";

    // map types
	/** Constant <code>NODE_TYPE="N"</code> */
	public static final String NODE_TYPE = "N";
	
	/** Constant <code>MAP_TYPE="M"</code> */
	public static final String MAP_TYPE= "M";
	
    /** Constant <code>NODE_HIDE_TYPE="H"</code> */
    public static final String NODE_HIDE_TYPE = "H";
    
    /** Constant <code>MAP_HIDE_TYPE="W"</code> */
    public static final String MAP_HIDE_TYPE= "W";

	// actions	
	/** Constant <code>MAPS_STARTUP_ACTION="MapStartUp"</code> */
	public static final String MAPS_STARTUP_ACTION = "MapStartUp";
	
	/** Constant <code>LOADDEFAULTMAP_ACTION="LoadDefaultMap"</code> */
	public static final String LOADDEFAULTMAP_ACTION = "LoadDefaultMap";
	
	/** Constant <code>LOADMAPS_ACTION="LoadMaps"</code> */
	public static final String LOADMAPS_ACTION = "LoadMaps";
	
	/** Constant <code>LOADNODES_ACTION="LoadNodes"</code> */
	public static final String LOADNODES_ACTION = "LoadNodes";

	/** Constant <code>LOADLABELMAP_ACTION="LoadLabelMap"</code> */
	public static final String LOADLABELMAP_ACTION = "LoadLabelMap";
	
	/** Constant <code>NEWMAP_ACTION="admin/NewMap"</code> */
	public static final String NEWMAP_ACTION = "admin/NewMap";

	/** Constant <code>OPENMAP_ACTION="OpenMap"</code> */
	public static final String OPENMAP_ACTION = "OpenMap";
	
	/** Constant <code>CLOSEMAP_ACTION="CloseMap"</code> */
	public static final String CLOSEMAP_ACTION = "CloseMap";
	
	/** Constant <code>SAVEMAP_ACTION="admin/SaveMap"</code> */
	public static final String SAVEMAP_ACTION = "admin/SaveMap";
	
	/** Constant <code>DELETEMAP_ACTION="admin/DeleteMap"</code> */
	public static final String DELETEMAP_ACTION = "admin/DeleteMap";	

	/** Constant <code>CLEAR_ACTION="admin/ClearMap"</code> */
	public static final String CLEAR_ACTION = "admin/ClearMap";

	/** Constant <code>RELOAD_ACTION="Reload"</code> */
	public static final String RELOAD_ACTION = "Reload";

	/** Constant <code>REFRESH_ACTION="Refresh"</code> */
	public static final String REFRESH_ACTION = "Refresh";
	
	/** Constant <code>ADDRANGE_ACTION="AddRange"</code> */
	public static final String ADDRANGE_ACTION = "AddRange";
	
	/** Constant <code>ADDNODES_ACTION="admin/AddNodes"</code> */
	public static final String ADDNODES_ACTION = "admin/AddNodes";

	/** Constant <code>ADDMAPS_ACTION="admin/AddMaps"</code> */
	public static final String ADDMAPS_ACTION = "admin/AddMaps";
	
	/** Constant <code>SEARCHMAPS_ACTION="SearchMap"</code> */
	public static final String SEARCHMAPS_ACTION = "SearchMap";

	/** Constant <code>ADDNODES_BY_CATEGORY_ACTION="AddNodesByCategory"</code> */
	public static final String ADDNODES_BY_CATEGORY_ACTION = "AddNodesByCategory";
	
	/** Constant <code>ADDNODES_BY_LABEL_ACTION="AddNodesByLabel"</code> */
	public static final String ADDNODES_BY_LABEL_ACTION = "AddNodesByLabel";
	
	/** Constant <code>ADDNODES_WITH_NEIG_ACTION="AddNodesWithNeig"</code> */
	public static final String ADDNODES_WITH_NEIG_ACTION = "AddNodesWithNeig";

	/** Constant <code>ADDNODES_NEIG_ACTION="AddNodesNeig"</code> */
	public static final String ADDNODES_NEIG_ACTION = "AddNodesNeig";

    /** Constant <code>DELETEELEMENT_ACTION="admin/DeleteElements"</code> */
    public static final String DELETEELEMENT_ACTION = "admin/DeleteElements";

	/** Constant <code>DELETENODES_ACTION="DeleteNodes"</code> */
	public static final String DELETENODES_ACTION = "DeleteNodes";

	/** Constant <code>DELETEMAPS_ACTION="DeleteMaps"</code> */
	public static final String DELETEMAPS_ACTION = "DeleteMaps";
	
	/** Constant <code>SWITCH_MODE_ACTION="admin/SwitchRole"</code> */
	public static final String SWITCH_MODE_ACTION = "admin/SwitchRole";
		
    /** Constant <code>RELOAD_CONFIG_ACTION="admin/ReloadConfig"</code> */
    public static final String RELOAD_CONFIG_ACTION = "admin/ReloadConfig";
    // map types
	/** Constant <code>USER_GENERATED_MAP="U"</code> */
	public static final String USER_GENERATED_MAP = "U";

    /** Constant <code>AUTOMATICALLY_GENERATED_MAP="A"</code> */
    public static final String AUTOMATICALLY_GENERATED_MAP = "A";
    
    /** Constant <code>AUTOMATIC_SAVED_MAP="S"</code> */
    public static final String AUTOMATIC_SAVED_MAP = "S";

    // map access 
    /** Constant <code>ACCESS_MODE_ADMIN="RW"</code> */
    public static final String ACCESS_MODE_ADMIN = "RW";

    /** Constant <code>ACCESS_MODE_USER="RO"</code> */
    public static final String ACCESS_MODE_USER = "RO";
    
    /** Constant <code>ACCESS_MODE_GROUP="RWRO"</code> */
    public static final String ACCESS_MODE_GROUP = "RWRO";
		
	// COLOR_SEMAPHORE
	/** Constant <code>COLOR_SEMAPHORE_BY_SEVERITY="S"</code> */
	public static final String COLOR_SEMAPHORE_BY_SEVERITY = "S";
	
	/** Constant <code>COLOR_SEMAPHORE_BY_STATUS="T"</code> */
	public static final String COLOR_SEMAPHORE_BY_STATUS = "T";
	
	/** Constant <code>COLOR_SEMAPHORE_BY_AVAILABILITY="A"</code> */
	public static final String COLOR_SEMAPHORE_BY_AVAILABILITY = "A";	
	
	//Failed Map String
	/** Constant <code>failed_string="Failed"</code> */
	public static final String failed_string = "Failed";

   //Success Map String
    /** Constant <code>success_string="OK"</code> */
    public static final String success_string = "OK";

	/**
	 * <p>Constructor for MapsConstants.</p>
	 */
	public MapsConstants() {
		super();
	}
}

