//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
package org.opennms.web.graph;

import java.util.*;
import org.opennms.core.utils.BundleLists;


public class PrefabGraph extends Object implements Comparable 
{
    public static final String DEFAULT_GRAPH_LIST_KEY = "reports";

    public static Map getPrefabGraphDefinitions(Properties props) {
        return getPrefabGraphDefinitions(props, DEFAULT_GRAPH_LIST_KEY);
    }
    

    public static Map getPrefabGraphDefinitions(Properties props, String listKey) {
        if( props == null || listKey == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        Map map = new HashMap();
        
        String listString = (String)props.get(listKey);
        if( listString == null ) {
            throw new IllegalArgumentException("Properties parameter must contain \"" + listKey + "\" property"); 
        }

        String[] list = BundleLists.parseBundleList(listString);
        
        for(int i=0; i < list.length; i++ ) {
            String key = list[i];
            PrefabGraph graph = new PrefabGraph(key, props, i);

            map.put(key, graph);            
        }
        
        return map;
    }    

    protected String name;
    protected String title;
    protected String[] columns;
    protected String command;
    protected String[] externalValues;
    protected int order;
    protected String type;
    protected String description;
    

    public PrefabGraph(String name, String title, String[] columns, String command, String[] externalValues, int order, String type, String description) {
        if( name == null || title == null || columns == null || command == null || externalValues == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        this.name = name;
        this.title = title;
        this.columns = columns;
        this.command = command;
        this.externalValues = externalValues;
        this.order = order;        
        
        //type can be null
        this.type = type;
        
        //description can be null
        this.description = description;
    }
    
    
    public PrefabGraph(String key, Properties props, int order) {
        if( key == null || props == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        this.name = key;                
        this.order = order;                
                
        this.title = props.getProperty("report." + key + ".name");
        if( this.title == null ) {
            throw new IllegalArgumentException("Properties parameter must contain \"report." + key + ".name\" property");
        }
                 
        this.command = props.getProperty("report." + key + ".command");
        if( this.command == null ) {
            throw new IllegalArgumentException("Properties parameter must contain \"report." + key + ".command\" property");
        }

        String columnString = props.getProperty("report." + key + ".columns");
        if( columnString == null ) {
            throw new IllegalArgumentException("Properties parameter must contain \"report." + key + ".columns\" property");
        }

        this.columns = BundleLists.parseBundleList(columnString);

        String externalValuesString = props.getProperty("report." + key + ".externalValues");
        if( externalValuesString == null ) {
            this.externalValues = new String[0];
        }
        else {
            this.externalValues = BundleLists.parseBundleList(externalValuesString); 
        }
        
        //can be null
        this.type = props.getProperty("report." + key + ".type");
        
        //can be null
        this.description = props.getProperty("report." + key + ".description");        
    }
    


    public String getName() {
        return this.name;
    }


    public String getTitle() {
        return this.title;
    }
    
    
    public int getOrder() {
        return this.order;
    }
    
    
    public String[] getColumns() {
        return this.columns;
    }
    
    
    public String getCommand() {
        return this.command;
    }
    
    
    public String[] getExternalValues() {
        return this.externalValues;
    }
    

    /** Can be null. */
    public String getType() {
        return this.type;
    }
    
    
    /** Can be null. */
    public String getDescription() {
        return this.description;
    }

    
    public int compareTo(Object obj) {
        if(obj == null ) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }
        
        if(!(obj instanceof PrefabGraph)) {
            throw new IllegalArgumentException("Can only compare to PrefabGraph objects.");
        }
        
        PrefabGraph otherGraph = (PrefabGraph)obj;
        
        return this.getOrder() - otherGraph.getOrder();
    }
}
