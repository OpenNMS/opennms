/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: October 31, 2006
 *
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
package org.opennms.web.map;

/**
 * 
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author maumig
 * This class contains some constants for Maps application
 *
 */
public final class MapsConstants {

    public static final String LOG4J_CATEGORY = "OpenNMS.Map";
	
    // map default ids
	public static final int MAP_NOT_OPENED = -1;
	
	public static final int NEW_MAP = -2;
	
    public static final int SEARCH_MAP = -3;

    // map types
	public static final String NODE_TYPE = "N";
	
	public static final String MAP_TYPE= "M";
	
    public static final String NODE_HIDE_TYPE = "H";
    
    public static final String MAP_HIDE_TYPE= "W";

	// actions	
	public static final String MAPS_STARTUP_ACTION = "MapStartUp";
	
	public static final String LOADDEFAULTMAP_ACTION = "LoadDefaultMap";
	
	public static final String LOADMAPS_ACTION = "LoadMaps";
	
	public static final String LOADNODES_ACTION = "LoadNodes";

	public static final String LOADLABELMAP_ACTION = "LoadLabelMap";
	
	public static final String NEWMAP_ACTION = "NewMap";

	public static final String OPENMAP_ACTION = "OpenMap";
	
	public static final String CLOSEMAP_ACTION = "CloseMap";
	
	public static final String SAVEMAP_ACTION = "SaveMap";
	
	public static final String DELETEMAP_ACTION = "DeleteMap";	

	public static final String CLEAR_ACTION = "ClearMap";

	public static final String RELOAD_ACTION = "Reload";

	public static final String REFRESH_ACTION = "Refresh";
	
	public static final String ADDRANGE_ACTION = "AddRange";
	
	public static final String ADDNODES_ACTION = "AddNodes";

	public static final String ADDMAPS_ACTION = "AddMaps";
	
	public static final String SEARCHMAPS_ACTION = "SearchMap";

	public static final String ADDNODES_BY_CATEGORY_ACTION = "AddNodesByCategory";
	
	public static final String ADDNODES_BY_LABEL_ACTION = "AddNodesByLabel";
	
	public static final String ADDNODES_WITH_NEIG_ACTION = "AddNodesWithNeig";

	public static final String ADDNODES_NEIG_ACTION = "AddNodesNeig";

    public static final String DELETEELEMENT_ACTION = "DeleteElements";

	public static final String DELETENODES_ACTION = "DeleteNodes";

	public static final String DELETEMAPS_ACTION = "DeleteMaps";
	
	public static final String SWITCH_MODE_ACTION = "SwitchRole";
		
    // map types
	public static final String USER_GENERATED_MAP = "U";

    public static final String AUTOMATICALLY_GENERATED_MAP = "A";
    
    public static final String AUTOMATIC_SAVED_MAP = "S";

    // map access 
    public static final String ACCESS_MODE_ADMIN = "RW";

    public static final String ACCESS_MODE_USER = "RO";
    
    public static final String ACCESS_MODE_GROUP = "RWRO";
		
	// COLOR_SEMAPHORE
	public static final String COLOR_SEMAPHORE_BY_SEVERITY = "S";
	
	public static final String COLOR_SEMAPHORE_BY_STATUS = "T";
	
	public static final String COLOR_SEMAPHORE_BY_AVAILABILITY = "A";
	
	
	public MapsConstants() {
		super();
	}
}

